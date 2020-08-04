/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.Message;

import java.util.Arrays;

public class TransientMessage implements Message {

    private final byte[] data;
    private String correlationId;

    public TransientMessage(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public byte[] getPayload() {
        return Arrays.copyOf(data, data.length);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
