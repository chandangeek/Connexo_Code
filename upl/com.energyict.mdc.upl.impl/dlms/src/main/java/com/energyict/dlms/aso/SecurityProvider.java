package com.energyict.dlms.aso;

import java.io.IOException;

/** SecurityProvider interface
 * 
 * The securityProvider is responsible for providing all possible keys.
 * 
 * @author gna
 *
 */
public interface SecurityProvider {
	
	/**
	 * This carries the challenge for the HLS authentication,
	 * for example a random number is used
	 * @throws IOException
	 */
	public byte[] getCallingAuthenticationValue() throws IOException;
	
	/**
	 * A global key which is held by the AssociotionLN/SN object.
	 * @throws IOException
	 */
	public byte[] getHLSSecret() throws IOException;
	
	/**
	 * A global key is a ciphering key that may be used to cipher xDLMS APDU's, 
	 * exchanged between the same client and server, in more than one session.
	 * @throws IOException
	 */
	public byte[] getGlobalKey() throws IOException;
	
	/**
	 * A dedicated key is a ciphering key that is delivered during AA establishment and that may be used in subsequent 
	 * transmissions to cipher xDLMS APDU's, exchanged between the same client and server, within the same AA.
	 * The lifetime of the dedicated key is the same as the lifetime of the AA. The dedicated key can be seen as a session key.
	 * @throws IOException
	 */
	public byte[] getDedicatedKey() throws IOException;
	
	/**
	 * A global key used for additional security in the GMC/GMAC encryption
	 * NOTE: this can be the same as the original globalKey
	 * @throws IOException
	 */
	public byte[] getAuthenticationKey() throws IOException;
	
	/**
	 * A master key shall be present in each COSEM server logical device configured in the system.
	 * This key is used for wrapping global keys
	 * @throws IOException
	 */
	public byte[] getMasterKey() throws IOException;

//	/**
//	 * @param plainText - the text to encrypt
//	 * @return the cipherdText
//	 */
//	public byte[] encrypt(byte[] plainText) throws IOException;
//	
//	/**
//	 * @param cipherdText - the encrypted text
//	 * @return the plainText
//	 */
//	public byte[] decrypt(byte[] cipherdText) throws IOException;
}
