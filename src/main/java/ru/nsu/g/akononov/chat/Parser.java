package ru.nsu.g.akononov.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final static int ARG_COUNT = 7;
    private final static int MIN_PERCENTAGE = 0;
    private final static int MAX_PERCENTAGE = 100;

    private List<String> arguments;

    private boolean isRoot = true;
    private int ownPort;
    private String parentIP;
    private String nodeName;

    private int parentPort;
    private int percentageLoss;

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

    private void parseAddress(String address) throws IllegalArgumentException {
        String[] addr = address.split(":");
        parentIP = addr[0];
        parentPort = Integer.parseInt(addr[1]);
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
                    percentageLoss = Integer.parseInt(getArgumentValue(i));
                    if (percentageLoss > MAX_PERCENTAGE || percentageLoss < MIN_PERCENTAGE) {
                        throw new IllegalArgumentException("percentage loss have to be between " + MIN_PERCENTAGE +" and " + MAX_PERCENTAGE);
                    }
                    continue;
                }
                if (arguments.get(i).equals("-parent")) {
                    parseAddress(getArgumentValue(i));
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

    public String getParentIP() throws RuntimeException{
        if (!isRoot) {
            return parentIP;
        } else throw new RuntimeException("Cannot get the parent ID, It's a root");
    }

    public int getParentPort() throws RuntimeException{
        if (!isRoot) {
            return parentPort;
        } else throw new RuntimeException("Cannot get the parent ID, It's a root");
    }

    public int getPercentageLoss() {
        return percentageLoss;
    }

    public String getNodeName() {
        return nodeName;
    }

}