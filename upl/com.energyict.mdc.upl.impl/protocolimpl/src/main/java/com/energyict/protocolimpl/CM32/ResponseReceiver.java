package com.energyict.protocolimpl.CM32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.energyict.dialer.connection.Connection;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

public class ResponseReceiver {
	
	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;
	
	private final int WAIT_FOR_START=0;
    private final int WAIT_FOR_DATA=2;
    private final int WAIT_FOR_CRC=3;
    private final int WAIT_FOR_END=4;
    
    private CM32Connection cm32Connection;
    
    public ResponseReceiver(CM32Connection cm32Connection) {
    	this.cm32Connection = cm32Connection;
    }
    
    protected void printState(int state) {
    	if (state == 0)
    		System.out.println("WAIT_FOR_START");
    	else if (state == 1)
    		System.out.println("WAIT_FOR_DATA");
    	else if (state == 2)
    		System.out.println("WAIT_FOR_CRC");
    	else if (state == 3)
    		System.out.println("WAIT_FOR_END");
    	else
    		System.out.println( "invalid state = " + state);
    }


    // check for "invalid command response"
	protected Response receiveResponse(Command command) throws IOException {
        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        ByteArrayOutputStream dataArrayOutputStream = new ByteArrayOutputStream();
		dataArrayOutputStream.reset();
		cm32Connection.echoCancellation();
		StringBuffer crcRead = new StringBuffer("");
        int kar;
        int state = WAIT_FOR_START;
        while (true) {
        	if ((kar = cm32Connection.readNext()) != -1) {
        		dataArrayOutputStream.write(kar);
        		if (state == WAIT_FOR_START) {
        			if ((byte)kar == Connection.STX)
        				state = WAIT_FOR_DATA;
        		}
        		else if (state == WAIT_FOR_DATA) {
        			if ((char)kar == '{') {
        				state = WAIT_FOR_CRC;
        			}
        		}
        		else if (state == WAIT_FOR_CRC) {
        			crcRead.append((char)kar);
        			if ((char)kar == '}') {
        				checkCrc(crcRead.toString(), dataArrayOutputStream);
        				state = WAIT_FOR_END;
        			}
        		}
        		else if (state == WAIT_FOR_END) {
        			if ((byte)kar == Connection.ETX){
        				byte[] data = dataArrayOutputStream.toByteArray();
        				Response response = 
        					new Response(ProtocolUtils.getSubArray2(data, 1, data.length-1));
	    				if (DEBUG>=1) 
	    					System.out.println("IG_DEBUG> CRC OK, response="+response);
	    				return response;
        			}
        		}
        	}
        	if (command != null) {
	        	if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
	                throw new ProtocolConnectionException(
	                		"receiveResponse() response timeout error", 
	                		cm32Connection.getTimeoutError());
	            }
        	}
        }
	}
	
	protected void checkCrc(String crcFound, ByteArrayOutputStream dataArrayOutputStream) throws IOException {
		int crcValueFound;
		try {
			crcValueFound = Integer.parseInt(crcFound);
		}
		catch (NumberFormatException e) {
			throw new IOException("Protocol error: crc value found must be an integer value");
		}
		byte[] data = dataArrayOutputStream.toByteArray();
		byte[] dataForCrcCalculation = 
			ProtocolUtils.getSubArray2(data, 1, data.length-6);
		checkCrc(crcValueFound, dataForCrcCalculation);
	}
	
	// crc calculation to be changed!
	public void checkCrc(int crcValueFound, byte[] data) throws IOException {
		int crcCalculated = CRCGenerator.calcCCITTCRCReverse(
				ProtocolUtils.getSubArray2(data, 0, data.length-2));
		if (crcValueFound != crcCalculated)
			throw new IOException("invalid crc");
	}


}
