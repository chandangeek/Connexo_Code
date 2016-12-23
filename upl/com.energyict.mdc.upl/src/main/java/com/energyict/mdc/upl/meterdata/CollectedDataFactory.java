package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.*;
import com.energyict.mdc.upl.properties.CertificateAlias;
import com.energyict.obis.ObisCode;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/05/13
 * Time: 16:30
 */
public interface CollectedDataFactory {

    CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier);

    CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier);

    CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier);

    CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile);

    CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters);

    CollectedMessage createCollectedMessageWithLogbookData(MessageIdentifier messageIdentifier, CollectedLogBook collectedLoadProfile);

    CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier);

    CollectedMessage createCollectedMessageWithUpdateSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue);

    CollectedMessage createCollectedMessageWithUpdateGeneralProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue);

    /**
     * Only to be used for sub-CA and root-CA certificates.
     * This adds a certificate (with a given alias) in the EIServer DLMS trust store.
     * No device properties are updated with this alias though.
     */
    CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateAlias> certificateAliases);

    CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages);

    CollectedMessageList createEmptyCollectedMessageList();

    CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier);

    CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber);

    CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    CollectedDeviceInfo createCollectedDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents);

    CollectedDeviceInfo createDeviceIpAddress(DeviceIdentifier deviceIdentifier, String ipAddress, String connectionTaskPropertyName);

    CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier);

    CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgementFromSms(MessageIdentifier messageIdentifier);

    CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier);
}