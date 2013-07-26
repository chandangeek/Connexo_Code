package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.MessageBuilder;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import org.joda.time.Seconds;

import java.sql.Connection;
import java.sql.SQLException;

class AQMessageOptionsBuilder implements MessageBuilder {
    private final AQMessage message;
    private DestinationSpecImpl destinationSpec;

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
        try (Connection connection = Bus.getConnection()) {
            OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
            oraConnection.enqueue(destinationSpec.getName(), new AQEnqueueOptions() , message);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    AQMessageOptionsBuilder(DestinationSpecImpl destinationSpec, AQMessage aqMessage) {
        this.destinationSpec = destinationSpec;
        this.message = aqMessage;
    }

    private MessageBuilder tryExpiringAfter(Seconds seconds) throws SQLException {
        if (seconds != null) {
            message.getMessageProperties().setExpiration(seconds.getSeconds());
        }
        return this;
    }
}
