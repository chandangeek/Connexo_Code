package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.messaging.Messaging;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;
import com.energyict.protocols.impl.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.impl.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.protocols.impl.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.protocols.impl.channels.sms.ProximusSmsComChannel;
import com.energyict.protocols.mdc.protocoltasks.CTRDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.Bus;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author: sva
 * @since: 16/10/12 (10:10)
 */
public class MTU155 implements DeviceProtocol {

    private final DeviceProtocolSecurityCapabilities securityCapabilities = new Mtu155SecuritySupport();

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
    private PropertySpecService propertySpecService;

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
    public List<CollectedLoadProfile>
    getLoadProfileData(List<LoadProfileReader> loadProfiles) {
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
        List<DeviceProtocolDialect> dialects = new ArrayList<>(1);
        dialects.add(new CTRDeviceProtocolDialect());
        return dialects;
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

    /**
     * @return
     */
    protected GprsObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new GprsObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    public Messaging getMessaging() {
        if (messaging == null) {
            this.messaging = new Messaging(this);
        }
        return messaging;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology deviceTopology = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newWarning(getOfflineDevice(), "devicetopologynotsupported"));
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
            this.properties = new MTU155Properties(allProperties, propertySpecService);
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
            collectedLogBook = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            if (logBook.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                try {
                    Date lastLogBookReading = logBook.getLastLogBook();
                    CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
                    List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(
                            meterEvent.getMeterEvents(lastLogBookReading));

                    collectedLogBook.setMeterEvents(meterProtocolEvents);
                } catch (CTRException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newProblem(logBook, "logBookXissue", logBook.getLogBookObisCode(), e));
                }

                collectedLogBooks.add(collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode()));
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
    public String getSecurityRelationTypeName() {
        return this.getClass().getSimpleName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        return securityCapabilities.getSecurityPropertySpec(name);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        Mtu155SecuritySupport mtu155SecuritySupport = new Mtu155SecuritySupport();
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
        connectionTypes.add(new CTRInboundDialHomeIdConnectionType(Bus.getPropertySpecService()));
        connectionTypes.add(new InboundProximusSmsConnectionType(Bus.getPropertySpecService()));
        connectionTypes.add(new OutboundProximusSmsConnectionType(Bus.getPropertySpecService()));
        connectionTypes.add(new SioOpticalConnectionType(Bus.getSerialComponentService()));
        connectionTypes.add(new RxTxOpticalConnectionType(Bus.getSerialComponentService()));
        return connectionTypes;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return null;
    }

    @Override
    public PropertySpec getPropertySpec(String s) {
        return null;
    }
}
