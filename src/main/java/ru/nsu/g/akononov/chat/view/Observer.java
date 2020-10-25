package ru.nsu.g.akononov.chat.view;

import ru.nsu.g.akononov.chat.model.message.Message;

public interface Observer {
    void printUserMessage(Message message);
}
