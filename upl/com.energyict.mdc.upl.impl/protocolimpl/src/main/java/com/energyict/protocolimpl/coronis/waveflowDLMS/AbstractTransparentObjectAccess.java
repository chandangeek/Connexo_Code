package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.*;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlowException;

/**
 * Implements the transparent access to a DLMS object. See page 13 of the Waveflow AC 150mW DLMS Version 1 Applicative Specification.
 * There are 3 subclasses implementing the set, get and action method invocation.  
 * @author kvds
 */
abstract class AbstractTransparentObjectAccess {

	/**
	 * the DLSM interaction get, set or action method
	 * @author kvds
	 *
	 */
	enum InteractionParameter {
		
		GET(0,"get"),
		SET(1,"set"),
		ACTION(2,"action");
		
		private final int id;
		private final String description;
		
		InteractionParameter(final int id, final String description) {
			this.id=id;
			this.description=description;
			
		}

		final int getId() {
			return id;
		}
	}
	
	private final int TRANSPARANT_OBJECT_READING_REQ_TAG=0x31;
	private final int TRANSPARANT_OBJECT_READING_RES_TAG=0xB1;
	
	/**
	 * Reference to the implementation class.
	 */
	AbstractDLMS abstractDLMS;
	
	/**
	 * Frame count to be received.this is part of the WaveFlow AC protocol implementation.
	 */
	int frameCount=0;
	
	/**
	 * Return the frameCount
	 * @return framecount
	 */
	final int getFrameCount() {
		return frameCount;
	}
	
	AbstractTransparentObjectAccess(AbstractDLMS abstractDLMS) {
		this.abstractDLMS = abstractDLMS;
	}

	/**
	 * Return the dlms primitive get, set or action
	 * @return interaction enum
	 */
	abstract InteractionParameter getInteractionParameter();
	
	/**
	 * Parse the received response from the waveflow DLMS
	 * @param data
	 * @throws IOException 
	 */
	abstract void parse(byte[] data) throws IOException;
	
	/**
	 * Prepare the transparent access to the Waveflow device
	 * @return
	 * @throws IOException 
	 */
	abstract byte[] prepare() throws IOException;
	
	private GenericHeader genericHeader;
	
	final GenericHeader getGenericHeader() {
		return genericHeader;
	}

	void invoke() throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(TRANSPARANT_OBJECT_READING_REQ_TAG);
			daos.write(prepare()); // write 1 parameter
			
			parseResponse(abstractDLMS.getWaveFlowConnect().sendData(baos.toByteArray()));
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}
	
	private void validateResultCode(int resultCode) throws WaveFlowDLMSException {
		
		switch(resultCode) {
			case 0xFF: throw new WaveFlowDLMSException("Transparant object access error. Error Bad request format!");
			case 0xFE: throw new WaveFlowDLMSException("Transparant object access error. Pairing request never sent!");
			case 0xFD: throw new WaveFlowDLMSException("Transparant object access error. Connection rejected!");
			case 0xFC: throw new WaveFlowDLMSException("Transparant object access error. Association rejected!");
			case 0xFB: throw new WaveFlowDLMSException("Transparant object access error. Interaction (Get, Set or Action) failed!");
			default: return;
		}
	}
	
	private void parseResponse(byte[] sendData) throws IOException {

		
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(sendData));
			int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
			if (responseTag != TRANSPARANT_OBJECT_READING_RES_TAG) {
				throw new WaveFlowException("Transparant object access error. Expected ["+WaveflowProtocolUtils.toHexString(TRANSPARANT_OBJECT_READING_RES_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
			}
			byte[] temp = new byte[GenericHeader.size()];
			dais.read(temp);
			genericHeader = new GenericHeader(temp, abstractDLMS);
			
			int resultCode = WaveflowProtocolUtils.toInt(dais.readByte());
			validateResultCode(resultCode);
			frameCount=resultCode;
			
			temp = new byte[dais.available()];
			dais.read(temp);
			parse(ProtocolUtils.getSubArray(temp, 0));
			
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
	}


}
