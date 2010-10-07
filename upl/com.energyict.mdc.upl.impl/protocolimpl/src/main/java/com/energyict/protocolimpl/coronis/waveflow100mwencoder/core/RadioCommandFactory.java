package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private WaveFlow100mW waveFlow100mW;

	// cached
	private FirmwareVersion firmwareVersion=null;
	private InternalDataCommand internalDataCommand=null;

	RadioCommandFactory(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}


	final EncoderCurrentReading readEncoderCurrentReading() throws IOException {
		EncoderCurrentReading o = new EncoderCurrentReading(waveFlow100mW);
		o.invoke();
		return o;
	}
	
	final EncoderDataloggingTable readEncoderDataloggingTable() throws IOException {
		EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow100mW);
		o.invoke();
		return o;
	}
	
	final EncoderDataloggingTable readEncoderDataloggingTable(final boolean portA, final boolean portB, final int nrOfValues, final int offsetFromMostRecentValue) throws IOException {
		EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow100mW,portA,portB,nrOfValues,offsetFromMostRecentValue);
		o.invoke();
		return o;
	}
	
	final FirmwareVersion readFirmwareVersion() throws IOException {
		if (firmwareVersion == null) {
			firmwareVersion = new FirmwareVersion(waveFlow100mW);
			firmwareVersion.invoke();
		}
		return firmwareVersion;
	}
	
	final InternalDataCommand readInternalData() throws IOException {
		if (internalDataCommand == null) {
			internalDataCommand = new InternalDataCommand(waveFlow100mW);
			internalDataCommand.invoke();
		}
		return internalDataCommand;
	}
	
	final LeakageEventTable readLeakageEventTable() throws IOException {
		LeakageEventTable leakageEventTable = new LeakageEventTable(waveFlow100mW);
		leakageEventTable.invoke();
		return leakageEventTable;
	}
	
}
