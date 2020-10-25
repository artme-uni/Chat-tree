package ru.nsu.g.akononov.chat.model.message;

import lombok.Getter;

import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Getter
public class MessagesStorage {
    private final static int CLEANING_PERIOD = 10 * 1000;
    private final static int MESSAGE_TTL = 20 * 1000;
    private long lastCleaningTime = 0;

    private final LinkedBlockingDeque<Message> messagesToBeSent = new LinkedBlockingDeque<>();
    private final CopyOnWriteArrayList<MessageInfo> messagesToBeConfirmed = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Message, SocketAddress> privateMessageDestinations = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<MessageInfo> receivedMessages = new CopyOnWriteArrayList<>();


    private static volatile MessagesStorage instance;
    private MessagesStorage() {
    }

    public static MessagesStorage getInstance() {
        if (instance == null) {
            synchronized (MessagesStorage.class) {
                if (instance == null) {
                    instance = new MessagesStorage();
                }
            }
        }
        return instance;
    }

    public void removeMessageToBeConfirmed(Message message) {
        messagesToBeConfirmed.removeAll(messagesToBeConfirmed.stream()
                .filter(messageInfo -> messageInfo.getData().equals(message))
                .collect(Collectors.toList()));
    }

    public List<Message> getUnsentMessages(SocketAddress neighbor){
        List<Message> unsentMessages = privateMessageDestinations
                .entrySet().stream()
                .filter(entry -> entry.getValue().equals(neighbor))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Message> notConfirmedMessages = messagesToBeConfirmed.stream()
                .filter(messageInfo -> messageInfo.getAddress().equals(neighbor))
                .map(MessageInfo::getData)
                .collect(Collectors.toList());

        unsentMessages.addAll(notConfirmedMessages);

        return unsentMessages;
    }

    public void addReceivedMessage(Message message, SocketAddress source){
        MessageInfo messageInfo = new MessageInfo(message, new Date(), source);
        receivedMessages.add(messageInfo);
    }

    public boolean isContainReceivedMessage(Message message){
        for (MessageInfo messageInfo: receivedMessages) {
            if(messageInfo.getData().equals(message)){
                return true;
            }
        }
        return false;
    }

    public SocketAddress getReceivedMessageSource(Message message){
        List<SocketAddress> source = receivedMessages.stream()
                .filter(messageInfo -> messageInfo.getData().equals(message))
                .map(MessageInfo::getAddress)
                .collect(Collectors.toList());

        if(source.size() > 1){
            throw new RuntimeException();
        }

        if(source.size() == 0){
            return null;
        }

        return source.get(0);
    }

    public void clean(){
        long now = System.currentTimeMillis();
        if(now - lastCleaningTime > CLEANING_PERIOD){
            receivedMessages.removeIf(messageInfo -> (now - messageInfo.getTime().getTime()) > MESSAGE_TTL);
            lastCleaningTime = now;
        }
    }
}