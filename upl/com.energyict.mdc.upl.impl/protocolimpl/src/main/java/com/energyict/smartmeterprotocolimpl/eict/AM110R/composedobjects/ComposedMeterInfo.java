package com.energyict.smartmeterprotocolimpl.eict.AM110R.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;

import java.io.IOException;

/**
 * Composed object containing Meter Information
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:0.0.0.2.0.255:2");
    public static final DLMSAttribute ELSTER_FIRMWARE_VERSION = DLMSAttribute.fromString("1:0.0.0.2.0.255:-1");
    public static final DLMSAttribute SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");

    public ComposedMeterInfo(final DlmsSession dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                FIRMWARE_VERSION,
                ELSTER_FIRMWARE_VERSION,
                SERIALNR
        };
    }

    public String getFirmwareVersion() throws IOException {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public String getElsterFirmwareVersion() throws IOException {
        AbstractDataType attribute = getAttribute(ELSTER_FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public String getSerialNumber() throws IOException {
        AbstractDataType attribute = getAttribute(SERIALNR);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }
}
