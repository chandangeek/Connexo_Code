package com.energyict.protocolimpl.iec1107.ppm.opus;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connections.Connection;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocolimpl.iec1107.ppm.Encryption;
import com.energyict.protocolimpl.iec1107.ppm.PPM;
import com.energyict.protocolimpl.iec1107.ppm.PPMIOException;
import com.energyict.protocolimpl.iec1107.ppm.PPMUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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
 *
 * @author fbo
 */

public class OpusConnection extends Connection {

	private PPM ppm = null;

	/* encryption class */
	private Encryption encryption = new Encryption();

	/* protocol properties */
	private String nodeId = null;
	private String password = null;
	private int maxRetry = 0;
	private long forceDelay = 0;
	private long timeout = 0;
	private TimeZone timeZone = null;
	private Logger logger = null;

	private int errorCount = 0;

	private boolean canWakeUp = false;
	private boolean canInstruct = false;

	private long delayAfterFail = 0;

	/* keep track of previous seed, for calculating next seed */
	private long seed = 0;

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

		this.ppm = ppm;
	}

	public int getErrorCount() {
		return this.errorCount;
	}

	/* ___________________ Communication Sequences _________________________ */

	public byte[] wakeUp() throws IOException {

		delay(this.forceDelay);
		copyEchoBuffer();
		flushEchoBuffer();

		byte[] identification = null;

		sendOut(createWakeUp(this.nodeId).toByteArray());
		identification = receive();
		return identification;

	}

	public OpusResponse readRegister(String dataIdentity, int packetNr, int dayNr, int nrPackets, boolean isProfileData)
            throws NestedIOException, ConnectionException, PPMIOException {

		ReadCommand command = new ReadCommand(dataIdentity, packetNr, dayNr, nrPackets, isProfileData);
		doCommand(command);
		return command.getOpusResponse();

	}

	public OpusResponse writeRegister(String dataIdentity, byte[] data) throws IOException {
		WriteCommand command = new WriteCommand(dataIdentity, data);
		doCommand(command);
		return command.getOpusResponse();
	}

	abstract class OpusCommand {

		abstract void execute() throws IOException;

		abstract OpusResponse getOpusResponse();

		abstract void clearOpusResponse();

		/* Check Message/Packet for
		 *  - outstation nr (=nodeId) matches configured outstation nr
		 *  - data identity matches requested data identity
		 *  - packet nr matches expected packet nr
		 */
		void check(byte[] rsp, String dataId, int pNr) throws IOException {

			int offset = (rsp[0] == SOH.getByteValue()) ? 1 : 0;

			String rcvd = toString(rsp, offset, 3);
			String xpctd = OpusConnection.this.nodeId;

			if (!rcvd.equals(xpctd)) {
				String msg = "Received NodeId incorrect: " + rcvd;
				OpusConnection.this.logger.severe(msg);
				throw new IOException(msg);
			}

			rcvd = toString(rsp, (offset + 3), 3);
			xpctd = dataId;

			if (!rcvd.equals(xpctd)) {
				String msg = "Received DataIdentity incorrect: " + rcvd;
				OpusConnection.this.logger.severe(msg);
				throw new IOException(msg);
			}

			rcvd = toString(rsp, (offset + 6), 3);
			int rcvdPNr = Integer.parseInt(rcvd);

			if (rcvdPNr != pNr) {
				String msg = "Received PacketNr not matching: " + rcvd;
				OpusConnection.this.logger.severe(msg);
				throw new IOException(msg);
			}
		}

		/* cut a peace from a byte array and convert to a String */
		String toString(byte[] ba, int start, int length) {
			byte[] rslt = new byte[length];
			System.arraycopy(ba, start, rslt, 0, length);
			return new String(rslt);
		}

	}

	class ReadCommand extends OpusCommand {

		OpusResponse opusResponse = null;

		String dataIdentity = null;
		String packetNumber = "001";
		String dayNumber = "000";
		int nrPackets = 0;
		boolean isProfileData = false;

		ReadCommand(String dataIdentity) {
			this.dataIdentity = dataIdentity;
		}

		ReadCommand(String dataIdentity, int packetNr, int dayNr, int nrPackets, boolean isProfileData) {
			this(dataIdentity);
			this.packetNumber = buildZeroLeadingString(packetNr, 3);
			this.dayNumber = buildZeroLeadingString(dayNr, 3);
			this.nrPackets = nrPackets;
			this.isProfileData = isProfileData;
		}

		/* Read sequence:
		 * ->   Wake Up
		 * <-   <AK>
		 * ->   Read Instruction Message
		 * <-   <AK>
		 * ->   <STX>
		 * <-   <SOH> Definition Message
		 * ->   <AK>
		 * <-   <SOH> Data Message x <O
		 * ->   <AK>
		 * ->   <EOT>
		 *
		 * The end of the message is the difficult part.  If the protocol knows
		 * the end is reached, it sends an End of Transmission <EOT>
		 * Else it sends and <ACK>.
		 *
		 * In case of the Profile data, the result has a dynamic lenght.
		 * If the end of the days has been reached, the meter sends an <ETX>
		 */
		public void execute() throws IOException {

			int packetCount = 1;
			boolean endOfRegister = false;

			sendOut(createInstruction(this.dataIdentity, this.packetNumber, READ, this.dayNumber));

			sendOut(STX.getByteValue());
			byte[] receive = receiveMessage(null);
			checkDefinition(receive);
			this.opusResponse.setDefinitionMessage(receive);

			sendOut(ACK.getByteValue());
			receiveCtrlChar();
			this.opusResponse.addDataMessage(receiveMessage(SOH));

			CtrlChar aChar = null;

			while ((packetCount < this.nrPackets) && !endOfRegister) {

				sendOut(ACK.getByteValue());
				aChar = receiveCtrlChar();
				if (aChar == SOH) {
					// if SOH, another part is comming
					byte[] resp = receiveMessage(SOH);

					check(resp, this.dataIdentity, packetCount + 1);

					this.opusResponse.addDataMessage(resp);
					packetCount = packetCount + 1;

				} else {
					// if something else (eg <ETX>) the transmission is done
					endOfRegister = true;
				}

			}

			// in the end send an <EOT>, then start new command
			if (aChar != EOT) {
				sendOut(EOT.getByteValue());
			}
		}

		/* Z field in definition message must be "R" */
		private void checkDefinition(byte[] rsp) throws IOException {

			String rcvd = toString(rsp, 11, 1);
			if (!rcvd.equals("R")) {
				String msg = "Received Z field=" + rcvd + " (expected=R)";
				OpusConnection.this.logger.severe(msg);
				throw new IOException(msg);
			}

			check(rsp, this.dataIdentity, 0);

		}

		public OpusResponse getOpusResponse() {
			return this.opusResponse;
		}

		public void clearOpusResponse() {
			this.opusResponse = new OpusResponse(OpusConnection.this.timeZone, this.isProfileData);
		}

	}

	class WriteCommand extends OpusCommand {

		OpusResponse opusResponse = null;

		String dataIdentity = null;
		String packetNumber = "000";
		String dayNumber = "850";

		byte[] data;

		public WriteCommand(String dataIdentity, byte[] data) {
			this.dataIdentity = dataIdentity;
			this.data = data;
		}

		public void execute() throws IOException {

			MessageComposer iMessage = createInstruction(this.dataIdentity, this.packetNumber, WRITE, this.dayNumber);
			sendOut(iMessage);

			sendOut(STX.getByteValue());
			byte[] receive = receiveMessage(null);
			checkDefinition(receive);
			this.opusResponse.setDefinitionMessage(receive);

			sendOut(ACK.getByteValue());

			receive(STX);

			MessageComposer message = createDataMessage(this.dataIdentity, "001", this.data);
			sendOut(message);

			sendOut(EOT.getByteValue());

		}

		public OpusResponse getOpusResponse() {
			return this.opusResponse;
		}

		public void clearOpusResponse() {
			this.opusResponse = new OpusResponse(OpusConnection.this.timeZone, false);

		}

		/* Z field in definition message must be "W" */
		private void checkDefinition(byte[] rsp) throws IOException {

			String rcvd = toString(rsp, 11, 1);
			if (!rcvd.equals("W")) {
				String msg = "Received Z field=" + rcvd + " (expected=W)";
				OpusConnection.this.logger.severe(msg);
				throw new IOException(msg);
			}

			check(rsp, this.dataIdentity, 0);

		}

	}

	private void doCommand(OpusCommand command) throws NestedIOException, PPMIOException {

		int tries = 0;
		boolean done = false;

		while ((tries < this.maxRetry) && !done) {
			//logger.info( "tries = " +  tries );
			//delay(forceDelay);
			tries += 1;
			command.clearOpusResponse();
			try {
				command.getOpusResponse().setIdentificationMessage(wakeUp());
				this.canWakeUp = true;
			} catch (NestedIOException nex) {
				throw nex;
			} catch (IOException ex) { // first time ignore
				this.logger.info("IOException handle in command.getOpusResponse() , try nr " + tries);
				this.errorCount++;
				if (tries == this.maxRetry) {
					String msg = "Error sending wake up: ";
					if (this.canWakeUp) {
						msg += "Connection broken.";
					} else {
						msg += "Probably node id is wrong ";
						msg += "(or the connection could not be established ).";
					}
					throw new PPMIOException(msg);
				} else {
					delay(this.delayAfterFail);
					continue;
				}
			} catch (NumberFormatException nfex) {
				this.logger.info("NumberFormatException handle in command.getOpusResponse() , try nr " + tries);
				this.errorCount++;
				if (tries == this.maxRetry) {
					String msg = "Error sending wake up: NumberFormatException";
					throw new PPMIOException(msg);
				} else {
					delay(this.delayAfterFail);
					continue;
				}
			}
			try {
				command.execute();
				this.canInstruct = true;
				done = true;
			} catch (NestedIOException nex) {
				throw nex;
			} catch (IOException ex) { // first time ignore
				this.logger.info("IOException handle in command.execute() , try nr " + tries);
				this.errorCount++;
				if (tries == this.maxRetry) {
					String msg = "Error sending instruction: ";
					if (this.canInstruct) {
						msg += "Connection is broken.";
					} else {
						msg += "Password is wrong.";
					}
					throw new PPMIOException(msg);
				} else {
					delay(this.delayAfterFail);
				}
			} catch (NumberFormatException nfex) {
				this.logger.info("NumberFormatException handle in command.execute() , try nr " + tries);
				this.errorCount++;
				if (tries == this.maxRetry) {
					String msg = "Error sending instruction: NumberFormatException";
					throw new PPMIOException(msg);
				} else {
					delay(this.delayAfterFail);
					continue;
				}
			}
		}

	}

	/** Control Characters used by opus protocol */
	private static final CtrlChar SOH = new CtrlChar(0x01, "SOH", "START OF MESSAGE");
	private static final CtrlChar ACK = new CtrlChar(0x06, "ACK", "ACKNOWLEDGE");
	private static final CtrlChar ETX = new CtrlChar(0x03, "ETX", "END OF TEXT");
	private static final CtrlChar NAK = new CtrlChar(0x15, "NAK", "NOT ACKNOWLEDGE");
	private static final CtrlChar STX = new CtrlChar(0x02, "STX", "NOT ACKNOWLEDGE");
	private static final CtrlChar EOT = new CtrlChar(0x04, "EOT", "END OF TRANSMISSION");
	private static final CtrlChar CR = new CtrlChar(0x0d, "CR", "Carriage return");
	private static final CtrlChar SHARP = new CtrlChar(0x23, "#", "SHARP");
	private static final CtrlChar READ = new CtrlChar(0x52, "R", "READ");
	private static final CtrlChar WRITE = new CtrlChar(0x57, "W", "WRITE");

	public void sendOut(MessageComposer aMessage) throws IOException {

		sendOut(aMessage.toByteArray());

		if (receiveCtrlChar() != ACK) {
			throw new IOException();
		}

	}

	/**
	 * low level receive function, stops at ETX
	 *
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {
		return receive(ETX);
	}

	/**
	 * low level receive function, stops at endCtrlChar
	 *
	 * @throws IOException
	 */
	public byte[] receive(CtrlChar endCtrlChar) throws IOException {

		long startMilliseconds = System.currentTimeMillis();
		long currentMilliseconds = System.currentTimeMillis();

		long timediff = 0;
		int input;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		copyEchoBuffer();

		do {
			input = readIn();
			if (input != -1) {
				bao.write(input);
			}
			currentMilliseconds = System.currentTimeMillis();
			timediff = currentMilliseconds - startMilliseconds;
			if (timediff > this.timeout) {
				throw new IOException("connection timeout");
			}
		} while (input != endCtrlChar.getByteValue());

		return bao.toByteArray();
	}

	/**
	 * low level receive function, receives single CtrlChar
	 *
	 * @throws IOException
	 */
	public CtrlChar receiveCtrlChar() throws IOException {

		int tries = 0, input = 0;
		CtrlChar result = null;
		copyEchoBuffer();

		do {
			input = readIn();

			if (input == ACK.getByteValue()) {
				result = ACK;
			}
			if (input == NAK.getByteValue()) {
				result = NAK;
			}
			if (input == EOT.getByteValue()) {
				result = EOT;
			}
			if (input == SOH.getByteValue()) {
				result = SOH;
			}
			tries++;
			//System.out.println( "tries " + tries );
		} while ((tries < 100) && (result == null));
		if (tries == 100) {
			throw new IOException("No meter response");
		}
		return result;

	}

	/** receive function for messages, does a checksum-check */
	private byte[] receiveMessage(CtrlChar startChar) throws IOException {
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

		while ((retries < this.maxRetry) && !checksumOk) {
			retries += 1;
			if (retries == this.maxRetry) {
				throw new IOException("receive failed, max retry exceeded");
			}
			sendOut(NAK.getByteValue());
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

		return calcOpusCheckSum(content, 0, content.length) == inputCheck;
	}

	private String encrypt(String seed) {
		return this.encryption.encrypt(this.password, seed);
	}

	/**
	 * Seed data for encryption: see Communication between instation and
	 * outstation 7.3 pg 5. But the information in the manufacturer is
	 * incorrect, the meter does NOT accept hexadecimal character, but only
	 * decimal characters.
	 *
	 * @return next seed
	 */
	private String getNextSeed() {
		if (this.seed == 0x9999999999999999L) {
			this.seed = 0x0;
		} else {
			this.seed += 0x1;
		}

		char[] ca = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0' };

		char[] sa = Long.toString(this.seed).toCharArray();

		System.arraycopy(sa, 0, ca, 16 - sa.length, sa.length);

		return new String(ca);
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

	/////////////////////

	MessageComposer createWakeUp(String nodeId) {
		MessageComposer m = new MessageComposer().add(CR).add(SOH);
		m.add(nodeId);
		return m;
	}

	protected MessageComposer createInstruction(String dataIdentity, String packetNr, CtrlChar Z, String dayOffset) throws ConnectionException {

		MessageComposer m = new MessageComposer();

		m.add(SOH).add(this.nodeId).add(dataIdentity).add(packetNr);

		m.add(SHARP).add(Z);

		m.add(SHARP).add(dayOffset);
		m.add(SHARP).add("0").add(SHARP).add("0");
		m.add(SHARP).add("0").add(SHARP).add("0");
		m.add(SHARP);

		String seed = getNextSeed();

		m.add(seed).add(SHARP).add(encrypt(seed));

		m.add(SHARP).add(SHARP);
		//byte[] r = m.toByteArray // KV 22072005 unused
		m.add(calc0pusChecksum(m));

		m.add(CR);
		return m;
	}

	protected MessageComposer createDataMessage(String dataIdentity, String packetNr, byte[] data) throws ConnectionException {
		MessageComposer m = new MessageComposer();

		m.add(SOH).add(this.nodeId).add(dataIdentity).add(packetNr);

		m.add(SHARP).add(data);

		m.add(SHARP).add("0").add(SHARP).add("0");
		m.add(SHARP).add("0").add(SHARP).add("0");
		m.add(SHARP).add("0").add(SHARP).add("0");
		m.add(SHARP).add("0");

		m.add(SHARP).add(SHARP);
		//byte[] r = m.toByteArray(); // KV 22072005 unused
		m.add(calc0pusChecksum(m));

		m.add(CR);
		return m;
	}

	public PPM getPpm() {
		return this.ppm;
	}

	public void setPpm(PPM ppm) {
		this.ppm = ppm;
	}

	class MessageComposer {

		ByteArrayOutputStream content = new ByteArrayOutputStream();

		byte[] add(byte b) {
			this.content.write(b);
			return this.content.toByteArray();
		}

		MessageComposer add(CtrlChar ctrlChar) {
			this.content.write(ctrlChar.getByteValue());
			return this;
		}

		MessageComposer add(byte[] b) {
			this.content.write(b, 0, b.length);
			return this;
		}

		MessageComposer add(int i) {
			byte[] data = new byte[2];
			ProtocolUtils.val2BCDascii(i, data, 0);
			this.content.write(data, 0, 1);
			return this;
		}

		MessageComposer add(String aString) {
			char[] c = aString.toCharArray();
			for (int i = 0; i < c.length; i++) {
				this.content.write(c[i]);
			}
			return this;
		}

		byte[] toByteArray() {
			return this.content.toByteArray();
		}

		public String toString() {
			return toHexaString();
		}

		public String toHexaString() {
			StringBuffer result = new StringBuffer();
			byte[] contentArray = this.content.toByteArray();
			for (int i = 0; i < contentArray.length; i++) {
				result.append(PPMUtils.toHexaString(contentArray[i]) + " ");
			}
			return result.toString();
		}

	}

}