package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/05/13
 * Time: 16:30
 */
@ProviderType
public interface CollectedDataFactory {

    public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier);

    public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier);

    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier);

    public CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier);

    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile);

    public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters);

    public CollectedMessage createCollectedMessageTopology(MessageIdentifier messageIdentifier, CollectedTopology collectedTopology);

    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier);

    public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages);

    public CollectedMessageList createEmptyCollectedMessageList();

    public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier);

    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier);

    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier, boolean supported);

    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents);

    public CollectedData createCollectedAddressProperties(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName);

    public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue);

    CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier<?> deviceIdentifier);

    CollectedCalendar createCalendarCollectedData(DeviceIdentifier<?> deviceIdentifier);

}