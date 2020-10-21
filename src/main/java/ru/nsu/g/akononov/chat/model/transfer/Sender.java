package ru.nsu.g.akononov.chat.model.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.message.MessageToBeConfirmed;
import ru.nsu.g.akononov.chat.model.message.MessageType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Sender implements Runnable {
    private static final long SEND_TIMEOUT = 2000;
    private static final long WAITING_ACK_TIMOUT = 10000;

    private final DatagramSocket socket;
    private final CopyOnWriteArrayList<SocketAddress> neighbors;

    private final MessagesStorage s;

    public Sender(DatagramSocket socket, CopyOnWriteArrayList<SocketAddress> neighbors, MessagesStorage storage) {
        this.socket = socket;
        this.neighbors = neighbors;
        this.s = storage;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = s.getMessagesToBeSent().poll(SEND_TIMEOUT, TimeUnit.MILLISECONDS);
                if (message != null) {
                    sendBroadcast(message);
                }
                resendAll();
            } catch (InterruptedException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcast(Message message) throws JsonProcessingException {
        var byteMessage = message.toByte();
        SocketAddress source = s.getReceivedMessages().get(message);
        for(SocketAddress neighborAddress : neighbors){
            if(source != null && source.equals(neighborAddress)) {
                continue;
            }
            try {
                send(byteMessage, neighborAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MessageToBeConfirmed messageToBeConfirmed = new MessageToBeConfirmed(message, new Date(), neighborAddress);
            s.getSentMessage().add(messageToBeConfirmed);
            if(message.getType() != MessageType.ACK) {
                s.getMessagesToBeConfirmed().add(messageToBeConfirmed);
            }
        }
    }

    private void send(byte[] message, SocketAddress destination) throws IOException {
        var packet = new DatagramPacket(message, message.length, destination);
        socket.send(packet);
    }

    private void resendAll() {
        for (MessageToBeConfirmed messageToBeConfirmed : s.getMessagesToBeConfirmed()) {
            long waitingAcknowledgeTime = System.currentTimeMillis() - messageToBeConfirmed.getTimeItWasSent().getTime();
            if (waitingAcknowledgeTime > WAITING_ACK_TIMOUT) {
                boolean canBeResend = messageToBeConfirmed.addResendAttempt();
                if (canBeResend) {
                    try {
                        resendMessage(messageToBeConfirmed);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    s.getMessagesCannotBeDelivered().add(messageToBeConfirmed);
                    System.err.println("Cannot deliver message");
                }
            }
        }
        s.getMessagesToBeConfirmed().removeAll(s.getMessagesCannotBeDelivered());
    }

    private void resendMessage(MessageToBeConfirmed message) throws IOException {
        var byteMessage = message.getData().toByte();
        send(byteMessage, message.getDestination());
        message.setTimeItWasSent(new Date());
    }
}