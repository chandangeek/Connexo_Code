package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

abstract public class AbstractRadioCommand {
	
	enum RadioCommandId {
		
		ReadParameterLegacy(0x10),
		WriteParameterLegacy(0x11),
		ReadParameter(0x18),
		WriteParameter(0x19),
		ExtendedDataloggingTable(0x09),
		ReadCurrentRTC(0x14), // page 38 waveflow V2 document
		WriteCurrentRTC(0x15), // page 38 waveflow V2 document
		
//		EncoderCurrentReading(0x01,true),
//		EncoderReadLeakageEventTable(0x04,true),
//		EncoderInternalData(0x0B,true),
//		MBusInternalLogs(0x0D,true),
//		LeakageEventTable(0x04,true),
//		MeterDetection(0x0C),
		FirmwareVersion(0x28);
		
		private int commandId;

		final int getCommandId() {
			return commandId;
		}

		RadioCommandId(final int commandId) {
			this.commandId=commandId;
		}
	} // enum RadioCommandId

	/**
	 * The reference to the Waveflow protocol implementation class
	 */
	private WaveFlow waveFlow;
	
	final WaveFlow getWaveFlow() {
		return waveFlow;
	}
	
	AbstractRadioCommand(WaveFlow waveFlow) {
		this.waveFlow = waveFlow;
	}

	abstract void parse(byte[] data) throws IOException;
	abstract byte[] prepare() throws IOException;
	abstract RadioCommandId getRadioCommandId();
	
	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(getRadioCommandId().getCommandId());
			daos.write(prepare()); // write 1 parameter
			parseResponse(getWaveFlow().getWaveFlowConnect().sendData(baos.toByteArray()));
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}

	/**
	 * Set the Radio parameter initiated value.
	 * @throws IOException
	 */
	void set() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(getRadioCommandId().getCommandId());
			daos.write(prepare()); // write 1 parameter
			parseResponse(getWaveFlow().getWaveFlowConnect().sendData(baos.toByteArray()));
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}
	
	
	private final void parseResponse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			
			int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
				throw new WaveFlowException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
			}
			else {
				
				if ((commandIdAck == (0x80 | RadioCommandId.ExtendedDataloggingTable.getCommandId())) && 
					(data.length == 2) && 
					(WaveflowProtocolUtils.toInt(data[1]) == 0xff)) {
					throw new WaveFlowException("Datalogging not yet available...");
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
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
	}
	

	
}
