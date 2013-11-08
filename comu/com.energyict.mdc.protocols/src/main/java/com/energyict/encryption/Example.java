package com.energyict.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Example {
	/**
	 * Turns array of bytes into string
	 * 
	 * @param buf
	 *            Array of bytes to convert to hex string
	 * @return Generated hex string
	 */
	public static String asHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	public static void main(String[] args) throws Exception {

		String message = "This is just an example";

		// Get the KeyGenerator

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128); // 192 and 256 bits may not be available

		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();

		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

		// Instantiate the cipher

		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		byte[] plain = "0000000000000000".getBytes();

		byte[] encrypted = cipher.doFinal(plain);
		System.out.println("length of encryption: " + (encrypted).length);
		System.out.println("encrypted string: " + asHex(encrypted));

		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] original = cipher.doFinal(encrypted);
		String originalString = new String(original);
		System.out.println("Original string: " + originalString + " "
				+ asHex(original));
	}
}
