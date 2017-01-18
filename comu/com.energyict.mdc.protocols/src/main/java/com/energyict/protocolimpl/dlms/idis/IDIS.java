package com.energyict.protocolimpl.dlms.idis;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.registers.IDISStoredValues;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.CacheMechanism;
import com.energyict.protocols.util.ProtocolUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/09/11
 * Time: 9:37
 */
public class IDIS extends AbstractDLMSProtocol implements MessageProtocol, CacheMechanism {

    @Override
    public String getProtocolDescription() {
        return "Elster AM500 DLMS (IDIS P1)";
    }

    public static final String CALLING_AP_TITLE = "CallingAPTitle";
    public static final String CALLING_AP_TITLE_DEFAULT = "0000000000000000";

    private static final ObisCode FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    private static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String LIMITMAXNROFDAYS_PROPERTY = "LimitMaxNrOfDays";
    private static final String LOAD_PROFILE_OBIS_CODE_PROPERTY = "LoadProfileObisCode";
    public static final String OBISCODE_LOAD_PROFILE1 = "1.0.99.1.0.255";   //Quarterly
    private static final String MAX_NR_OF_DAYS_DEFAULT = "0";
    public static final String VALIDATE_INVOKE_ID = "ValidateInvokeId";
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";
    private static final String TIMEOUT = "timeout";
    private static final String CONFIRM_UNKNOWN_METER = "CONFIRM_UNKNOWN_METER";

    private ProfileDataReader profileDataReader = null;
    private IDISMessageHandler messageHandler = null;
    private boolean readCache = false;      //Property indicating to read the cache out (useful because there's no config change state)
    private ObisCode loadProfileObisCode = ObisCode.fromString(OBISCODE_LOAD_PROFILE1);
    private IDISStoredValues storedValues = null;
    private ObisCodeMapper obisCodeMapper = null;
    private int limitMaxNrOfDays = 0;
    private final CalendarService calendarService;

    @Inject
    public IDIS(PropertySpecService propertySpecService, CalendarService calendarService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
        this.calendarService = calendarService;
    }

    protected CalendarService getCalendarService() {
        return calendarService;
    }

    private ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new ProfileDataReader(this);
        }
        return profileDataReader;
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    protected IDISMessageHandler getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new IDISMessageHandler(this, this.calendarService);
        }
        return messageHandler;
    }

    @Override
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        final Calendar newTimeToSet = Calendar.getInstance(getTimeZone());
        getCosemObjectFactory().getClock().setTimeAttr(new DateTime(newTimeToSet));
    }

    @Override
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            connectWithRetries();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e);
        }
        validateSerialNumber();
        checkCacheObjects();
    }

    private void connectWithRetries() throws IOException {
        int tries = 0;
        while (true) {
            IOException exception;
            try {
                if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    this.aso.createAssociation();
                }
                return;
            } catch (IOException e) {
                exception = e;
            } catch (DLMSConnectionException e) {
                exception = new IOException(e.getMessage());
                exception.initCause(e);
            }

            if ((exception.getMessage() != null) && exception.getMessage().toLowerCase().contains(TIMEOUT)) {
                getLogger().severe("Could not reach meter, it didn't reply to the association request! Stopping session.");
                throw exception;    //Don't retry the AARQ if it's a real timeout!
            } else if (exception.getMessage() != null && exception.getMessage().contains(CONFIRM_UNKNOWN_METER)) {
                getLogger().severe("Received error 'CONFIRM_UNKNOWN_METER' while trying to create an association, aborting session.");
                throw exception;    //Meter needs to be discovered again, stop session
            } else {
                if (++tries > retries) {
                    getLogger().severe("Unable to establish association after [" + tries + "/" + (retries + 1) + "] tries.");
                    throw new NestedIOException(exception);
                } else {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (retries + 1) + "] tries. Sending RLRQ and retry ...");
                    }
                    try {
                        this.aso.releaseAssociation();
                    } catch (IOException | DLMSConnectionException e) {
                        this.aso.setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                        // Absorb exception: in 99% of the cases we expect an exception here ...
                    }
                }
            }
        }
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        if (dlmsCache == null) {
            dlmsCache = new DLMSCache();
        }
        if (dlmsCache.getObjectList() == null || isReadCache()) {       //Don't read the object list from the meter, instead use a hardcoded copy. This is to avoid many round trips over a bad PLC connection...
            dlmsCache.saveObjectList(new IDISObjectList().getObjectList());  // save object list in cache
        }
        if (dlmsCache.getObjectList() != null) {
            dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {
            to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
            getLogger().info("getProfileData: toDate was 'null'. Changing toDate to: " + to);
        }
        ProfileData profileData = getProfileDataReader().getProfileData(new ProfileLimiter(from, to, getLimitMaxNrOfDays()), includeEvents);
        if ((profileData.getIntervalDatas().isEmpty()) && (getLimitMaxNrOfDays() > 0)) {
            profileData = getProfileDataReader().getProfileData(from, to, includeEvents);
        }
        return profileData;
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
            getLogger().warning("Using default time zone.");
        }
        return timeZone;
    }

    @Override
    protected InvokeIdAndPriorityHandler buildInvokeIdAndPriorityHandler() throws IOException {
        try {
            InvokeIdAndPriority iiap = new InvokeIdAndPriority();
            iiap.setPriority(this.iiapPriority);
            iiap.setServiceClass(this.iiapServiceClass);
            iiap.setTheInvokeId(this.iiapInvokeId);
            if (validateInvokeId()) {
                return new IncrementalInvokeIdAndPriorityHandler(iiap);
            } else {
                return new NonIncrementalInvokeIdAndPriorityHandler(iiap);
            }
        } catch (DLMSConnectionException e) {
            getLogger().info("Some configured properties are invalid. " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    protected boolean validateInvokeId() {
        return Integer.parseInt(properties.getProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) == 1;
    }

    @Override
    public void validateSerialNumber() throws IOException {
        //Not used.
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        readCache = Integer.parseInt(properties.getProperty(READCACHE_PROPERTY, READ_CACHE_DEFAULT_VALUE).trim()) == 1;
        limitMaxNrOfDays = Integer.parseInt(properties.getProperty(LIMITMAXNROFDAYS_PROPERTY, MAX_NR_OF_DAYS_DEFAULT).trim());
        String callingAPTitle = properties.getProperty(CALLING_AP_TITLE, CALLING_AP_TITLE_DEFAULT).trim();
        setCallingAPTitle(callingAPTitle);
        loadProfileObisCode = ObisCode.fromString(properties.getProperty(LOAD_PROFILE_OBIS_CODE_PROPERTY, OBISCODE_LOAD_PROFILE1).trim());

        String oldMacAddress = properties.getProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS, "1:17");
        String nodeAddress = properties.getProperty(AbstractDLMSProtocol.NODEID, "");
        String updatedMacAddress = oldMacAddress.replaceAll("x", nodeAddress);
        properties.setProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, updatedMacAddress.split(":").length > 2 ? updatedMacAddress.split(":")[1] : "17");
        properties.setProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, updatedMacAddress.split(":").length > 1 ? updatedMacAddress.split(":")[0] : "1");
    }

    private boolean isReadCache() {
        return readCache;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().readRegister(obisCode);
    }

    private ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    @Override
    protected List doGetOptionalKeys() {
        List<String> optional = new ArrayList<>();
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS); // Legacy property for migration, the protocol uses SERVER_MAC_ADDRESS property!
        optional.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS); // Legacy property for migration, the protocol uses SERVER_MAC_ADDRESS property!
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(READCACHE_PROPERTY);
        optional.add(LOAD_PROFILE_OBIS_CODE_PROPERTY);
        optional.add(CALLING_AP_TITLE);
        optional.add(DlmsProtocolProperties.ISKRA_WRAPPER);
        return optional;
    }

    @Override
    public List<String> getRequiredKeys() {
        return Arrays.asList(MeterProtocol.SERIALNUMBER);
    }

    /**
     * The protocol version
     */
    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-10-28 14:04:47 +0100 (Tue, 28 Oct 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        Data data = getCosemObjectFactory().getData(FIRMWARE_VERSION);
        return data.getString();
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode()).getCapturePeriod();
    }

    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    public IDISStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new IDISStoredValues(getCosemObjectFactory(), this);
        }
        return storedValues;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode());
        return getProfileDataReader().getChannelInfo(profileGeneric.getCaptureObjects()).size();
    }

    public void applyMessages(List messageEntries) throws IOException {
        getMessageHandler().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageHandler().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageHandler().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessageHandler().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessageHandler().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessageHandler().writeValue(value);
    }

    public int getGasSlotId() {
        return 0;     //E-meter has no gas slot id
    }

    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_IDIS.cache";
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }
}