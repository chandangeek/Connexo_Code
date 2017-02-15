package com.energyict.protocolimpl.iec1107.emh.lzqj;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the LZQJ meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|14112007|	Initial version
 *         JM|02102009|	Fix for mantis issue #5322
 *         Changed register date fields (eventTime, toTime, fromTime) to show the correct billing timestamps.
 *         The protocol can only read these values if the are configured to be in the datadump of the device.
 *         The registers with the billing timestamps are 0.1.2*01, 0.1.2*02, ... 0.1.2*xx
 *         JM|11032010| Added new registermappings JIRA: COMMUNICATION-28
 * @version 1.0
 * @endchanges
 */
public class LZQJ extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol {

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
    private int iIEC1107Compatible;
    private int profileInterval;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int dataReadoutRequest;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private LZQJRegistry lzqjRegistry = null;
    private LZQJProfile lzqjProfile = null;

    private List<RegisterValue> registerValues = null;

    private byte[] dataReadout = null;

    private boolean software7E1;
    private boolean isFixedProfileTimeZone;
    private boolean profileHelper = false;

    /**
     * Indication whether longNameObisCodes can be used
     */
    private boolean longNameObisCodes = false;

    public LZQJ(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        this.profileHelper = true;
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        ProfileData pd = getProfileData(calendar.getTime(), includeEvents);
        this.profileHelper = false;
        return pd;
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        this.profileHelper = true;
        ProfileData pd = getLzqjProfile().getProfileData(lastReading, includeEvents);
        this.profileHelper = false;
        return pd;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        this.profileHelper = true;
        ProfileData pd = getLzqjProfile().getProfileData(from, to, includeEvents);
        this.profileHelper = false;
        return pd;
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
        Calendar calendar;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getLzqjRegistry().setRegister("TimeDate2", date);
    }

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getLzqjRegistry().setRegister("Time", date);
        getLzqjRegistry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getLzqjRegistry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec("DataReadout", PropertyTranslationKeys.IEC1107_DATAREADOUT),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("VDEWCompatible", PropertyTranslationKeys.IEC1107_VDEWCOMPATIBLE),
                this.integerSpec("FixedProfileTimeZone", PropertyTranslationKeys.IEC1107_FIXED_PROFILE_TIMEZONE),
                this.stringSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE_7E1));
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

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getTypedProperty("IEC1107Compatible", "1").trim());
            profileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "900").trim());
            requestHeader = Integer.parseInt(properties.getTypedProperty("RequestHeader", "0").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "0,0,0,0"));
            dataReadoutRequest = Integer.parseInt(properties.getTypedProperty("DataReadout", "1").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getTypedProperty("VDEWCompatible", "1").trim());
            isFixedProfileTimeZone = (Integer.parseInt(properties.getTypedProperty("FixedProfileTimeZone", "1")) == 1);
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    private boolean isDataReadout() {
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
        getLzqjRegistry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        String name;
        String firmware = "";
        ByteArrayOutputStream byteArrayOutputStream;
        byte[] data;

        try {
            name = "0.2.0" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += "Configuration program version number: " + new String(data);
        } catch (Exception e) {
            firmware += "Configuration program version number: (none)";
        }

        try {
            name = "0.2.1*01" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Parameter number: " + new String(data);
        } catch (Exception e) {
            firmware += " - Parameter number: (none)";
        }

        try {
            name = "0.2.1*02" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Parameter settings: " + new String(data);
        } catch (Exception e) {
            firmware += " - Parameter settings: (none)";
        }

        try {
            name = "0.2.1*50" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Set number: " + new String(data);
        } catch (Exception e) {
            firmware += " - Set number: (none)";
        }
        return firmware;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            flagIEC1107Connection.setAddCRLF(true);
            lzqjRegistry = new LZQJRegistry(this, this);
        } catch (ConnectionException e) {
            logger.severe("LZQJ: init(...), " + e.getMessage());
        }

    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

            /*
            Check if we need to use LongName ObisCodes(the term longNames is not entirely correct because the F field is not even put in ...)
             */
            if (isDataReadout()) {
                this.longNameObisCodes = new String(dataReadout).indexOf("1-1:") > 0;
            } else {
                this.longNameObisCodes = getLzqjProfile().checkForLongObisCodes(getProfileInterval());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

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
            return getLzqjProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getLzqjProfile().getProfileHeader().getProfileInterval();
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
        if (profileHelper) {
            if (isFixedProfileTimeZone) {
                // Apparently the profile is always returned in GMT+01 ...
                return TimeZone.getTimeZone("GMT+01:00");
            } else {
                return timeZone;
            }
        } else {
            return timeZone;
        }
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    protected void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout.clone();
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
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
        EXCEPTION_INFO_MAP.put("ERROR01", "EMH LZQJ ERROR 01, invalid command!");
        EXCEPTION_INFO_MAP.put("ERROR06", "EMH LZQJ ERROR 06, invalid command!");
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
    public com.energyict.protocolimpl.base.ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();
            int VZ = Math.abs(obisCode.getF());

            if ((billingPoint - VZ) < 0) {
                throw new NoSuchRegisterException("No such a billing point.");
            }

            obisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint - VZ);

            // read the non billing register to reuse the unit in case of billingpoints...
            try {
                doReadRegister(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255), false);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp (toTime)
            try {
                int toBP = billingPoint - VZ;
                if ((toBP > 0) && (toBP <= 99)) {
                    doReadRegister(new ObisCode(1, 1, 0, 1, 2, toBP), true);
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp (fromTime)
            try {
                int fromBP = billingPoint - (VZ - 1);
                if ((fromBP > 0) && (fromBP <= 99)) {
                    doReadRegister(new ObisCode(1, 1, 0, 1, 2, fromBP), true);
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

        } // if (obisCode.getF() != 255)


        // JME:	Special case for obiscode == 1.1.0.1.0.255 (billing point):
        //		Read the date of the billing reset and apply it to the billingPointRegister as eventTime
        if (obisCode.toString().equalsIgnoreCase("1.1.0.1.0.255")) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();

            RegisterValue reg_date;
            try {
                reg_date = doReadRegister(new ObisCode(1, 1, 0, 1, 2, billingPoint), true);
                if (reg_date != null) {
                    billingPointRegister = new RegisterValue(
                            billingPointRegister.getObisCode(),
                            billingPointRegister.getQuantity(),
                            reg_date.getToTime(), // eventTime from billing point
                            billingPointRegister.getFromTime(),
                            billingPointRegister.getToTime(),
                            billingPointRegister.getReadTime(),
                            billingPointRegister.getRtuRegisterId(),
                            billingPointRegister.getText()
                    );
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            return billingPointRegister;
        }

        return doReadRegister(obisCode, false);
    }

    private RegisterValue doReadRegister(ObisCode obisCode, boolean billingTimestamp) throws IOException {
        RegisterValue registerValue = findRegisterValue(obisCode);
        if (registerValue == null) {
            if (billingTimestamp) {
                registerValue = doTheReadBillingRegisterTimestamp(obisCode);
            } else {
                registerValue = doTheReadRegister(obisCode);
            }
            registerValues.add(registerValue);
        }
        return registerValue;
    }

    private byte[] readRegisterData(ObisCode obisCode) throws IOException {
        String edisNotation = EdisObisMapper.getEdisCodeFromObisCode(obisCode, longNameObisCodes);
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            if (edisNotation.contains("0.1.0")) {
                String name = edisNotation + "(;)";
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(name.getBytes());
                flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
                data = flagIEC1107Connection.receiveRawData();
            } else {
                data = ddp.getRegisterStrValue(edisNotation).getBytes();
            }
        }
        return data;
    }

    private Quantity parseQuantity(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseQuantityBetweenBrackets(data, 0, 0);
    }

    private Date parseDate(byte[] data, int pos) throws IOException {
        Date date;
        try {
            DataParser dp = new DataParser(getTimeZone());
            VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
            String dateStr = dp.parseBetweenBrackets(data, 0, pos);
            if ("".compareTo(dateStr) == 0) {
                return null;
            }
            vts.parse(dateStr);
            date = vts.getCalendar().getTime();
            return date;
        } catch (DataParseException e) {
            //absorb
            return null;
        }
    }

    private RegisterValue doTheReadRegister(ObisCode obisCode) throws IOException {
        try {

            byte[] data = readRegisterData(obisCode);
            RegisterValue registerValue;
            Quantity quantity = parseQuantity(data);
            Date eventTime = parseDate(data, 1);
            Date fromTime = null;
            Date toTime = null;

            // in case of unitless AND billing register
            // find the non billing register and use that unit if the non billing register exist
            // also find the timestamp for that billingpoint and add it to the registervalue
            if (obisCode.getF() != 255) {

                if (quantity.getBaseUnit().getDlmsCode() == BaseUnit.UNITLESS) {
                    registerValue = findRegisterValue(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255));
                    if (registerValue != null) {
                        quantity = new Quantity(quantity.getAmount(), registerValue.getQuantity().getUnit());
                    }
                }

                registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, obisCode.getF()));
                if (registerValue != null) {
                    toTime = registerValue.getToTime();
                }

                int bp = obisCode.getF() - 1;
                if (bp > 0) {
                    registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, bp));
                    if (registerValue != null) {
                        fromTime = registerValue.getToTime();
                    }
                }

            } else if (!obisCode.equals(ObisCode.fromString("1.1.0.1.0.255"))) {
                RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
                int billingPoint = billingPointRegister.getQuantity().intValue();
                if (billingPoint > 0) {
                    registerValue = findRegisterValue(ObisCode.fromString("1.1.0.1.2." + billingPoint));
                    if (registerValue != null) {
                        fromTime = registerValue.getToTime();
                    }
                }
            }

            return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime, new Date());
        } catch (NoSuchRegisterException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    private RegisterValue doTheReadBillingRegisterTimestamp(ObisCode obisCode) throws IOException {
        try {
            byte[] data = readRegisterData(obisCode);
            Date date = parseDate(data, 0);
            return new RegisterValue(obisCode, null, null, date);
        } catch (NoSuchRegisterException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    private RegisterValue findRegisterValue(com.energyict.obis.ObisCode obisCode) {
        if (registerValues == null) {
            registerValues = new ArrayList<>();
        } else {
            for (RegisterValue r : registerValues) {
                if (r.getObisCode().equals(obisCode)) {
                    return r;
                }
            }
        }
        return null;
    }

    @Override
    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    private void getRegistersInfo() {
        StringBuilder builder = new StringBuilder();
        if (isDataReadout()) {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append(new String(getDataReadout()));
        } else {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append("All OBIS codes are translated to EDIS codes but not all codes are configured in the meter.\n");
            builder.append("It is not possible to retrieve a list with all registers in the meter. Consult the configuration of the meter.");
            builder.append("\n");
        }
        logger.info(builder.toString());

    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, isDataReadout());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    private LZQJRegistry getLzqjRegistry() {
        return lzqjRegistry;
    }

    private LZQJProfile getLzqjProfile() {
        lzqjProfile = new LZQJProfile(this, this, lzqjRegistry);
        return lzqjProfile;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    void profileHelperSetter(boolean value) {
        this.profileHelper = value;
    }

    protected void setConnection(FlagIEC1107Connection connection) {
        this.flagIEC1107Connection = connection;
    }

}