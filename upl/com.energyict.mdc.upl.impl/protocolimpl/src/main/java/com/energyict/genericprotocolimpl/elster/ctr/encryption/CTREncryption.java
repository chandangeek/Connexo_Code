package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 14:04:03
 */
public class CTREncryption {

    private Cipher cipher = null;
    private final byte[] keyT;
    private final byte[] keyC;
    private final byte[] keyF;
    private final int securityLevel;

    /**
     * @param properties
     */
    public CTREncryption(MTU155Properties properties) {
        this(
                properties.getKeyCBytes(),
                properties.getKeyTBytes(),
                properties.getKeyFBytes(),
                properties.getSecurityLevel()
        );
    }

    /**
     * @param keyC
     * @param keyT
     * @param keyF
     */
    public CTREncryption(String keyC, String keyT, String keyF, int securityLevel) {
        this(
                ProtocolTools.getBytesFromHexString(keyC, ""),
                ProtocolTools.getBytesFromHexString(keyT, ""),
                ProtocolTools.getBytesFromHexString(keyF, ""),
                securityLevel
        );
    }

    /**
     * @param keyC
     * @param keyT
     * @param keyF
     * @param securityLevel
     */
    public CTREncryption(byte[] keyC, byte[] keyT, byte[] keyF, int securityLevel) {
        this.keyT = keyT;
        this.keyC = keyC;
        this.keyF = keyF;
        this.securityLevel = securityLevel;
    }

    /**
     *
     * @return
     */
    private Cipher getAesCTRCipher() {
        if (cipher == null) {
            try {
                cipher = Cipher.getInstance("AES/CTR/NOPADDING");
            } catch (GeneralSecurityException e) {
                cipher = null;// absorb
            }
        }
        return cipher;
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
            iv = ProtocolTools.addOneToByteArray(iv);
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
            frame.generateAndSetCpa(getEncryptionKey());

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

        frame.setData(new Data(frame.getProfi().isLongFrame()).parse(data, 0));
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
            iv = ProtocolTools.addOneToByteArray(iv);
            result = ProtocolTools.concatByteArrays(result, input);
            offset += 16;
        }

        return result;
    }

    private Frame setEncryptionStatus(Frame frame) throws CtrCipheringException {
        frame.getFunctionCode().setEncryptionStatus(getEncryptionStatus());
        return frame;
    }

    private Frame setDecryptionStatus(Frame frame) {
        frame.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        return frame;
    }

    private byte[] encryptAES128(byte[] input, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        getAesCTRCipher().init(Cipher.ENCRYPT_MODE, aeskey, paramSpec);
        return getAesCTRCipher().doFinal(input);
    }

    private byte[] decryptAES128(byte[] input, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        getAesCTRCipher().init(Cipher.DECRYPT_MODE, aeskey, paramSpec);
        return getAesCTRCipher().doFinal(input);
    }

    private byte[] getEncryptionKey() throws CtrCipheringException {
        switch (securityLevel) {
            case 0:
                return keyT;
            case 1:
                return keyC;
            case 2:
                return keyF;
            default:
                throw new CtrCipheringException("Invalid security level. Should be [0 = KeyT, 1 = KeyC, 2 = KeyF]");
        }
    }

    private EncryptionStatus getEncryptionStatus() throws CtrCipheringException {
        switch (securityLevel) {
            case 0:
                return EncryptionStatus.KEYT_ENCRYPTION;
            case 1:
                return EncryptionStatus.KEYC_ENCRYPTION;
            case 2:
                return EncryptionStatus.KEYF_ENCRYPTION;
            default:
                throw new CtrCipheringException("Invalid security level. Should be [0 = KeyT, 1 = KeyC, 2 = KeyF]");
        }
    }

}