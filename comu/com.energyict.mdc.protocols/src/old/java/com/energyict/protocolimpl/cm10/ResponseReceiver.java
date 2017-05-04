/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

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

    private CM10Connection cm10Connection;

    public ResponseReceiver(CM10Connection cm10Connection) {
    	this.cm10Connection = cm10Connection;
    }

    protected void logState(int state, int kar, int currentBlockByteCount) {
    	if (state == 0)
    		log("WAIT_FOR_CM10_ID: " + ProtocolUtils.outputHexString(kar)  + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 1)
    		log("WAIT_FOR_BLOCK_SIZE: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 2)
    		log("WAIT_FOR_SOURCE_CODE: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 3)
    		log("WAIT_FOR_SOURCE_EXTENSION: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 4)
    		log("WAIT_FOR_DESTINATION_CODE: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 5)
    		log("WAIT_FOR_DESTINATION_EXTENSION: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 6)
    		log("WAIT_FOR_PROTOCOL_TYPE: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 7)
    		log("WAIT_FOR_PORT: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else if (state == 8)
    		log("HANDLE_DATA: " + ProtocolUtils.outputHexString(kar) + " currentBlockByteCount: " + currentBlockByteCount);
    	else
    		log("invalid state = " + state);
    }

    private int getTable(int cm10Id) {
    	return (int) (cm10Id & 0x0F);
    }


    // check for "invalid command response"
	protected Response receiveResponse(Command command) throws IOException {
		if (command.isAck())
			return new Response(new byte[0]);
		int expectedTable = getTable(command.getCM10Identifier());
        long protocolTimeout = System.currentTimeMillis() + cm10Connection.getTimeout();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
        allDataArrayOutputStream.reset();
		ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
		resultDataArrayOutputStream.reset();
		cm10Connection.echoCancellation();
		int currentBlockByteCount = 0;
	    int blockSize = 0;
	    int sourceCodeByteCount = 0;
	    int destCodeByteCount = 0;
	    boolean isLastFrame = true;

        int kar;
        int state = WAIT_FOR_CM10_ID;
        while (true) {
        	if ((kar = cm10Connection.readNext()) != -1) {
                protocolTimeout = System.currentTimeMillis() + cm10Connection.getTimeout();
            	//logState(state, kar, currentBlockByteCount);
        		if (state != WAIT_FOR_CM10_ID) {
        			allDataArrayOutputStream.write((byte) kar);
            		currentBlockByteCount++;
        		}
        		if (state == WAIT_FOR_CM10_ID) {
        			int tableReceived = getTable(kar);
        			if (tableReceived != expectedTable) {
        				throw new InvalidCommandException("Invalid CM10 identifier received after sending: " + ProtocolUtils.outputHexString(command.getCM10Identifier() )
        						+ ", CM10 identifier received: " + ProtocolUtils.outputHexString(kar));
        			}
        			else {
        				//log("CM10Id = " + ProtocolUtils.outputHexString(kar));
        				int blockIdentifier = (int) (kar & 0x60);
        				isLastFrame = ((blockIdentifier == 32) || (blockIdentifier == 96));
        				//log("blockIdentifier = " + blockIdentifier + ", isLastFrame = " + isLastFrame);
        				state = WAIT_FOR_BLOCK_SIZE;
        				allDataArrayOutputStream.write((byte)kar);
        				currentBlockByteCount = 1;
        			}
        		}
        		else if (state == WAIT_FOR_BLOCK_SIZE) {
        			state = WAIT_FOR_SOURCE_CODE;
        			if (kar == 0)
        				blockSize = 256;
        			else
        				blockSize = kar;
        			//log("blockSize = " + blockSize);
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
        			//log ("" + currentBlockByteCount + ", " + blockSize);
        			if (currentBlockByteCount < blockSize)
        				resultDataArrayOutputStream.write((byte)kar);
        			else {
        				checkCrc((int) kar, allDataArrayOutputStream, blockSize);
            			if (isLastFrame) {
            				byte[] data = resultDataArrayOutputStream.toByteArray();
            				Response response = new Response(ProtocolUtils.getSubArray2(data, 0, data.length));
            				//log("data = " + ProtocolUtils.outputHexString(response.getData()));
            				return response;
            			}
            			else {
            				state = WAIT_FOR_CM10_ID;
            				currentBlockByteCount = 0;
            			    blockSize = 0;
            			    sourceCodeByteCount = 0;
            			    destCodeByteCount = 0;
            				isLastFrame = true;
            			}
        			}
        		}
        	}
        	if (command != null) {
	        	if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
	                throw new ProtocolConnectionException(
	                		"receiveResponse() response timeout error",
	                		cm10Connection.getTimeoutError());
	            }
        	}
        }
	}


	protected void checkCrc(int crcFound, ByteArrayOutputStream dataArrayOutputStream, int blockSize) throws IOException {
		//log("crcFound = " + crcFound);
		byte[] data = dataArrayOutputStream.toByteArray();
		byte[] dataForCrcCalculation =
			ProtocolUtils.getSubArray2(data, data.length - blockSize, blockSize - 1);
		int size = dataForCrcCalculation.length;
		int crcCalculated = 0;
		for (int i = 0; i < size; i++) {
			crcCalculated = crcCalculated + (int) (dataForCrcCalculation[i] & 0xFF); //make it unsigned!
			//log("1) crcCalculated intermediate: " + crcCalculated + ", " + (int) dataForCrcCalculation[i] + ", " + ProtocolUtils.outputHexString((dataForCrcCalculation[i] & 0xFF)));
		}
		crcCalculated = 256 - (crcCalculated % 256);
		if (crcCalculated == 256)
			crcCalculated = 0;
		if (crcCalculated != crcFound)
			throw new IOException("invalid crc, value found = " + crcFound + ", value expected = " + crcCalculated + ", " + ProtocolUtils.outputHexString(dataForCrcCalculation));
		//log("crc ok");
	}


	protected void log(String logMessage) {
		this.cm10Connection.getCM10Protocol().getLogger().info(logMessage);
	}




}
