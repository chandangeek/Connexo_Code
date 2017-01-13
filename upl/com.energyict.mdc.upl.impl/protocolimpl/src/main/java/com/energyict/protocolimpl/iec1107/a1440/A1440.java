package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
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
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
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
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
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
import java.util.Properties;
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
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author jme
 * @since 19-aug-2009
 * <p/>
 * 19-08-2009 jme > Copied ABBA1350 protocol as base for new A1440 protocol
 */
public class A1440 extends PluggableMeterProtocol implements HHUEnabler, HalfDuplexEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, MessageProtocol, DemandResetProtocol, SerialNumberSupport {

    private static final int DEBUG = 0;
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";
    private static final String PROPERTY_DATE_FORMAT = "DateFormat";
    private static final String PROPERTY_BILLING_DATE_FORMAT = "BillingDateFormat";
    private static final String INVERT_BILLING_ORDER = "InvertBillingOrder";
    private static final String DEFAULT_DATE_FORMAT = "yy/mm/dd";
    private static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";

    private static final int MIN_LOADPROFILE = 1;
    private static final int MAX_LOADPROFILE = 2;
    private final PropertySpecService propertySpecService;

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
    private A1440Messages a1440Messages = new A1440Messages(this);
    private A1440ObisCodeMapper a1440ObisCodeMapper = new A1440ObisCodeMapper(this);

    private byte[] dataReadout = null;
    private int billingCount = -1;
    private String firmwareVersion = null;
    private Date meterDate = null;
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

    public A1440(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
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
        this.meterDate = (Date) getA1440Registry().getRegister("TimeDate");
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
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.stringSpec(NODEID.getName()),
                this.integerSpec("EchoCancelling"),
                this.integerSpec("ForceDelay"),
                this.stringSpec(PROFILEINTERVAL.getName()),
                ProtocolChannelMap.propertySpec("ChannelMap", false),
                this.stringSpec(PROPERTY_DATE_FORMAT),
                this.stringSpec(PROPERTY_BILLING_DATE_FORMAT),
                this.integerSpec("RequestHeader"),
                this.integerSpec("Scaler"),
                this.integerSpec("DataReadout"),
                this.integerSpec("ExtendedLogging"),
                this.integerSpec("VDEWCompatible"),
                this.integerSpec("LoadProfileNumber", Range.closed(MIN_LOADPROFILE, MAX_LOADPROFILE)),
                this.stringSpec("Software7E1"),
                this.integerSpec("RS485RtuPlusServer"),
                this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS),
                this.stringSpec(INVERT_BILLING_ORDER),
                this.stringSpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL),
                this.integerSpec("FailOnUnitMismatch"),
                this.integerSpec("HalfDuplex"));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, Range<Integer> validValues) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, this.propertySpecService::integerSpec);
        UPLPropertySpecFactory.addIntegerValues(specBuilder, validValues);
        return specBuilder.finish();
    }

    @Override
    public void setUPLProperties(TypedProperties typedProperties) throws MissingPropertyException, InvalidPropertyException {
        Properties properties = typedProperties.toStringProperties();
        try {
            this.strID = properties.getProperty(ADDRESS.getName(), "");
            this.strPassword = properties.getProperty(PASSWORD.getName());
            this.iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty(TIMEOUT.getName(), "20000").trim());
            this.iProtocolRetriesProperty = Integer.parseInt(properties.getProperty(RETRIES.getName(), "5").trim());
            this.iRoundtripCorrection = Integer.parseInt(properties.getProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            this.iSecurityLevel = Integer.parseInt(properties.getProperty(SECURITYLEVEL.getName(), "1").trim());
            this.nodeId = properties.getProperty(NODEID.getName(), "");
            this.iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            this.iForceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "0").trim());
            this.profileInterval = Integer.parseInt(properties.getProperty(PROFILEINTERVAL.getName(), "3600").trim());
            this.dateFormat = properties.getProperty(PROPERTY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
            this.billingDateFormat = properties.getProperty(PROPERTY_BILLING_DATE_FORMAT);
            this.requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
            // Todo: next to property parse instructions are in conflict
            this.channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
            this.protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0:0:0:0:0:0"));

            this.scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
            this.dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
            this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            this.vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
            this.loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));
            this.software7E1 = !"0".equalsIgnoreCase(properties.getProperty("Software7E1", "0"));
            this.failOnUnitMismatch = Integer.parseInt(properties.getProperty("FailOnUnitMismatch", "0"));
            this.halfDuplex = Integer.parseInt(properties.getProperty("HalfDuplex", "0").trim());
            this.rs485RtuPlusServer = Integer.parseInt(properties.getProperty("RS485RtuPlusServer", "0").trim());
            this.limitMaxNrOfDays = Integer.parseInt(properties.getProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
			this.invertBillingOrder = getBooleanProperty(properties, INVERT_BILLING_ORDER);
            this.useEquipmentIdentifierAsSerial = getBooleanProperty(properties, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    protected boolean isDataReadout() {
        return (this.dataReadoutRequest == 1);
    }

	private boolean getBooleanProperty(Properties properties, String propertyName) {
		return "1".equals(properties.getProperty(propertyName, "0").trim());
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

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-05-31 09:07:29 +0300 (Tue, 31 May 2016)$";
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

            if ("1.1.0.0.1.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.2.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.3.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.4.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.5.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.6.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.7.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.8.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.9.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.10.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("0.0.97.97.0.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister((String) this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
            }

            if (obis.getF() != 255) {
				if (getBillingCount() < (Math.abs(obis.getF()) + 1)) {
					throw new NoSuchRegisterException("Billing count is only " + getBillingCount() + " so cannot read register with F = " + obis.getF());
				}

				int f = invertBillingOrder
					? Math.abs(obis.getF())
					: getBillingCount() - Math.abs(obis.getF());
                fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);

                // try to read the time stamp, and us it as the register toTime.
                try {
                    String billingPoint = "";
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

        } catch (NoSuchRegisterException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new NoSuchRegisterException(m);
        } catch (InvalidPropertyException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new InvalidPropertyException(m);
        } catch (FlagIEC1107ConnectionException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new IOException(m);
        } catch (IOException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new IOException(m);
        } catch (NumberFormatException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new NoSuchRegisterException(m);
        }
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
            data = this.flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            data = ddp.getRegisterStrValue(edisNotation).getBytes();
        }
        return data;
    }

    private Quantity readTime() throws IOException {
        Long seconds = new Long(getTime().getTime() / 1000);
        return new Quantity(seconds, Unit.get(BaseUnit.SECOND));
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String reginfo = (String) this.a1440ObisCodeMapper.getObisMap().get(obisCode.toString());
        if (reginfo == null) {
            return new RegisterInfo(obisCode.toString());
        } else {
            return new RegisterInfo("");
        }
    }

    private void getRegistersInfo() throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String obis : this.a1440ObisCodeMapper.getObisMap().keySet()) {
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

    public A1440Registry getA1440Registry() {
        return this.a1440Registry;
    }

    public A1440Profile getA1440Profile() {
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
        }
        return this.billingCount;
    }

    private String getMeterSerial() throws IOException {
        if (this.meterSerial == null) {
            this.meterSerial = (String) getA1440Registry().getRegister(
                    this.useEquipmentIdentifierAsSerial
                    ? A1440Registry.IEC1107_ADDRESS_EL
                    : A1440Registry.SERIAL
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
        this.a1440Messages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.a1440Messages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
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
        if (registerName.equals(A1440ObisCodeMapper.ERROR_REGISTER)) {
            return new String((String) getA1440Registry().getRegister(A1440Registry.ERROR_REGISTER));
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

    @Override
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

}