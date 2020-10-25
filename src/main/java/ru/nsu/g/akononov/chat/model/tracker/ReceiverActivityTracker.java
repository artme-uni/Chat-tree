package ru.nsu.g.akononov.chat.model.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.chat.model.faultTolerance.Replacement;
import ru.nsu.g.akononov.chat.model.message.MessagesStorage;
import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.model.messageTransfer.MessageTransfer;

import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ReceiverActivityTracker implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ReceiverActivityTracker.class);

    private final static int MAX_INACTIVITY_PERIOD = 10 * 1000;

    private final CopyOnWriteArrayList<SocketAddress> neighbors;
    private final ConcurrentHashMap<SocketAddress, Date> lastReceivedMessageTime = new ConcurrentHashMap<>();
    private final MessagesStorage storage = MessagesStorage.getInstance();
    private final MessageTransfer transfer;
    private final Replacement replacement = Replacement.getInstance();

    public ReceiverActivityTracker(MessageTransfer transfer) {
        this.neighbors = transfer.getNeighbors();
        this.transfer = transfer;
    }

    public void addReceivedMessage(SocketAddress source, Date timeItWasReceived){
        lastReceivedMessageTime.put(source, timeItWasReceived);
    }

    public void refreshInactiveNeighbors(){
        neighbors.stream().filter(this::isNeighborInactive).
                collect(Collectors.toList()).forEach(this::addInactiveNeighbor);
    }

    private boolean isNeighborInactive(SocketAddress neighbor){
        Date messageTime = lastReceivedMessageTime.get(neighbor);
        if(messageTime == null){
            Date now = new Date();
            lastReceivedMessageTime.put(neighbor, now);
            messageTime = now;
        }

        long inactivePeriod = new Date().getTime() - messageTime.getTime();
        return inactivePeriod > MAX_INACTIVITY_PERIOD;
    }

    private void addInactiveNeighbor(SocketAddress neighbor){
        logger.info("Cannot connect to " + neighbor);
        neighbors.remove(neighbor);

        boolean isUpdated = replacement.updateOwnReplacement(neighbors);
        if(isUpdated){
            replacement.sendOwnReplacement(transfer);
        }

        List<Message> unsentMessages = storage.getUnsentMessages(neighbor);
        cleanMessageToBeSent(unsentMessages);

        if(!replacement.isReplacementFor(neighbor)){
            SocketAddress inactiveNodeReplacement = replacement.getNeighborReplacement(neighbor);
            if(inactiveNodeReplacement != null) {
                transfer.sendAll(unsentMessages, inactiveNodeReplacement);
            }
        }
    }

    private void cleanMessageToBeSent(List<Message> unsentMessages){
        unsentMessages.forEach(message -> {
            storage.getPrivateMessageDestinations().remove(message);
            storage.getMessagesToBeSent().remove(message);
            storage.removeMessageToBeConfirmed(message);
        });
    }

    @Override
    public void run() {
        refreshInactiveNeighbors();
    }
}