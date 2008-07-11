package com.energyict.protocolimpl.CM32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

public class CM32Connection extends Connection implements ProtocolConnection {
	
	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;

	private int timeout;
	private int maxRetries;
	private long forcedDelay;
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private ResponseReceiver responseReceiver;
	private int nodeAddress = 0;
	
	public CM32Connection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
    }
	
	protected ResponseReceiver getResponseReceiver() {
		if (responseReceiver == null) {
			responseReceiver = doGetResponseReceiver();
		}
		return responseReceiver;
	}
	
	protected int getTimeout() {
		return timeout;
	}
	
	protected ResponseReceiver doGetResponseReceiver() {
		return new ResponseReceiver(this);
	}
	
	public Response sendCommand(Command command) throws IOException {
        int retry=5;
        doSendCommand(command);
        while(true) {
            try {
                delayAndFlush(forcedDelay); 
                sendFrame();
                Thread.sleep(100); 
                Response response = receiveResponse(command);
                return response;
            }
            catch(InterruptedException e){
                    throw new NestedIOException(e);
            }
            catch(ConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == PROTOCOL_ERROR)
                    throw new ProtocolConnectionException("sendCommand() error, "+e.getMessage());
                else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException(
                        		"sendCommand() error maxRetries ("+maxRetries+"), "
                        		+e.getMessage());
                    }
                }
            }
        } 
    }
	
	public void wakeUp() throws IOException {
        int retry=0;
        doWakeUp();
        while(true) {
            try {
                delayAndFlush(forcedDelay); // KV_DEBUG
                sendFrame();
                Thread.sleep(500); 
                return;
            } catch(ConnectionException e) {
                if (DEBUG>=1) e.printStackTrace();
                if (e.getReason() == PROTOCOL_ERROR)
                    throw new ProtocolConnectionException("sendCommand() error, "+e.getMessage());
                else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException(
                        		"sendCommand() error maxRetries ("+maxRetries+"), "
                        		+e.getMessage());
                    }
                }
            }
            catch(InterruptedException e){
                throw new NestedIOException(e);
            }
        } 
    }

    
    public Response receiveResponse(Command command) throws IOException {
        return getResponseReceiver().receiveResponse(command);
    }
    
    int readNext() throws IOException {
    	return readIn();
    }
    
    byte getTimeoutError() {
    	return TIMEOUT_ERROR;
    }
    
    void echoCancellation() {
    	this.copyEchoBuffer();
    }
	

	private void sendFrame() throws ConnectionException {
        sendOut(outputStream.toByteArray());
    }
	
	private void doSendCommand(Command command) throws ConnectionException,IOException {
		outputStream.reset();
        int checksum=0;
        outputStream.write((byte)0x3A); // : (must start each pakket)
        writeCrc();
    } 
	
	protected void writeCrc() {
		int crc = CRCGenerator.calcCCITTCRCReverse(outputStream.toByteArray());
        //nt crc = 0;
        outputStream.write(crc&0xFF);
        outputStream.write((crc>>8)&0xFF);
	}
	
	private void doWakeUp() throws ConnectionException,IOException {
		outputStream.reset();
        outputStream.write((byte)0x20);
    } 
	
	protected ByteArrayOutputStream getOutputStream() {
		return this.outputStream;
	}
	
	public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {

		return null;
	}

	public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {

		return null;
	}

	public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {

	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
	}
	
	public void checkCrc(byte[] data, byte[] crc) throws IOException {
		int crcValueFound = ProtocolUtils.getIntLE(crc, 0, 2);
		int crcCalculated = CRCGenerator.calcCCITTCRCReverse(
				ProtocolUtils.getSubArray2(data, 0, data.length-2));
		//System.out.println("crcValueFound = " + crcValueFound);
		//System.out.println("crcCalculated = " + crcCalculated);
		if (crcValueFound != crcCalculated)
			throw new IOException("invalid crc");
	}

}
