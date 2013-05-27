package com.energyict.protocolimpl.dlms.idis;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.registers.IDISStoredValues;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/09/11
 * Time: 9:37
 */
public class IDIS extends AbstractDLMSProtocol implements MessageProtocol, FirmwareUpdateMessaging, CacheMechanism {

    private static final ObisCode FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    private static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final String CALLING_AP_TITLE_DEFAULT = "0000000000000000";
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String LIMITMAXNROFDAYS_PROPERTY = "LimitMaxNrOfDays";
    private static final String CALLING_AP_TITLE = "CallingAPTitle";
    private static final String LOAD_PROFILE_OBIS_CODE_PROPERTY = "LoadProfileObisCode";
    public static final String OBISCODE_LOAD_PROFILE1 = "1.0.99.1.0.255";   //Quarterly
    private static final String MAX_NR_OF_DAYS_DEFAULT = "0";
    public static final String VALIDATE_INVOKE_ID = "ValidateInvokeId";
    public static final String DEFAULT_VALIDATE_INVOKE_ID = "1";
    private static final String TIMEOUT = "timeout";

    private ProfileDataReader profileDataReader = null;
    private IDISMessageHandler messageHandler = null;
    private boolean readCache = false;      //Property indicating to read the cache out (useful because there's no config change state)
    private ObisCode loadProfileObisCode = ObisCode.fromString(OBISCODE_LOAD_PROFILE1);
    private IDISStoredValues storedValues = null;
    private ObisCodeMapper obisCodeMapper = null;
    private int limitMaxNrOfDays = 0;

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
            messageHandler = new IDISMessageHandler(this);
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
            }

            if ((exception.getMessage() != null) && exception.getMessage().toLowerCase().contains(TIMEOUT)) {
                getLogger().severe("Meter didn't reply to the association request! Stopping session.");
                throw exception;    //Don't retry the AARQ if it's a real timeout!
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
                    } catch (IOException e) {
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
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        if (to == null) {
            to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
            getLogger().info("getProfileData: toDate was 'null'. Changing toDate to: " + to);
        }
        ProfileData profileData = getProfileDataReader().getProfileData(new ProfileLimiter(from, to, getLimitMaxNrOfDays()), includeEvents);
        if ((profileData.getIntervalDatas().size() == 0) && (getLimitMaxNrOfDays() > 0)) {
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
        return Integer.parseInt(properties.getProperty(this.VALIDATE_INVOKE_ID, this.DEFAULT_VALIDATE_INVOKE_ID)) == 1;
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
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS);
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
    public List getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(MeterProtocol.SERIALNUMBER);
        return required;
    }

    /**
     * The protocol version
     */
    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
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
    public int getProfileInterval() throws UnsupportedException, IOException {
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
    public int getNumberOfChannels() throws UnsupportedException, IOException {
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

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        FirmwareUpdateMessagingConfig firmwareUpdateMessagingConfig = new FirmwareUpdateMessagingConfig();
        firmwareUpdateMessagingConfig.setSupportsUserFiles(true);
        firmwareUpdateMessagingConfig.setSupportsUrls(false);
        firmwareUpdateMessagingConfig.setSupportsUserFileReferences(false);
        return firmwareUpdateMessagingConfig;
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return new FirmwareUpdateMessageBuilder();
    }

    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_IDIS.cache";
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }
}