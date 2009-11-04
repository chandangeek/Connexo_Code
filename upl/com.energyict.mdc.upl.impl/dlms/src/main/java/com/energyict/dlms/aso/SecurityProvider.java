package com.energyict.dlms.aso;

import java.io.IOException;

/**
 * 
 * The securityProvider is responsible for providing all possible keys.
 * 
 * @author gna
 * 
 * @change
 * 3th nov. 2009 - Added the getNEWLLSSecret method
 */
public interface SecurityProvider {

	/**
	 * This carries the challenge for the HLS authentication,
	 * for example a random number is used
	 * @throws IOException
	 */
	byte[] getCallingAuthenticationValue() throws IOException;

	/**
	 * A global key which is held by the AssociotionLN/SN object.
	 * @throws IOException
	 */
	byte[] getHLSSecret() throws IOException;

	/**
	 * @return the NEW HLSSecret for the KeyChange functionality
	 * @throws IOException
	 */
	byte[] getNEWHLSSecret() throws IOException;
	
	/**
	 * @return the NEW LLSSecret for the KeyChange functionality
	 * @throws IOException
	 */
	byte[] getNEWLLSSecret() throws IOException;
	
	/**
	 * A global key is a ciphering key that may be used to cipher xDLMS APDU's,
	 * exchanged between the same client and server, in more than one session.
	 * @throws IOException
	 */
	byte[] getGlobalKey() throws IOException;

	/**
	 * @return the new GlobalKey for the KeyChange functionality
	 * @throws IOException
	 */
	byte[] getNEWGlobalKey() throws IOException;

	/**
	 * A dedicated key is a ciphering key that is delivered during AA establishment and that may be used in subsequent
	 * transmissions to cipher xDLMS APDU's, exchanged between the same client and server, within the same AA.
	 * The lifetime of the dedicated key is the same as the lifetime of the AA. The dedicated key can be seen as a session key.
	 * @throws IOException
	 */
	byte[] getDedicatedKey() throws IOException;

	/**
	 * A global key used for additional security in the GMC/GMAC encryption
	 * NOTE: this can be the same as the original globalKey
	 * @throws IOException
	 */
	byte[] getAuthenticationKey() throws IOException;

	/**
	 * @return the new authentication key for the KeyChange functionality
	 * @throws IOException
	 */
	byte[] getNEWAuthenticationKey() throws IOException;

	/**
	 * A master key shall be present in each COSEM server logical device configured in the system.
	 * This key is used for wrapping global keys. The MasterKey should not be transfered during a session.
	 * @throws IOException
	 */
	byte[] getMasterKey() throws IOException;

	//	/**
	//	 * @param plainText - the text to encrypt
	//	 * @return the cipherdText
	//	 */
	//	byte[] encrypt(byte[] plainText) throws IOException;
	//
	//	/**
	//	 * @param cipherdText - the encrypted text
	//	 * @return the plainText
	//	 */
	//	byte[] decrypt(byte[] cipherdText) throws IOException;
}
