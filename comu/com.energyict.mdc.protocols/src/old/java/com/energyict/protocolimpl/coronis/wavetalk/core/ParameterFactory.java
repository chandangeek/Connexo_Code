/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;
import java.util.Date;

public interface ParameterFactory {

	int readApplicationStatus() throws IOException;
	void writeApplicationStatus(final int status) throws IOException;
	void writeOperatingMode(final int operatingModeVal, final int mask) throws IOException;
	void writeOperatingMode(final int operatingModeVal) throws IOException;
	Date readTimeDateRTC() throws IOException;
	void writeTimeDateRTC(final Date date) throws IOException;
	BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException;
	Date readBatteryLifeDateEnd() throws IOException;
}
