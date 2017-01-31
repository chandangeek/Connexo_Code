/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineCalendar;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.impl.channels.inbound.CTRInboundDialHomeIdConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.impl.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.protocols.impl.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.protocols.impl.channels.sms.ProximusSmsComChannel;
import com.energyict.protocols.mdc.protocoltasks.CTRDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.messaging.Messaging;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 16/10/12 (10:10)
 */
public class MTU155 implements DeviceProtocol {

    private final DeviceProtocolSecurityCapabilities securityCapabilities;
    private final CollectedDataFactory collectedDataFactory;
    private final LoadProfileFactory loadProfileFactory;

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
    private GprsObisCodeMapper obisCodeMapper;
    private LoadProfileBuilder loadProfileBuilder;
    private Messaging messaging;

    private final Clock clock;
    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final TopologyService topologyService;
    private final MeteringService meteringService;
    private final Provider<Mtu155SecuritySupport> mtu155SecuritySupportProvider;

    @Inject
    public MTU155(
            CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory, Clock clock,
            Thesaurus thesaurus, PropertySpecService propertySpecService, SerialComponentService serialComponentService,
            IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService,
            TopologyService topologyService, MeteringService meteringService,
            Provider<Mtu155SecuritySupport> mtu155SecuritySupportProvider) {
        this.collectedDataFactory = collectedDataFactory;
        this.loadProfileFactory = loadProfileFactory;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.readingTypeUtilService = readingTypeUtilService;
        this.topologyService = topologyService;
        this.meteringService = meteringService;
        this.mtu155SecuritySupportProvider = mtu155SecuritySupportProvider;
        this.securityCapabilities = mtu155SecuritySupportProvider.get();
        this.propertySpecService = propertySpecService;
        this.serialComponentService = serialComponentService;
        this.issueService = issueService;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.copyProperties(offlineDevice.getAllProperties());
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
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache == null) {
            this.deviceCache = new CTRDeviceProtocolCache();
        } else {
            this.deviceCache = deviceProtocolCache;
        }
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        if (requestFactory instanceof SmsRequestFactory) {
            ((CTRDeviceProtocolCache) this.deviceCache).setSmsWriteDataBlockID((requestFactory).getWriteDataBlockID());
        }
        return this.deviceCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().getMeterInfo().setTime(timeToSet);
        } catch (CTRException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public Date getTime() {
        try {
            return getRequestFactory().getMeterInfo().getTime();
        } catch (CTRException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new CTRDeviceProtocolDialect(this.thesaurus, this.propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(dialectProperties); // this will add the dialectProperties to the deviceProperties
        } else {
            this.allProperties = dialectProperties;
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> rtuRegisters) {
        return getObisCodeMapper().readRegisters(rtuRegisters);
    }

    protected GprsObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new GprsObisCodeMapper(this, this.readingTypeUtilService, this.getIssueService(), this.collectedDataFactory);
        }
        return obisCodeMapper;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.issueService, this.collectedDataFactory);
        }
        return loadProfileBuilder;
    }

    public Messaging getMessaging() {
        if (messaging == null) {
            this.messaging = new Messaging(this, clock, this.topologyService, this.issueService, this.collectedDataFactory, this.loadProfileFactory);
        }
        return messaging;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(
                ResultType.NotSupported,
                getIssueService().newWarning(
                        getOfflineDevice(),
                        com.energyict.mdc.protocol.api.MessageSeeds.DEVICETOPOLOGY_NOT_SUPPORTED));
        return deviceTopology;
    }

    private IssueService getIssueService() {
        return this.issueService;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return getOfflineDevice().getDeviceIdentifier();
    }

    @Override
    public String getProtocolDescription() {
        return "Elster MTU155 CTR";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-05-05 13:34:19 +0200 (Mon, 05 May 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.allProperties.setAllProperties(properties);
    }

    public MTU155Properties getMTU155Properties() {
        if (this.properties == null) {
            this.properties = new MTU155Properties(this.allProperties, this.propertySpecService, this.thesaurus);
        }
        return this.properties;
    }

    private void updateRequestFactory(ComChannel comChannel) {
        if (comChannel instanceof ProximusSmsComChannel) {
            this.requestFactory = new SmsRequestFactory(
                    comChannel,
                    getLogger(),
                    getMTU155Properties(),
                    getTimeZone(),
                    ((CTRDeviceProtocolCache) deviceCache).getSmsWriteDataBlockID(),
                    false, propertySpecService, thesaurus);

        } else {
            this.requestFactory = new GprsRequestFactory(
                    comChannel,
                    getLogger(),
                    getMTU155Properties(),
                    getTimeZone(),
                    false,
                    this.propertySpecService,
                    this.thesaurus);
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
            if (logBook.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                try {
                    Date lastLogBookReading = Date.from(logBook.getLastLogBook());
                    CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
                    List<MeterProtocolEvent> meterProtocolEvents =
                            MeterEvent.mapMeterEventsToMeterProtocolEvents(
                                meterEvent.getMeterEvents(lastLogBookReading),
                                this.meteringService);

                    collectedLogBook.setMeterEvents(meterProtocolEvents);
                } catch (CTRException e) {
                    collectedLogBook.setFailureInformation(
                            ResultType.InCompatible,
                            getIssueService().newProblem(
                                    logBook,
                                    com.energyict.mdc.protocol.api.MessageSeeds.LOGBOOK_ISSUE,
                                    logBook.getLogBookObisCode(), e));
                }

                collectedLogBooks.add(collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        getIssueService().newWarning(
                                logBook,
                                com.energyict.mdc.protocol.api.MessageSeeds.LOGBOOK_NOT_SUPPORTED,
                                logBook.getLogBookObisCode()));
                collectedLogBooks.add(collectedLogBook);
            }
        }
        return collectedLogBooks;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return this.securityCapabilities.getCustomPropertySet();
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
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        Mtu155SecuritySupport mtu155SecuritySupport = mtu155SecuritySupportProvider.get();
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
        connectionTypes.add(new CTRInboundDialHomeIdConnectionType(thesaurus, getPropertySpecService()));
        connectionTypes.add(new InboundProximusSmsConnectionType(getPropertySpecService(), thesaurus));
        connectionTypes.add(new OutboundProximusSmsConnectionType(getPropertySpecService(), thesaurus));
        connectionTypes.add(new SioOpticalConnectionType(getSerialComponentService(), this.thesaurus));
        connectionTypes.add(new RxTxOpticalConnectionType(getSerialComponentService(), this.thesaurus));
        return connectionTypes;
    }

    private SerialComponentService getSerialComponentService() {
        return this.serialComponentService;
    }

    private PropertySpecService getPropertySpecService() {
        return this.propertySpecService;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = collectedDataFactory.createFirmwareVersionsCollectedData(getDeviceIdentifier());
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getRequestFactory().getIdentificationStructure().getVf().getValue(0).getStringValue());
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
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
        return this.offlineDevice
                .getCalendars()
                .stream()
                .filter(each -> each.getMRID().equals(tariffSchemaId))
                .findFirst()
                .map(OfflineCalendar::getName);
    }

}