package ru.nsu.g.akononov.chat.model.message;

import lombok.Getter;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Getter
public class MessagesStorage {
    private final LinkedBlockingDeque<Message> messagesToBeSent = new LinkedBlockingDeque<>();
    private final CopyOnWriteArrayList<MessageInfo> messagesToBeConfirmed = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Message, SocketAddress> receivedMessages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Message, SocketAddress> privateMessageDestinations = new ConcurrentHashMap<>();

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
                .filter(messageInfo -> messageInfo.getDestination().equals(neighbor))
                .map(MessageInfo::getData)
                .collect(Collectors.toList());

        unsentMessages.addAll(notConfirmedMessages);

        return unsentMessages;
    }
}
