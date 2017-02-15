/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a1440;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DemandResetProtocol;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.RtuPlusServerHalfDuplexController;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author jme
 * @since 19-aug-2009
 * <p>
 * 19-08-2009 jme > Copied ABBA1350 protocol as base for new A1440 protocol
 */
public class A1440 extends PluggableMeterProtocol implements HHUEnabler, HalfDuplexEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, MessageProtocol, DemandResetProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elster AS1440 IEC1107";
    }

    private static final int DEBUG = 0;
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";

    private static final int MIN_LOADPROFILE = 1;
    private static final int MAX_LOADPROFILE = 2;
    private static final int ACTION_DELAY_PER_5_SEC = 5;

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
    private A1440Registry a1440Registry = null;
    private A1440Profile a1440Profile = null;
    private A1440Messages a1440Messages = new A1440Messages(this);
    private A1440ObisCodeMapper a1440ObisCodeMapper = new A1440ObisCodeMapper(this);

    private byte[] dataReadout = null;
    private int billingCount = -1;
    private String firmwareVersion = null;
    private Date meterDate = null;
    private String meterSerial = null;

    private boolean software7E1;

    private HalfDuplexController halfDuplexController;
    private long halfDuplex;

    private int rs485RtuPlusServer = 0;
    private int limitMaxNrOfDays = 0;

    @Inject
    public A1440(PropertySpecService propertySpecService) {
        super(propertySpecService);
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

    public Date getTime() throws IOException {
        this.meterDate = (Date) getA1440Registry().getRegister("TimeDate");
        return new Date(this.meterDate.getTime() - this.iRoundtripCorrection);
    }

    /**
     * This implementation calls <code> validateProperties </code> and assigns
     * the argument to the properties field
     */
    @Override
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    /**
     * Validates the properties. The default implementation checks that all
     * required parameters are present.
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

        try {
            Optional<String> anyMissingKey =
                    this.getRequiredKeys()
                            .stream()
                            .filter(key -> properties.getProperty(key) == null)
                            .findAny();
            if (anyMissingKey.isPresent()) {
                throw new MissingPropertyException(anyMissingKey.get() + " key missing");
            }
            this.strID = properties.getProperty(MeterProtocol.ADDRESS, "");
            this.strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            this.iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            this.iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            this.iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            this.iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            this.nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            this.iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            this.iForceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "0").trim());
            this.profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            this.channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
            this.requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
            this.protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0:0:0:0:0:0"));
            this.scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
            this.dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
            this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            this.vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
            this.loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));
            this.software7E1 = !"0".equals(properties.getProperty("Software7E1", "0"));
            this.failOnUnitMismatch = Integer.parseInt(properties.getProperty("FailOnUnitMismatch", "0"));
            this.halfDuplex = Integer.parseInt(properties.getProperty("HalfDuplex", "0").trim());
            this.rs485RtuPlusServer = Integer.parseInt(properties.getProperty("RS485RtuPlusServer", "0").trim());
            this.limitMaxNrOfDays = Integer.parseInt(properties.getProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }

        if ((this.loadProfileNumber < MIN_LOADPROFILE) || (this.loadProfileNumber > MAX_LOADPROFILE)) {
            throw new InvalidPropertyException("Invalid loadProfileNumber (" + this.loadProfileNumber + "). Minimum value: " + MIN_LOADPROFILE
                    + " Maximum value: " + MAX_LOADPROFILE);
        }

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
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     */
    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        return Arrays.asList(
                "LoadProfileNumber",
                "Timeout",
                "Retries",
                "SecurityLevel",
                "EchoCancelling",
                "ChannelMap",
                "RequestHeader",
                "Scaler",
                "DataReadout",
                "ExtendedLogging",
                "VDEWCompatible",
                "ForceDelay",
                "Software7E1",
                "HalfDuplex",
                "FailOnUnitMismatch",
                "RS485RtuPlusServer",
                PR_LIMIT_MAX_NR_OF_DAYS);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (this.firmwareVersion == null) {
            this.firmwareVersion = (String) getA1440Registry().getRegister(A1440Registry.FIRMWAREID);
        }
        return this.firmwareVersion;
    }

    /**
     * initializes the receiver
     */
    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            this.flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, this.iIEC1107TimeoutProperty, this.iProtocolRetriesProperty,
                    this.iForceDelay, this.iEchoCancelling, 1, null, this.halfDuplex != 0 ? this.halfDuplexController : null, this.software7E1, logger);
            this.a1440Registry = new A1440Registry(this, this);
            this.a1440Profile = new A1440Profile(this, this, this.a1440Registry);

        } catch (ConnectionException e) {
            if (logger != null) {
                logger.severe("A1440: init(...), " + e.getMessage());
            }
        }

    }

    /**
     * @throws IOException
     */
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

        validateSerialNumber();
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

    @Override
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
    public Object getCache() {
        return null;
    }

    @Override
    public Object fetchCache(int rtuid) {
        return null;
    }

    @Override
    public void setCache(Object cacheObject) {
    }

    @Override
    public void updateCache(int rtuid, Object cacheObject) {
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

    static Map<String, String> exceptionInfoMap = new HashMap<>();

    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
        exceptionInfoMap.put("ERROR00", "A1440 ERROR 00, no valid command!");
        exceptionInfoMap.put("ERROR01", "A1440 ERROR 01, unknown command!");
        exceptionInfoMap.put("ERROR02", "A1440 ERROR 02, no access without manufacturer password");
        exceptionInfoMap.put("ERROR03", "A1440 ERROR 03, no access without hardware lock released");
        exceptionInfoMap.put("ERROR04", "A1440 ERROR 04, no valid class");
        exceptionInfoMap.put("ERROR05", "A1440 ERROR 05, no additional data available");
        exceptionInfoMap.put("ERROR06", "A1440 ERROR 06, command format not valid!");
        exceptionInfoMap.put("ERROR07", "A1440 ERROR 07, function is not supported!");
        exceptionInfoMap.put("ERROR08", "A1440 ERROR 08, demand reset not allowed!");
        exceptionInfoMap.put("ERROR09", "A1440 ERROR 09, load profile initialisation not activated!");
        exceptionInfoMap.put("ERROR10", "A1440 ERROR 10, ripple receiver not enabled!");
        exceptionInfoMap.put("ERROR11", "A1440 ERROR 11, no valid time and date!");
        exceptionInfoMap.put("ERROR12", "A1440 ERROR 12, no access to the desired storage!");
        exceptionInfoMap.put("ERROR13", "A1440 ERROR 13, no access, because the set mode was not activated by the alternate button!");
        exceptionInfoMap.put("ERROR14", "A1440 ERROR 14, no access without password!");
        exceptionInfoMap.put("ERROR15", "A1440 ERROR 15, no access with closed terminal cover!");
        exceptionInfoMap.put("ERROR16", "A1440 ERROR 16, no access due to configuration change denial!");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
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

    /**
     * Getter for property requestHeader.
     *
     * @return Value of property requestHeader.
     */
    @Override
    public boolean isRequestHeader() {
        return this.requestHeader == 1;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return this.protocolChannelMap;
    }

    /* Translate the obis codes to edis codes, and read */
    @Override
    public RegisterValue readRegister(ObisCode obis) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        Date eventTime = null;
        Date toTime = null;
        String fs = "";
        String toTimeString;
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
                int f = getBillingCount() - Math.abs(obis.getF());
                if (f < 0) {
                    f += 100;
                    //throw new NoSuchRegisterException("Billing count is only " + getBillingCount() + " so cannot read register with F = " + obis.getF());
                }
                fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);

                // try to read the time stamp, and us it as the register toTime.
                try {
                    String billingPoint;
                    if ("1.1.0.1.0.255".equalsIgnoreCase(obis.toString())) {
                        billingPoint = "*" + ProtocolUtils.buildStringDecimal(getBillingCount(), 2);
                    } else {
                        billingPoint = fs;
                    }
                    VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
                    timeStampData = read("0.1.2" + billingPoint);
                    toTimeString = dp.parseBetweenBrackets(timeStampData);
                    vts.parse(toTimeString);
                    toTime = vts.getCalendar().getTime();
                } catch (Exception e) {
                    // If encountering a timeout, protocol session should fail immediately. It is not useful to continue.
                    if (e instanceof FlagIEC1107ConnectionException && ((FlagIEC1107ConnectionException) e).getReason() == FlagIEC1107Connection.TIMEOUT_ERROR) {
                        throw (FlagIEC1107ConnectionException) e;
                    }
                }
            }

            String edis = obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
            data = read(edis);


            // read and parse the value an the unit ()if exists) of the register
            String temp = dp.parseBetweenBrackets(data, 0, 0);
            Unit readUnit = null;
            if (temp.indexOf('*') != -1) {
                readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
                temp = temp.substring(0, temp.indexOf('*'));
            }

            if ((temp == null) || (temp.isEmpty())) {
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
            if (obis.getUnitElectricity(this.scaler).isUndefined()) {
                q = new Quantity(bd, obis.getUnitElectricity(0));
            } else {
                if (readUnit != null) {
                    if (!readUnit.equals(obis.getUnitElectricity(this.scaler))) {
                        String message = "Unit or scaler from obiscode is different from register Unit in meter!!! ";
                        message += " (Unit from meter: " + readUnit;
                        message += " -  Unit from obiscode: " + obis.getUnitElectricity(this.scaler) + ")\n";
                        getLogger().info(message);
                        if (this.failOnUnitMismatch == 1) {
                            throw new InvalidPropertyException(message);
                        }
                    }
                }
                q = new Quantity(bd, obis.getUnitElectricity(this.scaler));
            }

            return new RegisterValue(obis, q, eventTime, toTime);

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

    Quantity readTime() throws IOException {
        Long seconds = new Long(getTime().getTime() / 1000);
        return new Quantity(seconds, Unit.get(BaseUnit.SECOND));
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String reginfo = (String) this.a1440ObisCodeMapper.getObisMap().get(obisCode.toString());
        if (reginfo == null) {
            reginfo = obisCode.getDescription();
        }
        return new RegisterInfo("" + reginfo);
    }

    private void getRegistersInfo() throws IOException {
        StringBuilder rslt = new StringBuilder();

        Iterator i = this.a1440ObisCodeMapper.getObisMap().keySet().iterator();
        while (i.hasNext()) {
            String obis = (String) i.next();
            ObisCode oc = ObisCode.fromString(obis);

            if (DEBUG >= 5) {
                try {
                    rslt.append(translateRegister(oc) + "\n");
                    rslt.append(readRegister(oc) + "\n");
                } catch (NoSuchRegisterException nsre) {
                    // ignore and continue
                }
            } else {
                rslt.append(obis + " " + translateRegister(oc) + "\n");
            }
        }

        getLogger().info(rslt.toString());
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
                    getLogger().info("Unable to read billingCounter. Defaulting to 0!");
                }
            }

        }
        return this.billingCount;
    }

    private String getMeterSerial() throws IOException {
        if (this.meterSerial == null) {
            this.meterSerial = (String) getA1440Registry().getRegister(A1440Registry.SERIAL);
        }
        return this.meterSerial;
    }

    protected void validateSerialNumber() throws IOException {
        if ((this.serialNumber == null) || ("".compareTo(this.serialNumber) == 0)) {
            return;
        }
        if (this.serialNumber.compareTo(getMeterSerial()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + getMeterSerial() + ", configured sn=" + this.serialNumber);
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
            String fw;
            String dev;

            if (this.iSecurityLevel < 1) {
                return "Unknown (SecurityLevel to low)";
            }

            String fwdev = (String) getA1440Registry().getRegister(A1440Registry.FIRMWARE);
            String hw = (String) getA1440Registry().getRegister(A1440Registry.HARDWARE);

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
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    @Override
    public Optional<BreakerStatus> getBreakerStatus() throws IOException {
        ContactorController cc = new A1440ContactorController(this);
        return Optional.of(cc.getContactorState());
    }
}