package com.energyict.protocolimplv2.edp;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ProtocolExceptionReference;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.edp.logbooks.LogbookReader;
import com.energyict.protocolimplv2.edp.messages.EDPMessageExecutor;
import com.energyict.protocolimplv2.edp.messages.EDPMessaging;
import com.energyict.protocolimplv2.edp.registers.RegisterReader;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 5/02/14
 * Time: 11:34
 * Author: khe
 */
public class CX20009 extends AbstractDlmsProtocol {

    private LogbookReader logbookReader = null;
    private RegisterReader registerReader;
    private EDPMessaging edpMessaging;
    private LoadProfileBuilder loadProfileBuilder;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public CX20009(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new EDPDlmsConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    /**
     * Class that holds all DLMS device properties (general, dialect & security related)
     */
    @Override
    public EDPProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new EDPProperties();
        }
        return (EDPProperties) dlmsProperties;
    }

    @Override
    public void logOn() {
        try {
            getDlmsSession().connect();
            checkCacheObjects();
        } catch (ProtocolRuntimeException e) {
            if (e instanceof ConnectionCommunicationException && (e.getExceptionReference().equals(ProtocolExceptionReference.PROTOCOL_CONNECT))) {
                logOff();
                getDlmsSession().connect();
                checkCacheObjects();
            } else {
                throw e;
            }
        }
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-05-09 09:38:14 +0300 (Mon, 09 May 2016)$";
    }

    /**
     * Direct electrical communication (RS485 interface) or TCP/IP (GPRS modem)
     */
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new SioSerialConnectionType(this.getPropertySpecService()));
        result.add(new RxTxSerialConnectionType(this.getPropertySpecService()));
        result.add(new SioAtModemConnectionType(this.getPropertySpecService()));
        result.add(new RxTxAtModemConnectionType(this.getPropertySpecService()));
        return result;
    }

    /**
     * Only disconnect the connection layer, the meter does not support release requests
     */
    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().getDlmsV2Connection().disconnectMAC();
        }
    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom CX2000-9 DLMS";
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<com.energyict.protocol.LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<com.energyict.protocol.LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogbookReader().getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(this.getPropertySpecService()), new TcpDeviceProtocolDialect(this.getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterReader().readRegisters(registers);
    }

    /**
     * No slave meters are supported, it's only the e-meter
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        return this.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    private LogbookReader getLogbookReader() {
        if (logbookReader == null) {
            logbookReader = new LogbookReader(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return logbookReader;
    }

    private EDPMessaging getMessaging() {
        if (edpMessaging == null) {
            edpMessaging =
                    new EDPMessaging(
                            this.getPropertySpecService(),
                            this.nlsService,
                            this.converter,
                            new EDPMessageExecutor(
                                    this,
                                    this.getCollectedDataFactory(),
                                    this.getIssueFactory()),
                            this.calendarExtractor,
                            this.messageFileExtractor);
        }
        return edpMessaging;
    }

    public RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerReader;
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