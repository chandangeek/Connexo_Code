package com.elster.protocolimpl.dsfg.connection;

import com.elster.protocolimpl.dsfg.telegram.DataBlock;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connections.Connection;
import com.energyict.mdc.upl.io.NestedIOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of dsfg protocol. <br>
 * <br>
 *
 * <b>General Description:</b><br>
 * <br>
 * <br>
 *
 *
 *
 * @author gh
 * @since 5-mai-2010
 *
 */

public class DsfgConnection extends Connection {

	int iProtocolTimeout = 20000;

	char dfueInstance = 0x00;

	/*
	 * possible additional parameters: this.iMaxRetries = iMaxRetries;
	 * this.lForceDelay = lForceDelay; this.iEchoCancelling = iEchoCancelling;
	 * this.iIEC1107Compatible = iIEC1107Compatible; this.encryptor=encryptor;
	 * iProtocolTimeout=iTimeout; boolFlagIEC1107Connected=false;
	 * this.errorSignature=errorSignature; this.software7E1 = software7E1;
	 */
	/**
	 * constructor of dsfg protocol
	 *
	 * @param inputStream
	 * @param outputStream
	 */
	public DsfgConnection(InputStream inputStream, OutputStream outputStream)
			throws ConnectionException {
		super(inputStream, outputStream);
	}

	/**
	 * calculate the BCC of a given String
	 *
	 * @param data - the data to build the bcc of
     *
	 * @return bcc
	 */
	private byte calculateBCC(byte[] data) {
		byte result = 0;
		boolean buildBCC = false;
		for (int i = 0; i < data.length; i++) {
			if (buildBCC) {
				result ^= data[i];
			}
			else {
				buildBCC = data[i] == STX;
			}
		}
		return result;
	}

	private void sendTelegram(String order, String data, boolean withBCC)
			throws IOException {

		/*
		 * sendRawData(STX);
		 *
		 * byte[] buffer = new byte[order.length() + data.length()]; int p = 0;
		 * for (int i = 0; i < order.length(); i++) buffer[p++] = (byte)
		 * order.charAt(i); for (int i = 0; i < data.length(); i++) buffer[p++]
		 * = (byte) data.charAt(i);
		 *
		 * sendRawData(buffer);
		 *
		 * sendRawData(ETX);
		 *
		 * if (withBCC) { byte bcc = calculateBCC(buffer); sendRawData((byte)
		 * ((bcc >> 4) + 0x20)); sendRawData((byte) ((bcc & 0xF) + 0x20)); }
		 */
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		if (order != null) {
			buffer.write(order.getBytes());
		}
		if (data != null) {
			buffer.write(data.getBytes());
		}
		sendTelegram(buffer.toByteArray(), withBCC);
	}

	private void sendTelegram(byte[] data, boolean withBCC) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		buffer.write(STX);
		buffer.write(data);
		buffer.write(ETX);
		if (withBCC) {
			byte bcc = calculateBCC(buffer.toByteArray());
			buffer.write((bcc >> 4) + 0x20);
			buffer.write((bcc & 0xF) + 0x20);
		}
		sendRawData(buffer.toByteArray());
	}

	/**
	 * receive a telegram
	 *
	 * @param withBCC
	 *            has to be set to true, if telegram will have bcc
	 *
	 * @return read data
	 *
	 * @throws ConnectionException
	 *             if - timeout occurs - bcc is wrong
	 * @throws NestedIOException
	 */
	private String receiveTelegram(boolean withBCC) throws ConnectionException, NestedIOException {

		String result = "";
		int iReceivedChar;
		boolean stxReceived = false;

		long lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

		copyEchoBuffer();

		while (true) {
			iReceivedChar = readIn();
			if (iReceivedChar != -1) {
				/* if a character has been received, reset timeout value */
				lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

				if (!stxReceived) {
					stxReceived = iReceivedChar == STX;
					if (stxReceived) {
						result = result + (char) iReceivedChar;
					}
					continue;
				}
				else {
					result = result + (char) iReceivedChar;
					if (iReceivedChar == ETX) {
						break;
					}
				}
			}
			if ((System.currentTimeMillis() - lMSTimeout) > 0) {
				throw new ConnectionException(
						"receiveTelegram() timeout error", TIMEOUT_ERROR);
			}
		}

		/* if telegram has bcc, receive the bcc */
		if (withBCC) {
			int c = 0;
			byte readBCC = 0;
			byte dataBCC = calculateBCC(result.getBytes());

			while (true) {
				if ((iReceivedChar = readIn()) != -1) {
					lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

					if (c == 0) {
						readBCC = (byte) ((iReceivedChar & 0x0F) << 4);
					}
					else {
						readBCC |= ((byte) (iReceivedChar & 0x0F));
						break;
					}
					c++;
				}
				if ((System.currentTimeMillis() - lMSTimeout) > 0) {
					throw new ConnectionException(
							"receiveTelegram() timeout error", TIMEOUT_ERROR);
				}
			}
			if (dataBCC != readBCC) {
				throw new ConnectionException("receiveTelegram() ", CRC_ERROR);
			}
		}

		//System.out.println("receiveTelegram:" + result);
		return result;
	}

	/**
	 * Send a data string (with bcc) to device and wait for acknowledge
	 *
	 * @param data
	 * @return answer of device (ACK, NAK, ENQ, CAN)
	 * @throws IOException
	 */
	public int sendDataAcknowledged(byte[] data) throws IOException {
		sendTelegram(data, true);

		int iReceivedChar;

		long lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

		copyEchoBuffer();

		while (true) {
			iReceivedChar = readIn();
			if (iReceivedChar != -1) {
				switch (iReceivedChar) {
				case ACK:
				case NAK:
				case ENQ:
				case CAN:
					return iReceivedChar;
				default:
					throw new ConnectionException(
							"sendDataAcknowledged() acknowledge error",
							PROTOCOL_ERROR);
				}
			}
			if ((System.currentTimeMillis() - lMSTimeout) > 0) {
				throw new ConnectionException(
						"sendDataAcknowledged() timeout error", TIMEOUT_ERROR);
			}
		}
	}

	/**
	 * starting communication by sending the K command to the dsfg device
	 *
	 * @return name of dfue instance
	 *
	 * @throws IOException
	 */
	public String connect() throws IOException {
		sendTelegram("K", "", false);
		return receiveTelegram(false);
	}

	/**
	 * tries to sign on to a dsfg device (has to be done directly after
	 * connection) if the login was successful, the instance address of the dfue
	 * unit is stored
	 *
	 * @param password
	 *            (has to be of a length of 16 char)
	 *
	 * @throws IOException
	 */
	public void signon(String password) throws IOException {

		/* password always has to be 16 char long ! */
		String tmp = password + "                ";
		String pw = tmp.substring(0, 16);
		sendTelegram("I", pw, false);
		String answer = receiveTelegram(false);
		dfueInstance = answer.charAt(1);
		sendTelegram("T", "", false);
		receiveTelegram(false);
	}

	/**
	 * getDfueInstance()
	 *
	 * @return the dfue instance letter
	 */
	public char getDfueInstance() {
		return this.dfueInstance;
	}

	public DataBlock sendRequest(DataBlock db) throws IOException {
		db.setSender(this.dfueInstance);
		if (sendDataAcknowledged(db.toBytes()) != Connection.ACK) {
			throw new ConnectionException("request() not acknowledged error",
					PROTOCOL_ERROR);
		}
		String in = receiveTelegram(true);
		return new DataBlock(in.getBytes());
	}
}
