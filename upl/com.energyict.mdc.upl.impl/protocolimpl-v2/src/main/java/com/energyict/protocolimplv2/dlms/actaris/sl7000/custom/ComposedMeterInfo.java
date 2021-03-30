package com.energyict.protocolimplv2.dlms.actaris.sl7000.custom;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;

import java.io.IOException;

public class ComposedMeterInfo extends com.energyict.protocolimplv2.common.composedobjects.ComposedMeterInfo {

    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:0.0.142.1.1.255:2");
    public static final ObisCode OBISCODE_SERIAL_NUMBER_REQ = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode OBISCODE_SERIAL_NUMBER_ACTUAL = ObisCode.fromString("0.0.96.1.255.255");
    public static final DLMSAttribute CONFIG_ATTRIBUTE = DLMSAttribute.fromString("4:0.0.96.2.0.255:2");

    public static final int CONFIG_NOT_READ = -1;

    private final DlmsSession dlmsSession;

    private String fVersion;
    private String sn;
    private int configNumber = CONFIG_NOT_READ;

    public ComposedMeterInfo(DlmsSession protocolLink, boolean bulkRequest, int roundTripCorrection, int retries) {
        super(protocolLink, bulkRequest, roundTripCorrection, retries, DEFAULT_SERIALNR, DEFAULT_EQUIPMENT_IDENTIFIER, CONFIG_ATTRIBUTE, FIRMWARE_VERSION, DEFAULT_CLOCK);
        this.dlmsSession = protocolLink;
    }


    @Override
    public String getFirmwareVersion()  {
        if (fVersion != null) {
            return fVersion;
        }
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        StringBuilder strbuff = new StringBuilder();
        strbuff.append(attribute.getStructure().getDataType(0).getUnsigned8().intValue());
        strbuff.append(".");
        strbuff.append(attribute.getStructure().getDataType(1).getUnsigned8().intValue());
        fVersion = strbuff.toString();
        return fVersion;
    }

    @Override
    public String getSerialNr() {
        if (sn != null) {
            return sn;
        }
        try {
            Data data = dlmsSession.getCosemObjectFactory().getData(OBISCODE_SERIAL_NUMBER_ACTUAL);
            sn = AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
            return sn;
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, dlmsSession.getProperties().getRetries() + 1);
        }
    }

    @Override
    public int getConfigurationChanges() {
        if (configNumber == CONFIG_NOT_READ) {
            try {
                ExtendedRegister composedCosemObject = dlmsSession.getCosemObjectFactory().getExtendedRegister(CONFIG_ATTRIBUTE.getObisCode());
                AbstractDataType attribute = composedCosemObject.getAttrbAbstractDataType(2);
                if (attribute.isNumerical()) {
                    configNumber = attribute.intValue();
                } else {
                    throw DLMSIOExceptionHandler.handle(new ProtocolException("Expected numerical config changes but got:" + attribute.getClass()), dlmsSession.getProperties().getRetries() + 1);
                }
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, dlmsSession.getProperties().getRetries() + 1);
            }
        }
        return configNumber;
    }


}
