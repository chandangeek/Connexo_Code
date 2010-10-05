package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class AesCMac128 {

    private static final byte[] ZERO_KEY = ProtocolTools.getBytesFromHexString("$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00");
    private static final byte[] RB = ProtocolTools.getBytesFromHexString("$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$87");

    private byte[] encryptionKey;

    private byte[] k1;
    private byte[] k2;

    public AesCMac128() {
        setKey(new byte[16]);
    }

    public AesCMac128(byte[] encryptionKey) {
        setKey(encryptionKey);
    }

    public void setKey(byte[] key) {
        this.encryptionKey = key;
        generateSubKeys();
    }

    private void generateSubKeys() {
        byte[] L = encryptAES128(ZERO_KEY);

        if ((L[0] & 0x80) == 0) {
            k1 = shiftLeft(L);
        } else {
            byte[] tmp = shiftLeft(L);
            k1 = xor(tmp, RB);
        }

        if ((k1[0] & 0x80) == 0) {
            k2 = shiftLeft(k1);
        } else {
            byte[] tmp = shiftLeft(k1);
            k2 = xor(tmp, RB);
        }

    }

    private byte[] shiftLeft(byte[] input) {
        byte[] output = new byte[input.length];
        byte overflow = 0;
        for (int i = (input.length - 1); i >= 0; i--) {
            output[i] = (byte) ((int) input[i] << 1 & 0xFF);
            output[i] |= overflow;
            overflow = ((input[i] & 0x80) != 0) ? (byte) 1 : (byte) 0;
        }
        return output;
    }

    private byte[] encryptAES128(byte[] input) {
        SecretKey AESKey = new SecretKeySpec(encryptionKey, 0, 16, "AES");
        try {
            Cipher aesCipher = Cipher.getInstance("AES/ECB/NOPADDING");
            aesCipher.init(Cipher.ENCRYPT_MODE, AESKey);
            byte[] result = aesCipher.doFinal(input);
            return result;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getAesCMac128(byte[] message) {
        return getAesCMac128(message, 0, message.length);
    }

    public byte[] getAesCMac128(byte[] message, int offset, int len) {
        byte[] input = new byte[len];
        System.arraycopy(message, offset, input, 0, len);

        boolean lastBlockComplete;
        int rounds = (input.length + 15) / 16;

        if (rounds == 0) {
            rounds = 1;
            lastBlockComplete = false;
        } else {
            if (input.length % 16 == 0) {
                lastBlockComplete = true;
            } else {
                lastBlockComplete = false;
            }
        }

        byte[] M_last;
        int srcPos = 16 * (rounds - 1);

        if (lastBlockComplete) {
            byte[] partInput = new byte[16];

            System.arraycopy(input, srcPos, partInput, 0, 16);
            M_last = xor(partInput, k1);
        } else {
            byte[] partInput = new byte[input.length % 16];

            System.arraycopy(input, srcPos, partInput, 0, input.length % 16);
            byte[] padded = padding(partInput);
            M_last = xor(padded, k2);
        }

        byte[] X = ZERO_KEY.clone();
        byte[] partInput = new byte[16];
        byte[] Y;

        for (int i = 0; i < rounds - 1; i++) {
            srcPos = 16 * i;
            System.arraycopy(input, srcPos, partInput, 0, 16);

            Y = xor(partInput, X); /* Y := Mi (+) X */
            X = encryptAES128(Y);
        }

        Y = xor(X, M_last);
        X = encryptAES128(Y);

        return X;
    }

    /**
     * 
     * @param input
     * @return
     */
    private byte[] padding(byte[] input) {
        byte[] padded = new byte[16];
        for (int j = 0; j < padded.length; j++) {
            if (j < input.length) {
                padded[j] = input[j];
            } else if (j == input.length) {
                padded[j] = (byte) 0x80;
            } else {
                padded[j] = (byte) 0x00;
            }
        }
        return padded;
    }

    /**
     * This method XOR's two byte arrays of the same length;
     * @param input1
     * @param input2
     * @return
     */
    private byte[] xor(byte[] input1, byte[] input2) {
        byte[] output = new byte[input1.length];
        for (int i = 0; i < input1.length; i++) {
            output[i] = (byte) (((int) input1[i] ^ (int) input2[i]) & 0xFF);
        }
        return output;
    }

}