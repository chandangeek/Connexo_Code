package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class Encryption {
	
	
	/**
	 * The 16 byte encryptionkey 
	 */
	final private byte[] wavenisEncryptionKey;	

	final private Logger logger;
	
	public Encryption(byte[] wavenisEncryptionKey, Logger logger) {
		this.wavenisEncryptionKey = wavenisEncryptionKey;
		this.logger=logger;
	}

	
	static private byte[] generateTemporaryKeyFromRadioAddress(byte[] address) throws IOException {
		
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
	
	static public byte[] generateEncryptedKey(byte[] radioAddress) throws IOException {
		
		
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
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}
	
	
	// only for testing
	private void start() {
		try {
			//String radioAddress="106E4AC0000C";
			//System.out.println(ProtocolUtils.outputHexString(generateEncryptedKey(WaveflowProtocolUtils.getArrayFromStringHexNotation(radioAddress))));
			//System.out.println(ProtocolUtils.outputHexString(encrypt(calculateTemporaryKey(radioAddress), initialKey(radioAddress))));
			byte[] encryptedData = encrypt(new byte[]{0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F});
			System.out.println("Encrypted: "+ProtocolUtils.outputHexString(encryptedData));
			System.out.println("Decrypted: "+ProtocolUtils.outputHexString(decrypt(encryptedData)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		//String radioAddress="106E4AC0000C";
		String radioAddress="0E6E4BC00045";
		Encryption o;
		try {
			o = new Encryption(Encryption.generateEncryptedKey(WaveflowProtocolUtils.getArrayFromStringHexNotation(radioAddress)),Logger.getAnonymousLogger());
			//o = new Encryption(new byte[]{0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F},Logger.getAnonymousLogger());
//			System.out.println(ProtocolUtils.outputHexString(o.decrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation("AE487317766779142DF3B7C0ED007A7436A6ECA6839D02362F1E521EA125DDC2"))));
			//System.out.println(ProtocolUtils.outputHexString(o.decrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation("A1FFAC26652947B5F6D4ECBDA0D2FAC0A0E66CED4ACE898A4F8B169F9759D26E2DA2249A2840E02DC866F57E50FFAF38BC87E8FA6A6F012EA28079C61B32A8B71C2D5119E0E0D718456B97621658680C4D2DE5A875A1B4EC11A0E21E7133B3DF7DBCE104CA872966D3405E485A174A07BEBB68E3042441D27C10C23372AA17A2894F1B90AC653B86D8458780134E1A5D"))));
			System.out.println(ProtocolUtils.outputHexString(o.decrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation("8F6DCA32E88F469AE3BD0DFEFBAC1973C493905FC11FD382255B021F0950B1BE"))));
			System.out.println(ProtocolUtils.outputHexString(o.decrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation("31021A187368F3EFA760BCE687B3617147C2270DAAA5F3910EA0435D33EDA69D"))));
			
			//o.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
