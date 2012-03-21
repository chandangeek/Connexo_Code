package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.EAXPBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.CCMParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.SimpleTest;

import java.security.SecureRandom;

public class EAXPTest
    extends SimpleTest
{
	// Note: rather than using a separate nonce directly,
	// EAXP calculates a nonce from the associated data which MUST 
	// contain a changing nonce.  Because several supporting functions
	// expect an explicit nonce, and to minimize the changes required,
	// this dummy Nonce is used for that purpose only.
	private static final byte[] dummyNonce = {(byte)0};
	
    private byte[] K1 = Hex.decode("6624C7E23034E4036FE5CB3A8B5DAB44");
    private byte[] A1 = Hex.decode("A211060F2B060104018285638e7f85f1c24e01a80602042bc81aa1ac0fa20da00ba10980010081044b97d2ccbe392837813588a60906072b060104828563004b97d2cc");
    private byte[] P1 = Hex.decode("1751303030303030303030303030303030303030303000000330000103300078033000790330007a0330007b0330007d");
    private byte[] C1 = Hex.decode("BEB0989FADB020EB72BA46353CC0A2AC2A007A101AFEBAF9680D3B9659F991121B865F254F6AC92CDD213D31E3C4D2CAe6f89b6d");
    private byte[] T1 = Hex.decode("e6f89b6d");

    private byte[] K2 = Hex.decode("6624c7e23034e4036fe5cb3a8b5dab44");
    private byte[] A2 = Hex.decode("a20e060c6086480186fc2f811caa4e01a806020406ac6342ac0fa20da00ba10980010081044b9262b0be752873817184a60906072b060104828563004b9262b0"+"17513030303030303030303030303030303030303030000003300001033000280330002a0330002c0330002d0330002e0330002f03300078033000790330007a0330007b0330007d0330007e03300082033000830330008503300801033008280330082a0330082d03300853");
    private byte[] P2 = Hex.decode("");
    private byte[] C2 = Hex.decode("4f2df860");
    private byte[] T2 = Hex.decode("4f2df860");
    
    private byte[] A3 = Hex.decode("FA3BFD4806EB53FA");
    private byte[] C3 = Hex.decode("19DD5C4C9331049D0BDAB0277408F67967E5");

    private static final int MAC_LEN = 8;
    private static final int AUTHEN_LEN = 20;

    public String getName()
    {
        return "EAXP";
    }

    public void performTest()
        throws Exception
    {
        checkVectors(1, K1, 32, A1, P1, T1, C1);
        checkVectors(2, K2, 32, A2, P2, T2, C2);

        EAXPBlockCipher eaxp = new EAXPBlockCipher(new AESFastEngine());
        ivParamTest(1, eaxp, K1);

        //
        // exception tests
        //

        try
        {
            eaxp.init(false, new CCMParameters(new KeyParameter(K1), 32, dummyNonce, A3));

            byte[] enc = new byte[C3.length]; 
            int len = eaxp.processBytes(C3, 0, C3.length, enc, 0);

            len += eaxp.doFinal(enc, len);

            fail("invalid cipher text not picked up");
        }
        catch (InvalidCipherTextException e)
        {
            // expected
        }

        try
        {
            eaxp.init(false, new KeyParameter(K1));

            fail("illegal argument not picked up");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        randomTests();
    }

    private void checkVectors(
        int count,
        byte[] k,
        int macSize,
        byte[] a,
        byte[] p,
        byte[] t,
        byte[] c)
        throws InvalidCipherTextException
    {
        EAXPBlockCipher encEaxp = new EAXPBlockCipher(new AESFastEngine());
        EAXPBlockCipher decEaxp = new EAXPBlockCipher(new AESFastEngine());

        AEADParameters parameters = new AEADParameters(new KeyParameter(k), macSize, dummyNonce, a);
        encEaxp.init(true, parameters);
        decEaxp.init(false, parameters);

        runCheckVectors(count, encEaxp, decEaxp, p, t, c);
        runCheckVectors(count, encEaxp, decEaxp, p, t, c);
    }

    private void runCheckVectors(
        int count,
        EAXPBlockCipher encEaxp,
        EAXPBlockCipher decEaxp,
        byte[] p,
        byte[] t,
        byte[] c)
        throws InvalidCipherTextException
    {
        byte[] enc = new byte[c.length];

        int len = encEaxp.processBytes(p, 0, p.length, enc, 0);
        // macBlock is zero here
        len += encEaxp.doFinal(enc, len);

        if (!areEqual(c, enc))
        {
            fail("encrypted stream fails to match in test " + count);
        }

        byte[] tmp = new byte[enc.length];

        len = decEaxp.processBytes(enc, 0, enc.length, tmp, 0);
        // macBlock is zero here, too
        len += decEaxp.doFinal(tmp, len);

        byte[] dec = new byte[len];
        
        System.arraycopy(tmp, 0, dec, 0, len);

        if (!areEqual(p, dec))
        {
            fail("decrypted stream fails to match in test " + count);
        }

        if (!areEqual(t, decEaxp.getMac()))
        {
            fail("MAC fails to match in test " + count);
        }
    }

    private void ivParamTest(
        int count,
        AEADBlockCipher eaxp,
        byte[] k)
        throws InvalidCipherTextException
    {
        byte[] p = Strings.toByteArray("hello world!!");

        eaxp.init(true, new ParametersWithIV(new KeyParameter(k), dummyNonce));

        byte[] enc = new byte[p.length + 8];

        int len = eaxp.processBytes(p, 0, p.length, enc, 0);

        len += eaxp.doFinal(enc, len);

        eaxp.init(false, new ParametersWithIV(new KeyParameter(k), dummyNonce));

        byte[] tmp = new byte[enc.length];

        len = eaxp.processBytes(enc, 0, enc.length, tmp, 0);

        len += eaxp.doFinal(tmp, len);

        byte[] dec = new byte[len];

        System.arraycopy(tmp, 0, dec, 0, len);

        if (!areEqual(p, dec))
        {
            fail("decrypted stream fails to match in test " + count);
        }
    }

    private void randomTests()
        throws InvalidCipherTextException
    {
        SecureRandom srng = new SecureRandom();
        for (int i = 0; i < 10; ++i)
        {
            randomTest(srng); 
        }
    }

    private void randomTest(
        SecureRandom srng)
        throws InvalidCipherTextException
    {
        int DAT_LEN = srng.nextInt() >>> 22; // Note: JDK1.0 compatibility
        byte[] authen = new byte[AUTHEN_LEN];
        byte[] datIn = new byte[DAT_LEN];
        byte[] key = new byte[16];
        srng.nextBytes(authen);
        srng.nextBytes(datIn);
        srng.nextBytes(key);

        AESFastEngine engine = new AESFastEngine();
        KeyParameter sessKey = new KeyParameter(key);
        EAXPBlockCipher eaxpCipher = new EAXPBlockCipher(engine);

        AEADParameters params = new AEADParameters(sessKey, MAC_LEN * 8, authen, authen);
        eaxpCipher.init(true, params);

        byte[] intrDat = new byte[eaxpCipher.getOutputSize(datIn.length)];
        int outOff = eaxpCipher.processBytes(datIn, 0, DAT_LEN, intrDat, 0);
        outOff += eaxpCipher.doFinal(intrDat, outOff);

        eaxpCipher.init(false, params);
        byte[] datOut = new byte[eaxpCipher.getOutputSize(outOff)];
        int resultLen = eaxpCipher.processBytes(intrDat, 0, outOff, datOut, 0);
        eaxpCipher.doFinal(datOut, resultLen);

        if (!areEqual(datIn, datOut))
        {
            fail("EAXP roundtrip failed to match");
        }
    }

    public static void main(String[] args)
    {
        runTest(new EAXPTest());
    }
}
