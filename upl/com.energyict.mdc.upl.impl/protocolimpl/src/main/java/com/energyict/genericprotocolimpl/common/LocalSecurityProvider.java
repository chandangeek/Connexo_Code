package com.energyict.genericprotocolimpl.common;

import java.io.IOException;
import java.util.Random;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.SecurityLevelException;

public class LocalSecurityProvider implements SecurityProvider {
	
	private int securityLevel;
	private String password;
	private byte[] cTOs;
	
	/**
	 * Create a new instance of LocalSecurityProvider
	 * @param authenticationLevel - depending on the level we provide a different callingAuthenticationKey
	 * @param password - this will be the HLSSecret with LowLevel Security
	 */
	public LocalSecurityProvider(int authenticationLevel, String password){
		this.password = password;
		this.securityLevel = authenticationLevel;
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

	public byte[] getAuthenticationKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getCallingAuthenticationValue() throws SecurityLevelException {

		switch(this.securityLevel){
		case 0: return new byte[0];
		case 1: {
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
		byte[] byteWord = new byte[this.password.length()];
		for(int i = 0; i < this.password.length(); i++){
			byteWord[i] = (byte)this.password.charAt(i);
		}
		return byteWord;
	}

	public byte[] getMasterKey() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
