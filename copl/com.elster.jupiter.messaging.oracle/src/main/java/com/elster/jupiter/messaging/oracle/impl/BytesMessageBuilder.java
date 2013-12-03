package com.elster.jupiter.messaging.oracle.impl;

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
import java.util.Arrays;

/**
 * MessageBuilder implementation that builds a message using raw bytes.
 */
class BytesMessageBuilder implements MessageBuilder {

    private static final int NO_RECIPIENTS_FOR_MESSAGE = 24033;
    private AQMessageProperties props;

    private final byte[] bytes;
    private DestinationSpec destinationSpec;

    /**
     * @param destinationSpec the intented DestinationSpec
     * @param bytes the payload.
     */
    BytesMessageBuilder(DestinationSpec destinationSpec, byte[] bytes) {
        this.destinationSpec = destinationSpec;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
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
        	// ignore ORA-24033: no recipients for message
        	if (ex.getErrorCode() != NO_RECIPIENTS_FOR_MESSAGE) {
        		throw new UnderlyingSQLFailedException(ex);
        	}
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
