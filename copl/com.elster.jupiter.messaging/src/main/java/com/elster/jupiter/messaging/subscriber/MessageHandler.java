package com.elster.jupiter.messaging.subscriber;

import com.elster.jupiter.messaging.Message;

import java.sql.SQLException;

public interface MessageHandler {

    void process(Message message) throws SQLException;
}
