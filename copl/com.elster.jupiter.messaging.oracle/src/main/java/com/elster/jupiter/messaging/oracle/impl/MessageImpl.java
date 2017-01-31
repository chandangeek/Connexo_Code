/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.Message;

import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;

/**
 * A Message implementation which is a simple wrapper around the payload.
 */
class MessageImpl implements Message {

    private final byte[] payload;

    MessageImpl(AQMessage aqMessage) throws SQLException {
        this.payload = aqMessage.getPayload();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

}