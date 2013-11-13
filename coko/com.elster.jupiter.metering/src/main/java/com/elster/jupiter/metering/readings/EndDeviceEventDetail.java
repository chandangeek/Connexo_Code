package com.elster.jupiter.metering.readings;

import com.elster.jupiter.util.units.Quantity;

public interface EndDeviceEventDetail {
	String getName();
	Quantity getValue();
}
