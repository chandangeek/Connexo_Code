package com.energyict.common;

import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IrreversibleKeyImpl implements IrreversibleKey {

    private static final String SEPARATOR = ":";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    protected String hsmKekLabel;
    protected byte[] encryptedKey;
    private String labelAndKeyValue;

    /**
     * Constructor
     *
     * @param labelAndKeyValue The value of the irreversible HSM key consists of 2 values, separated by colon ":"
     *                         <li>The bytes of the KEK label, formatted as hex
     *                         <li>The bytes of the encrypted key, formatted as hex
     */
    public IrreversibleKeyImpl(String labelAndKeyValue) {
        this.labelAndKeyValue = labelAndKeyValue;
    }

    public static IrreversibleKey fromByteArray(byte[] byteArray) {
        byte[] labelAndKeyValue = ProtocolTools.base64ToBytes(new String(byteArray, UTF_8));
        IrreversibleKeyImpl irreversibleKey = new IrreversibleKeyImpl(new String(labelAndKeyValue, UTF_8));
        irreversibleKey.lazyInit();
        return irreversibleKey;
    }

    /**
     * Get HSM KEK label
     */
    public String getKeyLabel() {
        lazyInit();
        return hsmKekLabel;
    }

    /**
     * Get encrypted key
     */
    public byte[] getEncryptedKey() {
        lazyInit();
        return encryptedKey;
    }

    public byte[] toBase64ByteArray() {
        return ProtocolTools.bytesToBase64(labelAndKeyValue.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Validate if the array has exact number of values
     *
     * @throws DeviceConfigurationException
     */
    protected void validateCountOfValues(String[] values, int expectedCount) throws DeviceConfigurationException {
        if (values.length != expectedCount) {
            throw DeviceConfigurationException.unexpectedHsmKeyFormat();
        }
    }

    protected void lazyInit() {
        if (this.hsmKekLabel == null) {
            String[] values = this.labelAndKeyValue.split(SEPARATOR);
            validateCountOfValues(values, 2);
            hsmKekLabel = values[0];
            encryptedKey = ProtocolTools.hexToBytes(values[1]);
        }
    }
}
