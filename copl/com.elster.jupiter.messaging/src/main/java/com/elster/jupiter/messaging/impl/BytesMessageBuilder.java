package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageEnqueuedEvent;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQFactory;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import org.joda.time.Seconds;

import java.sql.Connection;
import java.sql.SQLException;

class BytesMessageBuilder implements MessageBuilder {
    AQMessageProperties props;

    private final byte[] bytes;
    private DestinationSpecImpl destinationSpec;

    public BytesMessageBuilder(DestinationSpecImpl destinationSpec, byte[] bytes) {
        this.destinationSpec = destinationSpec;
        this.bytes = bytes;
    }

    @Override
    public MessageBuilder expiringAfter(Seconds seconds) {
        try {
            return tryExpiringAfter(seconds);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send() {
        try {
            trySend(bytes);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private AQMessageProperties getMessageProperties() throws SQLException {
        if (props == null) {
            props = AQFactory.createAQMessageProperties();
        }
        return props;
    }

    private MessageBuilder tryExpiringAfter(Seconds seconds) throws SQLException {
        getMessageProperties().setExpiration(seconds.getSeconds());
        return this;
    }

    private void trySend(byte[] bytes) throws SQLException {
        AQMessage message = AQFactory.createAQMessage(getMessageProperties());
        message.setPayload(bytes);
        try (Connection connection = Bus.getConnection()) {
            OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
            oraConnection.enqueue(destinationSpec.getName(), new AQEnqueueOptions() , message);
            Bus.fire(new MessageEnqueuedEvent(message));
        }
    }
}
