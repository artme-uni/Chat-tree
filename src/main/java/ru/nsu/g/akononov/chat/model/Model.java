package ru.nsu.g.akononov.chat.model;

import ru.nsu.g.akononov.chat.model.message.Message;
import ru.nsu.g.akononov.chat.view.Observer;

import java.util.ArrayList;

public interface Model {

    ArrayList<Observer> observers = new ArrayList<>();

    default void registerObserver(Observer observer){
        if (observer == null) {
            throw new NullPointerException();
        }
        if (observers.contains(observer)) {
            throw new IllegalArgumentException("Repeated observer:" + observer);
        }
        observers.add(observer);
    }

    default void newMessageNotification(Message message){
        for (Observer observer : observers) {
            observer.printNewMessage(message);
        }
    }
}
