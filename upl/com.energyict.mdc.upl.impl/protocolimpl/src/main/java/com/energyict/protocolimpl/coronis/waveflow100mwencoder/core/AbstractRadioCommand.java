package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.AbstractParameter.ParameterId;

abstract public class AbstractRadioCommand {
	
	enum EncoderRadioCommandId {
		
		ReadParameter(0x18),
		WriteParameter(0x19),
		EncoderCurrentReading(0x01),
		EncoderDataloggingTable(0x07),
		EncoderReadLeakageEventTable(0x04);
		
		private int commandId;
		
		final int getCommandId() {
			return commandId;
		}

		EncoderRadioCommandId(final int commandId) {
			this.commandId=commandId;
		}
	} // enum EncoderRadioCommandId
	
	private EncoderGenericHeader encoderGenericHeader;
	
	final EncoderGenericHeader getEncoderGenericHeader() {
		return encoderGenericHeader;
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
			
			int commandIdAck = Utils.toInt(dais.readByte());
			if (commandIdAck != (0x80 | getEncoderRadioCommandId().getCommandId())) {
				throw new WaveFlow100mwEncoderException("Invalid response tag ["+Utils.toHexString(commandIdAck)+"]");
			}
			else {
				encoderGenericHeader = new EncoderGenericHeader(dais, getWaveFlow100mW().getLogger(), getWaveFlow100mW().getTimeZone());
				parse(Utils.getSubArray(data, encoderGenericHeader.size()));
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
