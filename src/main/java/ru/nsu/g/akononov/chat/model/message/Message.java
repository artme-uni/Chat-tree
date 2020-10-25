package ru.nsu.g.akononov.chat.model.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Message {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String sourceName;
    private MessageType type;
    private UUID uuid;
    private String content;
    private Date createTime;

    public Message(String sourceName, MessageType type, String data) {
        this.sourceName = sourceName;
        this.type = type;
        this.content = data;

        this.uuid = UUID.randomUUID();
        this.createTime = new Date();
    }

    public Message() {
    }

    public Message(Message message) {
        this.sourceName = message.getSourceName();
        this.type = message.getType();
        this.content = message.getContent();
        this.uuid = message.getUuid();
        this.createTime = message.getCreateTime();
    }

    public byte[] toByte() throws JsonProcessingException {
        return Serializer.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return uuid.equals(message.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        if (type == MessageType.USER) {
            return " [" + dateFormat.format(createTime) + "] " + sourceName + " : " + content;
        }
        return type + " : " + content;
    }

    public static String getFormattedDate(Date current) {
        return dateFormat.format(current);
    }

}
