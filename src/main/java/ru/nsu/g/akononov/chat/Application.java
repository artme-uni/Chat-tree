package ru.nsu.g.akononov.chat;

import ru.nsu.g.akononov.chat.model.Node;
import ru.nsu.g.akononov.chat.view.GUI;

import java.net.SocketException;

public class Application {

    public static void main(String[] args) {
        try {
            Parser p = new Parser(args);
            Node node;
            if (p.isRoot()) {
                node = new Node(p.getOwnPort(), p.getLossesPercent(), p.getNodeName());
            } else {
                node = new Node(p.getOwnPort(), p.getLossesPercent(), p.getNodeName(), p.getParentAddr());
            }

            GUI view = new GUI(node);
            node.registerObserver(view);

        } catch (IllegalArgumentException | SocketException exception) {
            exception.printStackTrace();
        }
    }
}
