package com.energyict.genericprotocolimpl.common;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocolimpl.base.SecurityLevelException;

public class LocalSecurityProvider implements SecurityProvider {
	
	private Rtu rtu;
	private int securityLevel;
	private String algorithm;
	private String[] possibleAlgorithms = new String[]{"","","","MD5","SHA-1","GMAC"};
	private byte[] cTOs;
	
	public LocalSecurityProvider(Rtu rtu){
		this.rtu = rtu;
		this.securityLevel = Integer.parseInt(rtu.getProperties().getProperty("SecurityLevel", "0"));
		this.algorithm = this.possibleAlgorithms[this.securityLevel];
	}
	
	/**
	 * Generate a random challenge of 8 bytes long
	 */
	private void generateClientToServerChallenge(){
		if(this.cTOs == null){
			Random generator = new Random();
			this.cTOs = new byte[8];
			generator.nextBytes(this.cTOs);
		}
	}

	public byte[] decrypt(byte[] cipherdText) {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] encrypt(byte[] plainText) throws IOException {
		try {
			byte[] digest;
			MessageDigest md = MessageDigest.getInstance(this.algorithm);
			md.reset();
			digest = md.digest(plainText);
			return digest;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("" + this.algorithm + " algorithm isn't a valid algorithm type." + e.getMessage());
		}
	}

	public byte[] getAuthenticationKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getCallingAuthenticationValue() throws SecurityLevelException {

		switch(this.securityLevel){
		case 0: return new byte[0];
		case 1: {
//			String password = this.rtu.getPassword();
//			byte[] byteWord = new byte[password.length()];
//			for(int i = 0; i < password.length(); i++){
//				byteWord[i] = (byte)password.charAt(i);
//			}
//			return byteWord;
			return getHLSSecret();
		}
		case 2: throw new SecurityLevelException("SecurityLevel 2 is not implemented.");
		case 3: {	// this is a ClientToServer challenge for MD5
			generateClientToServerChallenge();
			return this.cTOs;
		}
		case 4: {	// this is a ClientToServer challenge for SHA-1
			generateClientToServerChallenge();
			return this.cTOs;
		}
		case 5: {	// this is a ClientToServer challenge for GMAC
			generateClientToServerChallenge();
			return this.cTOs;
		}
		default: return new byte[0];
		}
	}

	public byte[] getDedicatedKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getGlobalKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSecurityLevel() {
		return this.securityLevel;
	}

	public byte[] getHLSSecret() {
		String password = this.rtu.getPassword();
		byte[] byteWord = new byte[password.length()];
		for(int i = 0; i < password.length(); i++){
			byteWord[i] = (byte)password.charAt(i);
		}
		return byteWord;
	}

	public byte[] getMasterKey() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
