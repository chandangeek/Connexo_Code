package com.energyict.protocolimpl.dlms.idis;

import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

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
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.dlms.idis.registers.IDISStoredValues;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/09/11
 * Time: 9:37
 */
public class IDIS extends AbstractDLMSProtocol implements MessageProtocol, CacheMechanism, SerialNumberSupport {

    public static final String CALLING_AP_TITLE = "CallingAPTitle";
    public static final String CALLING_AP_TITLE_DEFAULT = "0000000000000000";

    private static final ObisCode FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    private static final int READ_CACHE_DEFAULT_VALUE = 0;
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String LIMITMAXNROFDAYS_PROPERTY = "LimitMaxNrOfDays";
    private static final String LOAD_PROFILE_OBIS_CODE_PROPERTY = "LoadProfileObisCode";
    private static final String OBISCODE_LOAD_PROFILE1 = "1.0.99.1.0.255";   //Quarterly
    private static final int MAX_NR_OF_DAYS_DEFAULT = 0;
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
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor extractor;

    public IDIS(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, NlsService nlsService) {
        super(propertySpecService, nlsService);
        this.calendarFinder = calendarFinder;
        this.extractor = extractor;
    }

    protected TariffCalendarFinder getCalendarFinder() {
        return calendarFinder;
    }

    protected TariffCalendarExtractor getExtractor() {
        return extractor;
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
            messageHandler = new IDISMessageHandler(this, this.calendarFinder, this.extractor);
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
    public String getSerialNumber() {
        try {
            return String.valueOf(getCosemObjectFactory().getMbusClient(ObisCode.fromString("0.1.24.1.0.255"), MbusClientAttributes.VERSION10).getIdentificationNumber().getValue());
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, retries + 1);
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            connectWithRetries();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e);
        }
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
                exception = new IOException(e.getMessage(), e);
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

    @Override
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
        // Todo: property is not added to the list of prop specs
        return Integer.parseInt(properties.getTypedProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) == 1;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> myPropertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        myPropertySpecs.add(this.integerSpec(READCACHE_PROPERTY, PropertyTranslationKeys.DLMS_READ_CACHE, false));
        myPropertySpecs.add(this.integerSpec(LIMITMAXNROFDAYS_PROPERTY, PropertyTranslationKeys.DLMS_LIMIT_MAX_NR_OF_DAYS, false));
        myPropertySpecs.add(this.stringSpec(CALLING_AP_TITLE, PropertyTranslationKeys.DLMS_CALLING_AP_TITLE, false));
        myPropertySpecs.add(new ObisCodePropertySpec(LOAD_PROFILE_OBIS_CODE_PROPERTY, false, getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_LOAD_PROFILE_OBIS_CODE).format(), getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_LOAD_PROFILE_OBIS_CODE_DESCRIPTION).format()));
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS, false));
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.SERVER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_MAC_ADDRESS, false));
        myPropertySpecs.add(this.stringSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS, false)); // Legacy property for migration, the protocol uses SERVER_MAC_ADDRESS property!
        myPropertySpecs.add(this.stringSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS, false)); // Legacy property for migration, the protocol uses SERVER_MAC_ADDRESS property!
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION, false));
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.ADDRESSING_MODE, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, false));
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.MAX_REC_PDU_SIZE, PropertyTranslationKeys.DLMS_MAX_REC_PDU_SIZE, false));
        myPropertySpecs.add(this.stringSpec(DlmsProtocolProperties.ISKRA_WRAPPER, PropertyTranslationKeys.DLMS_ISKRA_WRAPPER, false));
        return myPropertySpecs;
    }

    @Override
    protected boolean serialNumberIsRequired() {
        return true;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        readCache = properties.getTypedProperty(READCACHE_PROPERTY, READ_CACHE_DEFAULT_VALUE) == 1;
        limitMaxNrOfDays = properties.getTypedProperty(LIMITMAXNROFDAYS_PROPERTY, MAX_NR_OF_DAYS_DEFAULT);
        String callingAPTitle = properties.getTypedProperty(CALLING_AP_TITLE, CALLING_AP_TITLE_DEFAULT).trim();
        setCallingAPTitle(callingAPTitle);
        loadProfileObisCode = ObisCode.fromString(properties.getTypedProperty(LOAD_PROFILE_OBIS_CODE_PROPERTY, OBISCODE_LOAD_PROFILE1).trim());
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
    public String getProtocolDescription() {
        return "Elster AM500 DLMS (IDIS P1)";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Mon Jan 2 11:14:35 2017 +0100 $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        Data data = getCosemObjectFactory().getData(FIRMWARE_VERSION);
        return data.getString();
    }

    @Override
    public boolean isRequestTimeZone() {
        return false;
    }

    @Override
    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode()).getCapturePeriod();
    }

    @Override
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    @Override
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

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessageHandler().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageHandler().queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return getMessageHandler().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessageHandler().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessageHandler().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return getMessageHandler().writeValue(value);
    }

    public int getGasSlotId() {
        return 0;     //E-meter has no gas slot id
    }

    @Override
    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_IDIS.cache";
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

}