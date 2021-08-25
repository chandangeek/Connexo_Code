package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.util.Arrays;

public class EI7 extends A2 {

    /*Predefined OBIS Code for EI7 meter*/
    private static final ObisCode FIRMWARE_VERSION_OBIS_CODE = ObisCode.fromString("7.1.0.2.1.255");

    public EI7(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor);
    }

    public EI7DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsSessionProperties) {
        return new EI7DlmsSession(comChannel, dlmsSessionProperties, getHhuSignOnV2(), offlineDevice.getSerialNumber());
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {
        if (offlineDevice.getSerialNumber().equals(serialNumber)) {
            CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getFirmwareVersion());
            return firmwareVersionsCollectedData;
        }
        return super.getFirmwareVersions(serialNumber);
    }

    public String getFirmwareVersion() {
        String firmwareFinalVersion = new String();
        try {

            Data firmwareData = getDlmsSession().getCosemObjectFactory().getData(FIRMWARE_VERSION_OBIS_CODE);
            AbstractDataType valueAttr = firmwareData.getValueAttr();
            ;


            if (valueAttr.isOctetString()) {

                byte[] berEncodedByteArray = valueAttr.getOctetString().getBEREncodedByteArray();
                byte[] finalArray = Arrays.copyOfRange(berEncodedByteArray, 2, berEncodedByteArray.length);

                int versionNumber = 0;
                int commitNumber = 0;
                if (finalArray.length > 2) {
                    versionNumber = (finalArray[0] << 8) + finalArray[1];
                }
                if (finalArray.length > 4) {
                    commitNumber = (finalArray[2] << 8) + finalArray[3];
                }

                int major = (versionNumber & 0xF800) >> 11;
                int minor = (versionNumber & 0x07C0) >> 6;
                int fix = versionNumber & 0x003F;

                firmwareFinalVersion = String.format("%s.%s.%s", major, minor, fix);
            }


        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle((IOException) e, getDlmsSession().getProperties().getRetries() + 1);
        }

        return firmwareFinalVersion;
    }

    protected EI7Messaging createMessaging() {
        return new EI7Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor());
    }

    @Override
    public String getProtocolDescription() {
        return "EI7 ThemisUno DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-08-25 12:00:00 +0200 (Wed, 25 Aug 2020) $";
    }
}
