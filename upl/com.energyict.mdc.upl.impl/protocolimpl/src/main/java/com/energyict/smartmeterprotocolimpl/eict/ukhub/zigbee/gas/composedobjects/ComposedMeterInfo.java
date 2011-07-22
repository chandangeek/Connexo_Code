package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 17:20
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute FIRMWARE_VERSION_MID = new DLMSAttribute(ObisCodeProvider.FIRMWARE_VERSION_MID, DataAttributes.VALUE);
    public static final DLMSAttribute SERIAL_NUMBER = new DLMSAttribute(ObisCodeProvider.SERIAL_NUMBER, DataAttributes.VALUE);

    public ComposedMeterInfo(final DlmsSession dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                FIRMWARE_VERSION_MID,
                SERIAL_NUMBER
        };
    }

    public String getFirmwareVersionMid() throws IOException {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION_MID);
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
