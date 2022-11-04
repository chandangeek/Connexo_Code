package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.dlms.ei7.profiles.EI7LoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.ei7.properties.EI7ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.ei7.registers.EI7RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.util.List;

public class EI7 extends A2 {
   protected EI7RegisterFactory registerFactory = null;

    /*Predefined OBIS Code for EI7 meter */
    private static final ObisCode FIRMWARE_VERSION_METROLOGICAL_OBIS_CODE = ObisCode.fromString("7.0.0.2.1.255");
    private static final ObisCode FIRMWARE_VERSION_APPLICATION_OBIS_CODE = ObisCode.fromString("7.1.0.2.1.255");
    private static final ObisCode FIRMWARE_VERSION_BOOTLOADER_OBIS_CODE = ObisCode.fromString("7.3.0.2.1.255");

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
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getActiveMeterFirmwareVersion());
            firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion(getActiveCommunicationFirmwareVersion());
            firmwareVersionsCollectedData.setActiveAuxiliaryFirmwareVersion(getActiveAuxiliaryFirmwareVersion());
            return firmwareVersionsCollectedData;
        }

        return super.getFirmwareVersions(serialNumber);
    }

    public String getActiveMeterFirmwareVersion(){
        return  getFirmwareVersion(FIRMWARE_VERSION_METROLOGICAL_OBIS_CODE);
    }

    public String getActiveCommunicationFirmwareVersion(){
        return  getFirmwareVersion(FIRMWARE_VERSION_APPLICATION_OBIS_CODE);
    }

    public String getActiveAuxiliaryFirmwareVersion(){
        return getFirmwareVersion(FIRMWARE_VERSION_BOOTLOADER_OBIS_CODE);
    }

    public String getFirmwareVersion(ObisCode firmwareObiscode) {
        try {
            journal("Collecting firmware version from " + firmwareObiscode);
            Data firmwareData = getDlmsSession().getCosemObjectFactory().getData(firmwareObiscode);
            AbstractDataType valueAttr = firmwareData.getValueAttr();

            if (valueAttr.isOctetString()) {
                return(registerFactory.decodeFirmwareVersion(valueAttr.getOctetString()));
            }
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
        return "";
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    @Override
    public boolean supportsAuxiliaryFirmwareVersion() {
        return true;
    }

    @Override
    public String getProtocolDescription() {
        return "EI7 2021 ThemisLog2 DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-11-02$";
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

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getEI7RegisterFactory().readRegisters(registers);
    }

    private EI7RegisterFactory getEI7RegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new EI7RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return registerFactory;
    }
}
