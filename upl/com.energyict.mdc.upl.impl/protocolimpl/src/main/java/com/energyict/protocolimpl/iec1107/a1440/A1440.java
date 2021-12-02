package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.RtuPlusServerHalfDuplexController;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.google.common.collect.Range;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.EXTENDED_LOGGING;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SOFTWARE7E1;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author jme
 * @since 19-aug-2009
 * <p/>
 * 19-08-2009 jme > Copied ABBA1350 protocol as base for new A1440 protocol
 */
public class A1440 extends PluggableMeterProtocol implements HHUEnabler, HalfDuplexEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, MessageProtocol, SerialNumberSupport {

    private static final int DEBUG = 0;
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";
    private static final String PROPERTY_DATE_FORMAT = "DateFormat";
    private static final String PROPERTY_BILLING_DATE_FORMAT = "BillingDateFormat";
    private static final String INVERT_BILLING_ORDER = "InvertBillingOrder";
    private static final String DEFAULT_DATE_FORMAT = "ddMMyyHHmmss";
    private static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    private static final String READ_LOGBOOK_AND_LP_COMBINED = "ReadLogbookAndLoadProfilesCombined";

    private static final int MIN_LOADPROFILE = 1;
    private static final int MAX_LOADPROFILE = 2;
    private static final int ACTION_DELAY_PER_5_SEC = 5;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iForceDelay;

    private int profileInterval;
    private ChannelMap channelMap;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int scaler;
    private int dataReadoutRequest;
    private int loadProfileNumber;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;
    private int failOnUnitMismatch = 0;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private A1440Registry a1440Registry = null;
    private A1440Profile a1440Profile = null;
    private final A1440Messages a1440Messages = new A1440Messages(this);
    private final A1440ObisCodeMapper a1440ObisCodeMapper = new A1440ObisCodeMapper(this);

    private byte[] dataReadout = null;
    private int billingCount = -1;
    private String firmwareVersion = null;
    private String meterSerial = null;
    private String dateFormat = null;
    private String billingDateFormat = null;

    private boolean software7E1;

    private HalfDuplexController halfDuplexController;
    private long halfDuplex;

    private int rs485RtuPlusServer = 0;
    private int limitMaxNrOfDays = 0;
    private boolean invertBillingOrder;
    private boolean useEquipmentIdentifierAsSerial;
    private boolean readLogbookAndLoadProfilesCombined;

    /**
     * Creates a new instance of A1440, empty constructor
     */
    public A1440(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, boolean includeEvents) throws IOException {
        return getProfileData(from, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileWithLimiter(new ProfileLimiter(from, to, getLimitMaxNrOfDays()), includeEvents);
    }

    private ProfileData getProfileWithLimiter(ProfileLimiter limiter, boolean includeEvents) throws IOException {
        Calendar from = ProtocolUtils.getCleanCalendar(getTimeZone());
        from.setTime(limiter.getFromDate());

        Calendar to = ProtocolUtils.getCleanCalendar(getTimeZone());
        to.setTime(limiter.getToDate());

        if (to.before(from)) {
            return new ProfileData();
        }

        // Read the profile data, and take the limitMaxNrOfDays property in account.
        ProfileData profileData = getA1440Profile().getProfileData(from, to, includeEvents, this.loadProfileNumber);

        // If there are no intervals in the profile, read the profile data again, but now with limitMaxNrOfDays increased with the value of Custom Property limitMaxNrOfDays property
        // This way we can prevent the profile to be stuck at a certain date if there is a gap in the profile bigger than the limitMaxNrOfDays.
        if ((profileData.getIntervalDatas().isEmpty()) && (getLimitMaxNrOfDays() > 0) && (limiter.getOldToDate().getTime() != limiter.getToDate().getTime())) {
            profileData = getProfileWithLimiter(new ProfileLimiter(limiter.getOldFromDate(), limiter.getOldToDate(), limiter.getLimitMaxNrOfDays() + getLimitMaxNrOfDays()), includeEvents);
        }
        return profileData;

    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void setTime() throws IOException {
        if (this.vdewCompatible == 1) {
            setTimeVDEWCompatible();
        } else {
            setTimeAlternativeMethod();
        }
    }

    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
        calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
        Date date = calendar.getTime();
        getA1440Registry().setRegister("TimeDate2", date);
    }

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
        calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
        Date date = calendar.getTime();
        getA1440Registry().setRegister("Time", date);
        getA1440Registry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date meterDate = (Date) getA1440Registry().getRegister("TimeDate");
        return new Date(meterDate.getTime() - this.iRoundtripCorrection);
    }

    /**
     * Returns the serial number
     *
     * @return String serial number
     */
    @Override
    public String getSerialNumber() {
        try {
            return getMeterSerial();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("ForceDelay", PropertyTranslationKeys.IEC1107_FORCEDELAY),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()),
                this.stringSpec(PROPERTY_DATE_FORMAT, PropertyTranslationKeys.IEC1107_DATE_FORMAT),
                this.stringSpec(PROPERTY_BILLING_DATE_FORMAT, PropertyTranslationKeys.IEC1107_BILLING_DATE_FORMAT),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                this.integerSpec("Scaler", PropertyTranslationKeys.IEC1107_SCALER),
                this.integerSpec("DataReadout", PropertyTranslationKeys.IEC1107_DATAREADOUT),
                this.integerSpec(EXTENDED_LOGGING.getName(), PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("VDEWCompatible", PropertyTranslationKeys.IEC1107_VDEWCOMPATIBLE),
                this.integerSpec("LoadProfileNumber", PropertyTranslationKeys.IEC1107_LOADPROFILE_NUMBER, Range.closed(MIN_LOADPROFILE, MAX_LOADPROFILE)),
                this.stringSpec(SOFTWARE7E1.getName(), PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.integerSpec("RS485RtuPlusServer", PropertyTranslationKeys.IEC1107_RS485RTU_PLUS_SERVER),
                this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS, PropertyTranslationKeys.IEC1107_LIMIT_MAX_NR_OF_DAYS),
                this.stringSpec(INVERT_BILLING_ORDER, PropertyTranslationKeys.IEC1107_INVERT_BILLING_ORDER),
                this.stringSpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, PropertyTranslationKeys.IEC1107_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL),
                this.integerSpec("FailOnUnitMismatch", PropertyTranslationKeys.IEC1107_FAIL_ON_UNIT_MISMATCH),
                this.integerSpec("HalfDuplex", PropertyTranslationKeys.IEC1107_HALF_DUPLEX),
                this.stringSpec(READ_LOGBOOK_AND_LP_COMBINED, PropertyTranslationKeys.READ_LOGBOOK_AND_LP_COMBINED));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey, Range<Integer> validValues) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::integerSpec);
        UPLPropertySpecFactory.addIntegerValues(specBuilder, validValues);
        return specBuilder.finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        this.strID = properties.getTypedProperty(ADDRESS.getName(), "");
        this.strPassword = properties.getTypedProperty(PASSWORD.getName());
        this.iIEC1107TimeoutProperty = properties.getTypedProperty(TIMEOUT.getName(), 20000);
        this.iProtocolRetriesProperty = properties.getTypedProperty(RETRIES.getName(), 5);
        this.iRoundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
        this.iSecurityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), 1);
        this.nodeId = properties.getTypedProperty(NODEID.getName(), "");
        this.iEchoCancelling = properties.getTypedProperty("EchoCancelling", 0);
        this.iForceDelay = properties.getTypedProperty("ForceDelay", 0);
        this.profileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName(), 3600);
        this.dateFormat = properties.getTypedProperty(PROPERTY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
        this.billingDateFormat = properties.getTypedProperty(PROPERTY_BILLING_DATE_FORMAT);
        this.requestHeader = properties.getTypedProperty("RequestHeader", 1);
        // Todo: next to property parse instructions are in conflict
        this.channelMap = properties.getTypedProperty("ChannelMap", new ChannelMap("0"));
        this.protocolChannelMap = properties.getTypedProperty("ChannelMap", new ProtocolChannelMap("0:0:0:0:0:0"));
        this.scaler = properties.getTypedProperty("Scaler", 0);
        this.dataReadoutRequest = properties.getTypedProperty("DataReadout", 0);
        this.extendedLogging = properties.getTypedProperty(EXTENDED_LOGGING.getName(), 0);
        this.vdewCompatible = properties.getTypedProperty("VDEWCompatible", 0);
        this.loadProfileNumber = properties.getTypedProperty("LoadProfileNumber", 1);
        this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty(SOFTWARE7E1.getName(), "0"));
        this.failOnUnitMismatch = properties.getTypedProperty("FailOnUnitMismatch", 0);
        this.halfDuplex = properties.getTypedProperty("HalfDuplex", 0);
        this.rs485RtuPlusServer = properties.getTypedProperty("RS485RtuPlusServer", 0);
        this.limitMaxNrOfDays = properties.getTypedProperty(PR_LIMIT_MAX_NR_OF_DAYS, 0);
        this.invertBillingOrder = properties.getTypedProperty(INVERT_BILLING_ORDER, false);
        this.useEquipmentIdentifierAsSerial = properties.getTypedProperty( USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, false);
        this.readLogbookAndLoadProfilesCombined = properties.getTypedProperty( READ_LOGBOOK_AND_LP_COMBINED, false);
    }

    protected boolean isDataReadout() {
        return (this.dataReadoutRequest == 1);
    }

    @Override
    public String getRegister(String name) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.getBytes());
        this.flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] data = this.flagIEC1107Connection.receiveRawData();
        return new String(data);
    }

    @Override
    public void setRegister(String name, String value) throws UnsupportedException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     */
    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS1440 IEC1107";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2021-12-02$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (this.firmwareVersion == null) {
            this.firmwareVersion = (String) getA1440Registry().getRegister(A1440Registry.FIRMWAREID);
        }
        return this.firmwareVersion;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        this.flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, this.iIEC1107TimeoutProperty, this.iProtocolRetriesProperty,
                this.iForceDelay, this.iEchoCancelling, 1, null, this.halfDuplex != 0 ? this.halfDuplexController : null, this.software7E1, logger);
        this.a1440Registry = new A1440Registry(this, this, dateFormat, billingDateFormat);
        this.a1440Profile = new A1440Profile(this, this, this.a1440Registry);
    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                this.dataReadout = cleanDataReadout(this.flagIEC1107Connection.dataReadout(this.strID, this.nodeId));
                // A1440 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
                this.flagIEC1107Connection.disconnectMACWithoutBreak();
            }

            this.flagIEC1107Connection.connectMAC(this.strID, this.strPassword, this.iSecurityLevel, this.nodeId);

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
                this.dataReadout = cleanDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        this.a1440ObisCodeMapper.initObis();

        if (this.extendedLogging >= 2) {
            getMeterInfo();
        }
        if (this.extendedLogging >= 1) {
            getRegistersInfo();
        }

    }

    private byte[] cleanDataReadout(byte[] dataReadOur) {
        for (int i = 0; i < dataReadOur.length; i++) {
            if (((i + 3) < dataReadOur.length) && (dataReadOur[i] == '&')) {
                if (dataReadOur[i + 3] == '(') {
                    dataReadOur[i] = '*';
                }
            }
        }
        return dataReadOur;
    }

    @Override
    public void disconnect() throws IOException {
        try {
            this.flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (this.requestHeader == 1) {
            return getA1440Profile().getProfileHeader(this.loadProfileNumber).getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    public int getISecurityLevel() {
        return this.iSecurityLevel;
    }

    public int getProfileInterval() throws IOException {
        if (this.requestHeader == 1) {
            return getA1440Profile().getProfileHeader(this.loadProfileNumber).getProfileInterval();
        } else {
            return this.profileInterval;
        }
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagIEC1107Connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return true;
    }

    @Override
    public String getPassword() {
        return this.strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        return this.dataReadout;
    }

    @Override
    public ChannelMap getChannelMap() {
        return this.channelMap;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERROR", "Request could not execute!");
        EXCEPTION_INFO_MAP.put("ERROR00", "A1440 ERROR 00, no valid command!");
        EXCEPTION_INFO_MAP.put("ERROR01", "A1440 ERROR 01, unknown command!");
        EXCEPTION_INFO_MAP.put("ERROR02", "A1440 ERROR 02, no access without manufacturer password");
        EXCEPTION_INFO_MAP.put("ERROR03", "A1440 ERROR 03, no access without hardware lock released");
        EXCEPTION_INFO_MAP.put("ERROR04", "A1440 ERROR 04, no valid class");
        EXCEPTION_INFO_MAP.put("ERROR05", "A1440 ERROR 05, no additional data available");
        EXCEPTION_INFO_MAP.put("ERROR06", "A1440 ERROR 06, command format not valid!");
        EXCEPTION_INFO_MAP.put("ERROR07", "A1440 ERROR 07, function is not supported!");
        EXCEPTION_INFO_MAP.put("ERROR08", "A1440 ERROR 08, demand reset not allowed!");
        EXCEPTION_INFO_MAP.put("ERROR09", "A1440 ERROR 09, load profile initialisation not activated!");
        EXCEPTION_INFO_MAP.put("ERROR10", "A1440 ERROR 10, ripple receiver not enabled!");
        EXCEPTION_INFO_MAP.put("ERROR11", "A1440 ERROR 11, no valid time and date!");
        EXCEPTION_INFO_MAP.put("ERROR12", "A1440 ERROR 12, no access to the desired storage!");
        EXCEPTION_INFO_MAP.put("ERROR13", "A1440 ERROR 13, no access, because the set mode was not activated by the alternate button!");
        EXCEPTION_INFO_MAP.put("ERROR14", "A1440 ERROR 14, no access without password!");
        EXCEPTION_INFO_MAP.put("ERROR15", "A1440 ERROR 15, no access with closed terminal cover!");
        EXCEPTION_INFO_MAP.put("ERROR16", "A1440 ERROR 16, no access due to configuration change denial!");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public int getNrOfRetries() {
        return this.iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return this.requestHeader == 1;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return this.protocolChannelMap;
    }

    /**
     * Translate the obis codes to edis codes, and read
     * @param obis
     * @return
     * @throws IOException
     */
     @Override
    public RegisterValue readRegister(ObisCode obis) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        Date eventTime = null;
        Date toTime = null;
        String fs = "";
        String toTimeString = "";
        byte[] data;
        byte[] timeStampData;

        try {

            // it is not possible to translate the following edis code in this way
            if ("1.1.0.1.2.255".equals(obis.toString())) {
                return new RegisterValue(obis, readTime());
            }

            if ("1.1.0.0.0.255".equals(obis.toString())) {
                return new RegisterValue(obis, getMeterSerial());
            }
            if ("1.1.0.2.0.255".equals(obis.toString())) {
                return new RegisterValue(obis, getFirmwareVersion());
            }
            if ("1.1.0.2.1.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }

            // Angle factors L1, L2, L3
            if ("1.1.81.7.4.255".equals(obis.toString()) || "1.1.81.7.15.255".equals(obis.toString()) || "1.1.81.7.26.255".equals(obis.toString())) {
                String edis;
                if ("1.1.81.7.4.255".equals(obis.toString())) {
                    edis = "81.7.04";
                } else {
                    edis = convertToEdis(obis.getC()) + "." + convertToEdis(obis.getD()) + "." + convertToEdis(obis.getE());
                }
                data = read(edis);

                String angle = dp.parseBetweenBrackets(data, 0, 0);

                final Unit unit = Unit.get(BaseUnit.DEGREE);

                if (angle.indexOf('*') != -1) {
                    angle = angle.substring(0, angle.indexOf('*'));
                }
                final BigDecimal angleNumber = new BigDecimal(angle);

                Quantity quantity = new Quantity(angleNumber, unit);

                return new RegisterValue(obis, quantity, null, eventTime, toTime, new Date(), -1, "");
            }

            // Battery
            if ("1.1.96.6.1.255".equals(obis.toString())) {
                final String edis = "96.6.1";
                data = read(edis);

                String batteryUsage = dp.parseBetweenBrackets(data, 0, 0);

                return new RegisterValue(obis, batteryUsage);
            }

            if (ProtocolTools.setObisCodeField(obis, 2, (byte) 0).equals(ObisCode.fromString("1.1.0.0.11.255"))) {
                return readLoadControlThresholdRegister(obis, obis.getC());
            }
            if ("1.1.0.0.12.255".equals(obis.toString())) {
                try {
                    BigDecimal actionDelay = new BigDecimal(Integer.parseInt((String) getA1440Registry().getRegister(A1440Registry.LOAD_CONTROL_ACTION_DELAY_REGISTER)) * ACTION_DELAY_PER_5_SEC);
                    return new RegisterValue(obis, new Quantity(actionDelay, Unit.get(BaseUnit.SECOND)));
                } catch (NumberFormatException e) {
                    throw new NoSuchRegisterException(e.getMessage());
                }
            }
            if ("1.1.0.0.13.255".equals(obis.toString())) {
                try {
                    LoadControlMeasurementQuantity measurementQuantity = LoadControlMeasurementQuantity.getLoadControlMeasurementQuantityForQuantityCode(
                            (String) getA1440Registry().getRegister(A1440Registry.LOAD_CONTROL_MEASUREMENT_QUANTITY_REGISTER)
                    );
                    return new RegisterValue(obis, measurementQuantity.getDescription());
                } catch (NumberFormatException e) {
                    throw new NoSuchRegisterException(e.getMessage());
                }
            }

            if ("1.1.0.0.1.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.2.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.3.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.4.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.5.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.6.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.7.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.8.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.9.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.10.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("0.0.97.97.0.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("0.0.97.97.1.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }

            if (obis.getF() != 255) {
                if (getBillingCount() < (Math.abs(obis.getF()) + 1)) {
                    throw new NoSuchRegisterException("Billing count is only " + getBillingCount() + " so cannot read register with F = " + obis.getF());
                }

                int f = invertBillingOrder
                        ? Math.abs(obis.getF())
                        : getBillingCount() - Math.abs(obis.getF());
                fs = "*" + ProtocolUtils.buildStringDecimal(f%100, 2);

                // try to read the time stamp, and us it as the register toTime.
                try {
                    String billingPoint;
                    if ("1.1.0.1.0.255".equalsIgnoreCase(obis.toString())) {
                        billingPoint = "*" + ProtocolUtils.buildStringDecimal(invertBillingOrder ? 0 : getBillingCount(), 2);
                    } else {
                        billingPoint = fs;
                    }
                    timeStampData = read("0.1.2" + billingPoint);
                    toTimeString = dp.parseBetweenBrackets(timeStampData);
                    if (billingDateFormat == null || billingDateFormat.isEmpty()) {
                        VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
                        vts.parse(toTimeString);
                        toTime = vts.getCalendar().getTime();
                    } else {
                        SimpleDateFormat format = new SimpleDateFormat(billingDateFormat);
                        format.setTimeZone(getTimeZone());
                        try {
                            toTime = format.parse(toTimeString);
                        } catch (ParseException e) {
                            throw new IOException("Could not parse the received timestamp (" + toTimeString + ") in the configured format + " + billingDateFormat);
                        }
                    }
                } catch (Exception e) {
                    // If encountering a timeout, protocol session should fail immediately. It is not useful to continue.
                    if (e instanceof FlagIEC1107ConnectionException && ((FlagIEC1107ConnectionException) e).getReason() == FlagIEC1107Connection.TIMEOUT_ERROR) {
                        throw (FlagIEC1107ConnectionException) e;
                    }
                }
            }

            String edis = convertToEdis(obis.getC()) + "." + convertToEdis(obis.getD()) + "." + convertToEdis(obis.getE()) + fs;
            data = read(edis);


            // read and parse the value an the unit ()if exists) of the register
            String temp = dp.parseBetweenBrackets(data, 0, 0);
            Unit readUnit = null;
            if (temp.indexOf('*') != -1) {
                readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
                temp = temp.substring(0, temp.indexOf('*'));
            }

            if ((temp == null) || (temp.length() == 0)) {
                throw new NoSuchRegisterException();
            }

            BigDecimal bd = new BigDecimal(temp);

            // Read the eventTime (timestamp after the register data)
            try {
                String dString = dp.parseBetweenBrackets(data, 0, 1);
                if ("0000000000".equals(dString)) {
                    // If timestamp is empty - return eventTime null, instead of throwing an error
                    // throw new NoSuchRegisterException();
                    eventTime = null;
                } else {
                    VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
                    vts.parse(dString);
                    eventTime = vts.getCalendar().getTime();
                }
            } catch (DataParseException e) {
                if (DEBUG >= 3) {
                    e.printStackTrace();
                }
            } catch (NoSuchRegisterException e) {
                if (DEBUG >= 3) {
                    e.printStackTrace();
                }
                return new RegisterValue(obis, null, null, null);
            }

            Quantity q;
            Unit obisUnit = obis.getUnitElectricity(this.scaler);
            if (obisUnit.isUndefined()) {
                q = new Quantity(bd, obis.getUnitElectricity(0));
            } else {
                if (readUnit != null) {
                    if (!readUnit.getBaseUnit().equals(obisUnit.getBaseUnit())) {
                        String message = "Unit or scaler from obiscode is different from register Unit in meter!!! ";
                        message += " (Unit from meter: " + readUnit;
                        message += " -  Unit from obiscode: " + obisUnit + ")\n";
                        getLogger().info(message);
                        if (this.failOnUnitMismatch == 1) {
                            throw new InvalidPropertyException(message);
                        }
                    }
                }
                q = new Quantity(bd, readUnit == null ? obisUnit : readUnit);
            }

            return postProcessRegisterValue(obis, eventTime, toTime, q);

        } catch (NoSuchRegisterException | NumberFormatException e) {
            String m = "readRegister(" + obis.toString() +  ") error, " + e.getMessage();
            throw new NoSuchRegisterException(m);
        } catch (InvalidPropertyException e) {
            String m = "readRegister(" + obis.toString() +  ") error, " + e.getMessage();
            throw new InvalidPropertyException(m);
        } catch (IOException e) {
            String m = "readRegister(" + obis.toString() +  ") error, " + e.getMessage();
            throw new IOException(m);
        }
    }

    private RegisterValue readLoadControlThresholdRegister(ObisCode obis, int tariff) throws IOException {
        try {
            LoadControlMeasurementQuantity measurementQuantity = LoadControlMeasurementQuantity.getLoadControlMeasurementQuantityForQuantityCode(
                    (String) getA1440Registry().getRegister(A1440Registry.LOAD_CONTROL_MEASUREMENT_QUANTITY_REGISTER)
            );
            float loadControlThreshold = measurementQuantity.format((String) getA1440Registry().getRegister(A1440Registry.LOAD_CONTROL_THRESHOLD_REGISTER, getTariffCode(tariff)));
            return new RegisterValue(obis, new Quantity(loadControlThreshold, measurementQuantity.getUnit()));
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException(e.getMessage());
        }
    }

    private String getTariffCode(int tariff) {
        if (tariff == 0) {
            return "";
        }
        return String.format("%02X", 1 << (tariff - 1));
    }

    private String convertToEdis(int obisCodeField) {
        switch (obisCodeField) {
            case 96:
                return "C";
            case 97:
                return "F";
            case 98:
                return "L";
            case 99:
                return "9";
            default:
                return Integer.toString(obisCodeField);
        }
    }

    private RegisterValue postProcessRegisterValue(ObisCode obis, Date eventTime, Date toTime, Quantity q) {
        if ("1.1.96.3.0.255".equals(obis.toString())) {
            int breakerStatusCode = q.getAmount().intValue();
            switch (breakerStatusCode) {
                case 0:
                    return new RegisterValue(obis, new Quantity(0, Unit.getUndefined()), null, eventTime, toTime, new Date(), -1, "Disconnected");
                case 10000:
                    return new RegisterValue(obis, new Quantity(1, Unit.getUndefined()), null, eventTime, toTime, new Date(), -1, "Connected");
                case 20000:
                    return new RegisterValue(obis, new Quantity(2, Unit.getUndefined()), null, eventTime, toTime, new Date(), -1, "Armed");
                default:
                    return new RegisterValue(obis, q, null, eventTime, toTime, new Date(), -1, "Unknown breaker status");
            }
        }

        // in case no post processing is required
        return new RegisterValue(obis, q, eventTime, toTime);
    }

    private byte[] read(String edisNotation) throws IOException {
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            this.flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = this.flagIEC1107Connection.receiveRawData(edisNotation);
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            data = ddp.getRegisterStrValue(edisNotation).getBytes();
        }
        return data;
    }

    private Quantity readTime() throws IOException {
        Long seconds = getTime().getTime() / 1000;
        return new Quantity(seconds, Unit.get(BaseUnit.SECOND));
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String reginfo = this.a1440ObisCodeMapper.getObisMap().get(obisCode.toString());
        if (reginfo == null) {
            return new RegisterInfo(obisCode.toString());
        } else {
            return new RegisterInfo("");
        }
    }

    private void getRegistersInfo() throws IOException {
        StringBuilder result = new StringBuilder();

        for (String obis : this.a1440ObisCodeMapper.getObisMap().keySet()) {
            ObisCode oc = ObisCode.fromString(obis);
            if (DEBUG >= 5) {
                try {
                    result.append(translateRegister(oc) + "\n");
                    result.append(readRegister(oc) + "\n");
                } catch (NoSuchRegisterException nsre) {
                    // ignore and continue
                }
            } else {
                result.append(obis + " " + translateRegister(oc) + "\n");
            }
        }

        getLogger().info(result.toString());
    }

    private void getMeterInfo() throws IOException {
        String returnString = "";
        if (this.iSecurityLevel < 1) {
            returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
        } else {
            returnString += " Meter ID1: " + readSpecialRegister(A1440ObisCodeMapper.ID1) + "\n";
            returnString += " Meter ID2: " + readSpecialRegister(A1440ObisCodeMapper.ID2) + "\n";
            returnString += " Meter ID3: " + readSpecialRegister(A1440ObisCodeMapper.ID3) + "\n";
            returnString += " Meter ID4: " + readSpecialRegister(A1440ObisCodeMapper.ID4) + "\n";
            returnString += " Meter ID5: " + readSpecialRegister(A1440ObisCodeMapper.ID5) + "\n";
            returnString += " Meter ID6: " + readSpecialRegister(A1440ObisCodeMapper.ID6) + "\n";
            returnString += " Meter IEC1107 ID:" + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ID) + "\n";
            returnString += " Meter IECII07 address (optical):    " + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ADDRESS_OP) + "\n";
            returnString += " Meter IECII07 address (electrical): " + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ADDRESS_EL) + "\n";
        }
        getLogger().info(returnString);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, isDataReadout());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.iIEC1107TimeoutProperty, this.iProtocolRetriesProperty, 300, this.iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    protected A1440Registry getA1440Registry() {
        return this.a1440Registry;
    }

    private A1440Profile getA1440Profile() {
        return this.a1440Profile;
    }

    int getBillingCount() throws IOException {
        if (this.billingCount == -1) {

            if (isDataReadout()) {
                DataDumpParser ddp = new DataDumpParser(getDataReadout());
                this.billingCount = ddp.getBillingCounter();
            } else {
                String data;
                try {
                    data = new String(read("0.1.0"));
                } catch (NoSuchRegisterException e) {
                    if (!isDataReadout()) {
                        throw e;
                    }
                    data = "()";
                }

                int start = data.indexOf('(') + 1;
                int stop = data.indexOf(')');
                String value = data.substring(start, stop);

                try {
                    this.billingCount = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    this.billingCount = 0;
                    getLogger().info(A1440.class.getSimpleName() + " - Unable to read billingCounter. Defaulting to 0!");
                }
            }

            if (this.billingCount >= 100) {
                this.billingCount = 0;
                getLogger().warning(A1440.class.getSimpleName() + " - Encountered invalid billingCounter (" + this.billingCount + "). The billingCounter should be between 0 and 100, defaulting to 0!");
            }

            DataParser dp = new DataParser(getTimeZone());
            if (!"0000000000".equals(dp.parseBetweenBrackets(read("0.1.2*99")))) {
                billingCount=billingCount+100;
            }
        }
        return this.billingCount;
    }

    private String getMeterSerial() throws IOException {
        if (this.meterSerial == null) {
            this.meterSerial = (String) getA1440Registry().getRegister(
                    this.useEquipmentIdentifierAsSerial
                            ? A1440Registry.UTILITY_ID_1
                            : A1440Registry.SERIAL
            );
        }
        if (useEquipmentIdentifierAsSerial) {
            byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString(meterSerial, "");
            return ProtocolTools.getAsciiFromBytes(bytesFromHexString);
        }
        return this.meterSerial;
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {
        this.a1440Messages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.a1440Messages.queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return this.a1440Messages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return this.a1440Messages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return this.a1440Messages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return this.a1440Messages.writeValue(value);
    }

    private String readSpecialRegister(String registerName) throws IOException {
        if (registerName.equals(A1440ObisCodeMapper.ID1)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID1)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.ID2)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID2)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.ID3)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID3)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.ID4)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID4)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.ID5)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID5)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.ID6)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.ID6)).getBytes()));
        }

        if (registerName.equals(A1440ObisCodeMapper.IEC1107_ID)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.IEC1107_ID)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.IEC1107_ADDRESS_OP)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.IEC1107_ADDRESS_OP)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.IEC1107_ADDRESS_EL)) {
            return new String(ProtocolUtils.convert2ascii(((String) getA1440Registry().getRegister(A1440Registry.IEC1107_ADDRESS_EL)).getBytes()));
        }
        if (registerName.equals(A1440ObisCodeMapper.FIRMWAREID)) {
            return getFirmwareVersion();
        }
        if (registerName.equals(A1440ObisCodeMapper.PARAMETER_IDENTIFICATION)) {
            return (String) getA1440Registry().getRegister(A1440Registry.PARAMETER_IDENTIFICATION);
        }
        if (registerName.equals(A1440ObisCodeMapper.ERROR_REGISTER)) {
            return (String) getA1440Registry().getRegister(A1440Registry.ERROR_REGISTER);
        }
        if (registerName.equals(A1440ObisCodeMapper.ALARM_REGISTER)) {
            return (String) getA1440Registry().getRegister(A1440Registry.ALARM_REGISTER);
        }
        if (registerName.equals(A1440ObisCodeMapper.BATTERY_USE_TIME_REGISTER)) {
            return (String) getA1440Registry().getRegister(A1440Registry.BATTERY_USE_TIME_REGISTER);
        }

        if (registerName.equals(A1440ObisCodeMapper.FIRMWARE)) {
            if (this.iSecurityLevel < 1) {
                return "Unknown (SecurityLevel to low)";
            }
            String fwdev = (String) getA1440Registry().getRegister(A1440Registry.FIRMWARE);
            String hw = (String) getA1440Registry().getRegister(A1440Registry.HARDWARE);
            String fw;
            String dev;
            if ((fwdev != null) && (fwdev.length() >= 30)) {
                fw = fwdev.substring(0, 10);
                dev = fwdev.substring(10, 30);
                fw = new String(ProtocolUtils.convert2ascii(fw.getBytes())).trim();
                dev = new String(ProtocolUtils.convert2ascii(dev.getBytes())).trim();
            } else {
                fw = "Unknown";
                dev = "Unknown";
            }

            hw = (hw == null) ? "Unknown" : new String(ProtocolUtils.convert2ascii(hw.getBytes())).trim();

            return dev + " " + "v" + fw + " " + hw;
        }

        return "";
    }

    public void resetDemand() throws IOException {
        this.a1440Messages.doDemandReset();
    }

    private boolean isRS485RtuPlusServer() {
        return (this.rs485RtuPlusServer != 0);
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController controller) {
        if (isRS485RtuPlusServer()) {
            this.halfDuplexController = new RtuPlusServerHalfDuplexController(controller);
        } else {
            this.halfDuplexController = controller;
        }
        this.halfDuplexController.setDelay(this.halfDuplex);

        if (getFlagIEC1107Connection() != null) {
            getFlagIEC1107Connection().setHalfDuplexController(this.halfDuplex != 0 ? this.halfDuplexController : null);
        }
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    @Override
    public Optional<BreakerStatus> getBreakerStatus() throws IOException {
    	ContactorController cc = new A1440ContactorController(this);
    	return Optional.of(cc.getContactorState());
    }

    @Override
    public boolean hasSupportForSeparateEventsReading() {
        return !readLogbookAndLoadProfilesCombined;
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastReading) throws IOException {
        Calendar from = ProtocolUtils.getCleanCalendar(getTimeZone());
        from.setTime(lastReading);

        Calendar to = ProtocolUtils.getCleanCalendar(getTimeZone());
        to.setTime(new Date());

        return getA1440Profile().getMeterEvents(from, to);
    }
}
