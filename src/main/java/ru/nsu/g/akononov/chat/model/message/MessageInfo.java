package ru.nsu.g.akononov.chat.model.message;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.Date;

@Getter
@Setter
public class MessageInfo {
    private final static int MAX_ATTEMPT_NUMBER = 3;

    private Message data;
    private Date timeItWasSent;
    private final SocketAddress destination;

    public MessageInfo(Message message, Date timeItWasSent, SocketAddress destination) {
        data = message;
        this.timeItWasSent = timeItWasSent;
        this.destination = destination;
    }
}
