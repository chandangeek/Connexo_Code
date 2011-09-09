package com.energyict.dlms.aso;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.SecurityLevelException;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

/**
 * Default implementation of the securityProvider.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification
 *
 * @author gna
 *
 */
public class LocalSecurityProvider implements SecurityProvider {

	private int securityLevel;
	private byte[] cTOs;
	private byte[] authenticationPassword;
	private byte[] dataTransportPassword;
	private byte[] dedicatedKey;
	private byte[] masterKey;
	private String hlsSecret;
	private Properties properties;

	/** Property name of the new AuthenticationKey */
	public static final String NEW_AUTHENTICATION_KEY = "NewAuthenticationKey";
	/** Property name of the new Global encryption Key */
	public static final String NEW_GLOBAL_KEY = "NewGlobalKey";
	/** Property name of the new HighLevel security Secret */
	public static final String NEW_HLS_SECRET = "NewHLSSecret";
	/** Property name of the DataTransport EncryptionKey */
	public static final String DATATRANSPORTKEY = "DataTransportKey";
	/** Property name of the Master key, or KeyEncryptionKey */
	public static final String MASTERKEY = "MasterKey";
	/** Property name of the DataTransport AuthenticationKey */
	public static final String DATATRANSPORT_AUTHENTICATIONKEY = "DataTransportAuthenticationKey";
	/** Property name of the new LowLevel security Secret */
	public static final String NEW_LLS_SECRET = "NewLLSSecret";

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
		this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD,"");
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

	/**
	 * The global key or encryption key is a custom property of the rtu
	 */
	public byte[] getGlobalKey() {
		return this.dataTransportPassword;
	}

	/**
	 * The HLSSecret is the password of the RTU
	 * @return the password of the RTU
	 */
	public byte[] getHLSSecret() {
		byte[] byteWord = new byte[this.hlsSecret.length()];
		for(int i = 0; i < this.hlsSecret.length(); i++){
			byteWord[i] = (byte)this.hlsSecret.charAt(i);
		}
		return byteWord;
	}

	/**
	 * The LLSSecret is the same as the HLSSecret
	 * @return the password of the RTU
	 */
	public byte[] getLLSSecret(){
		return getHLSSecret();
	}

	/**
	 * @return the master key (this is the KeyEncryptionKey)
	 */
	public byte[] getMasterKey() throws IOException {
		return this.masterKey;
	}

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     */
    public byte[] associationEncryptionByManufacturer(final byte[] respondingAuthenticationValue) throws IOException {
        throw new IOException("High level security 2 is not supported.");
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        Random generator = new Random();
        return generator.nextLong();
    }

    //********** Return new keys for KeyChange functionality **********/

	/**
	 * @return the new data encryption Authentication Key
	 */
	public byte[] getNEWAuthenticationKey() throws IOException {
		if(this.properties.containsKey(NEW_AUTHENTICATION_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_AUTHENTICATION_KEY));
		}
		throw new IllegalArgumentException("New authenticationKey is not correctly filled in.");
	}

	/**
	 * @return the new Global Key
	 */
	public byte[] getNEWGlobalKey() throws IOException {
		if(this.properties.containsKey(NEW_GLOBAL_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_GLOBAL_KEY));
		}
		throw new IllegalArgumentException("New globalKey is not correctly filled in.");
	}

	/**
	 * @return the new HLS secret
	 */
	public byte[] getNEWHLSSecret() throws IOException {
		if(this.properties.containsKey(NEW_HLS_SECRET)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_HLS_SECRET));
		}
		throw new IllegalArgumentException("New HLSSecret is not correctly filled in.");
	}

	/**
	 * @return the new LLS secret
	 * @return
	 * @throws IOException
	 */
	public byte[] getNEWLLSSecret() throws IOException {
		if(this.properties.containsKey(NEW_LLS_SECRET)){
			String newLlsSecret = this.properties.getProperty(NEW_LLS_SECRET);
			byte[] byteWord = new byte[newLlsSecret.length()];
			for(int i = 0; i < newLlsSecret.length(); i++){
				byteWord[i] = (byte)newLlsSecret.charAt(i);
			}
			return byteWord;
		}
		throw new IllegalArgumentException("New LLSSecret is not correctly filled in.");
	}

	public byte[] getDedicatedKey() {
		if (dedicatedKey == null) {
			dedicatedKey = new byte[16];
			Random rnd = new Random();
			rnd.nextBytes(dedicatedKey);
		}
		return dedicatedKey;
	}

}
