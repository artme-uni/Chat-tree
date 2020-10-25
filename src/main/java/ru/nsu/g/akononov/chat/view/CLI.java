package ru.nsu.g.akononov.chat.view;

import ru.nsu.g.akononov.chat.model.Node;
import ru.nsu.g.akononov.chat.model.message.Message;

import java.util.Scanner;

public class CLI implements Observer {

    public CLI(Node node){
        Thread scan = new Thread(() ->
        {
            Scanner in = new Scanner(System.in);
            while (in.hasNext()){
                String message = in.nextLine();
                node.sendMessage(message);
            }
        });
        scan.start();
    }

    @Override
    public void printUserMessage(Message message) {
        System.out.println(message);
    }
}
