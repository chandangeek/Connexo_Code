package com.energyict.protocolimpl.coronis.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract class AbstractEscapeCommand {

	
	enum EscapeCommandId {
		
		/****************************************************************************************************************************
		 *  Radio parameters to control the WaveCard's behaviour
		 *  the id's are identical to the id's in the Wavecard document V13 paragraph '7.2. Parameters meaning' starting page 24
		 */
		
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
		
		
		/**
		 *  Radio commands. Use commands > 0x20
		 */
		USE_SEND_FRAME(0x20,"Use the SEND_FRAME command"),
		USE_SEND_MESSAGE(0x22,"Use the SEND_MESSAGE command"),
		USE_SERVICE_REQUEST(0x83,"Use the SERVICE_REQUEST command"),


		/****************************************************************************************************************************
		 *  Custom defined escape parameters to control the internals of the wavenis communication stack.
		 *  These escape parameter id's start with 0x80
		 */
		
		/**
		 *  We overrule that parameter with the ProtocolTimeout property defined in the protocol...
		 *  This is the eictwavenis.properties parameter 'configRFResponseTimeoutInMs' 
		 */
		WAVENIS_CONFIG_RF_TIMEOUT(0x80,"Wavenis communication stack RF response timeout"),
		
		/**
		 * Attempt nr to start a request/response. This is used to dynamically control timeout and retry parameters in the communicationstack
		 */
		WAVENIS_COMMUNICATION_ATTEMPT_NR(0x81,"Wavenis communication stack communication attempt nr"),
		
		/**
		 * Request the radio address for this communication session from the stack. 
		 */
		WAVENIS_REQUEST_RADIO_ADDRESS(0x82,"Wavenis request radio address");
		
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
			return "EscapeCommandId="+description+" ["+ Integer.toHexString(id)+"]";
		}				
	}
	
	
	abstract void parse(byte[] data) throws IOException;
	abstract byte[] prepare() throws IOException;
	abstract EscapeCommandId getEscapeCommandId();	
	
	/**
	 * The reference to the Waveflow100mW protocol implementation class
	 */
	private ProtocolStackLink protocolStackLink;
	
	final ProtocolStackLink getProtocolStackLink() {
		return protocolStackLink;
	}
	
	AbstractEscapeCommand(ProtocolStackLink protocolStackLink) {
		this.protocolStackLink = protocolStackLink;
	}	
	
	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(0);
			daos.writeByte(getEscapeCommandId().getId());
			daos.write(prepare()); // write 1 parameter
			byte[] escapeData = baos.toByteArray();
			byte[] data = getProtocolStackLink().sendEscapeData(escapeData);
			
			if (WaveflowProtocolUtils.toInt(data[0]) != 0) {
				throw new EscapeCommandException("Error invoking the escape sequence. Returned ["+ WaveflowProtocolUtils.toInt(data[0])+"] for escape sequence ["+ ProtocolUtils.outputHexString(escapeData)+"]");
			}
			
			parse(ProtocolUtils.getSubArray(data, 1));
			
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getProtocolStackLink().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}			
	}
	
	
}
