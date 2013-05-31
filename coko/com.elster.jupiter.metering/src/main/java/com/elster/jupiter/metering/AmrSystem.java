package com.elster.jupiter.metering;

public interface AmrSystem {
	int getId();
	String getName();
	Meter newMeter(String mRid);
}
