/**
 * MK10InputStreamParser.java
 * 
 * Created on 13-jan-2009, 10:02:05 by jme
 * 
 */
package com.energyict.protocolimpl.edmi.mk10.parsers;

import java.io.ByteArrayOutputStream;

import com.energyict.protocols.util.ProtocolUtils;


/**
 * @author jme
 *
 */
public class MK10OutputStreamParser {

	private static final int DEBUG			= 0;
	private static final long DEBUG_DELAY 	= 0;
	
	private static final byte STX			= 0x02;
	private static final byte ETX			= 0x03;
	private static final byte DLE			= 0x10;

	private static final int DLE_MASK		= 0x000000BF;
	private static final int BYTE_MASK 		= 0x000000FF;
	private static final int CRC_LENGTH		= 2;

	private boolean validPacket				= false;
	private int length						= 0;
	private byte[] bytes					= null;
	
	/*
	 * Constructors
	 */

	public MK10OutputStreamParser() {}
	
	/*
	 * Private getters, setters and methods
	 */
	
	private byte[] getBytesWithoutStuffing(byte[] bytes){
		ByteArrayOutputStream returnBytes = new ByteArrayOutputStream();
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] == DLE) && (i < (bytes.length - 1))) {
				i++;
				returnBytes.write(((int)bytes[i]) & DLE_MASK);
			} else {
				returnBytes.write(((int)bytes[i]) & BYTE_MASK);
			}
		}
		
		return returnBytes.toByteArray();
	}
	
	/*
	 * Public methods
	 */

	public void parse(byte[] bytes) {
		this.bytes = bytes;
		if (getBytes() == null) {
			this.validPacket = false;
			this.length = 0;
			return;
		}

		this.length = getBytes().length;
		
		if (getLength() <= 2) {
			this.validPacket = false;
			return;
		}
		
		this.validPacket = ((bytes[0] == STX) && (bytes[bytes.length - 1] == ETX));
		
		if (DEBUG >= 1){
			System.out.println(this.toString());
		}
		
	}
	
	public byte[] getValidPacket() {
		byte[] returnBytes;
		returnBytes = ProtocolUtils.getSubArray2(getBytes(), 1, getBytes().length - CRC_LENGTH - 2);
		returnBytes = getBytesWithoutStuffing(returnBytes);
		
		if (DEBUG >= 1) {
			try {Thread.sleep(DEBUG_DELAY);} 
			catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
		}
		
		return returnBytes;
	}

	public String toString() {
		String message = "MK10OutputStreamParser";
		message += ", valid = " + isValidPacket();
		message += ", bytes = " + ProtocolUtils.getResponseData(getBytes());
		if (isValidPacket()) message += ", validPacket = " + ProtocolUtils.getResponseData(getValidPacket());
		return message;
	}
	
	/*
	 * Public getters and setters
	 */
	
	public boolean isValidPacket() {
		return validPacket;
	}

	public byte[] getBytes() {
		return bytes;
	}
	
	public int getLength() {
		return length;
	}
}
