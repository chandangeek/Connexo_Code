/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceCache;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;

@Component(name = "com.energyict.mdc.engine.meterdata.collector", service = {CollectedDataFactory.class})
@SuppressWarnings("unused")
public class CollectedDataFactoryImpl implements CollectedDataFactory {

    private volatile Clock clock;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return new DeviceLoadProfile(loadProfileIdentifier);
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
    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        return new MaximumDemandDeviceRegister(registerIdentifier, readingType);
    }

    @Override
    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        return new AdapterDeviceRegister(registerIdentifier, readingType);
    }

    @Override
    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        return new BillingDeviceRegisters(registerIdentifier, readingType);
    }

    @Override
    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
        return new DefaultDeviceRegister(registerIdentifier, readingType);
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
    public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters) {
        DeviceProtocolMessageWithCollectedRegisterData collectedMessage = new DeviceProtocolMessageWithCollectedRegisterData(deviceIdentifier, messageIdentifier, collectedRegisters);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedMessage createCollectedMessageTopology(MessageIdentifier messageIdentifier, CollectedTopology collectedTopology) {
        DeviceProtocolMessageWithCollectedTopology collectedMessage = new DeviceProtocolMessageWithCollectedTopology(messageIdentifier, collectedTopology);
        collectedMessage.setSentDate(clock.instant());
        return collectedMessage;
    }

    @Override
    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier) {
        return new UpdatedDeviceCache(deviceIdentifier);
    }

    public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return new DeviceProtocolMessageList(offlineDeviceMessages);
    }

    public CollectedMessageList createEmptyCollectedMessageList() {
        return new NoOpCollectedMessageList();
    }

    @Override
    public CollectedMessage createCollectedMessage (MessageIdentifier deviceIdentifier) {
        DeviceProtocolMessage deviceProtocolMessage = new DeviceProtocolMessage(deviceIdentifier);
        deviceProtocolMessage.setSentDate(clock.instant());
        return deviceProtocolMessage;
    }

    @Override
    public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier) {
        return new DeviceRegisterList(deviceIdentifier);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier) {
        return new DeviceLoadProfileConfiguration(profileObisCode, deviceIdentifier);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier, boolean supported) {
        return new DeviceLoadProfileConfiguration(profileObisCode, deviceIdentifier, supported);
    }

    @Override
    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents) {
        return new DeviceUserFileConfigurationInformation(deviceIdentifier, fileExtension, contents);
    }

    @Override
    public CollectedData createCollectedAddressProperties(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName) {
        return new DeviceIpAddress(deviceIdentifier, ipAddress, ipAddressPropertyName);
    }

    @Override
    public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue) {
        return new DeviceProtocolProperty(deviceIdentifier, propertySpec, propertyValue);
    }

    @Override
    public CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceFirmwareVersion(deviceIdentifier);
    }

    @Override
    public CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier<?> deviceIdentifier) {
        return new DeviceBreakerStatus(deviceIdentifier);
    }

    @Override
    public CollectedCalendar createCalendarCollectedData(DeviceIdentifier<?> deviceIdentifier) {
        return new DeviceCalendar(deviceIdentifier);
    }

}