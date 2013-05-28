package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.CtrDeviceProtocolDialect;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;
import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.security.Mtu155SecuritySupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author: sva
 * @since: 16/10/12 (10:10)
 */
public class MTU155 implements DeviceProtocol {

    public static final String DEBUG_PROPERTY_NAME = "Debug";
    public static final String CHANNEL_BACKLOG_PROPERTY_NAME = "ChannelBacklog";
    public static final String EXTRACT_INSTALLATION_DATE_PROPERTY_NAME = "ExtractInstallationDate";
    public static final String REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME = "RemoveDayProfileOffset";

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
    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private ObisCodeMapper obisCodeMapper;
    private LoadProfileBuilder loadProfileBuilder;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
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
        List<PropertySpec> required = new ArrayList<PropertySpec>();
        return required;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<PropertySpec>();
        optional.add(debugPropertySpec());
        optional.add(channelBacklogPropertySpec());
        optional.add(extractInstallationDatePropertySpec());
        optional.add(removeDayProfileOffsetPropertySpec());
        return optional;
    }

    private PropertySpec debugPropertySpec() {
        return PropertySpecFactory.booleanPropertySpec(DEBUG_PROPERTY_NAME);
    }

    private PropertySpec channelBacklogPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(CHANNEL_BACKLOG_PROPERTY_NAME);
    }

    private PropertySpec extractInstallationDatePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(EXTRACT_INSTALLATION_DATE_PROPERTY_NAME);
    }

    private PropertySpec removeDayProfileOffsetPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME);
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
        this.deviceCache = deviceProtocolCache;     // Remark: for CTR protocol, the cache object is not used and is always empty.
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getRequestFactory().getMeterInfo().setTime(timeToSet);
        } catch (CTRException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
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
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return null;  //ToDo
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;  //ToDo
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;  //ToDo
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<DeviceProtocolDialect>(1);
        dialects.add(new CtrDeviceProtocolDialect());
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
        /** While reading one of the registers, a blocking issue was encountered; this indicates it makes no sense to try to read out the other registers **/
        boolean blockingIssueEncountered = false;
        /** The blocking Communication Exception **/
        ComServerExecutionException blockingIssue = null;

        List<CollectedRegister> collectedRegisters = new ArrayList<CollectedRegister>();
        for (OfflineRegister register : rtuRegisters) {
            if (!blockingIssueEncountered) {
                try {
                    RegisterValue registerValue = getObisCodeMapper().readRegister(register.getObisCode());

                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } catch (NoSuchRegisterException e) {   // Register with obisCode ... is not supported.
                    CollectedRegister defaultDeviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
                    defaultDeviceRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
                    collectedRegisters.add(defaultDeviceRegister);
                } catch (CTRException e) {  // See list of possible error messages
                    CollectedRegister defaultDeviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
                    defaultDeviceRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(register.getObisCode(), "registerXissue", register.getObisCode(), e));
                    collectedRegisters.add(defaultDeviceRegister);
                } catch (ComServerExecutionException e) {    // CommunicationException.numberOfRetriesReached or CommunicationException.cipheringException
                    blockingIssueEncountered = true;
                    blockingIssue = e;
                    CollectedRegister deviceRegister = createBlockingIssueDeviceRegister(register, blockingIssue);
                    collectedRegisters.add(deviceRegister);
                }
            } else {
                CollectedRegister deviceRegister = createBlockingIssueDeviceRegister(register, blockingIssue);
                collectedRegisters.add(deviceRegister);
            }
        }
        return collectedRegisters;
    }

    /**
     * Note: All possible CTR error messages occuring when reading registers:
     * Installation date cannot be read as a regular register.
     * Expected requestedId but received receivedId while reading registers.
     * Expected ... ResponseStructure but was
     * Query for register with id: " + idObject.toString() + " failed. Meter response was empty
     * Expected RegisterResponseStructure but was ...
     * Received no suitable data for this register
     * <p/>
     * Invalid Data: Qualifier was 0xFF at register reading for ID: regMap (Obiscode: obisCode.)
     * Invalid Measurement at register reading for ID: regMap (Obiscode: obisCode.)
     * Meter is subject to maintenance at register reading for ID: regMap (Obiscode: obisCode.)
     * Qualifier is 'Reserved' at register reading for ID: regMap (Obiscode: obisCode.)
     * <p/>
     * CTRParsingException
     */

    private CollectedRegister createBlockingIssueDeviceRegister(OfflineRegister register, ComServerExecutionException e) {
        CollectedRegister defaultDeviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        CTRException cause = (CTRException) e.getCause();
        defaultDeviceRegister.setFailureInformation(ResultType.Other, MdcManager.getIssueCollector().addProblem(register.getObisCode(), "registerXBlockingIssue", register.getObisCode(), cause));
        return defaultDeviceRegister;
    }

    /**
     * @return
     */
    protected ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(getRequestFactory());
        }
        return obisCodeMapper;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(getDeviceIdentifier().findDevice(), "devicetopologynotsupported"));
        return deviceTopology;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(offlineDevice.getSerialNumber());
        }
        return deviceIdentifier;
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
        this.requestFactory = new GprsRequestFactory(new ComChannelInputStreamAdapter(comChannel),
                new ComChannelOutputStreamAdapter(comChannel),
                getLogger(),
                getMTU155Properties(),
                getTimeZone());
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Logger getLogger() {       //ToDo: this is a temporary solution - should be removed again & substituted by a proper replacement
        if (protocolLogger == null) {
            protocolLogger = Logger.getLogger(this.getClass().getName());
        }
        return protocolLogger;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone((String) offlineDevice.getAllProperties().getProperty("TimeZone")); // TODO should make sure the timeZone is defined as property
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), new DeviceIdentifierBySerialNumber(offlineRtuRegister.getSerialNumber()));
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<CollectedLogBook>(1);
        CollectedLogBook collectedLogBook;

        LogBookReader logBook = logBooks.get(0);
        try {
            Date lastLogBookReading = logBook.getLastLogBook();
            CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
            List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(
                    meterEvent.getMeterEvents(lastLogBookReading));
            collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            collectedLogBook.setMeterEvents(meterProtocolEvents);
        } catch (CTRException e) {
            collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addProblem(logBook, "logBookXissue", null, e));  //ToDo: replace 'null' by the correct representation of the logBook (e.g.: ObisCode)?
        } catch (ComServerExecutionException e) {                                                                                           //ToDO: add DB key to todo-resources.sql
            collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
            collectedLogBook.setFailureInformation(ResultType.Other, MdcManager.getIssueCollector().addProblem(logBook, "logBookXBlockingIssue", null, e));  //ToDo: replace 'null' by the correct representation of the logBook (e.g.: ObisCode)?
        }                                                                                                                               //ToDO: add DB key to todo-resources.sql

        collectedLogBooks.add(collectedLogBook);
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
        //TODO provide proper functionality so your protocol can make proper use of the security properties
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>();
    }
}
