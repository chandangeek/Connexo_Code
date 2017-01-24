package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;

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
    private Long initialFrameCounter;
    private RespondingFrameCounterHandler respondingFrameCounterHandler = new DefaultRespondingFrameCounterHandler();

	/** Property name of the DataTransport EncryptionKey */
	public static final String DATATRANSPORTKEY = "DataTransportKey";
	/** Property name of the Master key, or KeyEncryptionKey */
	public static final String MASTERKEY = "MasterKey";
	/** Property name of the DataTransport AuthenticationKey */
	public static final String DATATRANSPORT_AUTHENTICATIONKEY = "DataTransportAuthenticationKey";
	/** Property name of the new LowLevel security Secret */
	public static final String NEW_LLS_SECRET = "NewLLSSecret";
    /** Property name of the initial frame counter */
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";

    private static final Random RANDOM = new Random();

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
		this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD, "");
		this.initialFrameCounter = properties.getProperty(INITIAL_FRAME_COUNTER) != null ? Long.parseLong(properties.getProperty(INITIAL_FRAME_COUNTER)) : null;
	}

	/**
     * This constructor takes the defaults of the DlmsSessionProperties class in account.
     *
     * @param sessionProperties
     */
    public LocalSecurityProvider(DlmsSessionProperties sessionProperties) {
        this.properties = sessionProperties.getProtocolProperties();
        this.securityLevel = sessionProperties.getAuthenticationSecurityLevel();
        this.dataTransportPassword = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORTKEY, ""));
        this.masterKey = DLMSUtils.hexStringToByteArray(properties.getProperty(MASTERKEY, ""));
        this.authenticationPassword = DLMSUtils.hexStringToByteArray(properties.getProperty(DATATRANSPORT_AUTHENTICATIONKEY, ""));
        this.hlsSecret = properties.getProperty(MeterProtocol.PASSWORD, "");
        this.initialFrameCounter = properties.getProperty(INITIAL_FRAME_COUNTER) != null ? Long.parseLong(properties.getProperty(INITIAL_FRAME_COUNTER)) : null;
    }

    /**
	 * Generate a random challenge of 8 bytes long
	 */
	private void generateClientToServerChallenge(){
		if(this.cTOs == null){
			this.cTOs = new byte[8];
			RANDOM.nextBytes(this.cTOs);
		}
	}

	/**
	 * Return the dataTransprot authenticationKey
	 */
	public byte[] getAuthenticationKey() {
		return this.authenticationPassword;
	}

	public byte[] getCallingAuthenticationValue() throws UnsupportedException {

		switch(this.securityLevel){
		case 0: return new byte[0];
		case 1: {
			return getHLSSecret();
		}
		case 2: throw new UnsupportedException("SecurityLevel 2 is not implemented.");
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
	public byte[] getMasterKey() {
		return this.masterKey;
	}

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     */
    public byte[] associationEncryptionByManufacturer(final byte[] respondingAuthenticationValue) throws IOException {
        throw new UnsupportedException("High level security 2 is not supported.");
    }

    /**
     * @return the initial frameCounter
     */
    public long getInitialFrameCounter() {
        return initialFrameCounter != null ? initialFrameCounter : RANDOM.nextLong();
    }

    @Override
    public void setInitialFrameCounter(long initialFrameCounter) {
        this.initialFrameCounter = initialFrameCounter;
    }

    /**
     * Provide the handler for the receiving frameCounter
     *
     * @param respondingFrameCounterHandler the object which will handle the received frameCounter
     */
    public void setRespondingFrameCounterHandling(final RespondingFrameCounterHandler respondingFrameCounterHandler) {
        this.respondingFrameCounterHandler = respondingFrameCounterHandler;
    }

    /**
     * @return the used handler for the responding frameCounter
     */
    public RespondingFrameCounterHandler getRespondingFrameCounterHandler() {
        return this.respondingFrameCounterHandler;
    }

    @Override
    public void changeEncryptionKey(byte[] newEncryptionKey) throws IOException {
        this.dataTransportPassword = newEncryptionKey;
    }

    @Override
    public void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException {
        this.authenticationPassword = newAuthenticationKey;
    }

	public byte[] getDedicatedKey() {
		if (dedicatedKey == null) {
			dedicatedKey = new byte[16];
			RANDOM.nextBytes(dedicatedKey);
		}
		return dedicatedKey;
	}

}
