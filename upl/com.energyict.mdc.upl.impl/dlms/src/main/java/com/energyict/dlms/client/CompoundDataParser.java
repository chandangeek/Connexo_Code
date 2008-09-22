package com.energyict.dlms.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.ProtocolUtils;

public class CompoundDataParser implements DLMSCOSEMGlobals {

	final int DEBUG=0;
	
	List<CosemAPDU> apdus=null;
	
	
	public CompoundDataParser() {
		
	}
	
	public void parse(byte[] data) throws IOException {
		
		// KV_TO_DO apply signature 
		
		int temp;
		int offset=0;

		int cosemAPDUService;
		int invokeAndPriority;
		CosemAttributeDescriptor cosemAttributeDescriptor=null;
		AbstractDataType dataType;
		
		while(offset < data.length) {
			cosemAPDUService = ProtocolUtils.getInt(data, offset++,1);
			switch(cosemAPDUService) {
			
				case (int)COSEM_SETREQUEST&0xff: {
					if (DEBUG>=1)
						System.out.print("SetRequest ");
	
					temp = ProtocolUtils.getInt(data, offset++,1);
					switch(temp) {
						case (int)COSEM_SETRESPONSE_NORMAL&0xff: {
							if (DEBUG>=1)
								System.out.println(" normal ");
							invokeAndPriority = ProtocolUtils.getInt(data, offset++,1);
							
							cosemAttributeDescriptor = new CosemAttributeDescriptor(data,offset);
							offset += CosemAttributeDescriptor.size();
							if (DEBUG>=1)
								System.out.println(cosemAttributeDescriptor);
							
							temp = ProtocolUtils.getInt(data, offset++,1);
							if (temp != 0)
								throw new IOException("Cosem APDU service SetRequest normal selective access is not supported!!");
							else {
								//temp = ProtocolUtils.getInt(data, offset++,1);
								dataType = AXDRDecoder.decode(data,offset);
								offset+=dataType.getDecodedSize();
								if (DEBUG>=1)
									System.out.println(dataType);
							}
							
						} break; // COSEM_SETRESPONSE_NORMAL
						
						default: {
							throw new IOException("Cosem APDU service SetRequest type 0x"+Integer.toHexString(temp)+" not supported!!");
						}
					} // switch((int)data[offset]&0xff)
					
				} break; // COSEM_SETREQUEST
			
				default: {
					throw new IOException("Cosem APDU service 0x"+Integer.toHexString(cosemAPDUService)+" not supported!!");
				}
			} // switch((int)data[offset]&0xff)
			
			if (apdus==null)
				apdus = new ArrayList();
			apdus.add(new CosemAPDU(cosemAPDUService,invokeAndPriority,cosemAttributeDescriptor,dataType));
		}
	} // public void parse(byte[] data)
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public List<CosemAPDU> getApdus() {
		return apdus;
	}


}
