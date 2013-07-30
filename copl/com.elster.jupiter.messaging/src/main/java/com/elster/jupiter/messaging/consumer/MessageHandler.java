package com.elster.jupiter.messaging.consumer;

import com.elster.jupiter.messaging.Message;

import java.sql.SQLException;

public interface MessageHandler {

    void process(Message message) throws SQLException;
}
