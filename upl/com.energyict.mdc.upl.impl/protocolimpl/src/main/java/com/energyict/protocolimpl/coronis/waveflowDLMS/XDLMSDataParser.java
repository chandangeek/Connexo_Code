package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class XDLMSDataParser {


	private final Logger logger;
	
	private final int DLMS_GET_RESPONSE=0xC4;
	private final int DLMS_RESPONSE_TAG_OFFSET=3;
	private final int DLMS_RESPONSE_TAG_TYPE_OFFSET=4;
	private final int DLMS_RESPONSE_STATUS_CODE_OFFSET=6;
	private final int DLMS_RESPONSE_RESULT_CODE_OFFSET=7;

	private final int DLMS_GET_RESPONSE_NORMAL=0x01;
	private final int DLMS_GET_RESPONSE_WITH_DATABLOCK=0x02;	
	
	XDLMSDataParser(Logger logger) {
		super();
		this.logger = logger;
	}

	byte[] parseAXDRData(byte[] dlmsData) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(dlmsData));
			
			boolean lastBlock=true;
			do {
				
				dais.read(new byte[3]); // skip LLC
				
				//System.out.println("dlmsData: "+ProtocolUtils.outputHexString(dlmsData));
				
				int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
				if (responseTag != DLMS_GET_RESPONSE) {
					throw new WaveFlowDLMSException("Transparant object get error. Expected DLMS tag [C4], received["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
				}
				
				int getResponseType = WaveflowProtocolUtils.toInt(dais.readByte());
				
				dais.readByte(); // skip invoke and priority
				
				if (getResponseType == DLMS_GET_RESPONSE_NORMAL) {
					int statusCode=WaveflowProtocolUtils.toInt(dais.readByte());
					if (statusCode != 0) {
						throw new DataAccessResultException(WaveflowProtocolUtils.toInt(dais.readByte()));
					}
					else {
						byte[] axdrData = new byte[dais.available()];
						dais.read(axdrData);
						baos.write(axdrData);
					}
				
				} // if (getResponseType == DLMS_GET_RESPONSE_NORMAL)
				
				else if (getResponseType == DLMS_GET_RESPONSE_WITH_DATABLOCK) {
					lastBlock = WaveflowProtocolUtils.toInt(dais.readByte()) == 1;
					long blockId = dais.readInt();
					int statusCode=WaveflowProtocolUtils.toInt(dais.readByte());
					if (statusCode == 1) {
						throw new DataAccessResultException(WaveflowProtocolUtils.toInt(dais.readByte()));
					}
					else if (statusCode == 0) {
						// raw data block as octetstring, get length
						int length = WaveflowProtocolUtils.toInt(dais.readByte());
						int LengthEncodedInBytes = 0;
						if ((length&0x80)==0x80) {
							LengthEncodedInBytes = length & 0x7F;
							length=0;
							for (int i=LengthEncodedInBytes-1;i>=0;i--) {
								length+=(WaveflowProtocolUtils.toInt(dais.readByte()) << (8*i)) ;
							}
						}
						
						byte[] temp = new byte[length]; //-(LengthEncodedInBytes+1)];
						dais.read(temp);
						baos.write(temp);
					}
					else {
						throw new WaveFlowDLMSException("Transparant object invalid Datablock-G result tag. Expected tag [0] or [1], received["+WaveflowProtocolUtils.toHexString(statusCode)+"]");
					}
				
				} // else if (getResponseType == DLMS_GET_RESPONSE_WITH_DATABLOCK)
			
			} while(!lastBlock);
			
			// return the assembled dlms data
			return baos.toByteArray();
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}				
	}	
}
