package ru.nsu.g.akononov.chat.model.messageTransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.chat.model.message.MessagesStorage;
import ru.nsu.g.akononov.chat.model.tracker.SenderActivityTracker;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageInfo;
import ru.nsu.g.akononov.chat.model.message.MessageType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Sender implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    private static final long SEND_TIMEOUT = 2000;
    private static final long WAITING_ACK_TIMOUT = 2000;

    private final DatagramSocket socket;
    private final CopyOnWriteArrayList<SocketAddress> neighbors;

    private final MessagesStorage s = MessagesStorage.getInstance();
    private final SenderActivityTracker tracker;

    public Sender(DatagramSocket socket, CopyOnWriteArrayList<SocketAddress> neighbors, SenderActivityTracker senderActivityTracker) {
        this.socket = socket;
        this.neighbors = neighbors;

        tracker = senderActivityTracker;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = s.getMessagesToBeSent().poll(SEND_TIMEOUT, TimeUnit.MILLISECONDS);
                if (message != null) {
                    processMessageToBeSent(message);
                }
                tracker.sendActivityNotifications();
                resendNotConfirmedMessages();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessageToBeSent(Message message) {
        SocketAddress destination = s.getPrivateMessageDestinations().get(message);
        s.getPrivateMessageDestinations().remove(message);

        if (destination == null) {
            sendBroadcastMessage(message);
        } else {
            sendUnicastMessage(message, destination);
        }
    }

    private void sendBroadcastMessage(Message message) {
        for (SocketAddress neighbor : neighbors) {
            if (isSource(neighbor, message)) {
                continue;
            }
            sendUnicastMessage(message, neighbor);
        }
    }

    private void sendUnicastMessage(Message message, SocketAddress destination) {
        try {
            sendPacket(message.toByte(), destination);
            addMessageToBeConfirmed(message, destination);
            tracker.addSentMessage(destination, new Date());
            logger.debug("Sent {} #{} to {}", message.getType(), message.getUuid(), destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isSource(SocketAddress neighborAddress, Message message) {
        SocketAddress messageSource = s.getReceivedMessages().get(message);
        return (messageSource != null && messageSource.equals(neighborAddress));
    }

    private void addMessageToBeConfirmed(Message message, SocketAddress messageDestination) {
        MessageInfo messageInfo = new MessageInfo(message, new Date(), messageDestination);
        if (message.getType() != MessageType.ACK) {
            s.getMessagesToBeConfirmed().add(messageInfo);
        }
    }

    private void sendPacket(byte[] message, SocketAddress destination) throws IOException {
        DatagramPacket packet = new DatagramPacket(message, message.length, destination);
        socket.send(packet);
    }

    private void resendNotConfirmedMessages() {
        for (MessageInfo messageToBeConfirmed : s.getMessagesToBeConfirmed()) {

            long waitingAcknowledgeTime = System.currentTimeMillis() - messageToBeConfirmed.getTimeItWasSent().getTime();
            if (waitingAcknowledgeTime > WAITING_ACK_TIMOUT) {
                try {
                    resendMessage(messageToBeConfirmed);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resendMessage(MessageInfo message) throws IOException {
        byte[] byteMessage = message.getData().toByte();
        sendPacket(byteMessage, message.getDestination());
        message.setTimeItWasSent(new Date());

        logger.debug("Resent {} #{} to {}", message.getData().getType(), message.getData().getUuid(), message.getDestination());
    }

}