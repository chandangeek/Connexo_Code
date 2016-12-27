package com.energyict.protocolimpl.instromet.connection;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstrometConnection extends Connection implements ProtocolConnection {

	private static final int DEBUG = 0;

	private int timeout;
	private int maxRetries;
	private long forcedDelay;
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private ResponseReceiver responseReceiver;
	private int nodeAddress = 0;

	public InstrometConnection(InputStream inputStream, OutputStream outputStream, int timeout, int maxRetries, long forcedDelay, int echoCancelling,
			HalfDuplexController halfDuplexController) throws ConnectionException {
		super(inputStream, outputStream, forcedDelay, echoCancelling, halfDuplexController);
		this.timeout = timeout;
		this.maxRetries = maxRetries;
		this.forcedDelay = forcedDelay;
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
		return new ResponseReceiver(this, timeout);
	}

	public void setNodeAddress(int nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	protected int getNodeAddress() {
		return this.nodeAddress;
	}

	public Response sendCommand(Command command) throws IOException {
		int retry = 0;
		doSendCommand(command);
		while (true) {
			try {
				delayAndFlush(forcedDelay); // KV_DEBUG
				sendFrame();
				Thread.sleep(100);
				Response response = receiveResponse(command);
				return response;
			} catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (IOException e) {
				if (DEBUG >= 1) {
					e.printStackTrace();
				}
				if ((e instanceof ConnectionException) && (((ConnectionException) e).getReason() == PROTOCOL_ERROR)) {
					throw new ProtocolConnectionException("sendCommand() error, " + e.getMessage(), PROTOCOL_ERROR);
				} else {
					if (retry++ >= maxRetries) {
						throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + "), " + e.getMessage());
					}
				}
			}
		}
	}

	public void wakeUp() throws IOException {
		int retry = 0;
		doWakeUp();
		while (true) {
			try {
				delayAndFlush(forcedDelay); // KV_DEBUG
				sendFrame();
				Thread.sleep(500);
				return;
			} catch (IOException e) {
				if (DEBUG >= 1) {
					e.printStackTrace();
				}
				if ((e instanceof ConnectionException) && (((ConnectionException) e).getReason() == PROTOCOL_ERROR)) {
					throw new ProtocolConnectionException("sendCommand() error, " + e.getMessage(), PROTOCOL_ERROR);
				} else {
					if (retry++ >= maxRetries) {
						throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + "), " + e.getMessage());
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
    			throw ConnectionCommunicationException.communicationInterruptedException(e);
			}
		}
	}

	public Response receiveResponse(Command command) throws IOException {
		return getResponseReceiver().receiveResponse(command, -1);
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

	private void doSendCommand(Command command) throws ConnectionException, IOException {
		outputStream.reset();
		if (command.getCommand() == 0) {
			return;
		}
		outputStream.write((byte) 0x3A); // : (must start each pakket)
		writeCommId(command);// : comm id
		outputStream.write(command.getCommand());
		writeAdress(command);
		writeLength(command);
		if (command.getData() != null) {
			outputStream.write(command.getData());
		}
		writeCrc();
	}

	protected void writeCrc() {
		int crc = CRCGenerator.calcCCITTCRCReverse(outputStream.toByteArray());
		outputStream.write(crc & 0xFF);
		outputStream.write((crc >> 8) & 0xFF);
	}

	private void doWakeUp() throws ConnectionException, IOException {
		outputStream.reset();
		outputStream.write((byte) 0x20);
	}

	protected ByteArrayOutputStream getOutputStream() {
		return this.outputStream;
	}

	protected void writeAdress(Command command) {
		int address = command.getStartAddress();
		outputStream.write((address >> 24) & 0xFF);
		outputStream.write((address >> 16) & 0xFF);
		outputStream.write((address >> 8) & 0xFF);
		outputStream.write(address & 0xFF);
	}

	protected void writeLength(Command command) {
		int length = command.getLength();
		outputStream.write((length >> 8) & 0xFF);
		outputStream.write(length & 0xFF);
	}

	protected void writeCommId(Command command) {
		int commId = nodeAddress;
		if (DEBUG >= 1) {
			System.out.println("commId = " + commId);
		}
		outputStream.write((commId >> 8) & 0xFF);
		outputStream.write(commId & 0xFF);
	}

	public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) {
		return null;
	}

	public byte[] dataReadout(String strID, String nodeId) {
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
		int crcCalculated = CRCGenerator.calcCCITTCRCReverse(ProtocolUtils.getSubArray2(data, 0, data.length - 2));
		if (DEBUG >= 1) {
			System.out.println("crcValueFound = " + crcValueFound);
			System.out.println("crcCalculated = " + crcCalculated);
		}
		if (crcValueFound != crcCalculated) {
			throw new IOException("invalid crc");
		}
	}

	public String toString() {
		return 	"InstrometConnection" +
		" [forcedDelay=" + forcedDelay +
		", maxRetries=" + maxRetries +
		", nodeAddress=" + nodeAddress +
		", outputStream=" + outputStream +
		", responseReceiver=" + responseReceiver +
		", timeout=" + timeout +
		"]";
	}

}
