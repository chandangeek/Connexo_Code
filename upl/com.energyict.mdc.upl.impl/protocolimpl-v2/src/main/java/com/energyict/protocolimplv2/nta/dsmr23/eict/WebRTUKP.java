/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.dsmr.OutboundTcpIpWithWakeUpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WebRTUKP extends AbstractSmartNtaProtocol {

    protected Dsmr23Messaging dsmr23Messaging;
    protected ComChannel comChannel;
    protected HHUSignOnV2 hhuSignOn;
    private Dsmr23LogBookFactory logBookFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private Dsmr23RegisterFactory registerFactory;
    private final Converter converter;
    private final NlsService nlsService;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final TariffCalendarExtractor calendarExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public WebRTUKP(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.calendarExtractor = calendarExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        setHasBreaker(false);
    }

    protected NlsService getNlsService() {return this.nlsService;}
    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {return keyAccessorTypeExtractor;}
    protected Converter getConverter () {return converter;}
    protected DeviceMessageFileExtractor getDeviceMessageFileExtractor () {return messageFileExtractor;}
    protected TariffCalendarExtractor getTariffCalendarExtractor () {return calendarExtractor;}
    protected NumberLookupExtractor getNumberLookupExtractor () {return numberLookupExtractor;}
    protected LoadProfileExtractor getLoadProfileExtractor () {return loadProfileExtractor;}


    @Override
    public void journal(String message) {
        super.journal("[WebRTUKP] " + message);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.comChannel = comChannel;
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        initDlmsSession();
    }

    protected void initDlmsSession() {
        setDlmsSession(newDlmsSession(comChannel));
    }

    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
    }

    protected HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private String getProperDeviceId() {
        String deviceId = getDlmsSessionProperties().getDeviceId();
        if (deviceId != null && !"".equalsIgnoreCase(deviceId)) {
            return deviceId;
        } else {
            return "!"; // the Kamstrup device requires a '!' sign in the IEC1107 signOn
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new SioOpticalConnectionType(this.getPropertySpecService()));
        result.add(new RxTxOpticalConnectionType(this.getPropertySpecService()));
        result.add(new OutboundTcpIpWithWakeUpConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    private Dsmr23LogBookFactory getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return logBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDsmr23Messaging().executePendingMessages(pendingMessages);
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (dsmr23Messaging == null) {
            dsmr23Messaging =
                    new Dsmr23Messaging(
                            new Dsmr23MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), keyAccessorTypeExtractor),
                            this.getPropertySpecService(), this.nlsService, this.converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
        }
        return dsmr23Messaging;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus result = super.getBreakerStatus();
        if (hasBreaker()) {
            try {
                Disconnector disconnector = getDlmsSession().getCosemObjectFactory().getDisconnector();
                TypeEnum controlState = disconnector.doReadControlState();
                switch (controlState.getValue()) {
                    case 0:
                        result.setBreakerStatus(BreakerStatus.DISCONNECTED);
                        break;
                    case 1:
                        result.setBreakerStatus(BreakerStatus.CONNECTED);
                        break;
                    case 2:
                        result.setBreakerStatus(BreakerStatus.ARMED);
                        break;
                    default:
                        ObisCode source = Disconnector.getDefaultObisCode();
                        result.setFailureInformation(ResultType.InCompatible, this.getIssueFactory()
                                .createProblem(source, "issue.protocol.readingOfBreakerStateFailed", "received value '" + controlState.getValue() + "', expected either 0, 1 or 2."));
                        break;
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                    ObisCode source = Disconnector.getDefaultObisCode();
                    result.setFailureInformation(ResultType.InCompatible, this.getIssueFactory().createProblem(source, "issue.protocol.readingOfBreakerStateFailed", e.toString()));
                }
            }
        }
        return result;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {

        if (serialNumber!=null){
            if (!serialNumber.equals(getSerialNumber())){
                return collectSlaveFirmwareVersions(serialNumber);
            }
        }

        CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));

        collectFirmwareVersionMeterCore(result);

        if (supportsCommunicationFirmwareVersion()) {
            collectFirmwareVersionCommunicationModule(result);
        }

        if (supportsAuxiliaryFirmwareVersion()) {
            collectFirmwareVersionAuxiliary(result);
        }

        return result;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(this.getPropertySpecService(), nlsService), new TcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService));
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return null;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    public Dsmr23RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-04-15$";
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) V2";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

}