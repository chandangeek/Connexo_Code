package com.energyict.dlms.aso;

/**
 * 
 * @author gna
 *
 */
public interface SecurityProvider {

	public byte[] getCallingAuthenticationValue();
	public byte[] getGlobalKey();
	public byte[] getDedicatedKey();
	public byte[] getAuthenticationKey();

	/**
	 * @param plainText - the text to encrypt
	 * @return the cipherdText
	 */
	public byte[] encrypt(byte[] plainText);
	
	/**
	 * @param cipherdText - the encrypted text
	 * @return the plainText
	 */
	public byte[] decrypt(byte[] cipherdText);
}
