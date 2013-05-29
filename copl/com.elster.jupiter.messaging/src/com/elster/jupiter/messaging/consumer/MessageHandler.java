package com.elster.jupiter.messaging.consumer;

import oracle.jdbc.aq.AQMessage;

public interface MessageHandler {
	void process(AQMessage message);
}
