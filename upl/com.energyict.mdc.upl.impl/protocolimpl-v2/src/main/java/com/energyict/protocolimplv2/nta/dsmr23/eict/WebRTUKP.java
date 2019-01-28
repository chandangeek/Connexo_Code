package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
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
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.*;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
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

/**
 * General error handling principle:
 * A DataAccessResultException, a ProtocolException or an ExceptionResponseException indicate we received an error from the meter.
 * E.g. the requested object does not exist, or it is not allowed to be read/written, etc.
 * These exceptions need to be caught and handled first! They all extend from IOException.
 * After that, all remaining IOExceptions are related to communication problems (e.g. timeout, connection broken,...).
 * <p>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 11:40
 * Author: khe
 */
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
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, getProperDeviceId()));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
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
        return result;
    }

    // TODO: 19.03.2018 for testing purposes
    @Override
    public boolean supportsCaConfigImageVersion() {
        return true;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));

        ObisCode coreActiveFirmwareVersionObisCode = ObisCode.fromString("1.0.0.2.8.255");
        try {
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(coreActiveFirmwareVersionObisCode).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveMeterFirmwareVersion(fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(coreActiveFirmwareVersionObisCode, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }   //Else a communication exception is thrown
        }

        // TODO: 19.03.2018 for testing purposes
        try {
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(coreActiveFirmwareVersionObisCode).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveCaConfigImageVersion(fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(coreActiveFirmwareVersionObisCode, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }   //Else a communication exception is thrown
        }

        ObisCode moduleActiveFirmwareVersionObisCode = ObisCode.fromString("1.1.0.2.8.255");
        try {
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(moduleActiveFirmwareVersionObisCode).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveCommunicationFirmwareVersion(fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(moduleActiveFirmwareVersionObisCode, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }   //Else a communication exception is thrown
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
        return "$Date: 2016-12-06 14:40:26 +0100 (Tue, 06 Dec 2016)$";
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

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(final ObisCode obisCode, final String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }

        if ((address == 0 && obisCode.getB() != -1 && obisCode.getB() != 128)) { // then don't correct the obisCode
            return obisCode;
        }

        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }

//    public ObisCode getPhysicalAddressCorrectedObisCode(final ObisCode obisCode, final String serialNumber) {
//        int address;
//
//        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
//            address = 0;
//        } else {
//            address = getPhysicalAddressFromSerialNumber(serialNumber);
//        }
//        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
//            return obisCode;
//        }
//        if (address != -1) {
//            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
//        }
//        return null;
//    }
}