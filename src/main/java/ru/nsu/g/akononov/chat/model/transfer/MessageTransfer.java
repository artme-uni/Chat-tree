package ru.nsu.g.akononov.chat.model.transfer;

import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageType;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageTransfer {
    private final DatagramSocket socket;
    private final MessagesStorage storage = MessagesStorage.getInstance();
    private final CopyOnWriteArrayList<SocketAddress> neighbors = new CopyOnWriteArrayList<>();

    private final String sourceName;
    private final Sender sender;
    private final Receiver receiver;


    public MessageTransfer(int port, String sourceName) throws SocketException {
        socket = new DatagramSocket(port);
        this.sourceName = sourceName;
        sender = new Sender(socket, neighbors, storage);
        receiver = new Receiver(socket, neighbors, storage);

        Thread senderThread = new Thread(sender);
        senderThread.start();
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void sendUserMessage(String message){
        storage.getMessagesToBeSent().add(new Message(sourceName, MessageType.USER, message));
    }

    public void addNeighbor(SocketAddress neighbor){
        neighbors.add(neighbor);
        storage.getMessagesToBeSent().add(new Message("", MessageType.HI, ""));
    }
}
