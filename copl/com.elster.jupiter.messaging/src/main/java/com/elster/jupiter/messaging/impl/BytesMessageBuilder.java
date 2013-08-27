package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;
import org.joda.time.Seconds;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MessageBuilder implementation that builds a message using raw bytes.
 */
class BytesMessageBuilder implements MessageBuilder {
    AQMessageProperties props;

    private final byte[] bytes;
    private DestinationSpec destinationSpec;

    BytesMessageBuilder(DestinationSpec destinationSpec, byte[] bytes) {
        this.destinationSpec = destinationSpec;
        this.bytes = bytes;
    }

    @Override
    public MessageBuilder expiringAfter(Seconds seconds) {
        try {
            return tryExpiringAfter(seconds);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public void send() {
        try {
            trySend(bytes);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private AQMessageProperties getMessageProperties() throws SQLException {
        if (props == null) {
            props = Bus.getAQFacade().createAQMessageProperties();
        }
        return props;
    }

    private MessageBuilder tryExpiringAfter(Seconds seconds) throws SQLException {
        getMessageProperties().setExpiration(seconds.getSeconds());
        return this;
    }

    private void trySend(byte[] bytes) throws SQLException {
        AQMessage message = Bus.getAQFacade().create(getMessageProperties());
        message.setPayload(bytes);
        try (Connection connection = Bus.getConnection()) {
            OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
            oraConnection.enqueue(destinationSpec.getName(), new AQEnqueueOptions() , message);
            Bus.fire(message);
        }
    }
}
