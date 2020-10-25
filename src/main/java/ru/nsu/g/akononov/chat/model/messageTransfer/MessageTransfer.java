package ru.nsu.g.akononov.chat.model.messageTransfer;

import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageType;
import ru.nsu.g.akononov.chat.model.message.MessagesStorage;
import ru.nsu.g.akononov.chat.model.tracker.ReceiverActivityTracker;
import ru.nsu.g.akononov.chat.model.tracker.SenderActivityTracker;
import ru.nsu.g.akononov.chat.view.Observer;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageTransfer {
    private final MessagesStorage storage = MessagesStorage.getInstance();

    private final CopyOnWriteArrayList<SocketAddress> neighbors;

    private final String sourceName;
    private final Sender sender;
    private final Receiver receiver;


    public MessageTransfer(int port, CopyOnWriteArrayList<SocketAddress> neighbors, String sourceName, int lossesPercent) throws SocketException {
        DatagramSocket socket = new DatagramSocket(port);
        this.neighbors = neighbors;
        this.sourceName = sourceName;

        SenderActivityTracker senderActivityTracker = new SenderActivityTracker(this);
        sender = new Sender(socket, neighbors, senderActivityTracker);

        ReceiverActivityTracker receiverActivityTracker = new ReceiverActivityTracker(this);
        receiver = new Receiver(socket, this, receiverActivityTracker, lossesPercent);

        Thread senderThread = new Thread(sender);
        senderThread.start();
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    public void sendUserMessage(String messageData){
        Message message = new Message(sourceName, MessageType.USER, messageData);
        storage.getMessagesToBeSent().addLast(message);
    }

    public void sendHi(SocketAddress destination){
        Message message = new Message(sourceName, MessageType.ALIVE, "");
        sendPrivateMessage(message, destination);
    }

    public void sendReplacement(SocketAddress replacementNode){
        neighbors.forEach(neighbor -> sendReplacement(replacementNode, neighbor));
    }

    public void sendReplacement(SocketAddress replacementNode, SocketAddress destination){
        String replacementNodeToString;
        if(replacementNode == destination){
            replacementNodeToString = "YOU";
        } else {
            replacementNodeToString = replacementNode.toString();
        }
        Message message = new Message(sourceName, MessageType.REPLACEMENT, replacementNodeToString);
        sendPrivateMessage(message, destination);
    }

    public void sendAll(List<Message> messages, SocketAddress destination){
        messages.forEach(message -> sendPrivateMessage(message, destination));
    }

    private void sendPrivateMessage(Message message, SocketAddress destination){
        storage.getPrivateMessageDestinations().put(message, destination);

        if(message.getType().equals(MessageType.REPLACEMENT)){
            storage.getMessagesToBeSent().addFirst(message);
        } else{
            storage.getMessagesToBeSent().addLast(message);
        }
    }

    public void registerObserver(Observer observer) {
        receiver.registerObserver(observer);
    }

    public CopyOnWriteArrayList<SocketAddress> getNeighbors() {
        return neighbors;
    }
}
