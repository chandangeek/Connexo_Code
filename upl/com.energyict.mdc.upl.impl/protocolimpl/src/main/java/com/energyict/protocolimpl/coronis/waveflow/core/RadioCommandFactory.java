package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private WaveFlow waveFlow;

	// cached
	private FirmwareVersion firmwareVersion=null;
	
	RadioCommandFactory(WaveFlow waveFlow) {
		this.waveFlow = waveFlow;
	}


	final public CurrentReading readCurrentReading() throws IOException {
		CurrentReading o = new CurrentReading(waveFlow);
		o.invoke();
		return o;
	}

	final public ExtendedDataloggingTable readExtendedDataloggingTable() throws IOException {
		ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow);
		o.invoke();
		return o;
	}
	
	final public ExtendedDataloggingTable readExtendedDataloggingTable(final boolean inputA, final boolean inputB, final boolean inputC, final boolean inputD, final int nrOfValues, final int offsetFromMostRecentValue) throws IOException {
		ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow,inputA,inputB,inputC,inputD,nrOfValues,offsetFromMostRecentValue);
		o.invoke();
		return o;
	}
	
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
