package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.logging.Logger;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

public class EncoderInternalData {

	private static final int ENCODER_INTERNAL_DATA_LENGTH = 58;
	
	/**
	 * 10 characters identifying the meter.
	 */
	private String userId;
	/**
	 * 8 digits giving the current meter reading
	 */
	private long currentIndex;
	
	/**
	 * 2 digits (1 byte) holding the value of up to 8 status flags
	 * bit0: cell log
	 * bit1: eeprom fault
	 * bit2..7 reserved
	 */
	private int status;
	
	/**
	 * 2 digits (1 byte) identifying the data packet version  
	 */
	private int version;
	
	/**
	 * 8 digits identifying the totalizer serial
	 */
	private String totalizerSerial;
	
	/**
	 * 8 digits identifying the transducer serial
	 */
	private String transducerSerial;
	
	/**
	 * 2 digits (1 byte) giving the last 2 digits of the meterreading
	 */
	private int lastPart; 
	
	/**
	 * 2 digits (1 byte) giving the unit
	 * 1 cubic meters, 2 litres, 3 cubic feet, 5 imperial gallons, 6 us gallons
	 */
	private EncoderUnitType encoderUnitType;
	
	/**
	 * 2 digits (1 byte) number of significant numbers of reading 
	 */
	private int decimalPosition;

	/**
	 * 2 digits (1 byte) decimal counter of dry electrode events
	 */
	private int dryCount;
	
	/**
	 * 2 digits (1 byte) decimal counter of leak events
	 */
	private int leakCount;
	
	/**
	 * 2 digits (1 byte) decimal counter of tamper events
	 */
	private int tamperCount;

	/**
	 * 2 digits (1 byte) decimal counter of no flow events
	 */
	private int noflowCount;

	final String getUserId() {
		return userId;
	}

	final long getCurrentIndex() {
		return currentIndex;
	}

	final int getStatus() {
		return status;
	}

	final int getVersion() {
		return version;
	}

	final String getTotalizerSerial() {
		return totalizerSerial;
	}

	final String getTransducerSerial() {
		return transducerSerial;
	}

	final int getLastPart() {
		return lastPart;
	}

	final EncoderUnitType getEncoderUnitType() {
		return encoderUnitType;
	}

	final int getDecimalPosition() {
		return decimalPosition;
	}

	final int getDryCount() {
		return dryCount;
	}

	final int getLeakCount() {
		return leakCount;
	}

	final int getTamperCount() {
		return tamperCount;
	}

	final int getNoflowCount() {
		return noflowCount;
	}

	public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EncoderInternalData:\n");
        strBuff.append("   userId="+getUserId()+"\n");
        strBuff.append("   totalizerSerial="+getTotalizerSerial()+"\n");
        strBuff.append("   transducerSerial="+getTransducerSerial()+"\n");
        strBuff.append("   version="+getVersion()+"\n");
        strBuff.append("   status="+Utils.toHexString(getStatus())+"\n");
        strBuff.append("   currentIndex="+getCurrentIndex()+"\n");
        strBuff.append("   decimalPosition="+getDecimalPosition()+"\n");
        strBuff.append("   unit="+getEncoderUnitType()+"\n");
        strBuff.append("   dryCount="+getDryCount()+"\n");
        strBuff.append("   lastPart="+getLastPart()+"\n");
        strBuff.append("   leakCount="+getLeakCount()+"\n");
        strBuff.append("   noflowCount="+getNoflowCount()+"\n");
        strBuff.append("   tamperCount="+getTamperCount()+"\n");
        return strBuff.toString();
    }

	EncoderInternalData(byte[] data, Logger logger) throws IOException {
		
		if (data.length != ENCODER_INTERNAL_DATA_LENGTH) {
			throw new WaveFlow100mwEncoderException("Invalid encoder internal data length. Expected length ["+ENCODER_INTERNAL_DATA_LENGTH+"], received length ["+data.length+"]");
		}
		
		System.out.println("Port B encoder internal data:\n"+new String(data));

		
		DataInputStream dais = null;
		try {
			byte[] temp;
			char c;
			int i;
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			
			c = (char)dais.readByte();
			if (c != '*') {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [*], read ["+c+"]"); 
			}
			
			temp = new byte[10];
			dais.read(temp);
			userId = new String(temp);

			temp = new byte[8];
			dais.read(temp);
			currentIndex = Long.parseLong(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			status = Integer.parseInt(new String(temp));
			
			i = Utils.toInt(dais.readByte());
			if (i!= 0x0d) {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [0x0D], read ["+Utils.toHexString(i)+"]");
			}
			
			c = (char)dais.readByte();
			if (c != '&') {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data. Expected [&], read ["+c+"]"); 
			}

			temp = new byte[2];
			dais.read(temp);
			version = Integer.parseInt(new String(temp));
			
			temp = new byte[8];
			dais.read(temp);
			totalizerSerial = new String(temp);
			
			temp = new byte[8];
			dais.read(temp);
			transducerSerial = new String(temp);
			
			temp = new byte[2];
			dais.read(temp);
			lastPart = Integer.parseInt(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			encoderUnitType = EncoderUnitType.fromId(Integer.parseInt(new String(temp)));
			
			temp = new byte[2];
			dais.read(temp);
			decimalPosition = Integer.parseInt(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			dryCount = Integer.parseInt(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			leakCount = Integer.parseInt(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			tamperCount = Integer.parseInt(new String(temp));
			
			temp = new byte[2];
			dais.read(temp);
			noflowCount = Integer.parseInt(new String(temp));

			temp = new byte[2];
			dais.read(temp);
			int receivedChecksum = Integer.parseInt(new String(temp),16);

			// calc checksum
			int calculatedChecksum=0;
			for (int j=1; j<=10 ; j++) {
				calculatedChecksum+=Utils.toInt(data[j]);
			}
			for (int j=11; j<=20 ; j+=2) {
				int v = Integer.parseInt(new String(new byte[]{data[j],data[j+1]}),16);
				calculatedChecksum+=v;
			}
			for (int j=23; j<=54 ; j+=2) {
				int v = Integer.parseInt(new String(new byte[]{data[j],data[j+1]}),16);
				calculatedChecksum+=v;
			}
			
			if (receivedChecksum != (calculatedChecksum&0xff)) {
				throw new WaveFlow100mwEncoderException("Invalid encoder internal data checksum. Calculated ["+Utils.toHexString(calculatedChecksum)+"], read ["+Utils.toHexString(receivedChecksum)+"]");
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
	}

/*	
	public static void main(String[] args) {
		
		String string = "2010519632";
		String string2 = "00130469000100000248FF02040039010490001200";

		int checksum=0;
		
		byte[] data = string.getBytes();
		for (int i=0;i<data.length;i++) {
			checksum+=Utils.toInt(data[i]);
		}
		
		
		
		byte[] data2 = string2.getBytes();
		for (int i=0;i<data2.length;i+=2) {
			int temp = Integer.parseInt(new String(new byte[]{data2[i],data2[i+1]}),16);
			checksum+=temp;
		}
		
		System.out.println(Utils.toHexString(checksum));
	}
*/	
	
}
