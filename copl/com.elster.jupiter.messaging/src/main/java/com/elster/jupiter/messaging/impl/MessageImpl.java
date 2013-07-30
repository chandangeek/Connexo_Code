package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.Message;
import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;

public class MessageImpl implements Message {

    private final byte[] payload;

    public MessageImpl(AQMessage aqMessage) throws SQLException {
        this.payload = aqMessage.getPayload();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}
