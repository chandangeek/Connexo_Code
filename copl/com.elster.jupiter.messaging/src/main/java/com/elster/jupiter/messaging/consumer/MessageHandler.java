package com.elster.jupiter.messaging.consumer;

import oracle.jdbc.aq.AQMessage;

import java.sql.SQLException;

public interface MessageHandler {

    void process(AQMessage message) throws SQLException;
}
