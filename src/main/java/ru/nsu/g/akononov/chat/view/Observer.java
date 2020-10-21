package ru.nsu.g.akononov.chat.view;

import ru.nsu.g.akononov.chat.model.message.Message;

public interface Observer {
    default void printNewMessage(Message message){
        System.out.println(message);
    }
}
