package com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:39:32
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    public static final DLMSAttribute EQUIPMENT_IDENTIFIER = DLMSAttribute.fromString("1:0.0.96.1.1.255:2");
    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");
    public static final DLMSAttribute CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");

    public ComposedMeterInfo(final DlmsSession dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                SERIALNR,
                EQUIPMENT_IDENTIFIER,
                FIRMWARE_VERSION,
                CONFIG_NUMBER
        };
    }

    public String getFirmwareVersion() throws IOException {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return getStringValueFrom((OctetString) attribute);
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public String getSerialNr() throws IOException {
        AbstractDataType attribute = getAttribute(SERIALNR);
        if (attribute instanceof OctetString) {
            return getStringValueFrom((OctetString) attribute);
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    public int getConfigurationChanges() throws IOException {
        return getAttribute(CONFIG_NUMBER).intValue();
    }

    public String getEquipmentIdentifier() throws IOException {
        AbstractDataType attribute = getAttribute(EQUIPMENT_IDENTIFIER);
        if (attribute instanceof OctetString) {
            return getStringValueFrom((OctetString) attribute);
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }

    protected String getStringValueFrom(OctetString octetString) {
        return octetString.getOctetString().stringValue();
    }
}
