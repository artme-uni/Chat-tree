package ru.nsu.g.akononov.chat.model.message;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
public class MessageInfo {
    private final static int MAX_ATTEMPT_NUMBER = 3;

    private Message data;
    private Date time;
    private final SocketAddress address;

    public MessageInfo(Message message, Date time, SocketAddress address) {
        data = message;
        this.time = time;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageInfo message = (MessageInfo) o;
        return data.equals(message.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
