package com.energyict.genericprotocolimpl.nta.abstractnta;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
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
public class NTASecurityProvider implements SecurityProvider {

	protected int securityLevel;
	protected byte[] cTOs;
	protected byte[] authenticationKey;
	protected byte[] encryptionKey;
	protected byte[] dedicatedKey;
	protected byte[] masterKey;
	protected String hlsSecret;
	protected Properties properties;

	/** Property name of the new AuthenticationKey */
	public static final String NEW_DATATRANSPORT_AUTHENTICATION_KEY = "NewDataTransportAuthenticationKey";
	/** Property name of the new Global encryption Key */
	public static final String NEW_DATATRANSPORT_ENCRYPTION_KEY = "NewDataTransportEncryptionKey";
	/** Property name of the new HighLevel security Secret */
	public static final String NEW_HLS_SECRET = "NewHLSSecret";
	/** Property name of the DataTransport EncryptionKey */
	public static final String DATATRANSPORT_ENCRYPTIONKEY = "DataTransportEncryptionKey";
	/** Property name of the Master key, or KeyEncryptionKey */
	public static final String MASTERKEY = "MasterKey";
	/** Property name of the DataTransport AuthenticationKey */
	public static final String DATATRANSPORT_AUTHENTICATIONKEY = "DataTransportAuthenticationKey";
	/** Property name of the new LowLevel security Secret */
	public static final String NEW_LLS_SECRET = "NewLLSSecret";

    Long initialFrameCounter;

	/**
	 * Create a new instance of LocalSecurityProvider
	 * @param properties - contains the keys for the authentication/encryption
	 */
	public NTASecurityProvider(Properties properties){
		this.properties = properties;
		String sl = properties.getProperty("SecurityLevel", "0");
		if(sl.indexOf(":") != -1){
			this.securityLevel = Integer.parseInt(sl.substring(0, sl.indexOf(":")));
		} else {
			this.securityLevel = Integer.parseInt(sl);
		}
		this.encryptionKey = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_ENCRYPTIONKEY, ""));
		this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty(MASTERKEY, ""));
		this.authenticationKey = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_AUTHENTICATIONKEY,""));
		this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD,"");
	}

	/**
	 * Generate a random challenge of 8 bytes long
	 */
	protected void generateClientToServerChallenge(){
		if(this.cTOs == null){
			Random generator = new Random();
			this.cTOs = new byte[16];
			generator.nextBytes(this.cTOs);
		}
	}

	/**
	 * Return the dataTransprot authenticationKey
	 */
	public byte[] getAuthenticationKey() {
		return this.authenticationKey;
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
		return this.encryptionKey;
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
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
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
        if(initialFrameCounter != null){
            return initialFrameCounter;
        } else {
            Random generator = new Random();
            return generator.nextLong();
        }
    }

    public void setInitialFrameCounter(long frameCounter){
        this.initialFrameCounter = frameCounter;
    }

    //********** Return new keys for KeyChange functionality **********/

	/**
	 * @return the new data encryption Authentication Key
	 */
	public byte[] getNEWAuthenticationKey() throws IOException {
		if(this.properties.containsKey(NEW_DATATRANSPORT_AUTHENTICATION_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_DATATRANSPORT_AUTHENTICATION_KEY));
		}
		throw new IllegalArgumentException("New authenticationKey is not correctly filled in.");
	}

	/**
	 * @return the new encryption Key
	 */
	public byte[] getNEWGlobalKey() throws IOException {
		if(this.properties.containsKey(NEW_DATATRANSPORT_ENCRYPTION_KEY)){
			return DLMSUtils.hexStringToByteArray(this.properties.getProperty(NEW_DATATRANSPORT_ENCRYPTION_KEY));
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
	 * @throws java.io.IOException
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
