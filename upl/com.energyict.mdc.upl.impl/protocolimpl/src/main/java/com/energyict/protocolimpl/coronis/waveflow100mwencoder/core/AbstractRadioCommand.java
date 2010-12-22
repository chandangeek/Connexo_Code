package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;

abstract public class AbstractRadioCommand {
	
	enum EncoderRadioCommandId {
		
		ReadParameter(0x18),
		WriteParameter(0x19),
		EncoderCurrentReading(0x01,true),
		EncoderDataloggingTable(0x07,true),
		EncoderReadLeakageEventTable(0x04,true),
		EncoderInternalData(0x0B,true),
		MBusInternalLogs(0x0D,true),
		LeakageEventTable(0x04,true),
		MeterDetection(0x0C),
		FirmwareVersion(0x28);
		
		private int commandId;
		private boolean readGenericHeader;
		
		final boolean isReadGenericHeader() {
			return readGenericHeader;
		}

		final int getCommandId() {
			return commandId;
		}

		EncoderRadioCommandId(final int commandId) {
			this(commandId,false);
		}
		EncoderRadioCommandId(final int commandId, final boolean readGenericHeader) {
			this.commandId=commandId;
			this.readGenericHeader=readGenericHeader;
		}
	} // enum EncoderRadioCommandId
	
	private GenericHeader genericHeader;
	
	final public GenericHeader getEncoderGenericHeader() {
		return genericHeader;
	}

	/**
	 * The reference to the Waveflow100mW protocol implementation class
	 */
	private WaveFlow100mW waveFlow100mW;
	
	final WaveFlow100mW getWaveFlow100mW() {
		return waveFlow100mW;
	}
	
	AbstractRadioCommand(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}

	abstract void parse(byte[] data) throws IOException;
	abstract byte[] prepare() throws IOException;
	abstract EncoderRadioCommandId getEncoderRadioCommandId();
	
	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(getEncoderRadioCommandId().getCommandId());
			daos.write(prepare()); // write 1 parameter
			parseRead(getWaveFlow100mW().getWaveFlowConnect().sendData(baos.toByteArray()));
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}
	
	private final void parseRead(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			
			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | getEncoderRadioCommandId().getCommandId())) {
				throw new WaveFlow100mwEncoderException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {
				
				if ((commandIdAck == (0x80 | EncoderRadioCommandId.EncoderDataloggingTable.getCommandId())) && 
					(data.length == 2) && 
					(WaveflowProtocolUtils.toInt(data[1]) == 0xff)) {
					throw new WaveFlow100mwEncoderException("Datalogging not yet available...");
				}
				
				if (getEncoderRadioCommandId().isReadGenericHeader()) {
					
					if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
						genericHeader = new EncoderGenericHeader(dais, getWaveFlow100mW().getLogger(), getWaveFlow100mW().getTimeZone());
					}
					else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
						genericHeader = new MBusGenericHeader(dais, getWaveFlow100mW().getLogger(), getWaveFlow100mW().getTimeZone());
					}
					
					waveFlow100mW.setCachedGenericHeader(genericHeader);
				}
				byte[] temp = new byte[dais.available()];
				dais.read(temp);
				parse(temp);
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
	}
}
