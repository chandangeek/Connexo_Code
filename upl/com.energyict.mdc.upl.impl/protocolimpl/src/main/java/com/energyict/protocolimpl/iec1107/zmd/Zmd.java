package com.energyict.protocolimpl.iec1107.zmd;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
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
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
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
 * @author FBL
 * @version 1.0
 * @beginchanges FBL |14062007|
 * || Bugfix:
 * || RegisterValues for registers from last billing point did not have a toTime.
 * || This is now filled with register: 0.1.0*F.
 * GNA |march 2008| Added serialnumber support and message is thrown when meter doesn't support this; then use property 'ignoreSerialNumberCheck'
 * JME |30-05-2011| Added event time to billing counter + made billing point *00 valid.
 * @endchanges
 */

public class Zmd extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, SerialNumberSupport {

    private static final ObisCode BILLING_COUNTER = ObisCode.fromString("1.1.0.1.0.255");
    private static final ObisCode SERIAL_NUMBER = ObisCode.fromString("1.0.9.0.0.255");
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    private String strID;
    private String strPassword;
    private String serialNumber;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int profileInterval;
    private ProtocolChannelMap protocolChannelMap;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;

    private FlagIEC1107Connection flagIEC1107Connection;
    private Registry registry;
    private Profile profile;

    private byte[] dataReadout;
    private int billingCount = -1;
    private Map<String, String> obisMap = new LinkedHashMap<>();

    private Date lastBillingTime = null;
    private int lastBilling = -1;

    private static SimpleDateFormat registerFormat;
    private DataDumpParser dataDumpParser;
    private boolean software7E1;

    public Zmd(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return profile.getProfileData(lastReading, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return profile.getProfileData(from, to, includeEvents);
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        registry.setRegister("Time", date);
        registry.setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) registry.getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.IEC1107_SERIALNUMBER),
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
            profileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "3600").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "0,0,0,0"));
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }

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
        registry.setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() {
        return "Unknown";
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        try {

            flagIEC1107Connection =
                    new FlagIEC1107Connection(inputStream, outputStream,
                            iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0,
                            iEchoCancelling, iIEC1107Compatible, software7E1, logger);

            flagIEC1107Connection.setErrorSignature("ER");

            registry = new Registry(this, this);
            profile = new Profile(this, this, registry);

            registerFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
            registerFormat.setTimeZone(getTimeZone());

        } catch (ConnectionException e) {
            logger.severe("init(...), " + e.getMessage());
            throw new NestedIOException(e);
        }

    }

    @Override
    public void connect() throws IOException {
        try {
            if (getFlagIEC1107Connection().getHhuSignOn() == null) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            if (getFlagIEC1107Connection().getHhuSignOn() != null) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

            if (!verifyMeterSerialNR()) {
                throw new IOException("L&G ZMD, connect, Wrong SerialNR!, EISerialNumber=" + serialNumber + ", MeterSerialNumber=" + getSerialNumber());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        initObis();

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

    }

    @Override
    public String getSerialNumber() {
        try {
            String mSerialNumber = getDataDumpParser().getRegisterFFStrValue("0.0.0");
            return mSerialNumber.substring(mSerialNumber.indexOf("(") + 1, mSerialNumber.indexOf(")"));
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    private boolean verifyMeterSerialNR() {
        return (serialNumber == null) || ("".compareTo(serialNumber) == 0) || (serialNumber.compareTo(getSerialNumber()) == 0);
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
        return getProtocolChannelMap().getNrOfProtocolChannels();
    }

    public int getProfileInterval() throws IOException {
        return profileInterval;
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

    @Override
    public String getExceptionInfo(String id) {
        String strippedId = ProtocolUtils.stripBrackets(id);
        if ("ER0001".equals(strippedId)) {
            return "command not recognised";
        }
        if ("ER0002".equals(strippedId)) {
            return "faulty parameters";
        }
        return null;
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public RegisterValue readRegister(ObisCode obis) throws IOException {
        try {
            return toRegisterValue(obis);
        } catch (NoSuchRegisterException | FlagIEC1107ConnectionException | NumberFormatException e) {
            throw createNoSuchRegisterException(obis);
        } catch (IOException e) {
            throw createNoSuchRegisterException(obis);
        }

    }

    private RegisterValue toRegisterValue(ObisCode obis) throws IOException {
        if (isTimeCode(obis) && (obis.getF() == 255)) {
            return new RegisterValue(obis, toQuantity(getTime()));
        }

        if (isTimeCode(obis) && (obis.getF() == 0)) {
            return new RegisterValue(obis, toQuantity(getLastBillTime()));
        }

        if ((obis.getC() == 97) && (obis.getD() == 97) && (obis.getE() == 0)) {
            String str = getDataDumpParser().getRegisterFFStrValue("F.F");
            return new RegisterValue(obis, str);
        }

        if (obis.equals(SERIAL_NUMBER)) {
            byte[] data = getDataDumpParser().getRegisterStrValue(toEdis(obis)).getBytes();
            String text = parseText(data);
            return new RegisterValue(obis, null, null, null, null, null, 0, text);
        }

        Quantity quantity = getDataDumpParser().getRegister(toEdis(obis));
        Date eventTime;
        if (obis.equals(BILLING_COUNTER)) {
            eventTime = getLastBillTime();
        } else {
            eventTime = getDataDumpParser().getRegisterDateTime(toEdis(obis), getTimeZone());
        }
        Date toTime = null;
        if (obis.getF() != 255) {
            toTime = getDataDumpParser().getRegisterDateTime("0.1.0" + getEdisBillingNotation(obis), getTimeZone());
        }
        return new RegisterValue(obis, quantity, eventTime, toTime);
    }

    private String parseText(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseBetweenBrackets(data, 0, 0);
    }

    /* Convert Obis code to Edis code. */
    private String toEdis(ObisCode obis) throws IOException {
        return obis.getC() + "." + obis.getD() + "." + obis.getE() + getEdisBillingNotation(obis);
    }

    private String getEdisBillingNotation(ObisCode obis) throws IOException {
        if (obis.getF() != 255) {
            return "*" + ProtocolUtils.buildStringDecimal(getBillingCount() - Math.abs(obis.getF()), 2);
        } else {
            return "";
        }
    }

    /* Is o a code represeting a timestamp? */
    private boolean isTimeCode(ObisCode o) {
        return o.getA() == 1 &&
                o.getB() == 1 &&
                o.getC() == 0 &&
                o.getD() == 1 &&
                o.getE() == 2;
    }

    private NoSuchRegisterException createNoSuchRegisterException(ObisCode o) {
        String msg = "ObisCode " + o.toString() + " is not supported.";
        return new NoSuchRegisterException(msg);
    }

    private DataDumpParser getDataDumpParser() throws IOException {
        if (dataDumpParser == null) {
            dataDumpParser = new DataDumpParser(getDataReadout());
        }
        return dataDumpParser;
    }

    private Quantity toQuantity(Date date) {
        Long seconds = new Long(date.getTime() / 1000);
        return new Quantity(seconds, Unit.get(BaseUnit.SECOND));
    }

    // billingcount = last billing period...
    private int getBillingCount() throws IOException {
        if (billingCount == -1) {
            Quantity quantity = getDataDumpParser().getRegister("0.1.0");
            billingCount = quantity.intValue();
        }
        return billingCount;
    }

    /* Read 0.1.0*F: toTime of last billing period */
    private Date getLastBillTime() throws IOException {
        if (lastBillingTime == null) {
            lastBillingTime = getDataDumpParser().getRegisterDateTime("0.1.0*" + ProtocolUtils.buildStringDecimal(getBillingCount(), 2), getTimeZone());
        }
        return lastBillingTime;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisMap.get(obisCode.toString()));
    }

    private void initObis() throws IOException {

        obisMap.put("1.1.0.1.2.255", "Date and time (0.9.1 0.9.2)");
        obisMap.put("1.1.0.1.2.VZ", "Date and time last billing point");
        obisMap.put("1.1.0.1.0.255", "Billing counter");


        obisMap.put("1.1.1.8.1.255", "Energy +A rate 1 (1.8.1)");
        String obis = "1.1.1.8.1.VZ";
        String dscr = "Energy +A rate 1 (1.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.1.8.2.255", "Energy +A rate 2 (1.8.2)");
        obis = "1.1.1.8.2.VZ";
        dscr = "Energy +A rate 2 (1.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.2.8.1.255", "Energy -A rate 1 (2.8.1)");
        obis = "1.1.2.8.1.VZ";
        dscr = "Energy -A rate 1 (2.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        obisMap.put("1.1.2.8.2.255", "Energy -A rate 2 (2.8.2)");
        obis = "1.1.2.8.2.VZ";
        dscr = "Energy +R rate 2 (2.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.3.8.1.255", "Energy +R rate 1 (3.8.1)");
        obis = "1.1.3.8.1.VZ";
        dscr = "Energy +R rate 1 (3.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        obisMap.put("1.1.3.8.2.255", "Energy +R rate 2 (3.8.2)");
        obis = "1.1.3.8.2.VZ";
        dscr = "Energy +R rate 2 (3.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.4.8.1.255", "Energy -R rate 1 (4.8.1)");
        obis = "1.1.4.8.1.VZ";
        dscr = "Energy -R rate 1 (4.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        obisMap.put("1.1.4.8.2.255", "Energy -R rate 2 (4.8.2)");
        obis = "1.1.4.8.2.VZ";
        dscr = "Energy -R rate 2 (4.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.1.6.1.255", "Maximum Demand +A rate 1 (1.6.1)");
        obis = "1.1.1.6.1.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.1.6.2.255", "Maximum Demand +A rate 2 (1.6.2)");
        obis = "1.1.1.6.2.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);


        obisMap.put("1.1.1.6.3.255", "Maximum Demand +A rate 3 (1.6.3)");
        obis = "1.1.1.6.3.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.3*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        obisMap.put("1.1.1.2.1.255", "Cumulative Maximum Demand +A rate 1 (1.2.1)");
        obisMap.put("1.1.1.2.2.255", "Cumulative Maximum Demand +A rate 2 (1.2.2)");
        obisMap.put("1.1.1.2.3.255", "Cumulative Maximum Demand +A rate 3 (1.2.3)");

    }

    private void getRegistersInfo() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (String obis : obisMap.keySet()) {
            ObisCode oc = ObisCode.fromString(obis);
            builder.append(obis)
                    .append(" ").append(translateRegister(oc).toString()).append("\n");
            if (extendedLogging == 2) {
                try {
                    RegisterValue value = readRegister(oc);
                    builder.append(value.toString()).append("\n");
                } catch (NoSuchRegisterException ex) {
                    // ignore
                }
            }

        }

        logger.info(builder.toString());

    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, true);
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

}