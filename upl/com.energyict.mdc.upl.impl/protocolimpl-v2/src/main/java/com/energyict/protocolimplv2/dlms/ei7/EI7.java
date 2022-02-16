package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.dlms.ei7.profiles.EI7LoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.util.Arrays;

public class EI7 extends A2 {

    /*Predefined OBIS Code for EI7 meter - What is the firmware version obis code???????? */
//    private static final ObisCode FIRMWARE_VERSION_OBIS_CODE = ObisCode.fromString("0.0.0.2.0.255");
    private static final ObisCode FIRMWARE_VERSION_OBIS_CODE = ObisCode.fromString("7.1.0.2.1.255");
    String firmwareFinalVersion = new String();

    public EI7(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
               NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
               KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor, keyAccessorTypeExtractor);
    }

    public EI7DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsSessionProperties) {
        return new EI7DlmsSession(comChannel, dlmsSessionProperties, getHhuSignOnV2(), offlineDevice.getSerialNumber());
    }

    protected EI7Messaging createMessaging() {
        return new EI7Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor(), getKeyAccessorTypeExtractor());
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();

        readObjectList();
        dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
    }

    @Override
    protected void readObjectList() {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(new EI7ObjectList().getObjectList());


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
        try {

            Data firmwareData = getDlmsSession().getCosemObjectFactory().getData(FIRMWARE_VERSION_OBIS_CODE);
            AbstractDataType valueAttr = firmwareData.getValueAttr();


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
                System.out.println();


                firmwareFinalVersion = String.format("%s.%s.%s", major, minor, fix);
            }


        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle((IOException) e, getDlmsSession().getProperties().getRetries() + 1);
        }

        return firmwareFinalVersion;
    }


    @Override
    public String getProtocolDescription() {
        return "EI7 2021 ThemisLog2 DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-02-16$";
    }

    @Override
    public A2ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new EI7LoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(),
                    getOfflineDevice(), getDlmsSessionProperties().getLimitMaxNrOfDays(), EI7LoadProfileDataReader.getSupportedLoadProfiles());
        }
        return profileDataReader;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new EI7ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

}
