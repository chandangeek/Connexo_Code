/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Encryption {

	/**
	 * The 16 byte encryptionkey
	 */
	private final byte[] wavenisEncryptionKey;

	private final Logger logger;

	public Encryption(byte[] wavenisEncryptionKey, Logger logger) {
		this.wavenisEncryptionKey = wavenisEncryptionKey;
		this.logger=logger;
	}


	private static byte[] generateTemporaryKeyFromRadioAddress(byte[] address) {

		byte[] radioAdress = new byte[6];
		// reverse
		for (int i=0;i<6;i++) {
			radioAdress[5-i]=address[i];
		}

		byte[] temporaryKey = new byte[16];

		temporaryKey[0] = (byte)(radioAdress[5] ^ radioAdress[4]);
		temporaryKey[1] = (byte)(radioAdress[4] ^ radioAdress[3]);
		temporaryKey[2] = (byte)(radioAdress[3] ^ radioAdress[2]);
		temporaryKey[3] = (byte)(radioAdress[2] ^ radioAdress[1]);
		temporaryKey[4] = (byte)(radioAdress[1] ^ radioAdress[0]);

		temporaryKey[5] = (byte)(temporaryKey[0] ^ temporaryKey[1]);
		temporaryKey[6] = (byte)(temporaryKey[1] ^ temporaryKey[2]);
		temporaryKey[7] = (byte)(temporaryKey[2] ^ temporaryKey[3]);
		temporaryKey[8] = (byte)(temporaryKey[3] ^ temporaryKey[4]);
		temporaryKey[9] = (byte)(temporaryKey[4] ^ temporaryKey[5]);
		temporaryKey[10] = (byte)(temporaryKey[5] ^ temporaryKey[6]);
		temporaryKey[11] = (byte)(temporaryKey[6] ^ temporaryKey[7]);
		temporaryKey[12] = (byte)(temporaryKey[7] ^ temporaryKey[8]);
		temporaryKey[13] = (byte)(temporaryKey[8] ^ temporaryKey[9]);
		temporaryKey[14] = (byte)(temporaryKey[9] ^ temporaryKey[10]);
		temporaryKey[15] = (byte)(temporaryKey[10] ^ temporaryKey[11]);

		return temporaryKey;
	}

	public static byte[] generateEncryptedKey(byte[] radioAddress) {


		byte[] tempKey = generateTemporaryKeyFromRadioAddress(radioAddress);

		AES128 o = new AES128(tempKey);
		byte[] buffer2Encrypt = new byte[16];
		System.arraycopy(radioAddress, 0, buffer2Encrypt, 0, 6);
		for(int i=6;i<16;i++) {
			buffer2Encrypt[i]=0x2F;
		}
		byte[] initialKeyFull = o.encrypt(buffer2Encrypt);
		byte[] initialKey = new byte[16];
		System.arraycopy(initialKeyFull, 0, initialKey, 0, initialKey.length);
		return initialKey;
	}

	private byte[] getPaddingArray(int length) {
		byte[] padding = new byte[length];
		for (int i=0;i<length;i++) {
			padding[i] = 0x2F;
		}
		return padding;
	}

	public byte[] encrypt(byte[] data) {
		return encrypt(data,null);
	}

	public byte[] encrypt(byte[] data, byte[] key) {

		if ((wavenisEncryptionKey == null) && (key == null)) {
			return data;
		}

		// prepare payload to encrypt
		byte[] rtc = TimeDateRTCParser.utcTimeFrame6();

		byte[] temp = ProtocolUtils.concatByteArrays(rtc, data);
		temp = ProtocolUtils.concatByteArrays(new byte[]{(byte)temp.length}, temp);
		int val = CRCGenerator.calcHDLCCRC(temp);
		temp = ProtocolUtils.concatByteArrays(temp,new byte[]{(byte)(val>>8),(byte)val});
		temp = ProtocolUtils.concatByteArrays(temp,getPaddingArray(16-(temp.length % 16)));

		AES128 aes128 = new AES128(key==null?wavenisEncryptionKey:key);
		byte[] encrypted = aes128.encrypt(temp);

		return encrypted;
	}


	/**
	 * Decrypt frame. The encrypted answer does not contain a crc nor RTC timestamp...
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public byte[] decrypt(final byte[] data) throws IOException {

		if (wavenisEncryptionKey == null) {
			return data;
		}

		AES128 aes128 = new AES128(wavenisEncryptionKey);
		byte[] decrypted = aes128.decrypt(data);
		if (decrypted == null) {
			throw new WaveFlowDLMSException("Encryption: decrypt(), error decrypted data is NULL..");
		}
		//System.out.println(ProtocolUtils.outputHexString(decrypted));
		byte[] temp;
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(decrypted));
			int length = WaveflowProtocolUtils.toInt(decrypted[0]);
			temp = new byte[length+1];
			dais.read(temp);
			byte[] decryptedData = ProtocolUtils.getSubArray(temp, 1);

			//System.out.println(ProtocolUtils.outputHexString(decryptedData));
			return decryptedData;
		}
		finally {
			if (dais != null) {
				dais.close();
			}
		}
	}

	 byte[] decryptFrame(byte[] data) throws IOException {

		 return decrypt(data);
	 }


	/**
	 * Loop over all frames and using low level multiframe sizes, decrypt the data
	 * @param data
	 * @return
	 * @throws IOException
	 */
	 byte[] decryptFrames(final byte[] data, int firstFrameLength, int nextFrameLength) throws IOException {

		if (wavenisEncryptionKey != null) {
			firstFrameLength = firstFrameLength + (16-firstFrameLength%16);
			nextFrameLength = nextFrameLength + (16-nextFrameLength%16);
		}

		byte[] temp;
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			int len = dais.available();

			if (len > firstFrameLength) {
				temp = new byte[firstFrameLength];
				dais.read(temp);
				baos.write(decrypt(temp));

				while(true) {
					int tag = WaveflowProtocolUtils.toInt(dais.readByte()); // skip B1
					int frameCounter = WaveflowProtocolUtils.toInt(dais.readByte()); // skip framecounter

					baos.write(new byte[]{(byte)tag,(byte)frameCounter});
					len = dais.available();
					if (len > nextFrameLength) {
						temp = new byte[nextFrameLength];
						dais.read(temp);
						baos.write(decrypt(temp));
					}
					else {
						temp = new byte[len];
						dais.read(temp);
						baos.write(decrypt(temp));
						break;
					}
				}
			}
			else {
				temp = new byte[len];
				dais.read(temp);
				baos.write(decrypt(temp));
			}


			return baos.toByteArray();

		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(ProtocolUtils.stack2string(e));
				}
			}
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					logger.severe(ProtocolUtils.stack2string(e));
				}
			}
		}

	}

}