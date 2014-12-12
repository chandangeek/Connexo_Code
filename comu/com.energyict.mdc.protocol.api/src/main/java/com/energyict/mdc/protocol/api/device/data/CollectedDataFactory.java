package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/05/13
 * Time: 16:30
 */
public interface CollectedDataFactory {

    public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier);

    public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier);

    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType);

    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, ReadingType readingType);

    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType);

    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType);

    public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier);

    public CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier);

    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile);

    public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters);

    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier);

    public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages);

    public CollectedMessageList createEmptyCollectedMessageList();

    public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier);

    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier);

    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier, boolean supported);

    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents);

    public CollectedData createCollectedAddressProperties(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName);

    public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue);

}