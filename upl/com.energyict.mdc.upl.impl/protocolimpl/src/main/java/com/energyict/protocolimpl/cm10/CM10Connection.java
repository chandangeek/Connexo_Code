package com.energyict.protocolimpl.cm10;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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

public class CM10Connection extends Connection implements ProtocolConnection {
	
	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;

	private int timeout;
	private int maxRetries;
	private long forcedDelay;
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private ResponseReceiver responseReceiver;
	private int nodeAddress = 0;
	private CM10 cm10Protocol;
	
	public CM10Connection(InputStream inputStream,
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
	
	public void setCM10(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public CM10 getCM10Protocol() {
		return cm10Protocol;
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
        while(true) {
            try {
            	// send command
                sendOut(command.getBytes());
                // receive response
                Response response = receiveResponse(command);
                // send ack
                if (command.sendAckAfterThisCommand()) // for testing power fail details
                	sendOut(command.getAckCommand().getBytes());
                outputStream.write(command.getAckCommand().getBytes());
                return response;
            }
            /*catch(InterruptedException e){
                    throw new NestedIOException(e);
            }*/
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
	

}
