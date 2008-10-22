package com.energyict.dlms.client;

import java.io.*;
import java.util.*;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.*;
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
		AbstractDataType dataType=null;
		
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
								//try {
								 dataType = AXDRDecoder.decode(data,offset);
								 offset+=dataType.getDecodedSize();
								 
//								}
//								catch(NegativeArraySizeException e) {
//									System.out.println("GOTCHA!");
//									for (int i=0;i<data.length;i++) {
//										if ((i%50)==0)
//											System.out.println();
//										if (i==offset)
//											System.out.print(" gotcha ");
//										System.out.print(ProtocolUtils.outputHexString((int)data[i]&0xff));
//										
//									}
//									System.out.println("offset = "+offset);
//									throw e;
//								}
								
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

		
		File file = new File("C:/Documents and Settings/kvds/My Documents/rubbish/ParseError.txt");
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			byte[] compoundData = new byte[data.length/3];
			try {
				fis.read(data);
				
				for (int i=0;i<(data.length-3);i+=3) {
					String val = new String(new byte[]{data[i+1],data[i+2]});
					compoundData[i/3]=(byte)Integer.parseInt(val,16);
				}
				
				
				CompoundDataParser o = new CompoundDataParser();
				o.parse(compoundData);
				for (int i=0;i<o.getApdus().size();i++) {
					System.out.println(o.getApdus().get(i));
				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public List<CosemAPDU> getApdus() {
		return apdus;
	}


}
