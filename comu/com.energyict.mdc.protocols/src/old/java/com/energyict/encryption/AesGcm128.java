package com.energyict.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of Galois/Counter mode of Operation(GCM)
 *
 * <pre>
 * Galois/Counter Mode (GCM) is an algorithm for authenticated encryption with associated data.
 * GCM is constructed from an approved symmetric key block cipher with a block size of 128 bits,
 * such as the Advanced Encryption Standard (AES) algorithm. Thus, GCM is a mode of operation of the AES algorithm.
 *
 * GCM provides assurance of the confidentiality of data using a variation of the Counter mode of operation for encryption.
 *
 * GCM provides assurance of the authenticity of the confidential data (up to about 64 gigabytes per invocation)
 * using a universal hash function that is defined over a binary Galois (i.e., finite) field.
 *
 * GCM can also provide authentication assurance for additional data (of practically unlimited length per invocation) that is not encrypted.
 * If the GCM input is restricted to data that is not to be encrypted, the resulting specialization of GCM, called GMAC,
 * is simply an authentication mode on the input data.
 *
 * GCM provides stronger authentication assurance than a (non-cryptographic) checksum or error detecting code; in particular, GCM can detect both
 * 	1) accidental modifications of the data and
 * 	2) intentional, unauthorized modifications.
 * </pre>
 *
 */
public class AesGcm128 {

	private BitVector key; // encryption key
	private BitVector iv;  // initialisation vector
	private BitVector p;   // plaintext
	private BitVector a;	// additional authenticated data
	private BitVector c;	// ciphertext
	private BitVector t;	// authenticationtag
	private BitVector h;	// hash

	private int tagSize = 16;	// The default tagSize for GCM is 16bit; NOTE: DLMS specifies a 12bit TagLength


	/**
	 * Creates a new instance of the AES Galois/Counter mode with an empty global encryption key
	 */
	public AesGcm128(){
		this.key = new BitVector(16);
		this.iv = new BitVector(16);
		this.h = aesEncrypt(new BitVector(16));
		this.p = new BitVector(0);
		this.a = new BitVector(0);
		this.c = new BitVector(p.length());
		this.t = new BitVector(16);
	}

	/**
	 * Creates a new instance of the AES Galois/Counter mode with a globalKey BitVector
	 * @param key - the global encryption Key
	 */
	public AesGcm128(BitVector key) {
		super();
		this.key = key;
		this.h = aesEncrypt(new BitVector(16));
		this.iv = new BitVector(0);
		this.p = new BitVector(0);
		this.a = new BitVector(0);
		this.c = new BitVector(0);
		this.t = new BitVector(0);
	}

	/**
	 * Creates a new instance of the AES Galois/Counter mode with a globalKey byteArray
	 * @param globalKey - the global encryption Key
	 * @param tagSize - the size of the authenticationTag
	 */
	public AesGcm128(byte[] globalKey, int tagSize){
		this(new BitVector(globalKey));
		this.tagSize = tagSize;
	}

	/**
	 * Encrypts a int[] according to AES128, using the key
	 * @return the aes encrypted int array
	 */
	public BitVector aesEncrypt(BitVector plain){
		BitVector result = new BitVector(plain.length());

		SecretKey skeySpec = new SecretKeySpec(key.getValue(), "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] aes = cipher.doFinal(plain.getValue());
			result.setValue(aes);


		} catch (InvalidKeyException e) {
			e.printStackTrace();
			if(e.getLocalizedMessage().indexOf("Invalid AES key length") != -1){
				throw new IllegalArgumentException("Invalid Global Key length, length should be 16 bytes.", e.getCause());
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Encrypts the plainText according to the Galois/Counter mode of Operation,
	 * using the provided input parameters.
	 */
	public void encrypt(){

		// y0 = iv||0000 0000 0000 0000 0000 0000 0000 0001
		int counter = 1;
		BitVector y0 = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
		int n = p.length()/16 + ((p.length()%16 > 0) ? 1 : 0);
		int u = p.length()%16;
		int m = a.length()/16 + ((a.length()%16 > 0) ? 1 : 0);
		int v = a.length()%16;

		c = new BitVector(p.length());

		for (int i = 0 ; i < n; i++){
			counter++;
			BitVector yi = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
			BitVector pi = p.get128Segment(i);
			BitVector ci = BitVector.addition(pi, aesEncrypt(yi));
			c.set128Segment(i, ci);
		}

		BitVector x = new BitVector(16);

		for (int i = 0; i < m; i++ ){
			BitVector ai = a.get128Segment(i);
			x = BitVector.multiplication(BitVector.addition(x, ai),h);
		}
		for (int i = 0; i < n; i++ ){
			BitVector ci = c.get128Segment(i);
			x = BitVector.multiplication(BitVector.addition(x, ci),h);
		}
		BitVector len = BitVector.concatenate(BitVector.convertFromInt(a.length()*8, 8),
				BitVector.convertFromInt(c.length()*8, 8));
		x = BitVector.multiplication(BitVector.addition(x, len),h);

		t = BitVector.addition(x, aesEncrypt(y0)).Msb2(this.tagSize);
	}

	/**
	 * Decrypts the cipherText. Will also check validity of the data.
	 * @return true if it's a valid encrypted frame, false otherwise
	 */
	public boolean decrypt(){
		int counter = 1;
		BitVector y0 = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
		int n = c.length()/16 + ((c.length()%16 > 0) ? 1 : 0);
		int u = c.length()%16;
		int m = a.length()/16 + ((a.length()%16 > 0) ? 1 : 0);
		int v = a.length()%16;

		BitVector x = new BitVector(16);

		for (int i = 0; i < m; i++ ){
			BitVector ai = a.get128Segment(i);
			x = BitVector.multiplication(BitVector.addition(x, ai),h);
		}
		for (int i = 0; i < n; i++ ){
			BitVector ci = c.get128Segment(i);
			x = BitVector.multiplication(BitVector.addition(x, ci),h);
		}
		BitVector len = BitVector.concatenate(BitVector.convertFromInt(a.length()*8, 8),
				BitVector.convertFromInt(c.length()*8, 8));
		x = BitVector.multiplication(BitVector.addition(x, len),h);

		BitVector t2 = BitVector.addition(x, aesEncrypt(y0)).Msb2(this.tagSize);

//		System.out.println(t2.toString());
//		System.out.println(t.toString());

		if ((t.getValue().length != 0) && (!t2.equals(t))) {
			return false;
		}

		p = new BitVector(c.length());

		for (int i = 0 ; i < n; i++){
			counter++;
			BitVector yi = BitVector.concatenate(iv, BitVector.convertFromInt(counter, 4));
			BitVector ci = c.get128Segment(i);
			BitVector pi = BitVector.addition(ci, aesEncrypt(yi));
			p.set128Segment(i, pi);
		}

		return true;
	}

	// Getters - Setters

	/**
	 * Getter for the global encryption key
	 * @return - the globalEncryptionKey
	 */
	public BitVector getKey() {
		return key;
	}

	/**
	 * Setter for the global encryption Key
	 * @param key - encryption key
	 */
	public void setGlobalKey(BitVector key) {
		this.key = key;
		this.h = aesEncrypt(new BitVector(16));
	}

	/**
	 * Getter for the initializationVector
	 * @return - the initializationVector
	 */
	public BitVector getInitializationVector() {
		return iv;
	}

	/**
	 * Setter for the initializationVector
	 * @param iv - initializationVector
	 */
	public void setInitializationVector(BitVector iv) {
		this.iv = iv;
	}

	/**
	 * Getter for the plainText
	 * @return the plainText
	 */
	public BitVector getPlainText() {
		return p;
	}

	/**
	 * Setter for the plainText
	 * @param p - the plainText
	 */
	public void setPlainText(BitVector p) {
		this.p = p;
	}

	/**
	 * Getter for the additional authenticationData.
	 * @return the additionalAuthenticationData
	 */
	public BitVector getAdditionalAuthenticationData() {
		return a;
	}

	/**
	 * Setter for the additional authenticationData
	 * @param a - the additionalAuthenticationData
	 */
	public void setAdditionalAuthenticationData(BitVector a) {
		this.a = a;
	}

	/**
	 * Getter for the cipherText
	 * @return the cipherText
	 */
	public BitVector getCipherText() {
		return c;
	}

	/**
	 * Setter for the cipherText
	 * @param c - the cipherText
	 */
	public void setCipherText(BitVector c) {
		this.c = c;
	}

	/**
	 * Getter for the authenticationTag
	 * @return the authenticationTag
	 */
	public BitVector getTag() {
		return t;
	}

	/**
	 * Setter for the authenticationTag
	 * @param t - the authenticationTag
	 */
	public void setTag(BitVector t) {
		this.t = t;
	}

	/**
	 * Setter for the size of the authenticationTag.
	 * Most common tagSizes are : 128, 120, 112, 104, or 96 bits
	 * Default the size is 128bits
	 * @param tagSize - the size of the tag.
	 */
	public void setTagSize(int tagSize){
		this.tagSize = tagSize;
	}

}
