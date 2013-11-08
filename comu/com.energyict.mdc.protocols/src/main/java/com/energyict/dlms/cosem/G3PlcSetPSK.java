package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.methods.G3PlcSetPSKMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 3/7/13
 * Time: 3:00 PM
 */
public class G3PlcSetPSK extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBISCODE = ObisCode.fromString("0.128.0.0.1.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public G3PlcSetPSK(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * @return The default obis code for this class
     */
    public static final ObisCode getDefaultObisCode() {
        return DEFAULT_OBISCODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.MANUFACTURER_SPECIFIC_8194.getClassId();
    }

    /**
     * Write a new PSK to the device
     *
     * @param key The new PSK for the G3 network. The key should be 16 bytes
     * @throws java.io.IOException If an error occurred while writing the new PSK
     */
    public final void setKey(byte[] key) throws IOException {
        if (key == null) {
            throw new IOException("Unable to write key [null] to meter!");
        }

        if (key.length != 16) {
            throw new IOException("Invalid key length for key [" + DLMSUtils.getHexStringFromBytes(key, "") + "]. Expected [16] but received [" + key.length + "]");
        }

        final OctetString keyValue = new OctetString(key);
        this.methodInvoke(G3PlcSetPSKMethods.SET_PSK, keyValue);

    }

}
