package com.energyict.protocolimpl.CM32;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CM32Connection extends Connection implements ProtocolConnection {

	private static final int DEBUG=0;
	private static final long TIMEOUT=60000;

	private int timeout;
	private int maxRetries;
	private long forcedDelay;
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private ResponseReceiver responseReceiver;
	private int nodeAddress = 0;
	private CM32 cm32Protocol;

	public CM32Connection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController) {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
    }

	public void setCM32(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}

	public CM32 getCM32Protocol() {
		return cm32Protocol;
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
                // send ack
                doSendCommand(command.getAckCommand());
                return response;
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
            catch(ConnectionException e) {
                if (DEBUG>=1) {
	                e.printStackTrace();
                }
                if (e.getReason() == PROTOCOL_ERROR) {
	                throw new ProtocolConnectionException("sendCommand() error, " + e.getMessage(), e.getReason());
                } else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException(
                        		"sendCommand() error maxRetries ("+maxRetries+"), "
                        		+e.getMessage(), e.getReason());
                    }
                }
            }
        }
    }

	/*public void wakeUp() throws IOException {
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
    }*/


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

	protected void writeCharacters(String data) {
        for (int i = 0; i < data.length(); i++) {
        	outputStream.write((int) data.charAt(i));
        }
	}


	private void doSendCommand(Command command) throws IOException {
		outputStream.reset();
		outputStream.write(command.getCM10Identifier());  // see p 5,6,7 CM10 doc
		outputStream.write((byte) 0x0B); // block size
		outputStream.write(command.getSourceCode());
		outputStream.write((byte) 0x00); // source extension
		outputStream.write(command.getDestinationCode());
		outputStream.write((byte) 0x00); // destionation extension
		outputStream.write((byte) 0x00); // protocol type CM10
		outputStream.write((byte) 0x00); // port (unused)
		writeCrc();
    }

	protected void writeCrc() {
		byte[] data = outputStream.toByteArray();
		int size = data.length;
		int sum = 0;
		for (int i = 0; i < size; i++) {
			sum = sum + (int) data[i];
		}
		int crc = 256 - (sum % 256);
        outputStream.write(crc);
	}

	private void doWakeUp() {
		outputStream.reset();
        outputStream.write((byte)0x20);
    }

	protected ByteArrayOutputStream getOutputStream() {
		return this.outputStream;
	}

	public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {

		return null;
	}

	public byte[] dataReadout(String strID, String nodeId) {

		return null;
	}

	public void disconnectMAC() {

	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
	}


}
