/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;

import java.io.IOException;

public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute FIRMWARE_VERSION_MONOLITIC = new DLMSAttribute(ObisCodeProvider.OperationalFirmwareMonlicitic, DataAttributes.VALUE);
    public static final DLMSAttribute SERIAL_NUMBER = new DLMSAttribute(ObisCodeProvider.DeviceId1, DataAttributes.VALUE);

    public ComposedMeterInfo(final DlmsSession dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                FIRMWARE_VERSION_MONOLITIC,
                SERIAL_NUMBER
        };
    }

    public String getFirmwareVersionMonolitic() throws IOException {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION_MONOLITIC);
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
