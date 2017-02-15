/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.ComposedCosemObject;

import java.io.IOException;

public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:0.0.142.1.1.255:2");
    public static final DLMSAttribute CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");
    public static final DLMSAttribute CONFIG_ID = DLMSAttribute.fromString("1:0.0.96.2.0.255:4");

    private String firmwareVersion;
    private int configNumber;
    private String configId;

    public ComposedMeterInfo(ProtocolLink protocolLink, boolean useGetWithList) {
        super(protocolLink, useGetWithList, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[] {
                FIRMWARE_VERSION,
                CONFIG_NUMBER,
                CONFIG_ID
        };
    }

    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
            StringBuffer strbuff = new StringBuffer();
            strbuff.append(String.valueOf(attribute.getStructure().getDataType(0).getUnsigned8().intValue()));
            strbuff.append(".");
            strbuff.append(String.valueOf(attribute.getStructure().getDataType(1).getUnsigned8().intValue()));
            firmwareVersion = strbuff.toString();
        }
        return firmwareVersion;
    }

    public int getNumberOfConfigurationChanges() throws IOException {
        if (configNumber == 0) {
            AbstractDataType attribute = getAttribute(CONFIG_NUMBER);
            if (attribute.isNumerical()) {
                configNumber = attribute.intValue();
            } else {
                throw new IOException("Expected Numerical value but was " + attribute.getClass().getSimpleName());
            }
        }
        return configNumber;
    }

    public String getConfigId() throws IOException {
        if (configId == "") {
            AbstractDataType attribute = getAttribute(CONFIG_ID);
            if (attribute instanceof OctetString) {
                configId = attribute.getOctetString().stringValue();
            } else if (attribute instanceof VisibleString) {
                configId = attribute.getVisibleString().getStr();
            } else {
                throw new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            }
        }
        return configId;
    }
}