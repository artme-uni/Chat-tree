package ru.nsu.g.akononov.chat.model.tracker;

import ru.nsu.g.akononov.chat.model.messageTransfer.MessageTransfer;

import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SenderActivityTracker {
    private final static int MAX_INACTIVITY_PERIOD = 1000;
    private final MessageTransfer transfer;

    private final CopyOnWriteArrayList<SocketAddress> neighbors;
    private final ConcurrentHashMap<SocketAddress, Date> sentMessageTime = new ConcurrentHashMap<>();

    public SenderActivityTracker(CopyOnWriteArrayList<SocketAddress> neighbors, MessageTransfer transfer) {
        this.neighbors = neighbors;
        this.transfer = transfer;
    }

    public void addSentMessage(SocketAddress destination, Date timeItWasSent){
        sentMessageTime.put(destination, timeItWasSent);
    }

    public void sendActivityNotifications(){
        neighbors.stream().filter(this::isNeedToSend)
                .collect(Collectors.toList()).forEach(transfer::sendHi);
    }

    private boolean isNeedToSend(SocketAddress neighbor){
        Date lastSentTime = sentMessageTime.get(neighbor);
        if(lastSentTime == null){
            return true;
        }
        long inactivePeriod = System.currentTimeMillis() - lastSentTime.getTime();
        return inactivePeriod > MAX_INACTIVITY_PERIOD;
    }
}