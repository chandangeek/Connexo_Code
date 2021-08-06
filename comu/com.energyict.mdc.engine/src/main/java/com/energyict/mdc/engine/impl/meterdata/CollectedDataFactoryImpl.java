/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.security.CertificateWrapper;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date: 14/05/13
 * Time: 11:31
 */
@Component(name = "com.energyict.mdc.engine.meterdata.collector", service = {CollectedDataFactory.class}, immediate = true)
@SuppressWarnings("unused")
public class CollectedDataFactoryImpl implements CollectedDataFactory {

    private volatile Clock clock;
    private volatile IdentificationService identificationService;
    private volatile DeviceMessageService deviceMessageService;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Activate
    public void activate() {
        Services.collectedDataFactory(this);
    }

    @Deactivate
    public void deactivate() {
        Services.collectedDataFactory(null);
    }

    @Reference
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Override
    public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return new DeviceLoadProfile(loadProfileIdentifier);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber) {
        return this.createCollectedLoadProfileConfiguration(profileObisCode, this.identificationService.createDeviceIdentifierBySerialNumber(meterSerialNumber), meterSerialNumber);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, String meterSerialNumber) {
        return new DeviceLoadProfileConfiguration(profileObisCode, deviceIdentifier, meterSerialNumber);
    }

    @Override
    public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier) {
        return new DeviceTopology(deviceIdentifier);
    }

    @Override
    public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
        return new DeviceLogBook(logBookIdentifier);
    }

    @Override
    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier) {
        return new MaximumDemandDeviceRegister(registerIdentifier);
    }

    @Override
    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier) {
        return new AdapterDeviceRegister(registerIdentifier);
    }

    @Override
    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier) {
        return new BillingDeviceRegisters(registerIdentifier);
    }

    @Override
    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier) {
        return new DefaultDeviceRegister(registerIdentifier);
    }

    @Override
    public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier) {
        return new NoLogBooksForDevice(deviceIdentifier);
    }

    @Override
    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile) {
        DeviceProtocolMessageWithCollectedLoadProfileData collectedMessage = new DeviceProtocolMessageWithCollectedLoadProfileData(messageIdentifier, collectedLoadProfile);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        DeviceProtocolMessageWithCollectedLoadProfileData collectedMessage = new DeviceProtocolMessageWithCollectedLoadProfileData(messageIdentifier, collectedLoadProfile, loadProfileReader);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedMessage createCollectedMessageWithLogbookData(MessageIdentifier messageIdentifier, CollectedLogBook collectedLoadProfile) {
        return new DeviceProtocolMessageWithCollectedLogbookData(messageIdentifier, collectedLoadProfile);
    }

    @Override
    public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters) {
        DeviceProtocolMessageWithCollectedRegisterData collectedMessage = new DeviceProtocolMessageWithCollectedRegisterData(deviceIdentifier, messageIdentifier, collectedRegisters);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUpdateSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
        return new DeviceProtocolMessageWithCollectedSecurityProperty(deviceIdentifier, messageIdentifier, propertyName, propertyValue);
    }

    @Override
    public CollectedMessage createCollectedMessageWithUpdateGeneralProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
        return new DeviceProtocolMessageWithCollectedGeneralProperty(deviceIdentifier, messageIdentifier, propertyName, propertyValue);
    }

    @Override
    public CollectedMessage createCollectedMessageWithFile(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String fileName, String fileExtension, byte[] contents) {
        DeviceProtocolMessageWithCollectedFile collectedMessage = new DeviceProtocolMessageWithCollectedFile(deviceIdentifier, messageIdentifier, fileName, fileExtension, contents);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedCertificateWrapper createCollectedCertificateWrapper(X509Certificate x509Certificate) {
        return new CollectedCertificateWrapperImpl(x509Certificate);
    }

    @Override
    public CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateWrapper> certificateWrappers) {
        return null;
    }

    @Override
    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier) {
        return new UpdatedDeviceCache(deviceIdentifier);
    }

    @Override
    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceCache) {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        updatedDeviceCache.setCollectedDeviceCache(deviceCache);
        return updatedDeviceCache;
    }

    public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return new DeviceProtocolMessageList(offlineDeviceMessages, deviceMessageService);
    }

    public CollectedMessageList createEmptyCollectedMessageList() {
        return new NoOpCollectedMessageList();
    }

    @Override
    public CollectedMessage createCollectedMessage(MessageIdentifier deviceIdentifier) {
        DeviceProtocolMessage deviceProtocolMessage = new DeviceProtocolMessage(deviceIdentifier);
        deviceProtocolMessage.setSentDate(clock.instant());
        return deviceProtocolMessage;
    }

    @Override
    public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier) {
        return new DeviceRegisterList(deviceIdentifier);
    }

    @Override
    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileName, String fileExtension, byte[] contents) {
        return new DeviceUserFileConfigurationInformation(deviceIdentifier, fileName, fileExtension, contents);
    }

    @Override
    public CollectedDeviceInfo createDeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object propertyValue, String propertyName) {
        return new DeviceConnectionProperty(deviceIdentifier, propertyValue, propertyName);
    }

    @Override
    public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        return new DeviceProtocolProperty(deviceIdentifier, propertyName, propertyValue);
    }

    @Override
    public CollectedDeviceInfo createCollectedDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        return new DeviceDialectProperty(deviceIdentifier, propertyName, propertyValue);
    }

    @Override
    public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
        return new DeviceProtocolMessageAcknowledgement(messageIdentifier);
    }

    @Override
    public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgementFromSms(MessageIdentifier messageIdentifier) {
        return new CTRDeviceProtocolMessageAcknowledgement(messageIdentifier);
    }

    @Override
    public CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceFirmwareVersion(deviceIdentifier);
    }

    @Override
    public CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceBreakerStatus(deviceIdentifier);
    }

    @Override
    public CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceCalendar(deviceIdentifier);
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanStructure(MessageIdentifier messageIdentifier, Map<String, Object> map, String structureCAS) {
        return new UmiwanStructure(messageIdentifier, map, structureCAS);
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanProfileControl(MessageIdentifier messageIdentifier, Date startDate) {
        return new UmiwanProfileControl(messageIdentifier, startDate);
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanEventControl(MessageIdentifier messageIdentifier, Date startTime, long controlFlags, long acknowledgeFlags) {
        return new UmiwanEventControl(messageIdentifier, startTime, controlFlags, acknowledgeFlags);
    }

}