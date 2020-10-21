package ru.nsu.g.akononov.chat;

import ru.nsu.g.akononov.chat.model.Node;
import ru.nsu.g.akononov.chat.view.CLI;

import java.net.SocketException;

public class Application {
    public static void main(String[] args) {
        try {
            var currentNode = new Parser(args);
            Node node;
            if(currentNode.isRoot()){
                node = new Node(currentNode.getOwnPort(), currentNode.getPercentageLoss(), currentNode.getNodeName());
            } else {
                node = new Node(currentNode .getOwnPort(), currentNode .getPercentageLoss(), currentNode.getNodeName(),
                        currentNode .getParentIP(), currentNode .getParentPort());
            }

            //Message

            var CLI = new CLI(node);
            node.registerObserver(CLI);

        } catch (IllegalArgumentException | SocketException exception){
            exception.printStackTrace();
        }
    }
}
