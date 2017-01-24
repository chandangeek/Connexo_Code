package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * @author  sva
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute SERIAL_NUMBER = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");

    private String serialNumber;
    private String firmwareVersion;

    public ComposedMeterInfo(ProtocolLink protocolLink, boolean useGetWithList) {
        super(protocolLink, useGetWithList, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[] {
                SERIAL_NUMBER,
                FIRMWARE_VERSION
        };
    }

    public String getMeterSerialNumber() throws IOException {
        if (serialNumber == null) {
            AbstractDataType attribute = getAttribute(SERIAL_NUMBER);
            serialNumber = attribute.getOctetString().stringValue();
        }
        return serialNumber;
    }

    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
            firmwareVersion = attribute.getOctetString().stringValue();
        }
        return firmwareVersion;
    }
}
