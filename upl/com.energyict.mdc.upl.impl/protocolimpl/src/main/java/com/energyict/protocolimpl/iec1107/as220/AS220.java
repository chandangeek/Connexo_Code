package com.energyict.protocolimpl.iec1107.as220;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
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
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author jme
 * @since 19-aug-2009
 * <p/>
 * 19-08-2009 jme > Copied ABBA1350 protocol as base for new AS220 protocol
 */
public class AS220 extends PluggableMeterProtocol implements HHUEnabler, HalfDuplexEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, MessageProtocol, SerialNumberSupport {

    private static final int DEBUG = 0;
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";

    private static final int MIN_LOADPROFILE = 1;
    private static final int MAX_LOADPROFILE = 2;
    private static final String PROPERTY_DATE_FORMAT = "DateFormat";
    private static final String PROPERTY_BILLING_DATE_FORMAT = "BillingDateFormat";
	private static final String INVERT_BILLING_ORDER = "InvertBillingOrder";
    private static final String DEFAULT_DATE_FORMAT = "yy/mm/dd";
    private static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private String serialNumber;
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
    private AS220Registry aS220Registry = null;
    private AS220Profile aS220Profile = null;
    private AS220Messages aS220Messages = new AS220Messages(this);
    private AS220ObisCodeMapper aS220ObisCodeMapper = new AS220ObisCodeMapper(this);

    private byte[] dataReadout = null;
    private int billingCount = -1;
    private String firmwareVersion = null;
    private Date meterDate = null;
    private String meterSerial = null;

    private boolean software7E1;

    private HalfDuplexController halfDuplexController;
    private int halfDuplex;

    private int rs485RtuPlusServer = 0;
    private int limitMaxNrOfDays = 0;
	private boolean invertBillingOrder;

    private DataDumpParser dataDumpParser;
    private String dateFormat = null;
    private String billingDateFormat = null;
    private boolean useEquipmentIdentifierAsSerial;

    public AS220(PropertySpecService propertySpecService, NlsService nlsService) {
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
        ProfileData profileData = getAS220Profile().getProfileData(from, to, includeEvents, this.loadProfileNumber);

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
        getAS220Registry().setRegister("TimeDate2", date);
    }

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
        calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
        Date date = calendar.getTime();
        getAS220Registry().setRegister("Time", date);
        getAS220Registry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        this.meterDate = (Date) getAS220Registry().getRegister("TimeDate");
        return new Date(this.meterDate.getTime() - this.iRoundtripCorrection);
    }

    @Override
    public String getSerialNumber() {
        try {
            return ProtocolTools.removeLeadingZerosFromString(getMeterSerial());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.IEC1107_SERIALNUMBER),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("ForceDelay", PropertyTranslationKeys.IEC1107_FORCEDELAY),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                this.integerSpec("Scaler", PropertyTranslationKeys.IEC1107_SCALER),
                this.stringSpec(PROPERTY_DATE_FORMAT, PropertyTranslationKeys.IEC1107_DATE_FORMAT),
                this.stringSpec(PROPERTY_BILLING_DATE_FORMAT, PropertyTranslationKeys.IEC1107_BILLING_DATE_FORMAT),
                this.integerSpec("DataReadout", PropertyTranslationKeys.IEC1107_DATAREADOUT, Range.closed(0, 2)),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("VDEWCompatible", PropertyTranslationKeys.IEC1107_VDEWCOMPATIBLE),
                this.integerSpec("LoadProfileNumber", PropertyTranslationKeys.IEC1107_LOADPROFILE_NUMBER, Range.closed(MIN_LOADPROFILE, MAX_LOADPROFILE)),
                this.stringSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.stringSpec("FailOnUnitMismatch", PropertyTranslationKeys.IEC1107_FAIL_ON_UNIT_MISMATCH),
                this.stringSpec("HalfDuplex", PropertyTranslationKeys.IEC1107_HALF_DUPLEX),
                this.integerSpec("RS485RtuPlusServer", PropertyTranslationKeys.IEC1107_RS485RTU_PLUS_SERVER),
                this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS, PropertyTranslationKeys.IEC1107_LIMIT_MAX_NR_OF_DAYS),
                this.stringSpec(INVERT_BILLING_ORDER, PropertyTranslationKeys.IEC1107_INVERT_BILLING_ORDER),
                this.stringSpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, PropertyTranslationKeys.IEC1107_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL));
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
        try {
            this.strID = properties.getTypedProperty(ADDRESS.getName(), "");
            this.strPassword = properties.getTypedProperty(PASSWORD.getName());
            this.serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            this.iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            this.iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            this.iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            this.iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            this.nodeId = properties.getTypedProperty(NODEID.getName(), "");
            this.iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            this.iForceDelay = Integer.parseInt(properties.getTypedProperty("ForceDelay", "0").trim());
            this.profileInterval = Integer.parseInt(properties.getTypedProperty("ProfileInterval", "3600").trim());
            this.channelMap = new ChannelMap(properties.getTypedProperty("ChannelMap", "0"));
            this.requestHeader = Integer.parseInt(properties.getTypedProperty("RequestHeader", "1").trim());
            this.protocolChannelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "0:0:0:0:0:0"));
            this.scaler = Integer.parseInt(properties.getTypedProperty("Scaler", "0").trim());
            this.dateFormat = properties.getTypedProperty(PROPERTY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
            this.billingDateFormat = properties.getTypedProperty(PROPERTY_BILLING_DATE_FORMAT);
            this.dataReadoutRequest = Integer.parseInt(properties.getTypedProperty("DataReadout", "0").trim());
            this.extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            this.vdewCompatible = Integer.parseInt(properties.getTypedProperty("VDEWCompatible", "0").trim());
            this.loadProfileNumber = Integer.parseInt(properties.getTypedProperty("LoadProfileNumber", "1"));
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
            this.failOnUnitMismatch = Integer.parseInt(properties.getTypedProperty("FailOnUnitMismatch", "0"));
            this.halfDuplex = Integer.parseInt(properties.getTypedProperty("HalfDuplex", "0").trim());
            this.rs485RtuPlusServer = Integer.parseInt(properties.getTypedProperty("RS485RtuPlusServer", "0").trim());
            this.limitMaxNrOfDays = Integer.parseInt(properties.getTypedProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
            this.invertBillingOrder = getBooleanProperty(properties, INVERT_BILLING_ORDER);
            this.useEquipmentIdentifierAsSerial = getBooleanProperty(properties, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

	private boolean getBooleanProperty(TypedProperties properties, String propertyName) {
		return "1".equals(properties.getTypedProperty(propertyName, "0").trim());
    }

    protected boolean isDataReadout() {
        return (this.dataReadoutRequest == 1) || (this.dataReadoutRequest == 2);
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
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-05-31 09:07:29 +0300 (Tue, 31 May 2016)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (this.firmwareVersion == null) {
            try {
                this.firmwareVersion = (String) getAS220Registry().getRegister(AS220Registry.FIRMWAREID);
            } catch (IOException e) {
                // If we use 'DataReadOut' to retrieve registers, the firmware version is extracted from the datadump.
                // If the datadump doesn't contain the firmware version register (0.2.0), then we get an IOException.
                if (e.getMessage().contains("register 0.2.0 does not exist in datareadout")) {
                    this.firmwareVersion = "N/A";
                } else {
                    throw new NestedIOException(e);
                }
            }
        }
        return this.firmwareVersion;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        this.flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, this.iIEC1107TimeoutProperty, this.iProtocolRetriesProperty,
                this.iForceDelay, this.iEchoCancelling, 1, null, this.halfDuplex != 0 ? this.halfDuplexController : null, this.software7E1, logger);
        this.aS220Registry = new AS220Registry(this, this, dateFormat, billingDateFormat);
        this.aS220Profile = new AS220Profile(this, this, this.aS220Registry);

    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                this.dataReadout = cleanDataReadout(this.flagIEC1107Connection.dataReadout(this.strID, this.nodeId));
                // AS220 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
                this.flagIEC1107Connection.disconnectMACWithoutBreak();
            }

            this.flagIEC1107Connection.connectMAC(this.strID, this.strPassword, this.iSecurityLevel, this.nodeId);

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
                this.dataReadout = cleanDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        this.aS220ObisCodeMapper.initObis();

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
            return getAS220Profile().getProfileHeader(this.loadProfileNumber).getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (this.requestHeader == 1) {
            return getAS220Profile().getProfileHeader(this.loadProfileNumber).getProfileInterval();
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
        EXCEPTION_INFO_MAP.put("ERROR00", "AS220 ERROR 00, no valid command!");
        EXCEPTION_INFO_MAP.put("ERROR01", "AS220 ERROR 01, unknown command!");
        EXCEPTION_INFO_MAP.put("ERROR02", "AS220 ERROR 02, no access without manufacturer password");
        EXCEPTION_INFO_MAP.put("ERROR03", "AS220 ERROR 03, no access without hardware lock released");
        EXCEPTION_INFO_MAP.put("ERROR04", "AS220 ERROR 04, no valid class");
        EXCEPTION_INFO_MAP.put("ERROR05", "AS220 ERROR 05, no additional data available");
        EXCEPTION_INFO_MAP.put("ERROR06", "AS220 ERROR 06, command format not valid!");
        EXCEPTION_INFO_MAP.put("ERROR07", "AS220 ERROR 07, function is not supported!");
        EXCEPTION_INFO_MAP.put("ERROR08", "AS220 ERROR 08, demand reset not allowed!");
        EXCEPTION_INFO_MAP.put("ERROR09", "AS220 ERROR 09, load profile initialisation not activated!");
        EXCEPTION_INFO_MAP.put("ERROR10", "AS220 ERROR 10, ripple receiver not enabled!");
        EXCEPTION_INFO_MAP.put("ERROR11", "AS220 ERROR 11, no valid time and date!");
        EXCEPTION_INFO_MAP.put("ERROR12", "AS220 ERROR 12, no access to the desired storage!");
        EXCEPTION_INFO_MAP.put("ERROR13", "AS220 ERROR 13, no access, because the set mode was not activated by the alternate button!");
        EXCEPTION_INFO_MAP.put("ERROR14", "AS220 ERROR 14, no access without password!");
        EXCEPTION_INFO_MAP.put("ERROR15", "AS220 ERROR 15, no access with closed terminal cover!");
        EXCEPTION_INFO_MAP.put("ERROR16", "AS220 ERROR 16, no access due to configuration change denial!");
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

    public boolean isRequestHeader() {
        return this.requestHeader == 1;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return this.protocolChannelMap;
    }

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
                Date billingDate = (Date) this.aS220Registry.getRegister(AS220Registry.BILLINGPOINTTIMESTAMP);
                return new RegisterValue(obis, new Quantity(new BigDecimal(billingDate.getTime()), Unit.getUndefined()), billingDate, null, null, new Date(), -1, billingDate.toString());
            }

            if ("1.1.0.0.0.255".equals(obis.toString()) || "0.0.96.1.0.255".equals(obis.toString())) {
                return new RegisterValue(obis, getMeterSerial());
            }
            if ("1.1.0.2.0.255".equals(obis.toString()) || "0.0.96.1.5.255".equals(obis.toString())) {
                return new RegisterValue(obis, getFirmwareVersion());
            }

            if ("1.1.0.0.1.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.2.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.3.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.4.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.5.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.6.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.7.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.8.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.9.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.10.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(this.aS220ObisCodeMapper.getObisMap().get(obis.toString())));
            }

            if (obis.getF() != 255) {
				if (Math.abs(obis.getF()) >= getBillingCount()) {
					throw new NoSuchRegisterException("Billing count is only " + getBillingCount() + " so cannot read register with F = " + obis.getF());
				}
				int f = -1;
				if (dataReadoutRequest == 2) {
					f = Math.abs(obis.getF());
				} else {
					f = invertBillingOrder
							? Math.abs(obis.getF())
							: getBillingCount() - Math.abs(obis.getF());
				}
				fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);
            }

            if ("1.1.14.7.0.255".equals(obis.toString())) {   // current frequency
                data = read("34.7");
            } else if ("0.0.96.1.4.255".equals(obis.toString())) {
                data = read("C.2.0");
            } else {
                String edis = obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
                data = read(edis);
            }

            // try to read the time stamp, and us it as the register toTime.
            try {
                String billingPoint = "";
                if ("1.1.0.1.0.255".equalsIgnoreCase(obis.toString())) {
                    billingPoint = "*" + ProtocolUtils.buildStringDecimal(getBillingCount(), 2);
                } else {
                    billingPoint = fs;
                }
                timeStampData = read("0.1.2" + billingPoint);
                toTimeString = dp.parseBetweenBrackets(timeStampData);

                if (billingDateFormat == null || billingDateFormat.length() == 0) {
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
            }

            // read and parse the value an the unit ()if exists) of the register
            String temp = dp.parseBetweenBrackets(data, 0, 0);
            Unit readUnit = null;
            if (temp.indexOf('*') != -1) {
                readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
                temp = temp.substring(0, temp.indexOf('*'));
            } else {
                readUnit = Unit.getUndefined();
            }

            if ((temp == null) || (temp.length() == 0)) {
                throw new NoSuchRegisterException();
            }

            BigDecimal bd = new BigDecimal(temp);

            // Read the eventTime (timestamp after the register data)
            try {
                String dString = dp.parseBetweenBrackets(data, 0, 1);
                if ("0000000000".equals(dString)) {
                    throw new NoSuchRegisterException();
                }
                VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
                vts.parse(dString);
                eventTime = vts.getCalendar().getTime();
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

            Quantity q = null;
            Unit unitFromObis = obis.getUnitElectricity(0);

            if (readUnit.getBaseUnit() != unitFromObis.getBaseUnit()) {
                String message = "Unit from obiscode is different from register Unit in meter!!! ";
                message += " (Unit from meter: " + readUnit;
                message += " -  Unit from obiscode: " + unitFromObis + ")\n";
                getLogger().warning(message);

                if (this.failOnUnitMismatch == 1) {
                    throw new InvalidPropertyException(message);
                }
            }

            q = new Quantity(bd, readUnit);
            return new RegisterValue(obis, q, eventTime, toTime);
        } catch (InvalidPropertyException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new InvalidPropertyException(m);
        } catch (IOException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new NoSuchRegisterException(m);
        }

    }

    private byte[] read(String edisNotation) throws IOException {
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            this.flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = this.flagIEC1107Connection.receiveRawData();
        } else {
            data = getDataDumpParser().getRegisterStrValue(edisNotation).getBytes();
        }
        return data;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String reginfo = this.aS220ObisCodeMapper.getObisMap().get(obisCode.toString());
        if (reginfo == null) {
            reginfo = obisCode.toString();
        }
        return new RegisterInfo("" + reginfo);
    }

    private void getRegistersInfo() throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String obis : this.aS220ObisCodeMapper.getObisMap().keySet()) {
            ObisCode oc = ObisCode.fromString(obis);
            if (DEBUG >= 5) {
                try {
                    builder.append(translateRegister(oc)).append("\n");
                    builder.append(readRegister(oc)).append("\n");
                } catch (NoSuchRegisterException nsre) {
                    // ignore and continue
                }
            } else {
                builder.append(obis).append(" ").append(translateRegister(oc)).append("\n");
            }
        }

        getLogger().info(builder.toString());
    }

    private void getMeterInfo() throws IOException {
        String returnString = "";
        if (this.iSecurityLevel < 1) {
            returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
        } else {
            returnString += " Meter ID1: " + readSpecialRegister(AS220ObisCodeMapper.ID1) + "\n";
            returnString += " Meter ID2: " + readSpecialRegister(AS220ObisCodeMapper.ID2) + "\n";
            returnString += " Meter ID3: " + readSpecialRegister(AS220ObisCodeMapper.ID3) + "\n";
            returnString += " Meter ID4: " + readSpecialRegister(AS220ObisCodeMapper.ID4) + "\n";
            returnString += " Meter ID5: " + readSpecialRegister(AS220ObisCodeMapper.ID5) + "\n";
            returnString += " Meter ID6: " + readSpecialRegister(AS220ObisCodeMapper.ID6) + "\n";
            returnString += " Meter IEC1107 ID:" + readSpecialRegister(AS220ObisCodeMapper.IEC1107_ID) + "\n";
            returnString += " Meter IECII07 address (optical):    " + readSpecialRegister(AS220ObisCodeMapper.IEC1107_ADDRESS_OP) + "\n";
            returnString += " Meter IECII07 address (electrical): " + readSpecialRegister(AS220ObisCodeMapper.IEC1107_ADDRESS_EL) + "\n";
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

    AS220Registry getAS220Registry() {
        return this.aS220Registry;
    }

    private AS220Profile getAS220Profile() {
        return this.aS220Profile;
    }

    int getBillingCount() throws IOException {
        if (this.billingCount == -1) {

            if (isDataReadout()) {
                this.billingCount = getDataDumpParser().getBillingCounter();
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
                    getLogger().info(AS220.class.getSimpleName() + " - Unable to read billingCounter. Defaulting to 0!");
                }
            }

            if (this.billingCount >= 100) {
                this.billingCount = 0;
                getLogger().warning(AS220.class.getSimpleName() + " - Encountered invalid billingCounter (" + this.billingCount + "). The billingCounter should be between 0 and 100, defaulting to 0!");
            }
        }
        return this.billingCount;
    }

    private String getMeterSerial() throws IOException {
        if (this.meterSerial == null) {
            this.meterSerial = (String) getAS220Registry().getRegister(
                    this.useEquipmentIdentifierAsSerial
                    ? AS220Registry.IEC1107_ADDRESS_EL
                    : AS220Registry.SERIAL
            );
        }
        if(useEquipmentIdentifierAsSerial){
            byte[] bytesFromHexString = ProtocolTools.getBytesFromHexString(meterSerial, "");
            return ProtocolTools.getAsciiFromBytes(bytesFromHexString);
        }
        return this.meterSerial;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        this.aS220Messages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.aS220Messages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return this.aS220Messages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return this.aS220Messages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return this.aS220Messages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return this.aS220Messages.writeValue(value);
    }

    private String readSpecialRegister(String registerName) throws IOException {
        if (registerName.equals(AS220ObisCodeMapper.ID1)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID1)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.ID2)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID2)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.ID3)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID3)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.ID4)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID4)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.ID5)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID5)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.ID6)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.ID6)).getBytes()));
        }

        if (registerName.equals(AS220ObisCodeMapper.IEC1107_ID)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.IEC1107_ID)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.IEC1107_ADDRESS_OP)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.IEC1107_ADDRESS_OP)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.IEC1107_ADDRESS_EL)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAS220Registry().getRegister(AS220Registry.IEC1107_ADDRESS_EL)).getBytes()));
        }
        if (registerName.equals(AS220ObisCodeMapper.FIRMWAREID)) {
            return getFirmwareVersion();
        }

        if (registerName.equals(AS220ObisCodeMapper.FIRMWARE)) {
            String fw;
            String hw;
            String dev;
            String fwdev;

            if (this.iSecurityLevel < 1) {
                return "Unknown (SecurityLevel to low)";
            }

            fwdev = (String) getAS220Registry().getRegister(AS220Registry.FIRMWARE);
            hw = (String) getAS220Registry().getRegister(AS220Registry.HARDWARE);

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
        this.aS220Messages.doDemandReset();
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

    private DataDumpParser getDataDumpParser() throws IOException {
        if (dataDumpParser == null) {
            if (dataReadoutRequest == 2) {
                dataDumpParser = new AS220DataDumpParser(getDataReadout()); // Custom DataDumpParser, cause the parsing of historical billing registers is not-standard
            } else {
                dataDumpParser = new DataDumpParser(getDataReadout());
            }
        }
        return dataDumpParser;
    }

}