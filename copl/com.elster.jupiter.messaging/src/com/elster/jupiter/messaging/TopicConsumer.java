package com.elster.jupiter.messaging;

public interface TopicConsumer {
	String receive();
	void subscribe();
	void unSubscribe();
}
