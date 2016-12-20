package com.energyict.mdc.protocol.api.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.PropertySpec;
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

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/05/13
 * Time: 16:30
 */
@ProviderType
public interface CollectedDataFactory {

    CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier);

    CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier);

    CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, String readingTypeMRID);

    CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier);

    CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile);

    CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters);

    CollectedMessage createCollectedMessageTopology(MessageIdentifier messageIdentifier, CollectedTopology collectedTopology);

    CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier);

    CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages);

    CollectedMessageList createEmptyCollectedMessageList();

    CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier);

    CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier);

    CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, boolean supported);

    CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents);

    CollectedData createCollectedAddressProperties(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName);

    CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue);

    CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier);

}