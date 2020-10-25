package ru.nsu.g.akononov.chat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.chat.model.faultTolerance.Replacement;
import ru.nsu.g.akononov.chat.model.messageTransfer.MessageTransfer;
import ru.nsu.g.akononov.chat.view.Observer;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node implements Model {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    private int lossesPercent;
    private int port;
    private String userName;
    private SocketAddress parent;
    private boolean isRoot;

    private final CopyOnWriteArrayList<SocketAddress> neighbors = new CopyOnWriteArrayList<>();

    private final MessageTransfer transfer;
    private final Replacement replacements = Replacement.getInstance();

    public Node(int port, int percentageLoss, String name) throws SocketException {
        isRoot = true;
        initialize(port, percentageLoss, name);
        transfer = new MessageTransfer(port, neighbors, userName, lossesPercent);
    }

    public Node(int port, int percentageLoss, String name, SocketAddress parent) throws SocketException {
        isRoot = false;
        initialize(port, percentageLoss, name);
        transfer = new MessageTransfer(port, neighbors, userName, lossesPercent);

        addParent(parent);
    }

    private void initialize(int port, int percentageLoss, String name) {
        this.port = port;
        this.lossesPercent = percentageLoss;
        this.userName = name;

        logger.info("I'm the node {}", userName);
    }

    public void addParent(SocketAddress neighbor) {
        neighbors.add(neighbor);
        replacements.updateOwnReplacement(neighbors);
        replacements.sendOwnReplacement(transfer, neighbor);
        logger.info("Connected to {}", neighbor);
    }

    @Override
    public void registerObserver(Observer observer) {
        transfer.registerObserver(observer);
    }

    public void sendMessage(String message) {
        transfer.sendUserMessage(message);
    }

    public String getUserName() {
        return userName;
    }
}
