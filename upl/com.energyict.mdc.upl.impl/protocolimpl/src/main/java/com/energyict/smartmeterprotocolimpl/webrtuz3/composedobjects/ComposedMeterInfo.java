package com.energyict.smartmeterprotocolimpl.webrtuz3.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 5-jan-2011
 * Time: 9:11:12
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    public static final DLMSAttribute RF_FIRMWAREVERSION = DLMSAttribute.fromString("1:1.129.0.2.0.255:2");
    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");
    public static final DLMSAttribute CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");

    public ComposedMeterInfo(ProtocolLink protocolLink, boolean useGetWithList) {
        super(protocolLink, useGetWithList, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[] {
                SERIALNR,
                FIRMWARE_VERSION,
                RF_FIRMWAREVERSION,
                CONFIG_NUMBER
        };
    }

    public String getRFFirmwareVersion() throws IOException {
        AbstractDataType attribute = getAttribute(RF_FIRMWAREVERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public String getFirmwareVersion() throws IOException {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public String getSerialNr() throws IOException {
        AbstractDataType attribute = getAttribute(SERIALNR);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public int getConfigurationChanges() throws IOException {
        return getAttribute(CONFIG_NUMBER).intValue();
    }

}
