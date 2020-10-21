package ru.nsu.g.akononov.chat.model.transfer;


import lombok.Getter;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageToBeConfirmed;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class MessagesStorage {
    private final LinkedBlockingQueue<Message> messagesToBeSent = new LinkedBlockingQueue<>();
    private final CopyOnWriteArrayList<MessageToBeConfirmed> messagesToBeConfirmed = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<MessageToBeConfirmed> messagesCannotBeDelivered = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Message, SocketAddress> receivedMessages = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<MessageToBeConfirmed> sentMessage = new CopyOnWriteArrayList<>();

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

}
