package com.energyict.protocolimpl.coronis.core;

import java.io.*;

abstract class AbstractEscapeCommand {

	
	enum EscapeCommandId {
		
		// Radio parameters
		
		/**
		 * Use multiple of 100ms to set the timeout.
		 * The default is 2 seconds or 0x14 (20 x 100ms = 2000ms or 2s)
		 * for the Meterdetect 0C command, it is recommended to set the timeout to 20 sec (0xC8)  
		 */
		RADIO_USER_TIMEOUT(0x0C,"Radio timeout for the reception of a frame"),
		/**
		 * Duration of the Wake up when long wake up is set up. This value must be set to Awakening period plus 100ms
		 */
		WAKEUP_LENGTH(0x02,"Duration of the Wake up when long wake up is set up."),
		/**
		 * Polling period for the RF medium. In unities of 100 ms. Default 10 = 1 sec.
		 */
		AWAKENING_PERIOD(0x00,"Polling period for the RF medium"),		
		
		
		// Radio commands. Use commands > 0x20
		USE_SEND_FRAME(0x20,"Use the SEND_FRAME command"),
		USE_SEND_MESSAGE(0x22,"Use the SEND_MESSAGE command");
		
		
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
	private ProtocolLink protocolLink;
	
	final ProtocolLink getProtocolLink() {
		return protocolLink;
	}
	
	AbstractEscapeCommand(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}	
	
	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(0);
			daos.writeByte(getEscapeCommandId().getId());
			daos.write(prepare()); // write 1 parameter
			parse(getProtocolLink().getWaveFlowConnect().sendData(baos.toByteArray()));
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getProtocolLink().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}
	
	
}
