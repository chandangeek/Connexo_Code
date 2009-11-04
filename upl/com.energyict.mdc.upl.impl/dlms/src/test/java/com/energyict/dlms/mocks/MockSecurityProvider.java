package com.energyict.dlms.mocks;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocol.ProtocolUtils;

public class MockSecurityProvider implements SecurityProvider{
	
	private String algorithm;
	private String[] possibleAlgorithms = new String[]{"","","","MD5","SHA-1","GMAC"};
	private byte[] cTOs;
	private byte[] authenticationKey;
	private byte[] dedicatedKey;
	private byte[] globalKey;
	
	public MockSecurityProvider(){
		
	}

	public byte[] getAuthenticationKey() throws IOException {
		return this.authenticationKey;
	}
	public void setAuthenticationKey(byte[] ak){
		this.authenticationKey = ak;
	}

	public byte[] getCallingAuthenticationValue() throws IOException {
		return this.cTOs;
	}

	public byte[] getDedicatedKey() throws IOException {
		return this.dedicatedKey;
	}
	public void setDedicatedKey(byte[] dk){
		this.dedicatedKey = dk;
	}

	public byte[] getGlobalKey() throws IOException {
		return this.globalKey;
	}
	public void setGlobalkey(byte[] gk){
		this.globalKey = gk;
	}

	public byte[] getHLSSecret() throws IOException {
		return new byte[]{(byte)0xFF,(byte)0x00,(byte)0xEE,(byte)0x11,(byte)0xDD,(byte)0x22,(byte)0xCC,(byte)0x33};
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

	public byte[] getMasterKey() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getNEWAuthenticationKey() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getNEWGlobalKey() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getNEWHLSSecret() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getNEWLLSSecret() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
