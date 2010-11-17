package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

abstract public class AbstractRadioCommand {
	
	enum RadioCommandId {
		
		ReadParameterLegacy(0x10),
		WriteParameterLegacy(0x11),
		ReadParameter(0x18,true),
		WriteParameter(0x19),
		ExtendedDataloggingTable(0x09), // page 62 waveflow V2 document
		ReadCurrentRTC(0x14), // page 38 waveflow V2 document
		WriteCurrentRTC(0x15), // page 38 waveflow V2 document
		GlobalIndexReading(0x05,true), // page 42 waveflow V2 document
		
//		EncoderReadLeakageEventTable(0x04,true),
//		LeakageEventTable(0x04,true),
		FirmwareVersion(0x28);
		
		private int commandId;
		/**
		 * Some of th eradio commands return the 1 byte operation moder and 1 byte application status
		 */
		private boolean status;
		/**
		 * some of the radio commands return a 23 bytes generic header, explained at page 22 of the waveflow V2
		 */
		private boolean genericHeader;

		final boolean isStatus() {
			return status;
		}


		final boolean isGenericHeader() {
			return genericHeader;
		}
		
		
		final int getCommandId() {
			return commandId;
		}

		
		RadioCommandId(final int commandId) {
			this(commandId,false);
		}
		
		RadioCommandId(final int commandId, final boolean status) {
			this(commandId,status,false);
		}
		
		RadioCommandId(final int commandId, final boolean status, final boolean genericHeader) {
			this.commandId=commandId;
			this.status=status;
			this.genericHeader=genericHeader;
		}
		
	} // enum RadioCommandId

	/**
	 * The reference to the Waveflow protocol implementation class
	 */
	private WaveFlow waveFlow;
	
	/**
	 * the 1 byte operation mode send together with some of the radio command responses 
	 */
	private int operationMode=-1;
	
	/**
	 * the 1 byte application status send together with some of the radio command responses
	 */
	private int applicationStatus=-1;
	
	final int getOperationMode() {
		return operationMode;
	}

	final int getApplicationStatus() {
		return applicationStatus;
	}
	
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
				
				if (getRadioCommandId().isGenericHeader()) {
					byte[] temp = new byte[23];
					dais.read(temp);
				}
				else if (getRadioCommandId().isStatus()) {
					operationMode = WaveflowProtocolUtils.toInt(dais.readByte());
					applicationStatus = WaveflowProtocolUtils.toInt(dais.readByte());
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
