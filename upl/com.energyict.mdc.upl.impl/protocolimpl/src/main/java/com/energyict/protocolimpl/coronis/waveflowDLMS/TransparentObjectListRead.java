package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.util.List;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflowDLMS.AbstractRadioCommand.RadioCommandId;

/**
 * Implements the transparent dlms object list read. See page 13 of the Waveflow AC 150mW DLMS Version 1 Applicative Specification.
 * There are 3 subclasses implementing the set, get and action method invocation.  
 * @author kvds
 */
class TransparentObjectListRead {
	
	private final int TRANSPARANT_OBJECT_LIST_READING_REQ_TAG=0x36;
	private final int TRANSPARANT_OBJECT_LIST_READING_RES_TAG=0xB6;
	
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
	
	TransparentObjectListRead(AbstractDLMS abstractDLMS) {
		this.abstractDLMS = abstractDLMS;
	}
	
	private GenericHeader genericHeader;
	
	final GenericHeader getGenericHeader() {
		return genericHeader;
	}

	private final byte[] objectList2ByteArray(List<ObjectInfo> objestInfos) throws IOException {
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			
			for (ObjectInfo ob : objestInfos) {
				daos.writeShort(ob.getClassId());
				daos.write(ob.getObisCode().getLN());
				daos.writeByte(ob.getAttribute());
			}
			
			return baos.toByteArray();
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
	
	void read(List<ObjectInfo> objestInfos) throws IOException {
		
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(TRANSPARANT_OBJECT_LIST_READING_REQ_TAG);
			daos.writeByte(objestInfos.size());
			
			
			daos.write(objectList2ByteArray(objestInfos)); 
			
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
	
	private void validateResultCode(int resultCode) throws WaveFlowException {
		switch(resultCode) {
			case 0xFF: throw new WaveFlowDLMSException("Transparant object list read error. No data in buffer!");
			case 0xFE: throw new WaveflowDLMSStatusError("Transparant object list read error. Bad request format!");
			default: return;
		}
	}
	
	private void parseResponse(byte[] sendData) throws IOException {

		
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(sendData));
			int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
			if (responseTag != TRANSPARANT_OBJECT_LIST_READING_RES_TAG) {
				throw new WaveFlowException("Transparant object list read error. Expected ["+WaveflowProtocolUtils.toHexString(TRANSPARANT_OBJECT_LIST_READING_RES_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
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
	
	void parse(byte[] data) {
		System.out.println(ProtocolUtils.outputHexString(data));
	}
	
}
