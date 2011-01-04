package com.energyict.dlms;

import com.energyict.dialer.connection.ConnectionException;

import java.io.*;

/**
 * @author jme
 *
 */
public class LLCConnection extends CosemPDUConnection {

	public LLCConnection(InputStream inputStream, OutputStream outputStream, int timeout, int forceDelay, int maxRetries, int clientAddress, int serverAddress)
			throws IOException {
		super(inputStream, outputStream, timeout, forceDelay, maxRetries, clientAddress, serverAddress);
	}

	private byte[] receiveData() throws IOException {
		byte[] data;
		long interFrameTimeout;
		copyEchoBuffer();
		delay(getForceDelay());
		interFrameTimeout = System.currentTimeMillis() + getTimeout();
		while (true) {
			data = readInArray();
			if (data != null) {
				if (data[0] == (byte)0x90) {
					return data;
				} else {
					while (readInArray() != null) {
					}
					throw new IOException("LLC packet should start with 0x90!");
				}
			}
			if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
				throw new ConnectionException("receiveData() response timeout error", TIMEOUT_ERROR);
			}
		}
	}

	/**
	 * Append the LLC header to the packet (Hard coded to 0x90, 0x01, 0x02)
	 */
	public byte[] sendRequest(byte[] data) throws IOException {

		int retry = 0;

		byte[] byteRequestBuffer = new byte[data.length];
		System.arraycopy(data, 3, byteRequestBuffer, 3, data.length - 3);
		byteRequestBuffer[0] = (byte) 0x90;
		byteRequestBuffer[1] = (byte) 0x01;
		byteRequestBuffer[2] = (byte) 0x02;

		while (true) {
			try {
				sendOut(byteRequestBuffer);
				delay(getForceDelay());
				return receiveData();
			} catch (ConnectionException e) {
				if (retry++ >= getMaxRetries()) {
					throw new IOException("sendRequest, IOException, " + com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}
	}

}
