package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceCache;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 14/05/13
 * Time: 11:31
 */
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
    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        return new MaximumDemandDeviceRegister(registerIdentifier, readingTypeMRID);
    }

    @Override
    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        return new AdapterDeviceRegister(registerIdentifier, readingTypeMRID);
    }

    @Override
    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        return new BillingDeviceRegisters(registerIdentifier, readingTypeMRID);
    }

    @Override
    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID) {
        return new DefaultDeviceRegister(registerIdentifier, readingTypeMRID);
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
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier) {
        return new DeviceLoadProfileConfiguration(profileObisCode, deviceIdentifier);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, boolean supported) {
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
    public CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceBreakerStatus(deviceIdentifier);
    }

    @Override
    public CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier) {
        return new DeviceCalendar(deviceIdentifier);
    }

}