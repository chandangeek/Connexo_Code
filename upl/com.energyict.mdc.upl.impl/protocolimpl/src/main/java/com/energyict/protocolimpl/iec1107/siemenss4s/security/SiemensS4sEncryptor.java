package com.energyict.protocolimpl.iec1107.siemenss4s.security;

import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 * 
 * Implements the security negotiation between client and server
 * @author gna
 * @since 21/08/2009
 */
public class SiemensS4sEncryptor implements Encryptor {
	
	private int securityLevel;	// contains the securityLevel for the securityNegotiation

	public SiemensS4sEncryptor(){
		this.securityLevel = 2;		// default value is 2
	}
	
	/**
	 * Encrypts the password along with the key(seed)
	 * The securityLevel is added to the left side
	 * @return the encrypted security-String
	 */
	public String encrypt(String passWord, String key) {
		int antiSeed = SecureAlgorithm.calculateAntiSeed(Integer.parseInt(key, 16));
		byte[] passBytes = S4sObjectUtils.hexStringToByteArray(Long.toHexString(Long.parseLong(passWord)));
		passBytes = S4sObjectUtils.revertByteArray(passBytes);
		int passInt = Integer.parseInt(outputHexString(passBytes), 16);
		int response = (passInt&0xFFFF)^antiSeed + ((passInt&0xFFFF0000)^(antiSeed<<16));
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(this.securityLevel);
		strBuff.append(Integer.toHexString(response));
		return strBuff.toString().toUpperCase();
	}
	
	/**
	 * Setter for the securityLevel
	 * @param level
	 */
	public void setSecurityLevel(int level){
		this.securityLevel = level;
	}
	
	/**
	 * Convert the hexByteArray to a hexaDecimal string
	 * @param data
	 * @return
	 */
    private static String outputHexString(byte[] data) {
        StringBuffer strBuff=new StringBuffer(); 
        for (int i=0;i<data.length;i++) {
            strBuff.append(outputHexString(data[i]&0xFF));
        }
        return strBuff.toString();
     }
    
    /**
     * It's actually a ProtocolUtils method, but without the '$' sign
     * @param bKar 
     * @return a hexConverted integer
     */
    private static String outputHexString(int bKar) {
    	StringBuffer strBuff = new StringBuffer();
    	strBuff.append(String.valueOf((char)ProtocolUtils.convertHexLSB(bKar)));
    	strBuff.append(String.valueOf((char)ProtocolUtils.convertHexMSB(bKar)));
        return strBuff.toString();
     }
}
