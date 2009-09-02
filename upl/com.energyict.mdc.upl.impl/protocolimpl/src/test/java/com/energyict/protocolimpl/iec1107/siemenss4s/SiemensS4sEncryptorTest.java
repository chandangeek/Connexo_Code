package com.energyict.protocolimpl.iec1107.siemenss4s;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.energyict.protocolimpl.iec1107.siemenss4s.security.SiemensS4sEncryptor;

public class SiemensS4sEncryptorTest {

	@Test
	public void encryptTest(){
		SiemensS4sEncryptor s4sEncryptor = new SiemensS4sEncryptor();
		String passWord = "4281602592";
		String key = "3A20";
		
		String encryptedString = s4sEncryptor.encrypt(passWord, key);
		
		assertEquals(encryptedString, "238142CF9");
		
	}
}
