/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import java.util.TimeZone;
import java.util.logging.Logger;

public interface ProtocolLink {
	
	Logger getLogger();
	TimeZone getTimeZone();
	WaveFlowConnect getWaveFlowConnect();
	int getInfoTypeProtocolRetriesProperty();
}
