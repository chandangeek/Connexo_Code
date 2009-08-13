package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.SecurityLevelException;

/**
 * Provides all the securityKeys, just for local purpose
 * @author gna
 *
 */
public class LocalSecurityProvider implements SecurityProvider {
	
	private int securityLevel;
	private byte[] cTOs;
	private byte[] authenticationPassword;
	private byte[] dataTransportPassword;
	private byte[] masterKey;
	private String hlsSecret;
	private Properties properties;
	
	/** Property name of the new AutenticationKey */
	private static String NEW_AUTHENTICATION_KEY = "NewAuthenticationKey";
	/** Property name of the new Global encryption Key */
	private static String NEW_GLOBAL_KEY = "NewGlobalKey";
	/** Property name of the new HighLevel security Secret */
	private static String NEW_HLS_SECRET = "NewHLSSecret";
	/** Property name of the DataTransport EncryptionKey */
	private static String DATATRANSPORTKEY = "DataTransportKey";
	/** Property name of the Master key, or KeyEncryptionKey */
	private static String MASTERKEY = "MasterKey";
	/** Property name of the DataTransport AuthenticationKey */
	private static String DATATRANSPORT_AUTHENTICATIONKEY = "DataTransportAuthenticationKey";
	
	/**
	 * Create a new instance of LocalSecurityProvider
	 * @param properties - contains the keys for the authentication/encryption
	 */
	public LocalSecurityProvider(Properties properties){
		this.properties = properties;
		String sl = properties.getProperty("SecurityLevel", "0");
		if(sl.indexOf(":") != -1){
			this.securityLevel = Integer.parseInt(sl.substring(0, sl.indexOf(":")));
		} else {
			this.securityLevel = Integer.parseInt(sl);
		}
		this.dataTransportPassword = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORTKEY, ""));
		this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty(MASTERKEY, ""));
		this.authenticationPassword = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_AUTHENTICATIONKEY,""));
		this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD);
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

	/**
	 * Return the dataTransprot authenticationKey
	 */
	public byte[] getAuthenticationKey() {
		return this.authenticationPassword;
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

	/**
	 * The global key or encryption key is a custom property of the rtu
	 */
	public byte[] getGlobalKey() {
		return this.dataTransportPassword;
	}

	/**
	 * The HLSSecret is the password of the RTU
	 */
	public byte[] getHLSSecret() {
		byte[] byteWord = new byte[this.hlsSecret.length()];
		for(int i = 0; i < this.hlsSecret.length(); i++){
			byteWord[i] = (byte)this.hlsSecret.charAt(i);
		}
		return byteWord;
	}

	public byte[] getMasterKey() throws IOException {
		return this.masterKey;
	}

	//********** Return new keys for KeyChange functionality **********/
	
	public byte[] getNEWAuthenticationKey() throws IOException {
		if(this.properties.containsKey(NEW_AUTHENTICATION_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_AUTHENTICATION_KEY));
		}
		throw new IllegalArgumentException("New authenticationKey is not correctly filled in.");
	}

	public byte[] getNEWGlobalKey() throws IOException {
		if(this.properties.containsKey(NEW_GLOBAL_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_GLOBAL_KEY));
		}
		throw new IllegalArgumentException("New globalKey is not correctly filled in.");
	}

	public byte[] getNEWHLSSecret() throws IOException {
		if(this.properties.containsKey(NEW_HLS_SECRET)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_HLS_SECRET));
		}
		throw new IllegalArgumentException("New HLSSecret is not correctly filled in.");
	}

}
