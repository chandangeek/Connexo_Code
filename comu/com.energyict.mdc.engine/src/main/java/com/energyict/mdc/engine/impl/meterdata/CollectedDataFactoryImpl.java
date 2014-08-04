package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceCache;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 14/05/13
 * Time: 11:31
 */
public class CollectedDataFactoryImpl implements CollectedDataFactory {

    private final ServiceProvider serviceProvider;

    public CollectedDataFactoryImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
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
        return new DeviceProtocolMessage(deviceIdentifier);
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

}