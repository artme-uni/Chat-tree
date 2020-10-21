package ru.nsu.g.akononov.chat.model.transfer;

import ru.nsu.g.akononov.chat.model.Model;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageType;
import ru.nsu.g.akononov.chat.model.message.Serializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Receiver implements Runnable, Model {
    private final static int MAX_MESSAGE_SIZE = 2000;

    private final DatagramSocket socket;
    private final CopyOnWriteArrayList<SocketAddress> neighbors;

    private final MessagesStorage s;

    public Receiver(DatagramSocket socket, CopyOnWriteArrayList<SocketAddress> neighbors, MessagesStorage storage) {
        this.socket = socket;
        this.neighbors = neighbors;
        this.s = storage;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] bytes = new byte[MAX_MESSAGE_SIZE];
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                socket.receive(packet);
                if(!neighbors.contains(packet.getSocketAddress())){
                    neighbors.add(packet.getSocketAddress());
                }
                Message message = Serializer.deserialization(packet.getData());

                if(!s.getReceivedMessages().containsKey(message)){
                    processNewMessage(message, packet);
                    s.getReceivedMessages().put(message, packet.getSocketAddress());
                }

                if(message.getType() != MessageType.ACK) {
                    addAcknowledge(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addAcknowledge(Message message){
        s.getMessagesToBeSent().add(new Message("", MessageType.ACK, message.getUuid().toString()));
    }

    private void processNewMessage(Message message, DatagramPacket packet){
        switch (message.getType()){
            case USER:
                newMessageNotification(message);
                s.getMessagesToBeSent().add(message);
                break;
            case SYSTEM:
                break;
            case ACK:
                s.getMessagesToBeConfirmed().removeIf(messageToBeConfirmed ->
                        messageToBeConfirmed.getData().getUuid().equals(UUID.fromString(message.getData())));
                break;
            case HI:
                break;
        }
    }
}
