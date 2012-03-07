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
    private byte[] keyT;
    private byte[] keyC;
    private byte[] keyF;
    private int securityLevel;

    private final static String CTRENCRYPTIONERROR = "An error occured while using the ciphering!";

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

    public void update(MTU155Properties properties) {
        this.keyC = properties.getKeyCBytes();
        this.keyT = properties.getKeyTBytes();
        this.keyF = properties.getKeyFBytes();
        this.securityLevel = properties.getSecurityLevel();
    }

    /**
     * @param keyC
     * @param keyT
     * @param keyF
     * @param securityLevel
     */
    public CTREncryption(byte[] keyC, byte[] keyT, byte[] keyF, int securityLevel) {
        this.keyT = keyT.clone();
        this.keyC = keyC.clone();
        this.keyF = keyF.clone();
        this.securityLevel = securityLevel;
    }

    /**
     *
     * @return cipher
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

    /**
     * Decrypts an entire Frame (SMS or GPRS)
     * @param frame: the frame that has to be decrypted
     * @return the decrypted frame
     * @throws CtrCipheringException: when decryption fails
     */

    public Frame decryptFrame(Frame frame) throws CtrCipheringException {
        EncryptionStatus eStatus = frame.getFunctionCode().getEncryptionStatus();
        if (eStatus.isEncrypted()) {
            try {
                frame = setData(frame, decryptStream(frame));
            } catch (GeneralSecurityException e) {
                throw new CtrCipheringException(CTRENCRYPTIONERROR, e);
            } catch (CTRParsingException e) {
                throw new CtrCipheringException(CTRENCRYPTIONERROR, e);
            }

            frame = setDecryptionStatus(frame);
        }
        return frame;
    }

    /**
     * Decrypts the relevant fields of a frame
     * @param frame: the frame that needs decryption
     * @return the byte array containing the decrypted fields
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
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

    /**
     * Encrypts a given frame
     * @param frame: the frame (SMS or GPRS) that needs encryption
     * @return the encrypted frame
     * @throws CtrCipheringException
     */
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

        if (frame.getFunctionCode().getFunction().equals(Function.DOWNLOAD)) {
            frame.generateAndSetCpa(getEncryptionKey());
            return frame;
        }

        if (!eStatus.isEncrypted()) {
            frame = setEncryptionStatus(frame);
            frame.generateAndSetCpa(getEncryptionKey());

            try {
                frame = setData(frame, encryptStream(frame));
            } catch (GeneralSecurityException e) {
                throw new CtrCipheringException(CTRENCRYPTIONERROR, e);
            } catch (CTRParsingException e) {
                throw new CtrCipheringException(CTRENCRYPTIONERROR, e);
            }
        }
        return frame;
    }

    /**
     * Sets the relevant fields of a given frame with the decrypted/encrypted data
     * @param frame: the frame to set fields on
     * @param stream
     * @return
     * @throws CTRParsingException
     */
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

    /**
     * Encrypts the relevant fields of a given frame
     * @param frame: the frame that needs encryption
     * @return byte array with the encrypted fields
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
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

    /**
     * Encrypts a given byte array with the AES128 encryption (Counter Mode)
     * @param input: the byte array to encrypt
     * @param iv: the initial vector for the encryption
     * @return the encrypted byte array
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private byte[] encryptAES128(byte[] input, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aeskey = new SecretKeySpec(keyC, 0, 16, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        cipher = Cipher.getInstance("AES/CTR/NOPADDING");
        getAesCTRCipher().init(Cipher.ENCRYPT_MODE, aeskey, paramSpec);
        return getAesCTRCipher().doFinal(input);
    }

    /**
     * Decrypts a given byte array with the AES128 decryption (Counter Mode)
     * @param input: the byte array to decrypt
     * @param iv: the initial vector for the decryption
     * @return the decrypted byte array
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
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
