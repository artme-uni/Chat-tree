package ru.nsu.g.akononov.chat.model.faultTolerance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.chat.Parser;
import ru.nsu.g.akononov.chat.model.messageTransfer.MessageTransfer;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Replacement {
    private static final Logger logger = LoggerFactory.getLogger(Replacement.class);

    private SocketAddress ownReplacement = null;
    private final ConcurrentHashMap<SocketAddress, SocketAddress> neighborsReplacements = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<SocketAddress> dependentNodes = new CopyOnWriteArrayList<>();

    private static volatile Replacement instance;
    private Replacement() {
    }
    public static Replacement getInstance() {
        if (instance == null) {
            synchronized (Replacement.class) {
                if (instance == null) {
                    instance = new Replacement();
                }
            }
        }
        return instance;
    }

    public void addReplacementNode(SocketAddress neighbor, String replacementAddress){
        if(replacementAddress.equals("YOU")){
            dependentNodes.add(neighbor);
            logger.info("I'm replacement for {}", neighbor);
            return;
        }

        SocketAddress replacementNode = Parser.parseAddress(replacementAddress.substring(1));
        addReplacementNode(neighbor, replacementNode);
    }

    public void addReplacementNode(SocketAddress neighbor, SocketAddress replacementNode){
        logger.info("{} is replacement for {}", replacementNode, neighbor);
        neighborsReplacements.put(neighbor, replacementNode);
    }

    public SocketAddress getOwnReplacementNode(){
        return ownReplacement;
    }

    public void sendOwnReplacement(MessageTransfer transfer, SocketAddress destination){
        transfer.sendReplacement(ownReplacement, destination);
    }

    public void sendOwnReplacement(MessageTransfer transfer){
        transfer.sendReplacement(ownReplacement);
    }

    public boolean isReplacementFor(SocketAddress neighbor){
        return dependentNodes.contains(neighbor);
    }

    public SocketAddress getNeighborReplacement(SocketAddress neighbor){
        return neighborsReplacements.get(neighbor);
    }

    public boolean updateOwnReplacement(CopyOnWriteArrayList<SocketAddress> neighbors){
        if(neighbors.isEmpty()){
            if(ownReplacement == null){
                return false;
            } else {
                ownReplacement = null;
                return true;
            }
        }

        if(ownReplacement == null || !neighbors.contains(ownReplacement)){
            ownReplacement = neighbors.get(0);
            return true;
        }

        return false;
    }

}
