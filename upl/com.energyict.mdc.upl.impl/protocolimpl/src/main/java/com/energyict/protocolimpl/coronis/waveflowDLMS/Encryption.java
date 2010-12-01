package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;

public class Encryption {
	
	private byte[] radioAddressAsArray(String radioAddressStr) {
		
		byte[] radioAddress = new byte[6];
		for (int i=0;i<6;i++) {
			radioAddress[i] = (byte)Integer.parseInt(radioAddressStr.substring(i*2, (i*2)+2),16);
		}
		return radioAddress;
	}
	
	private byte[] calculateTemporaryKey(String radioAddressStr) {
		
		byte[] radioAdress =  radioAddressAsArray(radioAddressStr);
		
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
	
	private byte[] initialKey(String radioAddressStr) {
		
		byte[] tempKey = calculateTemporaryKey(radioAddressStr);
		AES128 o = new AES128(tempKey);
		byte[] buffer2Encrypt = new byte[16];
		System.arraycopy(radioAddressAsArray(radioAddressStr), 0, buffer2Encrypt, 0, 6);
		for(int i=6;i<16;i++) {
			buffer2Encrypt[i]=0x2F;
		}
		byte[] initialKeyFull = o.encrypt(buffer2Encrypt);
		byte[] initialKey = new byte[16];
		System.arraycopy(initialKeyFull, 0, initialKey, 0, initialKey.length);
		return initialKey;
	}
	
	private void start() {
		
		AES128 o = new AES128(new byte[]{0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F});
		byte[] rawData = new byte[]{0x11,0x01,0x01,0x01,0x01,0x01,0x01,0x00,0x01,0x01,0x00,0x60,0x01,0x00,(byte)0xFF,0x02,0x00,0x00};
		int val = CRCGenerator.calcHDLCCRC(rawData);
		System.out.println(Integer.toHexString(val));
		
		byte[] encrypted = o.encrypt(new byte[]{0x11,0x01,0x01,0x01,0x01,0x01,0x01,0x00,0x01,0x01,0x00,0x60,0x01,0x00,(byte)0xFF,0x02,0x00,0x00,0x0C,(byte)0xBD,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F,0x2F});
		System.out.println("Encrypted data: "+ProtocolUtils.outputHexString(encrypted));
		
		byte[] data = o.decrypt(encrypted);
		System.out.println("Data :"+ProtocolUtils.outputHexString(data));
		
		System.out.println("Encrypted temporary key:"+ProtocolUtils.outputHexString(initialKey("010203040506")));
	}
	
	
	public static void main(String[] args) {
		Encryption o = new Encryption();
		o.start();
	}
}
