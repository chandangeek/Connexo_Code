package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType;
import com.energyict.mdc.channels.sms.ServerProximusSmsComChannel;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.CTRDeviceProtocolDialect;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.messaging.Messaging;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;

import java.util.*;
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

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.addProperties(offlineDevice.getAllProperties());
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
    public List<PropertySpec> getRequiredProperties() {
        return getMTU155Properties().getRequiredGeneralProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getMTU155Properties().getOptionalGeneralProperties();
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
            throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().getMeterInfo().setTime(timeToSet);
        } catch (CTRException e) {
            throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
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
        final CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(getOfflineDevice(), "devicetopologynotsupported"));
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
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        if (this.allProperties != null) {
            this.allProperties.setAllProperties(properties); // this will add the properties to the existing properties
        } else {
            this.allProperties = properties;
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
            collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            if (logBook.getLogBookObisCode().equals(LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                try {
                    Date lastLogBookReading = logBook.getLastLogBook();
                    CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
                    List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(
                            meterEvent.getMeterEvents(lastLogBookReading));

                    collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                } catch (CTRException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(logBook, "logBookXissue", logBook.getLogBookObisCode(), e));
                }

                collectedLogBooks.add(collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode()));
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
}
