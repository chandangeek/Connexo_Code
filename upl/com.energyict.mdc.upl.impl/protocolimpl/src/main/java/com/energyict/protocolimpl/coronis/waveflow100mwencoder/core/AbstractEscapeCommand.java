package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.AbstractRadioCommand.EncoderRadioCommandId;

abstract class AbstractEscapeCommand {

	
	enum EscapeCommandId {
			
		/**
		 * Use multiple of 100ms to set the timeout.
		 * The default is 2 seconds or 0x14 (20 x 100ms = 2000ms or 2s)
		 * for the Meterdetect 0C command, it is recommended to set the timeout to 20 sec (0xC8)  
		 */
		RADIO_USER_TIMEOUT(0x0C,"Radio timeout for the reception of a frame");
		
		final int id;
		final String description;
		
		EscapeCommandId(int id,String description) {
			this.id=id;
			this.description=description;
		}

		final int getId() {
			return id;
		}
		
		final String getDescription() {
			return description;
		}
		
		public String toString() {
			return "EscapeCommandId="+description+" ["+Integer.toHexString(id)+"]";
		}				
	}
	
	
	abstract void parse(byte[] data) throws IOException;
	abstract byte[] prepare() throws IOException;
	abstract EscapeCommandId getEscapeCommandId();	
	
	/**
	 * The reference to the Waveflow100mW protocol implementation class
	 */
	private WaveFlow100mW waveFlow100mW;
	
	final WaveFlow100mW getWaveFlow100mW() {
		return waveFlow100mW;
	}
	
	AbstractEscapeCommand(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}	
	
	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(0);
			daos.writeByte(getEscapeCommandId().getId());
			daos.write(prepare()); // write 1 parameter
			parse(getWaveFlow100mW().getWaveFlowConnect().sendData(baos.toByteArray()));
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
	
	
}
