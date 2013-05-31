package com.elster.jupiter.messaging.consumer;

import java.sql.SQLException;

import oracle.jdbc.aq.AQMessage;

public interface MessageHandler {
	void process(AQMessage message) throws SQLException;
}
