package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
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
import com.energyict.mdc.upl.security.CertificateAlias;

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
    private volatile IdentificationService identificationService;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
    public CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateAlias> certificateAliases) {
        return null;
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
    public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents) {
        return new DeviceUserFileConfigurationInformation(deviceIdentifier, fileExtension, contents);
    }

    @Override
    public CollectedDeviceInfo createDeviceIpAddress(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName) {
        return new DeviceIpAddress(deviceIdentifier, ipAddress, ipAddressPropertyName);
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

}