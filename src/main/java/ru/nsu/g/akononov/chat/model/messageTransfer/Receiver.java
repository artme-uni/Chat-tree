package ru.nsu.g.akononov.chat.model.messageTransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.chat.model.Model;
import ru.nsu.g.akononov.chat.model.faultTolerance.Replacement;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageType;
import ru.nsu.g.akononov.chat.model.message.MessagesStorage;
import ru.nsu.g.akononov.chat.model.message.Serializer;
import ru.nsu.g.akononov.chat.model.tracker.ReceiverActivityTracker;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Receiver implements Runnable, Model {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final static int MAX_MESSAGE_SIZE = 2 * 1000;
    private final static int RECEIVE_TIMEOUT = 1000;

    private final DatagramSocket socket;
    private final CopyOnWriteArrayList<SocketAddress> neighbors;
    private final MessageTransfer transfer;

    private final MessagesStorage s = MessagesStorage.getInstance();
    private final ReceiverActivityTracker tracker;
    private final Replacement replacements = Replacement.getInstance();

    private final int lossesPercent;

    public Receiver(DatagramSocket socket, MessageTransfer transfer,
                    ReceiverActivityTracker receiverActivityTracker, int lossesPercent) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(RECEIVE_TIMEOUT);
        this.neighbors = transfer.getNeighbors();
        this.lossesPercent = lossesPercent;
        this.transfer = transfer;

        tracker = receiverActivityTracker;

    }

    @Override
    public void run() {
        byte[] bytes = new byte[MAX_MESSAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        while (true) {
            try {
                socket.receive(packet);
                if(!isMissing()) {
                    processReceivePacket(packet);
                }
            } catch (SocketTimeoutException ignored){}
            catch (IOException e) {
                e.printStackTrace();
            } finally {
                tracker.refreshInactiveNeighbors();
            }
        }
    }

    private void processReceivePacket(DatagramPacket packet) throws IOException {
        SocketAddress packetSource = packet.getSocketAddress();

        if(!neighbors.contains(packetSource)){
            logger.info("Join {}", packetSource);
            neighbors.add(packetSource);
            replacements.updateOwnReplacement(neighbors);
            replacements.sendOwnReplacement(transfer, packetSource);
        }
        Message message = Serializer.deserialization(packet.getData());
        tracker.addReceivedMessage(packetSource, new Date());

        if(!s.isContainReceivedMessage(message)){
            logger.debug("Receive {} #{} from {}", message.getType(), message.getUuid(), packetSource);
            processReceivedMessage(message, packet);
            s.addReceivedMessage(message, packetSource);
        }

        if(message.getType() != MessageType.ACK) {
            addAcknowledge(message);
        }
    }

    private void addAcknowledge(Message message){
        s.getMessagesToBeSent().addLast(new Message("", MessageType.ACK, message.getUuid().toString()));
    }

    private boolean isMissing(){
        int randomDouble = (int) ( Math.random() * 99);
        return randomDouble < lossesPercent;
    }

    private void processReceivedMessage(Message message, DatagramPacket packet){
        switch (message.getType()){
            case USER:
                newMessageNotification(message);
                s.getMessagesToBeSent().addLast(message);
                break;
            case ACK:
/*              List<MessageInfo> ack = s.getMessagesToBeConfirmed().stream()
                        .filter(messageToBeConfirmed ->
                                messageToBeConfirmed.getData().getUuid().equals(UUID.fromString(message.getContent())))
                        .filter(message1 -> message1.getData().getType().equals(MessageType.USER)).collect(Collectors.toList());

                if(ack.size() != 0){
                    logger.info("Receive {} #{} from {}", message.getType(), ack.get(0).getData().getUuid(), packet.getSocketAddress());
                }*/

                s.getMessagesToBeConfirmed()
                        .removeIf(messageToBeConfirmed ->
                                (messageToBeConfirmed.getData().getUuid().equals(UUID.fromString(message.getContent()))
                        && (messageToBeConfirmed.getAddress().equals(packet.getSocketAddress()))));
                break;
            case ALIVE:
                break;
            case REPLACEMENT:
                replacements.addReplacementNode(packet.getSocketAddress(), message.getContent());
                break;
        }
    }
}