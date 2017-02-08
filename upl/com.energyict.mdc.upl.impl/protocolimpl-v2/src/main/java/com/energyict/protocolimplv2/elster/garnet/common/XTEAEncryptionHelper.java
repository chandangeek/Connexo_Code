package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimplv2.elster.garnet.GarnetProperties;
import com.energyict.protocolimplv2.elster.garnet.exception.CipheringException;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.Frame;
import com.energyict.protocolimplv2.elster.garnet.frame.RequestFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Crc;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;

/**
 * @author sva
 * @since 17/06/2014 - 15:34
 */
public class XTEAEncryptionHelper {

    private static final boolean ENCRYPT_STREAM = true;
    private static final boolean DECRYPT_STREAM = false;

    private byte[] manufacturerKey;
    private byte[] customerKey;
    private byte[] sessionKey;

    private final GarnetProperties properties;

    public XTEAEncryptionHelper(GarnetProperties properties) {
        this.properties = properties;
    }

    /**
     * Decrypts an entire Frame
     *
     * @param frame: the frame that has to be decrypted
     * @return the decrypted frame
     * @throws CipheringException : when decryption fails
     */
    public Frame decryptFrame(Frame frame) throws GarnetException {
        FunctionCode functionCode = frame.getFunction().getFunctionCode();
        if (functionCode.needsEncryption()) {
            byte[] stream = encryptStream(frame, DECRYPT_STREAM);
            rebuildFrame(frame, stream);
        }
        return frame;
    }

    /**
     * Encrypts a given frame
     *
     * @param frame: the frame that needs encryption
     * @return the encrypted frame
     * @throws CipheringException
     */
    public Frame encryptFrame(Frame frame) throws GarnetException {
        FunctionCode functionCode = frame.getFunction().getFunctionCode();
        if (functionCode.needsEncryption()) {
            byte[] stream = encryptStream(frame, ENCRYPT_STREAM);
            rebuildFrame(frame, stream);
        }
        return frame;
    }

    /**
     * Encrypts or decrypts the relevant fields of a frame
     *
     * @param frame: the frame that needs decryption
     * @param  encrypt: the mode (true in case of encrypt - false in case of decrypt)
     * @return the byte array containing the decrypted fields
     */
    private byte[] encryptStream(Frame frame, boolean encrypt) throws CipheringException {
        byte[] bytes = frame.getBytes();
        XTEAEncryption encryptionAgent = new XTEAEncryption();
        switch (frame.getFunction().getFunctionCode().getEncryptionMode()) {
            case MANUFACTURER_KEY:
                encryptionAgent.setKey(getManufacturerKey());
                break;
            case CUSTOMER_KEY:
                encryptionAgent.setKey(getCustomerKey());
                break;
            case SESSION_KEY:
                encryptionAgent.setKey(getSessionKey());
                break;
            default:
                // We should never reach this point
                throw CodingException.unrecognizedEnumValue(frame.getFunction().getFunctionCode().getEncryptionMode());
        }

        if (encrypt) {
            encryptionAgent.encrypt(
                    bytes,
                    frame.getFunction().getFunctionCode().usesExtendedFrameFormat()
                            ? (frame instanceof RequestFrame ? 5 : 7)
                            : (frame instanceof RequestFrame ? 3 : 5),
                    frame.getFunction().getFunctionCode().getDataLength() + Crc.LENGTH
            );
        } else {
            encryptionAgent.decrypt(
                    bytes,
                    frame.getFunction().getFunctionCode().usesExtendedFrameFormat()
                            ? (frame instanceof RequestFrame ? 5 : 7)
                            : (frame instanceof RequestFrame ? 3 : 5),
                    frame.getFunction().getFunctionCode().getDataLength() + Crc.LENGTH
            );
        }
       return bytes;
    }

    /**
     * Sets the relevant fields of a given frame with the decrypted/encrypted data
     *
     * @param frame: the frame to set fields on
     * @param stream
     * @return
     * @throws ParsingException
     */
    private Frame rebuildFrame(Frame frame, byte[] stream) throws ParsingException {
        try {
            frame.parse(stream, 0);
            return frame;
        } catch (ParsingException e) {
            throw new ParsingException("Failed to rebuild the frame after ciphering was applied.", e);
        }
    }

    /**
     * Getter for the manufacturer key, loaded from the properties
     */
    public byte[] getManufacturerKey() {
        if (manufacturerKey == null) {
            this.manufacturerKey = getProperties().getManufacturerKey();
        }
        return manufacturerKey;
    }

    /**
     * Getter for the customer key, loaded from the properties
     */
    public byte[] getCustomerKey() {
        if (customerKey == null) {
            this.customerKey = getProperties().getCustomerKey();
        }
        return customerKey;
    }

    /**
     * <p>Getter for the session key</p>
     * <b>Warning:</b> this method will only return a valid key after one it written
     * (by usage of {@link #setSessionKey(byte[])}, else null is returned
     */
    public byte[] getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public GarnetProperties getProperties() {
        return properties;
    }
}