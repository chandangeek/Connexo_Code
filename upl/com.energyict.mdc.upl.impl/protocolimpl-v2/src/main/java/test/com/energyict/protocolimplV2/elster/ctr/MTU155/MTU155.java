package test.com.energyict.protocolimplV2.elster.ctr.MTU155;

import com.energyict.cbo.BusinessException;
import com.energyict.comserver.adapters.common.ComChannelInputStreamAdapter;
import com.energyict.comserver.adapters.common.ComChannelOutputStreamAdapter;
import com.energyict.comserver.exceptions.LegacyProtocolException;
import com.energyict.comserver.issues.Problem;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.LogBook;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.meterdata.identifiers.RegisterDataIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.ServerComChannel;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.SerialNumberDeviceIdentifier;
import com.energyict.mdc.shadow.messages.DeviceMessageShadow;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.offline.OfflineRtu;
import com.energyict.mdw.offline.OfflineRtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import test.com.energyict.mdc.tasks.CtrDeviceProtocolDialect;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.events.CTRMeterEvent;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRException;

import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * The offline rtu
     */
    private OfflineRtu offlineRtu;

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
    private SerialNumberDeviceIdentifier deviceIdentifier;
    private ObisCodeMapper obisCodeMapper;
    private LoadProfileBuilder loadProfileBuilder;

    @Override
    public void init(OfflineRtu offlineDevice, ComChannel comChannel) {
        this.offlineRtu = offlineDevice;
        updateRequestFactory(comChannel);
    }

    @Override
    public void terminate() {
        // not needed
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
            throw new LegacyProtocolException(e);
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
            throw new LegacyProtocolException(e);
        }
    }

    @Override
    public List<CollectedLogBook> getMeterEvents(List<LogBook> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<CollectedLogBook>(1);
        CollectedLogBook collectedLogBook;

        LogBook logBook = logBooks.get(0);
        try {
            Date lastLogBookReading = logBook.getLastLogBookReading();
            CTRMeterEvent meterEvent = new CTRMeterEvent(getRequestFactory());
            List<MeterEvent> meterEvents = meterEvent.getMeterEvents(lastLogBookReading);

            collectedLogBook = new DeviceLogBook(0, ResultType.Supported);  //ToDo: id?
            ((DeviceLogBook) collectedLogBook).setMeterEvents(meterEvents);
        } catch (CTRException e) {
            collectedLogBook = new DeviceLogBook(0, ResultType.InCompatible);//ToDO: id?
            collectedLogBook.setFailureInformation(ResultType.InCompatible, new Problem<LogBook>(logBook, "logBookXissue", null, e));  //ToDo: replace 'null' by the correct representation of the logBook (e.g.: ObisCode)?
        } catch (CommunicationException e) {                                                                                           //ToDO: add DB key to todo-resources.sql
            collectedLogBook = new DeviceLogBook(0, ResultType.Other); //Todo: id?
            collectedLogBook.setFailureInformation(ResultType.Other, new Problem<LogBook>(logBook, "logBookXBlockingIssue", null, e));  //ToDo: replace 'null' by the correct representation of the logBook (e.g.: ObisCode)?
        }                                                                                                                               //ToDO: add DB key to todo-resources.sql

        collectedLogBooks.add(collectedLogBook);
        return collectedLogBooks;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedStandardMessages() {
        return null;  //ToDo
    }

    @Override
    public CollectedMessage executePendingMessages(List<DeviceMessageShadow> pendingMessages) {
        return null;  //ToDo
    }

    @Override
    public CollectedData updateSentMessages(List<DeviceMessageShadow> sentMessages) {
        return null;  //ToDo
    }

    @Override
    public void upgradeMessagesAndCategories() throws BusinessException, SQLException {
        //ToDo
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
    public List<CollectedRegister> readRegisters(List<OfflineRtuRegister> rtuRegisters) {
        /** While reading one of the registers, a blocking issue was encountered; this indicates it makes no sense to try to read out the other registers **/
        boolean blockingIssueEncountered = false;
        /** The blocking Communication Exception **/
        CommunicationException blockingIssue = null;

        List<CollectedRegister> collectedRegisters = new ArrayList<CollectedRegister>();
        for (OfflineRtuRegister register : rtuRegisters) {
            if (!blockingIssueEncountered) {
                try {
                    RegisterValue registerValue = getObisCodeMapper().readRegister(register.getObisCode());

                    MaximumDemandDeviceRegister deviceRegister = new MaximumDemandDeviceRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } catch (NoSuchRegisterException e) {   // Register with obisCode ... is not supported.
                    DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getRegisterIdentifier(register));
                    defaultDeviceRegister.setFailureInformation(ResultType.NotSupported, new Problem<ObisCode>(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
                    collectedRegisters.add(defaultDeviceRegister);
                } catch (CTRException e) {  // See list of possible error messages
                    DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getRegisterIdentifier(register));
                    defaultDeviceRegister.setFailureInformation(ResultType.InCompatible, new Problem<ObisCode>(register.getObisCode(), "registerXissue", register.getObisCode(), e));
                    collectedRegisters.add(defaultDeviceRegister);
                } catch (CommunicationException e) {    // CommunicationException.numberOfRetriesReached or CommunicationException.cipheringException
                    blockingIssueEncountered = true;
                    blockingIssue = e;
                    DefaultDeviceRegister deviceRegister = createBlockingIssueDeviceRegister(register, blockingIssue);
                    collectedRegisters.add(deviceRegister);
                }
            } else {
                DefaultDeviceRegister deviceRegister = createBlockingIssueDeviceRegister(register, blockingIssue);
                collectedRegisters.add(deviceRegister);
            }
        }
        return collectedRegisters;
    }

    /** Note: All possible CTR error messages occuring when reading registers:
     * Installation date cannot be read as a regular register.
     * Expected requestedId but received receivedId while reading registers.
     * Expected ... ResponseStructure but was
     * Query for register with id: " + idObject.toString() + " failed. Meter response was empty
     * Expected RegisterResponseStructure but was ...
     * Received no suitable data for this register
     *
     * Invalid Data: Qualifier was 0xFF at register reading for ID: regMap (Obiscode: obisCode.)
     * Invalid Measurement at register reading for ID: regMap (Obiscode: obisCode.)
     * Meter is subject to maintenance at register reading for ID: regMap (Obiscode: obisCode.)
     * Qualifier is 'Reserved' at register reading for ID: regMap (Obiscode: obisCode.)
     *
     * CTRParsingException
     */

    private DefaultDeviceRegister createBlockingIssueDeviceRegister(OfflineRtuRegister register, CommunicationException e) {
        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getRegisterIdentifier(register));
        CTRException cause = (CTRException) e.getMessageArguments()[0];
        defaultDeviceRegister.setFailureInformation(ResultType.Other, new Problem<ObisCode>(register.getObisCode(), "registerXBlockingIssue", register.getObisCode(), cause));
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
        final DeviceTopology deviceTopology = new DeviceTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, new Problem<Device>(getDeviceIdentifier().findDevice(), "devicetopologynotsupported"));
        return deviceTopology;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            this.deviceIdentifier = new SerialNumberDeviceIdentifier(offlineRtu.getSerialNumber());
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
        this.requestFactory = new GprsRequestFactory(new ComChannelInputStreamAdapter((ServerComChannel) comChannel),
                new ComChannelOutputStreamAdapter((ServerComChannel) comChannel),
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
        return offlineRtu.getTimeZone();
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRtuRegister offlineRtuRegister) {
        return new RegisterDataIdentifier(offlineRtuRegister.getObisCode(), new SerialNumberDeviceIdentifier(offlineRtuRegister.getSerialNumber()));
    }
}
