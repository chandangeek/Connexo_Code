package com.elster.jupiter.messaging;

public interface ConsumerSpec {
	DestinationSpec getDestination();
	String getName();
	int getWorkerCount();
}
