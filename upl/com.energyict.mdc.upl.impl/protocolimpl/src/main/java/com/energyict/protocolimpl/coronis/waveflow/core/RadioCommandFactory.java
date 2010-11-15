package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private WaveFlow waveFlow;

	// cached
	private FirmwareVersion firmwareVersion=null;
	
	RadioCommandFactory(WaveFlow waveFlow) {
		this.waveFlow = waveFlow;
	}


//	final EncoderCurrentReading readEncoderCurrentReading() throws IOException {
//		EncoderCurrentReading o = new EncoderCurrentReading(waveFlow);
//		o.invoke();
//		return o;
//	}
//	
//	final public EncoderDataloggingTable readEncoderDataloggingTable() throws IOException {
//		EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow);
//		o.invoke();
//		return o;
//	}
//	
//	final public EncoderDataloggingTable readEncoderDataloggingTable(final boolean portA, final boolean portB, final int nrOfValues, final int offsetFromMostRecentValue) throws IOException {
//		EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow,portA,portB,nrOfValues,offsetFromMostRecentValue);
//		o.invoke();
//		return o;
//	}
	
	final FirmwareVersion readFirmwareVersion() throws IOException {
		if (firmwareVersion == null) {
			firmwareVersion = new FirmwareVersion(waveFlow);
			firmwareVersion.invoke();
		}
		return firmwareVersion;
	}
	
	
	
//	final public LeakageEventTable readLeakageEventTable() throws IOException {
//		LeakageEventTable leakageEventTable = new LeakageEventTable(waveFlow);
//		leakageEventTable.invoke();
//		return leakageEventTable;
//	}
	
	
}
