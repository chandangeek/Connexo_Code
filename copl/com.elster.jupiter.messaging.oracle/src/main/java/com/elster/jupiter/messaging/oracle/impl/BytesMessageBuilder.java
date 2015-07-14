package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pubsub.Publisher;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.AQEnqueueOptions;
import oracle.jdbc.aq.AQMessage;
import oracle.jdbc.aq.AQMessageProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;

/**
 * MessageBuilder implementation that builds a message using raw bytes.
 */
class BytesMessageBuilder implements MessageBuilder {

    private static final int NO_RECIPIENTS_FOR_MESSAGE = 24033;
    private AQMessageProperties props;

    private final byte[] bytes;
    private DestinationSpec destinationSpec;
    private final AQFacade aqFacade;
    private final DataModel dataModel;
    private final Publisher publisher;

    /**
     * @param aqFacade
     * @param destinationSpec the intented DestinationSpec
     * @param bytes the payload.
     */
    BytesMessageBuilder(DataModel dataModel, AQFacade aqFacade, Publisher publisher, DestinationSpec destinationSpec, byte[] bytes) {
        this.dataModel = dataModel;
        this.publisher = publisher;
        this.aqFacade = aqFacade;
        this.destinationSpec = destinationSpec;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public MessageBuilder expiringAfter(Duration  duration) {
        try {
            return tryExpiringAfter(duration);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public MessageBuilder withCorrelationId(String correlationId) {
        try {
            getMessageProperties().setCorrelation(correlationId);
            return this;
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
            props = aqFacade.createAQMessageProperties();
        }
        return props;
    }

    private MessageBuilder tryExpiringAfter(Duration duration) throws SQLException {
        getMessageProperties().setExpiration((int) duration.getSeconds());
        return this;
    }

    private void trySend(byte[] bytes) throws SQLException {
        AQMessage message = aqFacade.create(getMessageProperties());
        AQEnqueueOptions options = new AQEnqueueOptions();
        if (destinationSpec.isBuffered()) {
        	options.setVisibility(AQEnqueueOptions.VisibilityOption.IMMEDIATE);
        	options.setDeliveryMode(AQEnqueueOptions.DeliveryMode.BUFFERED);
        }
        message.setPayload(bytes);
        try (Connection connection = dataModel.getConnection(false)) {
            OracleConnection oraConnection= connection.unwrap(OracleConnection.class);
            oraConnection.enqueue(destinationSpec.getName(), options, message);
            publisher.publish(message);
        }
    }
}
