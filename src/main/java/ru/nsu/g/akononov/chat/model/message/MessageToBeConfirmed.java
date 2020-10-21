package ru.nsu.g.akononov.chat.model.message;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.Date;

@Getter
@Setter
public class MessageToBeConfirmed{
    private final static int MAX_ATTEMPT_NUMBER = 3;

    private Message data;
    private Date timeItWasSent;
    private final SocketAddress destination;

    private int attemptNumber = 0;

    public MessageToBeConfirmed(Message message, Date timeItWasSent, SocketAddress destination) {
        data = message;
        this.timeItWasSent = timeItWasSent;
        this.destination = destination;
    }

    public boolean addResendAttempt(){
        return ++attemptNumber <= MAX_ATTEMPT_NUMBER;
    }
}
