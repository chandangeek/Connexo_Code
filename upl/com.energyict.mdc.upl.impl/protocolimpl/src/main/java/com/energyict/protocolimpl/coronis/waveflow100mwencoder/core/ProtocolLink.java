package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.util.TimeZone;
import java.util.logging.Logger;

public interface ProtocolLink {
	
	Logger getLogger();
	TimeZone getTimeZone();
	WaveFlowConnect getWaveFlowConnect();
}
