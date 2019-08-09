/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.messaging.impl;

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
    public void send() {
    }

    public MessageBuilder withPriority(int priority){
        return this;
    }

    @Override
    public MessageBuilder withDelay(int delay) {
        return this;
    }

    @Override
    public MessageBuilder expiringAfter(Duration  duration) {
        return this;
    }

    @Override
    public MessageBuilder withCorrelationId(String correlationId) {
        return this;
    }

}
