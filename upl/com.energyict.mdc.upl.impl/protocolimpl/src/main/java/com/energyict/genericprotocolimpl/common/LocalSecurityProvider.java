package com.energyict.genericprotocolimpl.common;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.SecurityLevelException;

public class LocalSecurityProvider implements SecurityProvider {
	
	private int securityLevel;
	private byte[] cTOs;
	private String authenticationPassword;
	private byte[] dataTransportPassword;
	private byte[] masterKey;
	
	/**
	 * Create a new instance of LocalSecurityProvider
	 * @param properties - contains the keys for the authentication/encryption
	 */
	public LocalSecurityProvider(Properties properties){
		String sl = properties.getProperty("SecurityLevel", "0");
		if(sl.indexOf(":") != -1){
			this.securityLevel = Integer.parseInt(sl.substring(0, sl.indexOf(":")));
		} else {
			this.securityLevel = Integer.parseInt(sl);
		}
		this.dataTransportPassword = DLMSUtils.hexStringToByteArray(properties.getProperty("DataTransportKey", ""));
		this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty("MasterKey", ""));
		this.authenticationPassword = properties.getProperty(MeterProtocol.PASSWORD);
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
		byte[] byteWord = new byte[this.authenticationPassword.length()];
		for(int i = 0; i < this.authenticationPassword.length(); i++){
			byteWord[i] = (byte)this.authenticationPassword.charAt(i);
		}
		return byteWord;
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
		return this.dataTransportPassword;
	}

	public byte[] getHLSSecret() {
		byte[] byteWord = new byte[this.authenticationPassword.length()];
		for(int i = 0; i < this.authenticationPassword.length(); i++){
			byteWord[i] = (byte)this.authenticationPassword.charAt(i);
		}
		return byteWord;
	}

	public byte[] getMasterKey() throws IOException {
		return this.masterKey;
	}

}
