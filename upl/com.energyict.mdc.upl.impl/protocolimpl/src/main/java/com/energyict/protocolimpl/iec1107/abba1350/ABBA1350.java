package com.energyict.protocolimpl.iec1107.abba1350;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
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
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
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
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.google.common.collect.Range;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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
 * @author Koenraad Vanderschaeve
 * @author fbl
 * @author jme
 * @version 1.0
 * @beginchanges 18-11-2008 jme > Implemented MessageProtocol to support messages. Messages for new Switch Point Clock data from alphaSET 3.0
 * 20-11-2008 jme > Added check for serial number match
 * 24-11-2008 jme > Added firmware version and hardware key readout
 * 24-11-2008 jme > Added support for power Quality readout (P.02)
 * 27-11-2008 jme > Added support for register readout from
 * 22-01-2009 jme > Removed break command after dataReadout, to prevent non responding meter issues.
 * 20-02-2009 jme > Added timestamp of billing point to registers as toTime.
 * @endchanges
 */
public class ABBA1350
        extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo,
        RegisterProtocol, MessageProtocol, SerialNumberSupport {

    private static final int DEBUG = 0;

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
    private ABBA1350Registry abba1350Registry = null;
    private ABBA1350Profile abba1350Profile = null;
    private ABBA1350Messages abba1350Messages = new ABBA1350Messages(this);
    private ABBA1350ObisCodeMapper abba1350ObisCodeMapper = new ABBA1350ObisCodeMapper(this);

    private byte[] dataReadout = null;
    private int billingCount = -1;
    private String firmwareVersion = null;
    private Date meterDate = null;
    private String meterSerial = null;

    private boolean software7E1;

    public ABBA1350(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAbba1350Profile().getProfileData(lastReading, includeEvents, loadProfileNumber);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getAbba1350Profile().getProfileData(from, to, includeEvents, loadProfileNumber);
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
        if (vdewCompatible == 1) {
            setTimeVDEWCompatible();
        } else {
            setTimeAlternativeMethod();
        }
    }

    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1350Registry().setRegister("TimeDate2", date);
    }

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1350Registry().setRegister("Time", date);
        getAbba1350Registry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        sendDebug("getTime request !!!", 2);
        //if (this.meterDate == null)
        this.meterDate = (Date) getAbba1350Registry().getRegister("TimeDate");
        return new Date(this.meterDate.getTime() - iRoundtripCorrection);
    }

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
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.stringSpec(NODEID.getName()),
                this.integerSpec("EchoCancelling"),
                this.integerSpec("ForceDelay"),
                this.integerSpec(PROFILEINTERVAL.getName()),
                ProtocolChannelMap.propertySpec("ChannelMap", false),
                this.integerSpec("RequestHeader"),
                this.integerSpec("Scaler"),
                this.integerSpec("DataReadout"),
                this.integerSpec("ExtendedLogging"),
                this.integerSpec("VDEWCompatible"),
                this.integerSpec("LoadProfileNumber", false, Range.closed(MIN_LOADPROFILE, MAX_LOADPROFILE)),
                this.stringSpec("Software7E1"));
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

    private PropertySpec integerSpec(String name, boolean required, Range<Integer> validValues) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.propertySpecService::integerSpec);
        UPLPropertySpecFactory.addIntegerValues(specBuilder, validValues);
        return specBuilder.finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName(), "");
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iForceDelay = Integer.parseInt(properties.getTypedProperty("ForceDelay", "0").trim());
            profileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "3600").trim());
            channelMap = new ChannelMap(properties.getTypedProperty("ChannelMap", "0"));
            requestHeader = Integer.parseInt(properties.getTypedProperty("RequestHeader", "1").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "0:0:0:0:0:0"));
            scaler = Integer.parseInt(properties.getTypedProperty("Scaler", "0").trim());
            dataReadoutRequest = Integer.parseInt(properties.getTypedProperty("DataReadout", "0").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getTypedProperty("VDEWCompatible", "0").trim());
            loadProfileNumber = Integer.parseInt(properties.getTypedProperty("LoadProfileNumber", "1"));
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    protected boolean isDataReadout() {
        return (dataReadoutRequest == 1);
    }

    @Override
    public String getRegister(String name) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.getBytes());
        flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] data = flagIEC1107Connection.receiveRawData();
        return new String(data);
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getAbba1350Registry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:40 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (this.firmwareVersion == null) {
            this.firmwareVersion = (String) getAbba1350Registry().getRegister(ABBA1350Registry.FIRMWAREID);
        }
        return this.firmwareVersion;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty,
                    iProtocolRetriesProperty, iForceDelay, iEchoCancelling, 1, software7E1, logger);
            abba1350Registry = new ABBA1350Registry(this, this);
            abba1350Profile = new ABBA1350Profile(this, this, abba1350Registry);

        } catch (ConnectionException e) {
            if (logger != null) {
                logger.severe("ABBA1350: init(...), " + e.getMessage());
            }
        }

    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                dataReadout = cleanDataReadout(flagIEC1107Connection.dataReadout(strID, nodeId));
                // ABBA1350 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
                flagIEC1107Connection.disconnectMACWithoutBreak();
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
                dataReadout = cleanDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        abba1350ObisCodeMapper.initObis();

        if (extendedLogging >= 2) {
            getMeterInfo();
        }
        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

    }

    private byte[] cleanDataReadout(byte[] dro) {
        if (DEBUG >= 1) {
            sendDebug("cleanDataReadout()  INPUT dro = " + new String(dro), 2);
        }

        for (int i = 0; i < dro.length; i++) {
            if (((i + 3) < dro.length) && (dro[i] == '&')) {
                if (dro[i + 3] == '(') {
                    dro[i] = '*';
                }
            }
        }
        if (DEBUG >= 1) {
            sendDebug("cleanDataReadout() OUTPUT dro = " + new String(dro), 2);
        }
        return dro;
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (requestHeader == 1) {
            return getAbba1350Profile().getProfileHeader(loadProfileNumber).getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    int getISecurityLevel() {
        return iSecurityLevel;
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getAbba1350Profile().getProfileHeader(loadProfileNumber).getProfileInterval();
        } else {
            return profileInterval;
        }
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return true;
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    @Override
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERROR", "Request could not execute!");
        EXCEPTION_INFO_MAP.put("ERROR01", "A1350 ERROR 01, invalid command!");
        EXCEPTION_INFO_MAP.put("ERROR06", "A1350 ERROR 06, invalid command!");
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
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return requestHeader == 1;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

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

            sendDebug("readRegister() obis: " + obis.toString(), 2);
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
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.2.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.3.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.4.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.5.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.6.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.7.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.8.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.9.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }
            if ("1.1.0.0.10.255".equals(obis.toString())) {
                return new RegisterValue(obis, readSpecialRegister(abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
            }

            if (obis.getF() != 255) {
                int f = getBillingCount() - Math.abs(obis.getF());
                fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);
            }
            String edis = obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
            try {
                data = read(edis);
            } catch (IOException e1) {
                if (DEBUG >= 3) {
                    e1.printStackTrace();
                }
                throw e1;
            }

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
            }


            // read and parse the value an the unit ()if exists) of the register
            String temp = dp.parseBetweenBrackets(data, 0, 0);
            Unit readUnit = null;
            if (temp.indexOf('*') != -1) {
                readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
                temp = temp.substring(0, temp.indexOf('*'));
                sendDebug("ReadUnit: " + readUnit, 3);
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

            Quantity q;
            if (obis.getUnitElectricity(scaler).isUndefined()) {
                q = new Quantity(bd, obis.getUnitElectricity(0));
            } else {
                if (readUnit != null) {
                    if (!readUnit.equals(obis.getUnitElectricity(scaler))) {
                        String message = "Unit or scaler from obiscode is different from register Unit in meter!!! ";
                        message += " (Unit from meter: " + readUnit;
                        message += " -  Unit from obiscode: " + obis.getUnitElectricity(scaler) + ")\n";

                        sendDebug(message);
                        getLogger().info(message);
                        if (failOnUnitMismatch == 1) {
                            throw new InvalidPropertyException(message);
                        }
                    }
                }
                q = new Quantity(bd, obis.getUnitElectricity(scaler));
            }

            return new RegisterValue(obis, q, eventTime, toTime);

        } catch (NoSuchRegisterException e) {
            String m = "ObisCode " + obis.toString() + " is not supported!";
            if (DEBUG >= 3) {
                e.printStackTrace();
            }
            throw new NoSuchRegisterException(m);
        } catch (InvalidPropertyException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            if (DEBUG >= 3) {
                e.printStackTrace();
            }
            throw new InvalidPropertyException(m);
        } catch (FlagIEC1107ConnectionException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            if (DEBUG >= 3) {
                e.printStackTrace();
            }
            throw new IOException(m);
        } catch (IOException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            if (DEBUG >= 3) {
                e.printStackTrace();
            }
            throw new IOException(m);
        } catch (NumberFormatException e) {
            String m = "ObisCode " + obis.toString() + " is not supported!";
            if (DEBUG >= 3) {
                e.printStackTrace();
            }
            throw new NoSuchRegisterException(m);
        }

    }

    private byte[] read(String edisNotation) throws IOException {
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            sendDebug("Requesting read(): edisNotation = " + edisNotation, 2);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream
                    .toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            sendDebug("Requesting read(): edisNotation = " + edisNotation + " dataReadOut: " + getDataReadout().length, 2);
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
        sendDebug(" translateRegister(): " + obisCode.toString(), 2);
        String reginfo = abba1350ObisCodeMapper.getObisMap().get(obisCode.toString());
        if (reginfo == null) {
            return new RegisterInfo(obisCode.toString());
        } else {
            return new RegisterInfo("");
        }
    }


    private void getRegistersInfo() throws IOException {
        StringBuilder builder = new StringBuilder();

        for (String obis : abba1350ObisCodeMapper.getObisMap().keySet()) {
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

        if (logger != null) {
            logger.info(builder.toString());
        }
    }

    private void getMeterInfo() throws IOException {
        String returnString = "";
        if (iSecurityLevel < 1) {
            returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
        } else {
            returnString += " Meter ID1: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID1) + "\n";
            returnString += " Meter ID2: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID2) + "\n";
            returnString += " Meter ID3: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID3) + "\n";
            returnString += " Meter ID4: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID4) + "\n";
            returnString += " Meter ID5: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID5) + "\n";
            returnString += " Meter ID6: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID6) + "\n";

            returnString += " Meter IEC1107 ID:" + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ID) + "\n";
            returnString += " Meter IECII07 address (optical):    " + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_OP) + "\n";
            returnString += " Meter IECII07 address (electrical): " + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_EL) + "\n";

        }
        if (logger != null) {
            logger.info(returnString);
        }
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, isDataReadout());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty,
                iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    private ABBA1350Registry getAbba1350Registry() {
        return abba1350Registry;
    }

    private ABBA1350Profile getAbba1350Profile() {
        return abba1350Profile;
    }

    int getBillingCount() throws IOException {
        if (this.billingCount == -1) {

            if (isDataReadout()) {
                sendDebug("Requesting getBillingCount() dataReadOut: " + getDataReadout().length, 2);
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
                    getLogger().info(ABBA1350.class.getSimpleName() + " - Unable to read billingCounter. Defaulting to 0!");
                }
            }

            if (this.billingCount >= 100) {
                this.billingCount = 0;
                getLogger().warning(ABBA1350.class.getSimpleName() + " - Encountered invalid billingCounter (" + this.billingCount + "). The billingCounter should be between 0 and 100, defaulting to 0!");
            }
        }
        return this.billingCount;
    }

    private String getMeterSerial() throws IOException {
        if (this.meterSerial == null) {
                this.meterSerial = (String) getAbba1350Registry().getRegister(ABBA1350Registry.SERIAL);
        }
        return this.meterSerial;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        abba1350Messages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return abba1350Messages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return abba1350Messages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return abba1350Messages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return abba1350Messages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return abba1350Messages.writeValue(value);
    }

    public void sendDebug(String str) {
        if (DEBUG >= 1) {
            str = "######## DEBUG > " + str + "\n";
            Logger log = getLogger();
            if (log != null) {
                getLogger().info(str);
            } else {
                System.out.println(str);
            }
        }
    }

    private String readSpecialRegister(String registerName) throws IOException {
        if (registerName.equals(ABBA1350ObisCodeMapper.ID1)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID1)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.ID2)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID2)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.ID3)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID3)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.ID4)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID4)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.ID5)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID5)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.ID6)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.ID6)).getBytes()));
        }

        if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ID)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ID)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_OP)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ADDRESS_OP)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_EL)) {
            return new String(ProtocolUtils.convert2ascii(((String) getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ADDRESS_EL)).getBytes()));
        }
        if (registerName.equals(ABBA1350ObisCodeMapper.FIRMWAREID)) {
            return getFirmwareVersion();
        }

        if (registerName.equals(ABBA1350ObisCodeMapper.FIRMWARE)) {
            String fw;
            String hw;
            String dev;
            String fwdev;

            if (iSecurityLevel < 1) {
                return "Unknown (SecurityLevel to low)";
            }
            fwdev = (String) getAbba1350Registry().getRegister(ABBA1350Registry.FIRMWARE);
            hw = (String) getAbba1350Registry().getRegister(ABBA1350Registry.HARDWARE);

            if ((fwdev != null) && (fwdev.length() >= 30)) {
                fw = fwdev.substring(0, 10);
                dev = fwdev.substring(10, 30);
                fw = new String(ProtocolUtils.convert2ascii(fw.getBytes())).trim();
                dev = new String(ProtocolUtils.convert2ascii(dev.getBytes())).trim();
            } else {
                fw = "Unknown";
                dev = "Unknown";
            }

            if (hw != null) {
                hw = new String(ProtocolUtils.convert2ascii(hw.getBytes())).trim();
            } else {
                hw = "Unknown";
            }

            return dev + " " + "v" + fw + " " + hw;
        }

        return "";
    }

    private void sendDebug(String string, int i) {
        if (DEBUG >= i) {
            sendDebug(string);
        }
    }

}