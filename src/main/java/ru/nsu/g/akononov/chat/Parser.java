package ru.nsu.g.akononov.chat;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final static int ARG_COUNT = 7;
    private final static int MIN_PERCENTAGE = 0;
    private final static int MAX_PERCENTAGE = 100;

    private List<String> arguments;

    private boolean isRoot = true;
    private SocketAddress parentAddress;
    private String nodeName;
    private int ownPort;
    private int lossesPercent;

    public Parser(String... args) throws IllegalArgumentException {
        try {
            parse(args);
        } catch (IllegalArgumentException exception) {
            System.err.println("Cannot parse arguments: " + (exception.getMessage() != null ? exception.getMessage() : ""));
            printHint();
            throw exception;
        }
    }

    private String getArgumentValue(int index) {
        String value = arguments.get(index + 1);
        arguments.remove(index + 1);
        arguments.remove(index);
        return value;
    }

    public static InetSocketAddress parseAddress(String address) throws IllegalArgumentException {
        String[] addr = address.split(":");
        String host = addr[0];
        int port = Integer.parseInt(addr[1]);

        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        if(socketAddress.isUnresolved()){
            throw new IllegalArgumentException();
        }

        return new InetSocketAddress(host, port);
    }


    public void parse(String[] args) throws IllegalArgumentException {
        if (args.length == ARG_COUNT || args.length == ARG_COUNT - 2) {
            arguments = new ArrayList<>(Arrays.asList(args));

            for (int i = 0; i < arguments.size(); ) {
                if (arguments.get(i).equals("-port")) {
                    ownPort = Integer.parseInt(getArgumentValue(i));
                    continue;
                }
                if (arguments.get(i).equals("-loss")) {
                    lossesPercent = Integer.parseInt(getArgumentValue(i));
                    if (lossesPercent > MAX_PERCENTAGE || lossesPercent < MIN_PERCENTAGE) {
                        throw new IllegalArgumentException("percentage loss have to be between " + MIN_PERCENTAGE +" and " + MAX_PERCENTAGE);
                    }
                    continue;
                }
                if (arguments.get(i).equals("-parent")) {
                    parentAddress = parseAddress(getArgumentValue(i));
                    isRoot = false;
                    continue;
                }
                i++;
            }
            if (arguments.size() != 1) {
                throw new IllegalArgumentException();
            }
            nodeName = arguments.get(0);

        } else {
            throw new IllegalArgumentException();
        }
    }

    public void printHint() {
        System.out.println("Obligatory arguments: \n" +
                "\t*\t NODE_NAME \n" +
                "\t*\t -port [OWN_PORT] \n" +
                "\t*\t -loss [PERCENTAGE_LOSS]\n" +
                "Optional arguments: \n" +
                "\t*\t -parent [PARENT_ADDR:PORT]\n");
    }

    public boolean isRoot() {
        return isRoot;
    }

    public int getOwnPort() {
        return ownPort;
    }

    public SocketAddress getParentAddr() throws RuntimeException{
        if (!isRoot) {
            return parentAddress;
        } else throw new RuntimeException("Cannot get the parent address, It's a root");
    }

    public int getLossesPercent() {
        return lossesPercent;
    }

    public String getNodeName() {
        return nodeName;
    }

}