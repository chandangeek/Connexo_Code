package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.model.EncryptBaseException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class SymmetricPaddingAlgorithmTest {

    @Rule
    public ExpectedException exp = ExpectedException.none();

    @Test
    public void testFromNull() throws EncryptBaseException {
        exp.expect(EncryptBaseException.class);
        exp.expectMessage("Cowardly refusing to build symmetric padding algorithm from null or empty string");
        SymmetricPaddingAlgorithm.from(null);
    }

    @Test
    public void testFromWrongFormat() throws EncryptBaseException {
        String str = "AES";
        exp.expect(EncryptBaseException.class);
        exp.expectMessage("Cowardly refusing to create symmetric padding algorithm with wrong input format:" + str + ". When expecting Cipher format (e.g: AES/NOPADDING/NoPadding)");
        SymmetricPaddingAlgorithm.from(str);
    }


    @Test
    public void fromNoPadding() throws EncryptBaseException {
        Assert.assertEquals(SymmetricPaddingAlgorithm.NOPADDING, SymmetricPaddingAlgorithm.from("AES/CBC/NoPadding"));
    }

    @Test
    public void fromPKCS5Padding () throws EncryptBaseException {
        Assert.assertEquals(SymmetricPaddingAlgorithm.PKCS5PADDING, SymmetricPaddingAlgorithm.from("AES/ECB/PKCS5Padding"));
    }


}
