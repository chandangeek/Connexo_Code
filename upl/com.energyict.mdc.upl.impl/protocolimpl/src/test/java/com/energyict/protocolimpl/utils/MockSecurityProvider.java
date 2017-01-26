package com.energyict.protocolimpl.utils;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.dlms.aso.framecounter.RespondingFrameCounterHandler;
import com.energyict.mdc.upl.UnsupportedException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MockSecurityProvider implements SecurityProvider{

	private String algorithm;
	private String[] possibleAlgorithms = new String[]{"","","","MD5","SHA-1","GMAC"};
	private byte[] cTOs;
	private byte[] authenticationKey;
	private byte[] dedicatedKey;
	private byte[] globalKey;
	private byte[] hlsSecret;
	private byte[] callingAuthenticationValue;
    private RespondingFrameCounterHandler respondingFrameCounterHandler = new DefaultRespondingFrameCounterHandler();

	public MockSecurityProvider(){

	}

    /**
     * Build up a stringbuffer containing the hex values from the byteArray.
     * Adds zero to the left if necessary.
     * ex:
     * b = {7, 1, 67, 7};
     * strByff.toString() = "07014307";
     * @param b - the byteArray containing the ascii chars
     * @return
     */
    public static String decimalByteToString(byte[] b){
		StringBuffer strBuff = new StringBuffer();
		for(int i = 0; i < b.length; i++){
			String str = Integer.toHexString(b[i]&0xFF);
			if(str.length() == 1) {
				strBuff.append("0");
			}
			strBuff.append(str);
		}
		return strBuff.toString();
    }

	public static void main(String args[]){
		try {
			MockSecurityProvider dsp = new MockSecurityProvider();
			dsp.setAlgorithm("SHA-1");
			byte[] plainText = ProtocolUtils.concatByteArrays(DLMSUtils.hexStringToByteArray("0102030405060708"), dsp.getHLSSecret());
			byte[] cipherText = dsp.encrypt(plainText);

			// Too bad, doesn't take leading zero's into account
//			BigInteger bi = new BigInteger(1, cipherText);
//			System.out.println(bi.toString(16));

			System.out.println(decimalByteToString(cipherText));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] getAuthenticationKey() {
		return this.authenticationKey;
	}

	public void setAuthenticationKey(byte[] ak){
		this.authenticationKey = ak;
	}

	/**
	 * Getter for the CallingAuthenticationValue (the challenge from the server/meter)
	 * @return the CallingAuthenticationValue
	 */
	public byte[] getCallingAuthenticationValue() throws UnsupportedException {
		return this.callingAuthenticationValue;
	}

	/**
	 * Setter for the callingAuthenticationValue
	 * @param callingAuthenticationValue
	 */
	public void setCallingAuthenticationValue(byte[] callingAuthenticationValue) {
		this.callingAuthenticationValue = callingAuthenticationValue;
	}

	public byte[] getDedicatedKey() {
		return this.dedicatedKey;
	}

	public void setDedicatedKey(byte[] dk){
		this.dedicatedKey = dk;
	}

	public byte[] getGlobalKey() {
		return this.globalKey;
	}

	public void setGlobalkey(byte[] gk){
		this.globalKey = gk;
	}

	/**
	 * Getter for the HLSSecret
	 * @return the HLSSecret
	 */
	public byte[] getHLSSecret() {
		if(hlsSecret == null){
			return new byte[]{(byte)0xFF,(byte)0x00,(byte)0xEE,(byte)0x11,(byte)0xDD,(byte)0x22,(byte)0xCC,(byte)0x33};
		} else{
			return hlsSecret;
		}
	}

	/**
	 * Setter of the HLS secret (as a byteArray)
	 * @param hls - the HLS Secret as a byteArray
	 */
	public void setHLSSecretByteArray(byte[] hls){
		this.hlsSecret = hls;
	}

	/**
	 * Setter for the HLS secret (as a string)
	 * @param password - the HLS Secret as a String (plaintext)
	 */
	public void setHLSSecretString(String password){
		this.hlsSecret = new byte[password.length()];
		for(int i = 0; i < password.length(); i++){
			hlsSecret[i] = (byte)password.charAt(i);
		}
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String[] getPossibleAlgorithms() {
		return possibleAlgorithms;
	}

	public void setPossibleAlgorithms(String[] possibleAlgorithms) {
		this.possibleAlgorithms = possibleAlgorithms;
	}

	public byte[] getCTOs() {
		return cTOs;
	}

	public void setCTOs(byte[] os) {
		cTOs = os;
	}

	private byte[] encrypt(byte[] plainText) throws IOException {
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

	public byte[] getMasterKey() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
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
        SecureRandom generator = new SecureRandom();
        return generator.nextLong();
    }

    @Override
    public void setInitialFrameCounter(long initialFrameCounter) {
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
    }

    @Override
    public void changeAuthenticationKey(byte[] newAuthenticationKey) throws IOException {
    }

}