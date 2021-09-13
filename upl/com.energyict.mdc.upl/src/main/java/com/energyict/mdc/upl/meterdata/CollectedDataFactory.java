package com.energyict.mdc.upl.meterdata;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date: 8/05/13
 * Time: 16:30
 */
@ConsumerType
public interface CollectedDataFactory {

    CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier);

    CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier);

    CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier);

    CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier);

    CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedRegister createTextCollectedRegister(RegisterIdentifier registerIdentifier);

    CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier);

    CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile);

    CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader);

    CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters);

    CollectedMessage createCollectedMessageWithLogbookData(MessageIdentifier messageIdentifier, CollectedLogBook collectedLoadProfile);

    CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier);

    CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceCache);

    CollectedMessage createCollectedMessageForSwappingSecurityAccessorKeys(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName);

    CollectedMessage createCollectedMessageWithUpdateSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue);

    CollectedMessage createCollectedMessageWithUpdateGeneralProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue);

    CollectedMessage createCollectedMessageWithFile(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String fileName, String fileExtension, byte[] contents);

    CollectedCertificateWrapper createCollectedCertificateWrapper(X509Certificate x509Certificate);

    /**
     * Only to be used for sub-CA and root-CA certificates.
     * This adds a certificate (with a given alias) in the EIServer DLMS trust store.
     * No device properties are updated with this alias though.
     */
    CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateWrapper> certificateWrappers);

    CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages);

    CollectedMessageList createEmptyCollectedMessageList();

    CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier);

    CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber);

    CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, String meterSerialNumber);

    CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    CollectedDeviceInfo createCollectedDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue);

    CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileName, String fileExtension, byte[] contents);

    CollectedDeviceInfo createDeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object connectionPropertyValue, String connectionTaskPropertyName);

    CollectedDeviceInfo createDeviceConnectionProperties(DeviceIdentifier deviceIdentifier, Map<String, Object> connectionPropertyNameAndValue);

    CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier);

    CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgementFromSms(MessageIdentifier messageIdentifier);

    CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedCreditAmount createCreditAmountCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier);

    CollectedMessage createCollectedMessageWithUmiwanStructure(MessageIdentifier deviceMessageIdentifierById, Map<String, Object> properties, String structureCAS);

    CollectedMessage createCollectedMessageWithUmiwanProfileControl(MessageIdentifier deviceMessageIdentifierById, Date startDate);

    CollectedMessage createCollectedMessageWithUmiwanEventControl(MessageIdentifier deviceMessageIdentifierById, Date startTime, long controlFlags, long acknowledgeFlags);

}