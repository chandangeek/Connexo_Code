package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ObisCodeProvider;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 17:20
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute FIRMWARE_VERSION = new DLMSAttribute(ObisCodeProvider.OperationalFirmwareMonlicitic, DataAttributes.VALUE);
    public static final DLMSAttribute ELSTER_FIRMWARE_VERSION = new DLMSAttribute(ObisCodeProvider.OperationalFirmwareMonlicitic, -1, DLMSClassId.DATA);
    public static final DLMSAttribute SERIAL_NUMBER = new DLMSAttribute(ObisCodeProvider.DeviceId1, DataAttributes.VALUE);

    public ComposedMeterInfo(final DlmsSession dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                FIRMWARE_VERSION,
                ELSTER_FIRMWARE_VERSION,
                SERIAL_NUMBER
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
        AbstractDataType attribute = getAttribute(SERIAL_NUMBER);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
        }
    }


}
