package com.elster.jupiter.metering;

import com.elster.jupiter.util.HasName;

public interface AmrSystem extends HasName {
	int getId();
	Meter newMeter(String mRid);
}
