package ru.nsu.g.akononov.chat.model;

import ru.nsu.g.akononov.chat.model.transfer.MessageTransfer;
import ru.nsu.g.akononov.chat.view.Observer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class Node implements Model{
    private int percentageLoss;
    private int port;
    private String userName;
    private SocketAddress parent;
    private boolean isRoot;

    private MessageTransfer transfer;

    public Node(int port, int percentageLoss, String name) throws SocketException {
        isRoot = true;
        initialize(port, percentageLoss, name);
    }

    public Node(int port, int percentageLoss, String name, String parentIP, int parentPort) throws SocketException {
        isRoot = false;
        initialize(port, percentageLoss, name);

        parent = new InetSocketAddress(parentIP, parentPort);
        transfer.addNeighbor(parent);
    }

    private void initialize(int port, int percentageLoss, String name) throws SocketException {
        this.port = port;
        this.percentageLoss = percentageLoss;
        this.userName = name;
        transfer = new MessageTransfer(port, userName);
    }

    @Override
    public void registerObserver(Observer observer) {
        transfer.getReceiver().registerObserver(observer);
    }

    public void sendMessage(String message){
        transfer.sendUserMessage(message);
    }


}
