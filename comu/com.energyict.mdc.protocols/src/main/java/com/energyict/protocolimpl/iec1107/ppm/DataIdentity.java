package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppm.opus.OpusResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/** @author Koen, fbo */

class DataIdentity implements Serializable {

	public static final boolean PROFILE = true;
	public static final boolean NORMAL = false;

	private byte[][] dataBlocks = null;

	private String idendityNumber = null;
	private int length = 0;
	private int nrPackets = 0;
	private boolean reverseIndexing = false;

	private DataIdentityFactory dataIdentityFactory = null;
	private PPM ppm = null;

	// JME: Fixed bug: changed "lenght" to "length" in argument name.
	DataIdentity(String name, int length, int nrPackets, boolean reverseIndexing) {
		init(name, length, nrPackets, reverseIndexing);
	}

	void init(String name, int length, int nrPackets, boolean reverseIndexing) {
		this.idendityNumber = name;
		this.length = length;
		this.nrPackets = nrPackets;
		this.dataBlocks = new byte[nrPackets][];
		this.reverseIndexing = reverseIndexing;
	}

	void setDataIdentityFactory(DataIdentityFactory dataIdentityFactory) {
		this.dataIdentityFactory = dataIdentityFactory;
	}

	PPM ppm() {
		if (this.ppm == null) {
			this.ppm = this.dataIdentityFactory.getPpm();
		}
		return this.ppm;
	}

	String getName() {
		return this.idendityNumber;
	}

	void clearCache() {
		this.dataBlocks = new byte[this.nrPackets][];
	}

	int getLength() {
		return this.length;
	}

	void writeRegister(String dataID, String value) throws FlagIEC1107ConnectionException, IOException {

		if (ppm().isOpus()) {
			ppm().getOpusConnection().writeRegister(dataID, value.getBytes());
		} else {
			String data = dataID + "001(" + value + ")";
			ppm().getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.WRITE1, data.getBytes());
		}
		clearCache();

	}

	// read register in the meter if not cached
	byte[] readRegister(String dataID, boolean cached, int dataLength, int set) throws FlagIEC1107ConnectionException, IOException {
		if ((!cached) || (this.dataBlocks[set] == null)) {
			if (ppm().isOpus()) {
				OpusResponse or = ppm().getOpusConnection().readRegister(dataID, 0, 0, this.nrPackets, false);
				this.dataBlocks[set] = or.getDataMessageContent();
			} else {
				if (this.reverseIndexing) {
					this.dataBlocks[set] = doReadRegisterProfile(dataID, dataLength, set);
				} else {
					this.dataBlocks[set] = doReadFlagRegister(dataID, dataLength, set);
				}
			}
		}
		return this.dataBlocks[set];
	}

	byte[] doReadFlagRegister(String dataID, int dataLen, int set)
	throws FlagIEC1107ConnectionException, IOException {
		byte[] dataBlock = null;
		if (dataLen <= 0) {
			throw new FlagIEC1107ConnectionException(
					"DataIdentity, doReadRawRegister, wrong dataLength ("
					+ dataLen + ")!");
		}
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		String strLength;
		int packetid = ((dataLen / 64) + ((dataLen % 64) == 0 ? 0 : 1)) * set
		+ 1; // calculate packetid
		int dataLength = dataLen;
		while (dataLength > 0) {
			strLength = ((dataLength / 64) > 0) ? Integer.toHexString(64) : Integer.toHexString(dataLength % 64);
			dataLength -= 64;
			StringBuffer strbuff = new StringBuffer();
			strbuff.append(dataID);
			strbuff.append(buildPacketID(packetid++, 3));
			strbuff.append('(');
			strbuff.append(strLength.length() < 2 ? "0" + strLength : strLength);
			strbuff.append(')');
			this.dataIdentityFactory.getPpm().getFlagIEC1107Connection()
			.sendRawCommandFrame(FlagIEC1107Connection.READ1,
					strbuff.toString().getBytes());
			byte[] ba = this.dataIdentityFactory.getPpm().getFlagIEC1107Connection()
			.receiveData();
			String str = new String(ba);
			// KV 19012004
			if (str.indexOf("ERR") != -1) {
				// KV 22072005
				//String exceptionId = str.substring(str.indexOf("ERR"), str.indexOf("ERR") + 4);
				throw new FlagIEC1107ConnectionException("DataIdentity, doReadRawRegister" );
			}
			data.write(ba);
		} // while (dataLength > 0)
		dataBlock = ProtocolUtils.convert2ascii(data.toByteArray());
		return dataBlock;
	}

	/* omitting 101 & 192 due to meterbug (spec pg 5 ) */
	private short[] profileAddressArray = {191, 190, 189, 188, 187, 186, 185,
			184, 183, 182, 181, 180, 179, 178, 177, 176, 175, 174, 173, 172,
			171, 170, 169, 168, 167, 166, 165, 164, 163, 162, 161, 160, 159,
			158, 157, 156, 155, 154, 153, 152, 151, 150, 149, 148, 147, 146,
			145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133,
			132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120,
			119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107,
			106, 105, 104, 103, 102, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91,
			90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74,
			73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57,
			56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40,
			39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23,
			22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5,
			4, 3, 2, 1};

	byte[] doReadRegisterProfile(String dataID, int dataLen, int set)
	throws FlagIEC1107ConnectionException, IOException {

		if (dataLen <= 0) {
			throw new FlagIEC1107ConnectionException(
					"DataIdentity, doReadRawRegister, wrong dataLength ("
					+ dataLen + ")!");
		}

		ByteArrayOutputStream data = new ByteArrayOutputStream();

		int nrOfPackets = ((dataLen / 128) + ((dataLen % 128) == 0 ? 0 : 1));
		nrOfPackets = (nrOfPackets > this.profileAddressArray.length) ? this.profileAddressArray.length : nrOfPackets;

		int pIndex = nrOfPackets;

		for (; pIndex > 0; pIndex--) {
			StringBuffer strbuff = new StringBuffer();
			strbuff.append(dataID);
			strbuff.append(buildPacketID(this.profileAddressArray[pIndex - 1], 3));
			strbuff.append("(40)");

			this.dataIdentityFactory.getPpm().getFlagIEC1107Connection()
			.sendRawCommandFrame(FlagIEC1107Connection.READ1, strbuff.toString().getBytes());

			byte[] ba = this.dataIdentityFactory.getPpm().getFlagIEC1107Connection().receiveData();
			String str = new String(ba);

			if (str.indexOf("ERR") != -1) {
				String exceptionId = str.substring(str.indexOf("ERR"), str.indexOf("ERR") + 4);
				throw new FlagIEC1107ConnectionException("DataIdentity, doReadRawRegister, " + ppm().getExceptionInfo(exceptionId));
			}
			data.write(ba);
		}

		return ProtocolUtils.convert2ascii(data.toByteArray());

	}

	String buildPacketID(int packetID, int length) {
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

}
