package ru.nsu.g.akononov.chat.view;

import ru.nsu.g.akononov.chat.model.Node;

import java.util.Scanner;

public class CLI implements Observer {

    public CLI(Node node){
        Thread scan = new Thread(() ->
        {
            var in = new Scanner(System.in);
            while (in.hasNext()){
                String message = in.nextLine();
                node.sendMessage(message);
            }
        });
        scan.start();
    }
}
