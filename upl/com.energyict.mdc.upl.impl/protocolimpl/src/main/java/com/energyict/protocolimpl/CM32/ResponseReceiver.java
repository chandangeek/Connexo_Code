package com.energyict.protocolimpl.CM32;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseReceiver {
	
	private static final long TIMEOUT=60000;
	
	private final int WAIT_FOR_CM10_ID=0;
    private final int WAIT_FOR_BLOCK_SIZE=1;
    private final int WAIT_FOR_SOURCE_CODE=2;
    private final int WAIT_FOR_SOURCE_EXTENSION=3;
    private final int WAIT_FOR_DESTINATION_CODE=4;
    private final int WAIT_FOR_DESTINATION_EXTENSION=5;
    private final int WAIT_FOR_PROTOCOL_TYPE=6;
    private final int WAIT_FOR_PORT=7;
    private final int HANDLE_DATA=8;
    
    private CM32Connection cm32Connection;
    private int blockSize;
    private int sourceCodeByteCount = 0;
    private int destCodeByteCount = 0;
    private boolean isLastFrame = true;
    private int currentBlockByteCount = 0;
    
    public ResponseReceiver(CM32Connection cm32Connection) {
    	this.cm32Connection = cm32Connection;
    }
    
    protected void logState(int state, int kar) {
    	if (state == 0)
    		log("WAIT_FOR_CM10_ID: " + outputHex(kar)  + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 1)
    		log("WAIT_FOR_BLOCK_SIZE: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 2)
    		log("WAIT_FOR_SOURCE_CODE: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 3)
    		log("WAIT_FOR_SOURCE_EXTENSION: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 4)
    		log("WAIT_FOR_DESTINATION_CODE: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 5)
    		log("WAIT_FOR_DESTINATION_EXTENSION: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 6)
    		log("WAIT_FOR_PROTOCOL_TYPE: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 7)
    		log("WAIT_FOR_PORT: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 8)
    		log("HANDLE_DATA: " + outputHex(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else 
    		log("invalid state = " + state);
    }
    
    private void logValue(int kar) {
    	log(outputHex(kar));
    }
    
    private String outputHex(int bKar) {
    	StringBuffer buf = new StringBuffer("");
    	buf.append((char)ProtocolUtils.convertHexLSB(bKar));
    	buf.append((char)ProtocolUtils.convertHexMSB(bKar));
    	return buf.toString();
     }


    // check for "invalid command response"
	protected Response receiveResponse(Command command) throws IOException {
		int expectedCM10Id = command.getCM10Identifier();
		byte[] expectedSourceCode = command.getDestinationCode();
		byte[] expectedDestinationCode = command.getSourceCode();
        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        allDataArrayOutputStream.reset();
		ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
		resultDataArrayOutputStream.reset();
		cm32Connection.echoCancellation();
        int kar;
        int state = WAIT_FOR_CM10_ID;
        while (true) {
        	if ((kar = cm32Connection.readNext()) != -1) {
        		currentBlockByteCount++;
            	logState(state, kar);
        		allDataArrayOutputStream.write(kar);
        		if (state == WAIT_FOR_CM10_ID) {
        			if (kar != expectedCM10Id) {
        				throw new ProtocolConnectionException(
        						"Expected CM10 identifier: " + expectedCM10Id 
        						+ ", CM10 identifier found: " + kar);
        			}
        			handleCM10Id((byte) kar);
        			state = WAIT_FOR_BLOCK_SIZE;
        		}
        		else if (state == WAIT_FOR_BLOCK_SIZE) {
        			state = WAIT_FOR_SOURCE_CODE;
        			setBlockSize(kar);
        		}
        		else if (state == WAIT_FOR_SOURCE_CODE) {
        			sourceCodeByteCount++;
        			if (sourceCodeByteCount == 2)
        				state = WAIT_FOR_SOURCE_EXTENSION;
        		}
        		else if (state == WAIT_FOR_SOURCE_EXTENSION) {
        			state = WAIT_FOR_DESTINATION_CODE;
        		}
        		else if (state == WAIT_FOR_DESTINATION_CODE) {
        			destCodeByteCount++;
        			if (destCodeByteCount == 2)
        				state = WAIT_FOR_DESTINATION_EXTENSION;
        		}
        		else if (state == WAIT_FOR_DESTINATION_EXTENSION) {
        			state = WAIT_FOR_PROTOCOL_TYPE;
        		}
        		else if (state == WAIT_FOR_PROTOCOL_TYPE) {
        			state = WAIT_FOR_PORT;
        		}
        		else if (state == WAIT_FOR_PORT) {
        			state = HANDLE_DATA;
        		}
        		else if (state == HANDLE_DATA) {
        			log ("" + currentBlockByteCount + ", " + blockSize);
        			if (currentBlockByteCount < blockSize)
        				resultDataArrayOutputStream.write(kar);
        			else {
        				checkCrc(kar, allDataArrayOutputStream);
            			if (isLastFrame) {
            				byte[] data = resultDataArrayOutputStream.toByteArray();
            				Response response = new Response(ProtocolUtils.getSubArray2(data, 0, data.length));
            				log("data = " + ProtocolUtils.outputHexString(response.getData()));
            				return response;
            			}
            			else {
            				state = WAIT_FOR_CM10_ID;
            				currentBlockByteCount = 0;
            				isLastFrame = true;
            			}
        			}
        		}
        	}
        	if (command != null) {
	        	if (System.currentTimeMillis() - protocolTimeout > 0) {
	                throw new ProtocolConnectionException(
	                		"receiveResponse() response timeout error", 
	                		cm32Connection.getTimeoutError());
	            }
        	}
        }
	}
	
	protected void setBlockSize(int kar) {
		if (kar == 0)
			blockSize = 256;
		else
			blockSize = kar;
		log("blockSize = " + blockSize);
	}
	
	protected void checkCrc(int crcFound, ByteArrayOutputStream dataArrayOutputStream) throws IOException {
		log("crcFound = " + crcFound);
		byte[] data = dataArrayOutputStream.toByteArray();
		byte[] dataForCrcCalculation = 
			ProtocolUtils.getSubArray2(data, data.length - blockSize, blockSize - 1);
		int size = dataForCrcCalculation.length;
		int crcCalculated = 0;
		for (int i = 0; i < size; i++) {
			crcCalculated = crcCalculated + (int) dataForCrcCalculation[i];
		}
		crcCalculated = 256 - (crcCalculated % 256);
		if (crcCalculated != crcFound)
			throw new IOException("invalid crc");
		log("crc ok");
	}
	
	protected void handleCM10Id(byte value) {
		log("CM10Id = " + value);
		int blockIdentifier = value & 0x60;
		this.isLastFrame = ((blockIdentifier == 64) || (blockIdentifier == 96));
		log("blockIdentifier = " + blockIdentifier + ", isLastFrame = " + isLastFrame);
	}
	
	protected void log(String logMessage) {
		this.cm32Connection.getCM32Protocol().getLogger().info(logMessage);
	}


}
