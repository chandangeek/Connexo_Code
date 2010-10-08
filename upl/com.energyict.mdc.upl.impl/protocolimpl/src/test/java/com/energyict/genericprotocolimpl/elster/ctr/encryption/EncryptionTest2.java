package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:36:31
 */
public class EncryptionTest2 extends TestCase {

    @Test
    public void test() throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {

        byte[] bytes = "Single block msg".getBytes();
        byte[] iv = new byte[]{0,0,0,0x30,0,0,0,0,0,0,0,0,0,0,0,0x01};
        byte[] keyc = ProtocolTools.getBytesFromHexString("$AE$68$52$F8$12$10$67$CC$4B$F7$A5$76$55$77$F3$9E");
        byte[] result = encryptAES128(bytes, iv, keyc);
        byte[] resultExpected = ProtocolTools.getBytesFromHexString("$E4$09$5D$4F$B7$A7$B3$79$2D$61$75$A3$26$13$11$B8");
        byte[] decrypted = decryptAES128(result, iv, keyc);

        assertArrayEquals(result, resultExpected);
        assertEquals(new String(decrypted), "Single block msg");
    }

    private byte[] encryptAES128(byte[] input, byte[] iv, byte[] keyC) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aeskey, paramSpec);
        return cipher.doFinal(input);
    }


    private byte[] decryptAES128(byte[] input, byte[] iv, byte[] keyC) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aeskey, paramSpec);
        return cipher.doFinal(input);
    }

}
