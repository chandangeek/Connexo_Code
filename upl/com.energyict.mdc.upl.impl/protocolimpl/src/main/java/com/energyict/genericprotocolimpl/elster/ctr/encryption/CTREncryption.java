package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 14:04:03
 */
public class CTREncryption {

    Cipher cipher;
    private byte[] keyT;
    private byte[] keyC;
    private byte[] keyF;

    public CTREncryption() {
        try {
            cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public CTREncryption(String keyC, String keyT, String keyF) {
        this.keyT = ProtocolTools.getBytesFromHexString(keyT, "");
        this.keyC = ProtocolTools.getBytesFromHexString(keyC, "");
        this.keyF = ProtocolTools.getBytesFromHexString(keyF, "");
    }


    public Frame decryptFrame(Frame frame) throws CtrCipheringException {
        EncryptionStatus eStatus = frame.getFunctionCode().getEncryptionStatus();
        if (eStatus.isEncrypted()) {
            try {
                frame = setData(frame, decryptStream(frame));
            } catch (GeneralSecurityException e) {
                throw new CtrCipheringException("An error occured while using the ciphering!", e);
            } catch (CTRParsingException e) {
                throw new CtrCipheringException("An error occured while using the ciphering!", e);
            }
            frame = setDecryptionStatus(frame);
        }
        return frame;
    }

    private byte[] decryptStream(Frame frame) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] bytes = ProtocolTools.concatByteArrays(frame.getStructureCode().getBytes(), frame.getChannel().getBytes(), frame.getData().getBytes());
        byte[] cpa = frame.getCpa().getBytes();
        byte[] iv = ProtocolTools.concatByteArrays(cpa, cpa, cpa, cpa);
        byte[] result = null;

        int offset = 0;
        while (offset < bytes.length) {
            byte[] input = ProtocolTools.getSubArray(bytes, offset, ((offset + 16) < bytes.length) ? offset + 16 : offset + (bytes.length % 16));
            input = decryptAES128(input, iv);
            iv = addOneToByteArray(iv);
            result = ProtocolTools.concatByteArrays(result, input);
            offset += 16;
        }
        return result;
    }

    public Frame encryptFrame(Frame frame) throws CtrCipheringException {

        EncryptionStatus eStatus = frame.getFunctionCode().getEncryptionStatus();

        switch (frame.getFunctionCode().getFunction()) {
            case NACK:
            case END_OF_SESSION:
            case IDENTIFICATION_REPLY:
            case IDENTIFICATION_REQUEST:
            case ACK:
                return frame;
        }

        if (!eStatus.isEncrypted()) {
            frame = setEncryptionStatus(frame);
            frame = setCpa(frame);
            try {
                frame = setData(frame, encryptStream(frame));
            } catch (GeneralSecurityException e) {
                throw new CtrCipheringException("An error occured while using the ciphering!", e);
            } catch (CTRParsingException e) {
                throw new CtrCipheringException("An error occured while using the ciphering!", e);
            }
        }
        return frame;
    }

    private Frame setData(Frame frame, byte[] stream) throws CTRParsingException {
        int offset = 0;
        byte[] struct = ProtocolTools.getSubArray(stream, offset, offset + frame.getStructureCode().getBytes().length);
        offset += struct.length;

        byte[] chan = ProtocolTools.getSubArray(stream, offset, offset + frame.getChannel().getBytes().length);
        offset += chan.length;

        byte[] data = ProtocolTools.getSubArray(stream, offset, offset + frame.getData().getBytes().length);

        frame.setData(new Data().parse(data, 0));
        frame.setChannel(new Channel().parse(chan, 0));
        frame.setStructureCode(new StructureCode().parse(struct, 0));
        return frame;
    }

    private byte[] encryptStream(Frame frame) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] bytes = ProtocolTools.concatByteArrays(frame.getStructureCode().getBytes(), frame.getChannel().getBytes(), frame.getData().getBytes());
        byte[] cpa = frame.getCpa().getBytes();
        byte[] iv = ProtocolTools.concatByteArrays(cpa, cpa, cpa, cpa);
        byte[] result = null;

        int offset = 0;
        while (offset < bytes.length) {
            byte[] input = ProtocolTools.getSubArray(bytes, offset, ((offset + 16) < bytes.length) ? offset + 16 : offset + (bytes.length % 16));
            input = encryptAES128(input, iv);
            iv = addOneToByteArray(iv);
            result = ProtocolTools.concatByteArrays(result, input);
            offset += 16;
        }

        return result;
    }


    private byte[] addOneToByteArray(byte[] value) {
        value = ProtocolTools.concatByteArrays(new byte[0], value);
        BigInteger convertedValue = new BigInteger(value);
        convertedValue = convertedValue.add(new BigInteger("1"));
        byte[] copy = new byte[16];

        byte[] converted = convertedValue.toByteArray();
        for (int i = 0; i <= converted.length - 1; i++) {
            copy[15 - i] = converted[converted.length - i - 1];
        }
        return copy;
    }

    public Frame setCpa(Frame frame) {
        AesCMac128 aesCmac128 = new AesCMac128();
        aesCmac128.setKey(keyC);
        byte[] cpaInput = ProtocolTools.concatByteArrays(
                frame.getAddress().getBytes(),
                frame.getProfi().getBytes(),
                frame.getFunctionCode().getBytes(),
                frame.getStructureCode().getBytes(),
                frame.getChannel().getBytes(),
                frame.getData().getBytes()
        );
        byte[] cpa = ProtocolTools.getSubArray(aesCmac128.getAesCMac128(cpaInput), 0, 4);
        frame.setCpa(new Cpa().parse(cpa, 0));
        return frame;
    }

    private Frame setEncryptionStatus(Frame frame) {
        frame.getFunctionCode().setEncryptionStatus(EncryptionStatus.KEYC_ENCRYPTION);
        return frame;
    }

    private Frame setDecryptionStatus(Frame frame) {
        frame.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        return frame;
    }

    private byte[] encryptAES128(byte[] input, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aeskey, paramSpec);
        return cipher.doFinal(input);
    }

    private byte[] decryptAES128(byte[] input, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aeskey, paramSpec);
        return cipher.doFinal(input);
    }
}