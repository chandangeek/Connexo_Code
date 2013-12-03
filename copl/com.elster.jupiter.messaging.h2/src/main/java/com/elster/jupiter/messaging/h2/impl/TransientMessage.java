package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.Message;

import java.util.Arrays;

class TransientMessage implements Message {

    private final byte[] data;

    public TransientMessage(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    @Override
    public byte[] getPayload() {
        return Arrays.copyOf(data, data.length);
    }
}
