package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMIOException;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * For information regarding Opus protocol, read manufacturer documentation:
 * Protocol for communication between an instation and outstation.
 *
 * @author fbo
 */
public class OpusConnection extends Connection {

	/* protocol properties */
	private String nodeId = null;
	private String password = null;
	private int maxRetry = 0;
	private long forceDelay = 0;
	private long delayAfterFail = 0;
	private long timeout = 0;
	private TimeZone timeZone = null;
	private Logger logger = null;

	private int errorCount = 0;
	private boolean canWakeUp = false;
	private boolean canInstruct = false;

	/**
	 * Just a constructor, move along
	 *
	 * @param inputStream
	 * @param outputStream
	 * @param ppm
	 * @throws ConnectionException
	 */
	public OpusConnection(InputStream inputStream, OutputStream outputStream, PPM ppm) {
		super(inputStream, outputStream, 0, 0);
		this.nodeId = ppm.getNodeId();
		this.password = ppm.getPassword();
		this.maxRetry = ppm.getMaxRetry();
		this.forceDelay = ppm.getForceDelay();
		this.delayAfterFail = ppm.getDelayAfterFail();
		this.timeout = ppm.getTimeout();
		this.timeZone = ppm.getTimeZone();
		this.logger = ppm.getLogger();
	}

	public int getErrorCount(){
		return errorCount;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public byte[] wakeUp() throws IOException {

		delay(forceDelay);
		copyEchoBuffer();
		flushEchoBuffer();

		byte[] identification = null;

		sendOut(createWakeUp(nodeId).toByteArray());
		identification = receive(CtrlChar.ACK);
		return identification;

	}

	public OpusResponse readRegister(String dataIdentity, int packetNr, int dayNr, int nrPackets, boolean isProfileData)
            throws NestedIOException, ConnectionException, PPMIOException {
		ReadCommand command = new ReadCommand(dataIdentity, packetNr, dayNr, nrPackets, isProfileData, this);
		doCommand(command);
		return command.getOpusResponse();

	}

	public OpusResponse writeRegister(String dataIdentity, byte[] data) throws IOException {
		WriteCommand command = new WriteCommand(dataIdentity, data, this);
		doCommand(command);
		return command.getOpusResponse();
	}

	private void doCommand(OpusCommand command) throws NestedIOException, PPMIOException {

		int tries = 0;
		boolean done = false;

		while ( tries < maxRetry && !done) {
			tries += 1;
			command.clearOpusResponse();
			try {
				// KV 22072005 changed to use setter & getter!
				command.getOpusResponse().setIdentificationMessage(wakeUp());
				canWakeUp = true;
			} catch( NestedIOException nex ){
				throw nex;
			} catch (IOException ex) { // first time ignore
				logger.info( "IOException handle in command.getOpusResponse() , try nr " + tries );
				errorCount ++;
				if( tries == maxRetry ) {
					String msg = "Error sending wake up: ";
					if( canWakeUp ){
						msg += "Connection broken.";
					} else {
						msg += "Probably node id is wrong ";
						msg += "(or the connection could not be established ).";
					}
					throw new PPMIOException( msg );
				} else {
					delay( delayAfterFail );
					continue;
				}
			} catch( NumberFormatException nfex ){
				logger.info( "NumberFormatException handle in command.getOpusResponse() , try nr " + tries );
				errorCount ++;
				if( tries == maxRetry ) {
					String msg = "Error sending wake up: NumberFormatException";
					throw new PPMIOException( msg );
				} else {
					delay( delayAfterFail );
					continue;
				}
			}
			try {
				command.execute();
				canInstruct = true;
				done = true;
			} catch( NestedIOException nex ){
				throw nex;
			} catch (IOException ex) { // first time ignore
				logger.info( "IOException handle in command.execute() , try nr " + tries );
				errorCount ++;
				if( tries == maxRetry ) {
					String msg = "Error sending instruction: ";
					if( canInstruct ) {
						msg += "Connection is broken.";
					} else {
						msg += "Password is wrong.";
					}
					throw new PPMIOException( msg );
				} else {
					delay( delayAfterFail );
				}
			} catch( NumberFormatException nfex ){
				logger.info( "NumberFormatException handle in command.execute() , try nr " + tries );
				errorCount ++;
				if( tries == maxRetry ) {
					String msg = "Error sending instruction: NumberFormatException";
					throw new PPMIOException( msg );
				} else {
					delay( delayAfterFail );
					continue;
				}
			}
		}

	}

	public void sendOut(MessageComposer aMessage) throws IOException {
		sendOut(aMessage.toByteArray());
		if (receiveCtrlChar() != CtrlChar.ACK) {
			throw new IOException();
		}
	}

	/**
	 * low level receive function, stops at ETX
	 *
	 * @return
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {
		return receive(CtrlChar.ETX);
	}

	/**
	 * low level receive function, stops at endCtrlChar
	 *
	 * @param endCtrlChar
	 * @return
	 * @throws IOException
	 */
	public byte[] receive(CtrlChar endCtrlChar) throws IOException {

		long startMilliseconds = System.currentTimeMillis();
		long currentMilliseconds = System.currentTimeMillis();

		long timediff = 0;
		int input;
		//String result = "";// KV 22072005 unused code
		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		copyEchoBuffer();

		do {
			input = readIn();
			if (input != -1) {
				bao.write(input);
			}
			currentMilliseconds = System.currentTimeMillis();
			timediff = currentMilliseconds - startMilliseconds;
			if (timediff > timeout) {
				throw new IOException("connection timeout");
			}
		} while (input != endCtrlChar.getByteValue());

		return bao.toByteArray();
	}

	/**
	 * low level receive function, receives single CtrlChar
	 * This can be <ACK>, <NAK>, <EOT>, <SOH>
	 *
	 * @return
	 * @throws IOException
	 */
	public CtrlChar receiveCtrlChar() throws IOException {
		int tries = 0, input = 0;
		CtrlChar result = null;
		copyEchoBuffer();

		do {
			input = readIn();
			if (input == CtrlChar.ACK.getByteValue()) {
				result = CtrlChar.ACK;
			}
			if (input == CtrlChar.NAK.getByteValue()) {
				result = CtrlChar.NAK;
			}
			if (input == CtrlChar.EOT.getByteValue()) {
				result = CtrlChar.EOT;
			}
			if (input == CtrlChar.SOH.getByteValue()) {
				result = CtrlChar.SOH;
			}
			tries++;
		} while (tries < 100 && result == null);
		if (tries == 100) {
			throw new IOException("No meter response");
		}
		return result;

	}

	/** receive function for messages, does a checksum-check */
	byte[] receiveMessage(CtrlChar startChar) throws IOException {
		int retries = 0;
		byte[] message = null;
		boolean checksumOk = false;

		message = receive();
		if (startChar == null) {
			checksumOk = isCheckSumOk(message);
		} else {
			byte conMessage[] = new byte[message.length + 1];
			conMessage[0] = startChar.getByteValue();
			System.arraycopy(message, 0, conMessage, 1, message.length);
			checksumOk = isCheckSumOk(conMessage);
		}
		if (checksumOk) {
			return message;
		}

		while (retries < maxRetry && !checksumOk) {
			retries += 1;
			if (retries == maxRetry) {
				throw new IOException("receive failed, max retry exceeded");
			}
			sendOut(CtrlChar.NAK.getByteValue());
			message = receive();
			checksumOk = isCheckSumOk(message);
			if (checksumOk) {
				return message;
			}

		}
		throw new IOException("receive failed, max retry exceeded");
	}

	/** Opus checksum: add up all characters of a message, modulo 256 */
	private String calc0pusChecksum(MessageComposer m) {
		byte[] data = m.toByteArray();
		int checksum = calcOpusCheckSum(data, 0, data.length);
		char[] csa = Integer.toString(checksum).toCharArray();
		char[] ba = { '0', '0', '0' };
		System.arraycopy(csa, 0, ba, 3 - csa.length, csa.length);
		return new String(ba);
	}

	/** Opus checksum: add up all characters of a message, modulo 256 */
	private int calcOpusCheckSum(byte[] data, int offset, int length) {
		int checksum = 0;
		for (int i = offset; i < length; i++) {
			checksum += data[i] & 0xff;
			checksum = checksum & 0xff; // modulo
		}
		return checksum;
	}

	private boolean isCheckSumOk(byte[] input) {
		byte[] content = new byte[input.length - 4];
		byte[] checksum = new byte[3];
		System.arraycopy(input, 0, content, 0, input.length - 4);
		System.arraycopy(input, input.length - 4, checksum, 0, 3);

		String bcd = PPMUtils.parseBCDString(checksum);
		int inputCheck = Integer.parseInt(bcd);

		int receiveCheck = calcOpusCheckSum(content, 0, content.length);

		return receiveCheck == inputCheck;
	}



	String buildZeroLeadingString(int packetID, int length) {
		String str = Integer.toString(packetID);
		StringBuffer strbuff = new StringBuffer();
		if (length >= str.length()) {
			for (int i = 0; i < (length - str.length()); i++) {
				strbuff.append('0');
			}
		}
		strbuff.append(str);
		return strbuff.toString();
	}

	MessageComposer createWakeUp(String nodeId) {
		MessageComposer m = new MessageComposer().add(CtrlChar.CR).add(CtrlChar.SOH);
		m.add(nodeId);
		return m;
	}

	MessageComposer createId(String outstationNumber) {
		MessageComposer m = new MessageComposer();
		return m;
	}

	protected MessageComposer createInstruction(String dataIdentity) throws ConnectionException {
		return createInstruction(dataIdentity, nodeId, CtrlChar.READ, "000");
	}

	protected MessageComposer createInstruction(String dataIdentity, String packetNr, CtrlChar Z, String dayOffset)	throws ConnectionException {
		MessageComposer m = new MessageComposer();
		m.add(CtrlChar.SOH).add(nodeId).add(dataIdentity).add(packetNr);
		m.add(CtrlChar.SHARP).add(Z);
		m.add(CtrlChar.SHARP).add(dayOffset);
		m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP);
		m.add(password).add(CtrlChar.SHARP).add(password);
		m.add(CtrlChar.SHARP).add(CtrlChar.SHARP);
		m.add(calc0pusChecksum(m));
		m.add(CtrlChar.CR);
		return m;
	}

	protected MessageComposer createDataMessage(String dataIdentity, String packetNr, byte[] data) throws ConnectionException {
		MessageComposer m = new MessageComposer();
		m.add(CtrlChar.SOH).add(nodeId).add(dataIdentity).add(packetNr);
		m.add(CtrlChar.SHARP).add(data);
		m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP).add("0").add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP).add("0");
		m.add(CtrlChar.SHARP).add(CtrlChar.SHARP);
		m.add(calc0pusChecksum(m));
		m.add(CtrlChar.CR);
		return m;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public Logger getLogger() {
		return logger;
	}

	public String getNodeId() {
		return nodeId;
	}
}