package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.model.EncryptBaseException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class SymmetricAlgorithmTest {

    @Rule
    public ExpectedException exp = ExpectedException.none();

    @Test
    public void testFromNull() throws EncryptBaseException {
        exp.expect(EncryptBaseException.class);
        exp.expectMessage("Cowardly refusing to build symmetric algorithm from null or empty string");
        SymmetricAlgorithm.from(null);
    }

    @Test
    public void testFromWrongFormat() throws EncryptBaseException {
        String str = "AES";
        exp.expect(EncryptBaseException.class);
        exp.expectMessage("Cowardly refusing to create symmetric algorithm with wrong input format:" + str + ". When expecting Cipher format (e.g: AES/NOPADDING/NoPadding)");
        SymmetricAlgorithm.from(str);
    }


    @Test
    public void fromAES() throws EncryptBaseException {
        Assert.assertEquals(SymmetricAlgorithm.AES, SymmetricAlgorithm.from("AES/NOPADDING/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.AES, SymmetricAlgorithm.from("AES/CCB/PKCS5Padding"));
        Assert.assertEquals(SymmetricAlgorithm.AES, SymmetricAlgorithm.from("AES/PKCS5/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.AES, SymmetricAlgorithm.from("AES/PKCS5/PKCS5Padding"));
    }

    @Test
    public void fromDES() throws EncryptBaseException {
        Assert.assertEquals(SymmetricAlgorithm.DES, SymmetricAlgorithm.from("DES/NOPADDING/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.DES, SymmetricAlgorithm.from("DES/NOPADDING/PKCS5Padding"));
        Assert.assertEquals(SymmetricAlgorithm.DES, SymmetricAlgorithm.from("DES/PKCS5/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.DES, SymmetricAlgorithm.from("DES/PKCS5/PKCS5Padding"));
    }

    @Test
    public void fromDESede() throws EncryptBaseException {
        Assert.assertEquals(SymmetricAlgorithm.DESede, SymmetricAlgorithm.from("DESede/NOPADDING/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.DESede, SymmetricAlgorithm.from("DESede/NOPADDING/PKCS5Padding"));
        Assert.assertEquals(SymmetricAlgorithm.DESede, SymmetricAlgorithm.from("DESede/PKCS5/NoPadding"));
        Assert.assertEquals(SymmetricAlgorithm.DESede, SymmetricAlgorithm.from("DESede/PKCS5/PKCS5Padding"));
    }

    @Test
    public void fromRSA() throws EncryptBaseException {
        Assert.assertEquals(SymmetricAlgorithm.RSA, SymmetricAlgorithm.from("RSA/PKCS5/PKCS1Padding"));
        Assert.assertEquals(SymmetricAlgorithm.RSA, SymmetricAlgorithm.from("RSA/PKCS5/OAEPWithSHA-1AndMGF1Padding"));
        Assert.assertEquals(SymmetricAlgorithm.RSA, SymmetricAlgorithm.from("RSA/PKCS5/OAEPWithSHA-256AndMGF1Padding"));
    }

}
