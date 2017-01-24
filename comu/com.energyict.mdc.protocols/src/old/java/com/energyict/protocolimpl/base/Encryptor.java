/*
 * Encryptor.java
 *
 * Created on 6 juli 2004, 18:23
 */

package com.energyict.protocolimpl.base;

/**
 * 
 * @author  Koen
 */
public interface Encryptor {
	
	/**
	 * Implement the encryption for the security negotiation
	 * @param passWord - the password of the RTU
	 * @param key - the seed received from the device, converted from byteArray to String
	 * @return
	 */
    String encrypt(String passWord, String key);
}
