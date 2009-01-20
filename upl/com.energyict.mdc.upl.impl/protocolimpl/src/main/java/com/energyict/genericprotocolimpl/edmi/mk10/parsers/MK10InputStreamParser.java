/**
 * MK10InputStreamParser.java
 * 
 * Created on 13-jan-2009, 10:02:05 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;

/**
 * @author jme
 *
 */
public class MK10InputStreamParser {

	private static final int DEBUG			= 0;
	private static final long DEBUG_DELAY 	= 0;
	
	private static final int CRC_LENGTH 	= 2;
	private static final int BYTEMASK		= 0x000000FF;

	private static final byte STX			= 0x02;
	private static final byte ETX			= 0x03;
	private static final byte XON			= 0x11;
	private static final byte XOFF			= 0x13;
	private static final byte DLE			= 0x10;
	private static final byte STUFFING 		= 0x40;

	private static final byte PUSH_START	= (byte)0x8F;
	private static final int PUSH_LENGTH	= 24;
	
	private static final byte EXT_START		= 'E';
	private static final byte INFO_START	= 'I';
	private static final byte READ_START	= 'R';
	private static final byte FARC_START	= 'F';
	
	private static final int EXT_MIN_LENGTH		= 12 + CRC_LENGTH;
	private static final int INFO_MIN_LENGTH	= 6 + CRC_LENGTH;
	private static final int READ_MIN_LENGTH	= 4 + CRC_LENGTH;
	private static final int FARC_MIN_LENGTH	= 10 + CRC_LENGTH;

	private static final int EXT_DATA_OFFSET	= 11;
	private static final int SERIAL_OFFSET		= 4;

	private byte[] bytes					= null;
	private boolean validPacket				= false;
	private boolean pushPacket				= false;
	private int length						= 0;
	private int crcCalc						= 0;
	private int crcData						= 0;
	private int serial						= 0;
	
	/*
	 * Constructors
	 */

	public MK10InputStreamParser() {}
	
	/*
	 * Private getters, setters and methods
	 */

	private byte[] getBytesCharStuffing(byte[] inputBytes) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		for (int i = 0; i < inputBytes.length; i++) {
			byte b = inputBytes[i];
			if ((b == STX) || (b == ETX) || (b == XON) || (b == XOFF) || (b == DLE)) {
				b = (byte) (b | STUFFING);
				buffer.write(DLE);
			}
			buffer.write(b);
		}
		
		return buffer.toByteArray(); 
	}
	
	private byte[] appendCRC(byte[] inputBytes) throws IOException {
		byte[] byteBuffer = new byte[inputBytes.length + 2];
		int tempCRC = CRCGenerator.ccittCRC(inputBytes, inputBytes.length);
		ProtocolUtils.arrayCopy(inputBytes, byteBuffer, 0);

		byteBuffer[byteBuffer.length - 1] = (byte)(tempCRC & BYTEMASK);
		byteBuffer[byteBuffer.length - 2] = (byte)((tempCRC>>8) & BYTEMASK);
				
		return byteBuffer;
	}

	private boolean validatePacket(byte[] bts) {
		switch (bts[0]) {
		case PUSH_START: 
			if (bts.length != PUSH_LENGTH) return false;
			return true;
		case INFO_START: 
			if (bts.length < INFO_MIN_LENGTH) return false;
			return true;
		case EXT_START: 
			if (bts.length < EXT_MIN_LENGTH) return false;
			return validatePacket(ProtocolUtils.getSubArray(bts, EXT_DATA_OFFSET));
		case READ_START: 
			if (bts.length < READ_MIN_LENGTH) return false;
			return true;
		case FARC_START: 
			if (bts.length < FARC_MIN_LENGTH) return false;
			return true;
		default: return true;
		}
	}

	/*
	 * Public methods
	 */

	public int parse(byte[] bytes, boolean isLastByte) {
		boolean valid = false;
		boolean push = false;
		
		if (bytes == null) throw new NullPointerException("Invalid argument: bytes cannot be null;");
		this.bytes = bytes;
		this.length = getBytes().length;

		if (getLength() > CRC_LENGTH) {
			this.crcCalc = CRCGenerator.ccittCRC(bytes, bytes.length - CRC_LENGTH);
			this.crcData = 
				(((int)bytes[getLength() - 1]) & BYTEMASK) +  
				((((int)bytes[getLength() - 2]) & BYTEMASK) * 256);
			
			valid = (getCrcCalc() == getCrcData()) && validatePacket(bytes);
			push = ((bytes[0] == PUSH_START) && (bytes.length == PUSH_LENGTH)) && valid; 

			if (push) { 					// Packet is push packet so it doesn't matter there are still bytes in the input stream  
				this.validPacket = true;
				this.pushPacket = true;
			} else {
				if (isLastByte) {
					if (valid) {
						this.validPacket = true;
						this.pushPacket = false;
					} else {
						this.validPacket = searchPacket(getBytes());
						this.pushPacket = false;
					}
				} else {
					this.validPacket = false;
					this.pushPacket = false;
				}
			}
		} else {
			this.validPacket = false;
			this.pushPacket = false;
			this.crcCalc = 0;
			this.crcData = 0;
		}

		if (isPushPacket()) {
			this.serial = 0;
			for (int i = 0; i < 4; i++) {
				int value = ((int) bytes[SERIAL_OFFSET + (3-i)] & 0x000000FF);
				this.serial += value << (8*i);
			}
		} else {
			this.serial = 0;
		}
		
		if ((DEBUG >= 2) && isValidPacket()) System.out.println("MK10InputStreamParser.parse(): " + this.toString());
		
		return getBytes().length;
	}
		
	private boolean searchPacket(byte[] bts) {
		boolean valid = false;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		for (int i = 0; i < bts.length; i++) {
			buffer.write(bts[i]);
			if (buffer.toByteArray().length > CRC_LENGTH) {
				int tempCrcCalc = CRCGenerator.ccittCRC(buffer.toByteArray(), buffer.toByteArray().length - CRC_LENGTH);
				int tempCrcData = (((int)bytes[getLength() - 1]) & BYTEMASK) + ((((int)bytes[getLength() - 2]) & BYTEMASK) * 256);
				if ((tempCrcCalc == tempCrcData) && validatePacket(buffer.toByteArray())) valid = true;
			}
		}
		
		if (valid) this.bytes = buffer.toByteArray();
		return valid;
	}

	public String toString() {
		String returnValue = "";
		returnValue += "length = " + getLength() + ", ";
		returnValue += "crcCalc = " + getCrcCalc() + ", ";
		returnValue += "crcData = " + getCrcData() + ", ";
		returnValue += "validPacket = " + isValidPacket() + ", ";
		returnValue += "pushPacket = " + isPushPacket() + ", ";
		returnValue += "serial = " + getSerial() + ", ";
		returnValue += "bytes = " + ProtocolUtils.getResponseData(getBytes());
		return returnValue;
	}
	
	/*
	 * Public getters and setters
	 */
	
	public byte[] getValidPacket() throws IOException {
		byte[] returnBytes;
		byte[] bytesNoCRC = ProtocolUtils.getSubArray2(getBytes(), 0, getBytes().length - CRC_LENGTH);
		
		returnBytes = ProtocolUtils.concatByteArrays(new byte[] {STX}, bytesNoCRC); 	// append STX to calculate CRC (including STX)
		returnBytes = appendCRC(returnBytes);											// calculate and append CRC
		returnBytes = ProtocolUtils.getSubArray(returnBytes, 1);						// remove STX from packet to prevent stuffing of real STX
		returnBytes = getBytesCharStuffing(returnBytes);								// apply stuffing on packet
		returnBytes = ProtocolUtils.concatByteArrays(new byte[] {STX}, returnBytes);	// add STX to start of frame
		returnBytes = ProtocolUtils.concatByteArrays(returnBytes, new byte[] {ETX});	// append ETX to end of frame 
		
		if (DEBUG >= 1) {
			System.out.println();
			System.out.println(" Input data   = " + ProtocolUtils.getResponseData(getBytes()));
			System.out.println(" Valid packet = " + ProtocolUtils.getResponseData(returnBytes));
		}

		if (DEBUG >= 3) {
			try {Thread.sleep(DEBUG_DELAY);} 
			catch (InterruptedException e) {e.printStackTrace();}
		}

		
		return returnBytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getLength() {
		return length; 
	}
	
	public int getCrcCalc() {
		return crcCalc;
	}
	
	public int getCrcData() {
		return crcData;
	}
	
	public boolean isValidPacket() {
		return validPacket;
	}
	
	public boolean isPushPacket() {
		return pushPacket;
	}
	
	public int getSerial() {
		return serial;
	}
	
	public String getSerialAsString() {
		return String.valueOf(serial);
	}
	
}
