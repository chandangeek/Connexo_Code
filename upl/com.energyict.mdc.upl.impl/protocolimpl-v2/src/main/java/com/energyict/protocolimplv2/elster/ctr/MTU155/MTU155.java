package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.ServerProximusSmsComChannel;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.CTRDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.messaging.Messaging;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 16/10/12 (10:10)
 */
public class MTU155 implements DeviceProtocol, SerialNumberSupport {

    private final DeviceProtocolSecurityCapabilities securityCapabilities;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final Extractor extractor;

    /**
     * The offline rtu
     */
    private OfflineDevice offlineDevice;

    /**
     * Collection of all TypedProperties.
     */
    private MTU155Properties properties;

    /**
     * The Cache of the current RTU
     */
    private DeviceProtocolCache deviceCache;

    /**
     * The request factory, to be used to communicate with the MTU155
     */
    private RequestFactory requestFactory;

    /**
     * Legacy logger
     */
    private Logger protocolLogger;
    private TypedProperties allProperties;
    private DeviceIdentifier deviceIdentifier;
    private GprsObisCodeMapper obisCodeMapper;
    private LoadProfileBuilder loadProfileBuilder;
    private Messaging messaging;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public MTU155(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.extractor = extractor;
        this.securityCapabilities = new Mtu155SecuritySupport(propertySpecService);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.setProperties(offlineDevice.getAllProperties());
        updateRequestFactory(comChannel);
    }

    @Override
    public void terminate() {
        // not needed
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.getMTU155Properties().getPropertySpecs(this.propertySpecService);
    }

    @Override
    public void logOn() {
        // not needed
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        if (getMTU155Properties().isSendEndOfSession()) {
            getRequestFactory().sendEndOfSession();
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    /**
     *  Read out the serial number of the device
     *  Note: This reads out the serial number of the Convertor
     *  The serial numbers of MTU155 and attached Gas device are not read/checked!
     **/
    public String getSerialNumber() {
        return getRequestFactory().getMeterInfo().getConverterSerialNumber();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        if (requestFactory instanceof SmsRequestFactory) {
            ((CTRDeviceProtocolCache) this.deviceCache).setSmsWriteDataBlockID((requestFactory).getWriteDataBlockID());
        }
        return this.deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache == null) {
            this.deviceCache = new CTRDeviceProtocolCache();
        } else {
            this.deviceCache = deviceProtocolCache;
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile>
    getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public Date getTime() {
        try {
            return getRequestFactory().getMeterInfo().getTime();
        } catch (CTRException e) {
            throw CommunicationException.unexpectedResponse(e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().getMeterInfo().setTime(timeToSet);
        } catch (CTRException e) {
            throw CommunicationException.unexpectedResponse(e);
        }
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>(1);
        dialects.add(new CTRDeviceProtocolDialect());
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(dialectProperties); // this will add the dialectProperties to the deviceProperties
        } else {
            this.allProperties = TypedProperties.copyOf(dialectProperties);
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> rtuRegisters) {
        return getObisCodeMapper().readRegisters(rtuRegisters);
    }

    protected GprsObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new GprsObisCodeMapper(this, this.collectedDataFactory, this.issueFactory);
        }
        return obisCodeMapper;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, collectedDataFactory, issueFactory);
        }
        return loadProfileBuilder;
    }

    public Messaging getMessaging() {
        if (messaging == null) {
            this.messaging = new Messaging(this, collectedDataFactory, issueFactory, propertySpecService, nlsService, converter, extractor);
        }
        return messaging;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(getOfflineDevice(), "devicetopologynotsupported"));
        return deviceTopology;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        }
        return deviceIdentifier;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster MTU155 CTR";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-12-06 13:29:40 +0100 (Tue, 06 Dec 2016)$";
    }

    @Override
    public void setProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(TypedProperties.copyOf(properties)); // this will add the properties to the existing properties
        } else {
            this.allProperties = TypedProperties.copyOf(properties);
        }
    }

    public MTU155Properties getMTU155Properties() {
        if (this.properties == null) {
            this.properties = new MTU155Properties(allProperties);
        }
        return this.properties;
    }

    private void updateRequestFactory(ComChannel comChannel) {
        if (comChannel instanceof ServerProximusSmsComChannel) {
            this.requestFactory = new SmsRequestFactory(
                    comChannel,
                    getLogger(),
                    getMTU155Properties(),
                    getTimeZone(),
                    ((CTRDeviceProtocolCache) deviceCache).getSmsWriteDataBlockID(),
                    false);

        } else {
            this.requestFactory = new GprsRequestFactory(
                    comChannel,
                    getLogger(),
                    getMTU155Properties(),
                    getTimeZone(),
                    false
            );
        }
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Logger getLogger() {
        if (protocolLogger == null) {
            protocolLogger = Logger.getLogger(this.getClass().getName());
        }
        return protocolLogger;
    }

    public TimeZone getTimeZone() {
        return getMTU155Properties().getTimeZone();
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>(1);
        CollectedLogBook collectedLogBook;

        for (LogBookReader logBook : logBooks) {
            collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBook.getLogBookIdentifier());
            if (logBook.getLogBookObisCode().equals(LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                try {
                    Date lastLogBookReading = logBook.getLastLogBook();
                    CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
                    List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(
                            meterEvent.getMeterEvents(lastLogBookReading));

                    collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                } catch (CTRException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createProblem(logBook, "logBookXissue", logBook.getLogBookObisCode(), e));
                }

                collectedLogBooks.add(collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode()));
                collectedLogBooks.add(collectedLogBook);
            }
        }
        return collectedLogBooks;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return securityCapabilities.getSecurityProperties();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return securityCapabilities.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return securityCapabilities.getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return securityCapabilities.getSecurityPropertySpec(name);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport(propertySpecService);
        TypedProperties securityProperties = mtu155SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(securityProperties); // this will add the dialectProperties to the deviceProperties
        } else {
            this.allProperties = securityProperties;
        }
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new CTRInboundDialHomeIdConnectionType());
        connectionTypes.add(new InboundProximusSmsConnectionType());
        connectionTypes.add(new OutboundProximusSmsConnectionType());
        connectionTypes.add(new SioOpticalConnectionType());
        connectionTypes.add(new RxTxOpticalConnectionType());
        return connectionTypes;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(getDeviceIdentifier());
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getRequestFactory().getIdentificationStructure().getVf().getValue(0).getStringValue());
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.collectedDataFactory.createBreakerStatusCollectedData(getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(getDeviceIdentifier());
        this.toCalendarName(this.getRequestFactory().getIdentificationStructure().getIdPT().getValue()[0].getIntValue())
                .ifPresent(collectedCalendar::setActiveCalendar);
        this.toCalendarName(this.getRequestFactory().getIdentificationStructure().getIdPT().getValue()[2].getIntValue())
                .ifPresent(collectedCalendar::setPassiveCalendar);
        return collectedCalendar;
    }

    private Optional<String> toCalendarName(int tariffSchemaId) {
        if (tariffSchemaId > 0) {
            return this.toCalendarName(String.valueOf(tariffSchemaId));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> toCalendarName(String tariffSchemaId) {
        return Optional.of(tariffSchemaId);

        //TODO uncomment the code below when OfflineCalendar is available on OfflineDevice
/*        return this.offlineDevice
                .getCalendars()
                .stream()
                .filter(each -> each.getMRID().equals(tariffSchemaId))
                .findFirst()
                .map(OfflineCalendar::getName);*/
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