/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.mocks;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedDeviceCache;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageAcknowledgement;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
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

public class MockCollectedDataFactory implements CollectedDataFactory {
    @Override
    public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        return new MockLoadProfile(loadProfileIdentifier);
    }

    @Override
    public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
        return new MockCollectedLogBook();
    }

    @Override
    public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier) {
        return null;
    }

    @Override
    public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier) {
        return null;
    }

    @Override
    public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier) {
        return null;
    }

    @Override
    public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier) {
        return new MockCollectedRegister(registerIdentifier);
    }

    @Override
    public CollectedRegister createTextCollectedRegister(RegisterIdentifier registerIdentifier) {
        return new MockTextCollectedRegister(registerIdentifier);
    }

    @Override
    public CollectedRegister createDeviceTextRegister(RegisterIdentifier registerIdentifier) {
        return null;
    }

    @Override
    public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithLogbookData(MessageIdentifier messageIdentifier, CollectedLogBook collectedLoadProfile) {
        return null;
    }

    @Override
    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceCache) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageForSwappingSecurityAccessorKeys(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUpdateSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUpdateGeneralProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithFile(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String fileName, String fileExtension, byte[] contents) {
        return null;
    }

    @Override
    public CollectedCertificateWrapper createCollectedCertificateWrapper(X509Certificate x509Certificate) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateWrapper> certificateWrappers) {
        return null;
    }

    @Override
    public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
        return null;
    }

    @Override
    public CollectedMessageList createEmptyCollectedMessageList() {
        return null;
    }

    @Override
    public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier) {
        return new MockCollectedRegisterList();
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber) {
        return new MockCollectedLoadProfileConfiguration(profileObisCode, meterSerialNumber);
    }

    @Override
    public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, String meterSerialNumber) {
        return new MockCollectedLoadProfileConfiguration(profileObisCode, deviceIdentifier, meterSerialNumber);
    }

    @Override
    public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        return null;
    }

    @Override
    public CollectedDeviceInfo createCollectedDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        return null;
    }

    @Override
    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileName, String fileExtension, byte[] contents) {
        return null;
    }

    @Override
    public CollectedDeviceInfo createDeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object connectionPropertyValue, String connectionTaskPropertyName) {
        return null;
    }

    @Override
    public CollectedDeviceInfo createDeviceConnectionProperties(DeviceIdentifier deviceIdentifier, Map<String, Object> connectionPropertyNameAndValue) {
        return null;
    }

    @Override
    public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
        return null;
    }

    @Override
    public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgementFromSms(MessageIdentifier messageIdentifier) {
        return null;
    }

    @Override
    public CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedCreditAmount createCreditAmountCollectedData(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanStructure(MessageIdentifier deviceMessageIdentifierById, Map<String, Object> properties, String structureCAS) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanProfileControl(MessageIdentifier deviceMessageIdentifierById, Date startDate) {
        return null;
    }

    @Override
    public CollectedMessage createCollectedMessageWithUmiwanEventControl(MessageIdentifier deviceMessageIdentifierById, Date startTime, long controlFlags, long acknowledgeFlags) {
        return null;
    }
}
