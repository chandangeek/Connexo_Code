package com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.modes;

import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.*;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.macs.CMacP;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.params.AEADParameters;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.params.ParametersWithIV;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.util.Arrays;

/**
 * EAX' is a Two-Pass Authenticated-Encryption Scheme based on EAX.
 * by A. Moise, E. Beroset, T. Phinney, M. Burns
 * 
 * http://csrc.nist.gov/groups/ST/toolkit/BCM/documents/proposedmodes/eax-prime/eax-prime-spec.pdf
 * 
 * A Two-Pass Authenticated-Encryption Scheme Optimized for Simplicity and 
 * Efficiency - by M. Bellare, P. Rogaway, D. Wagner.
 * 
 * http://www.cs.ucdavis.edu/~rogaway/papers/eax.pdf
 * 
 * EAX is an AEAD scheme based on CTR and OMAC1/CMAC, that uses a single block 
 * cipher to encrypt and authenticate data. It's on-line (the length of a 
 * message isn't needed to begin processing it), has good performances, it's
 * simple and provably secure (provided the underlying block cipher is secure).
 * 
 * Of course, this implementations is NOT thread-safe.
 */
public class EAXPBlockCipher
    implements AEADBlockCipher
{
    private SICBlockCipher cipher;

    private boolean forEncryption;

    private int blockSize;

    private CMacP mac;

    private byte[] associatedTextMac;
    private byte[] macBlock;
    private byte[] macCtr;
    
    private int macSize;
    private byte[] bufBlock;
    private int bufOff;

    /**
     * Constructor that accepts an instance of a block cipher engine.
     *
     * @param cipher the engine to use
     */
    public EAXPBlockCipher(BlockCipher cipher)
    {
        blockSize = cipher.getBlockSize();
        mac = new CMacP(cipher);
        macBlock = new byte[blockSize];
        macCtr = new byte[blockSize];
        bufBlock = new byte[blockSize * 2];
        associatedTextMac = new byte[mac.getMacSize()];
        this.cipher = new SICBlockCipher(cipher);
    }

    public String getAlgorithmName()
    {
        return cipher.getUnderlyingCipher().getAlgorithmName() + "/EAX";
    }

    public BlockCipher getUnderlyingCipher()
    {
        return cipher.getUnderlyingCipher();
    }

    public int getBlockSize()
    {
        return cipher.getBlockSize();
    }

    public void init(boolean forEncryption, CipherParameters params)
        throws IllegalArgumentException
    {
        this.forEncryption = forEncryption;

        byte[] associatedText;
        CipherParameters keyParam;

        if (params instanceof AEADParameters)
        {
            AEADParameters param = (AEADParameters)params;

            associatedText = param.getAssociatedText();
            macSize = param.getMacSize() / 8;
            keyParam = param.getKey();
        }
        else if (params instanceof ParametersWithIV)
        {
            ParametersWithIV param = (ParametersWithIV)params;

            associatedText = new byte[0];
            macSize = mac.getMacSize() / 2;
            keyParam = param.getParameters();
        }
        else
        {
            throw new IllegalArgumentException("invalid parameters passed to EAXP");
        }

        mac.init(keyParam);
         mac.update(associatedText, 0, associatedText.length);
        mac.doFinal(associatedTextMac, 0);
        System.arraycopy(associatedTextMac, 0, macCtr, 0, blockSize);
        // EAX' optimization 
        macCtr[blockSize-2] &= 0x7f;
        macCtr[blockSize-4] &= 0x7f;

        cipher.init(true, new ParametersWithIV(keyParam, macCtr));
    }

    private void calculateMac(boolean nodata)
    {
        byte[] outC = new byte[blockSize];
        if (!nodata)
        	mac.doFinal(outC, 0);

        for (int i = 0; i < macBlock.length; i++)
        {
            macBlock[i] = (byte)(associatedTextMac[i] ^ outC[i]);
        }
    }

    public void reset()
    {
        reset(true);
    }

    private void reset(
        boolean clearMac)
    {
        cipher.reset();
        mac.reset();

        bufOff = 0;
        Arrays.fill(bufBlock, (byte) 0);

        if (clearMac)
        {
            Arrays.fill(macBlock, (byte)0);
        }
    }

    public int processByte(byte in, byte[] out, int outOff)
        throws DataLengthException
    {
        return process(in, out, outOff);
    }

    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff)
        throws DataLengthException
    {
        int resultLen = 0;
        mac.setQMode(true);

        for (int i = 0; i != len; i++)
        {
            resultLen += process(in[inOff + i], out, outOff + resultLen);
        }

        return resultLen;
    }

    public int doFinal(byte[] out, int outOff)
        throws IllegalStateException, InvalidCipherTextException
    {
        int extra = bufOff;
        byte[] tmp = new byte[bufBlock.length];

        bufOff = 0;

        if (forEncryption)
        {
        	cipher.processBlock(bufBlock, 0, tmp, 0);
        	cipher.processBlock(bufBlock, blockSize, tmp, blockSize);

        	System.arraycopy(tmp, 0, out, outOff, extra);
        	mac.update(tmp, 0, extra);

        	calculateMac(extra == 0);

        	System.arraycopy(macBlock, macBlock.length - macSize, out, outOff + extra, macSize);

            reset(false);

            return extra + macSize;
        }
        else
        {
            if (extra > macSize)
            {
                mac.update(bufBlock, 0, extra - macSize);

                cipher.processBlock(bufBlock, 0, tmp, 0);
                cipher.processBlock(bufBlock, blockSize, tmp, blockSize);

                System.arraycopy(tmp, 0, out, outOff, extra - macSize);
            }

            calculateMac(extra == macSize);

            if (!verifyMac(bufBlock, extra - macSize))
            {
                throw new InvalidCipherTextException("mac check in EAXP failed");
            }

            reset(false);

            return extra - macSize;
        }
    }

    public byte[] getMac()
    {
        byte[] mac = new byte[macSize];

        System.arraycopy(macBlock, blockSize - macSize, mac, 0, macSize);

        return mac;
    }

    public int getUpdateOutputSize(int len)
    {
        return ((len + bufOff) / blockSize) * blockSize;
    }

    public int getOutputSize(int len)
    {
        if (forEncryption)
        {
             return len + bufOff + macSize;
        }
        else
        {
             return len + bufOff - macSize;
        }
    }

    private int process(byte b, byte[] out, int outOff)
    {
        bufBlock[bufOff++] = b;

        if (bufOff == bufBlock.length)
        {
            int size;

            if (forEncryption)
            {
                size = cipher.processBlock(bufBlock, 0, out, outOff);

                mac.update(out, outOff, blockSize);
            }
            else
            {
                mac.update(bufBlock, 0, blockSize);

                size = cipher.processBlock(bufBlock, 0, out, outOff);
            }

            bufOff = blockSize;
            System.arraycopy(bufBlock, blockSize, bufBlock, 0, blockSize);

            return size;
        }

        return 0;
    }

    private boolean verifyMac(byte[] mac, int off)
    {
        for (int i = 0; i < macSize; i++)
        {
            if (macBlock[blockSize - macSize + i] != mac[off + i])
            {
                return false;
            }
        }

        return true;
    }
}
