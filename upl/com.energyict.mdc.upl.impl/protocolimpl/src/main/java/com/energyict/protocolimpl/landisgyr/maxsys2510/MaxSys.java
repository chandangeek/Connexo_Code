package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;

/**
 * @author fbo
 * @beginchanges
 * @endchanges Table 15 contains all the register values.  It has 2 + x nr of places in
 * memory to make a copy of all register data.
 * It is divided:
 * - current season:   registers currently updated
 * - previous season:  registers from previous season
 * - self read data:   registers copy from previous rate reset.
 * <p/>
 * From the (S4)manual:
 * "When actuated, the self-read function will save a copy of all energy and
 * demand information, including TOU metrics, and mark the current date and
 * time."
 * <p/>
 * And
 * <p/>
 * "The S4 is capable of storing up to six sets of self-read data. A self-read
 * initiates the transfer of current data into the next available self-read
 * memory block, followed by a demand reset. Only the most recent six self-reads
 * are retained. The oldest self-read is replaced by the most recent when the
 * programmable number has been reached."
 * <p/>
 * There are 2 (independent) types of billing periods:
 * - Seasons
 * - Rate resets
 * <p/>
 * A season has a week schedule.  For every day there are several time of uses
 * defined.  A time of use defines which datablocks are active.
 */

public class MaxSys extends PluggableMeterProtocol implements RegisterProtocol,SerialNumberSupport {

    /**
     * Property keys
     */
    protected static final String PK_NODE_PREFIX = "NodeIdPrefix";
    private static final String PK_SHOULD_DISCONNECT = "ShouldDisconnect";
    private static final String PK_ROUNDTRIPCORRECTION = ROUNDTRIPCORRECTION.getName();
    private static final String PK_FORCE_DELAY = "ForceDelay";
    private static final String PK_READ_UNIT1_SERIALNUMBER = "ReadUnit1SerialNumber";
    private static final String PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE = "ReadProfileDataBeforeConfigChange";

    /**
     * Property Default values
     */
    private static final String PD_NODE_PREFIX = "3";   // MSU Slave (used in EU, doesn't work in US)
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final String PD_EXTENDED_LOGGING = "0";
    private static final int PD_FORCE_DELAY = 250;
    private final PropertySpecService propertySpecService;

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    private String pNodeId = PD_NODE_ID;
    private String pSerialNumber = null;
    private byte[] pPassword;

    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pCorrectTime = 0;
    private int pForceDelay = PD_FORCE_DELAY;

    private String pExtendedLogging = PD_EXTENDED_LOGGING;
    private boolean pShouldDisconnect;

    private LinkLayer linkLayer;
    CommandFactory commandFactory;

    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    private Logger logger = null;

    private Firmware firmware;
    private boolean readUnit1SerialNumber = false;
    private boolean readProfileDataBeforeConfigChange = true;
    private String PD_SHOULD_DISCONNECT = "1";

    private int crn = 0;

    private Table0 table0;
    private Table1 table1;
    private Table3 table3;
    private Table4 table4;
    private Table8 table8;
    private Table11 table11;
    private Table13 table13;
    private Table14 table14;
    private Table15 table15;
    private Table16 table16;
    private Table18 table18;

    public MaxSys(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public String getSerialNumber()  {
        try{
            if (!readUnit1SerialNumber) {
                TableAddress ta = new TableAddress(this, 2, 19);
                return ta.readString(11);
            } else {
                TableAddress ta = new TableAddress(this, 2, 0);
                byte[] values = ta.readBytes(4);
                return getSerialNumber(values).substring(1);
            }
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, pRetries + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.LANDISGYR_SERIALNUMBER),
                this.stringSpecofExactLength(NODEID.getName(), 7, PropertyTranslationKeys.LANDISGYR_NODEID),
                this.stringSpec(PK_NODE_PREFIX, PropertyTranslationKeys.LANDISGYR_NODE_PREFIX),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.LANDISGYR_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.LANDISGYR_RETRIES),
                this.integerSpec(PK_ROUNDTRIPCORRECTION, PropertyTranslationKeys.LANDISGYR_ROUNDTRIPCORRECTION),
                this.integerSpec(CORRECTTIME.getName(), PropertyTranslationKeys.LANDISGYR_CORRECTTIME),
                this.integerSpec(PK_FORCE_DELAY, PropertyTranslationKeys.LANDISGYR_FORCE_DELAY),
                this.stringSpec(EXTENDED_LOGGING.getName(), PropertyTranslationKeys.LANDISGYR_EXTENDED_LOGGING),
                this.stringSpec(PK_SHOULD_DISCONNECT, PropertyTranslationKeys.LANDISGYR_SHOULD_DISCONNECT),
                this.stringSpec(PK_READ_UNIT1_SERIALNUMBER, PropertyTranslationKeys.LANDISGYR_READ_UNIT1_SERIALNUMBER),
                this.stringSpec(PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE, PropertyTranslationKeys.LANDISGYR_READ_PROFILE_DATA_BEFORE_CONFIG_CHANGE));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec stringSpecofExactLength(String name, int length, TranslationKey translationKey) {
        return this.spec(name, translationKey, () -> this.propertySpecService.stringSpecOfExactLength(length));
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        if (properties.getTypedProperty(SERIALNUMBER.getName()) != null) {
            pSerialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
        }

        if (properties.getTypedProperty(NODEID.getName()) != null) {
            pNodeId = getpNodePrefix(properties) + properties.getTypedProperty(NODEID.getName());

            //Replace integer.parse because of overflow, REGEX is cleaner as well
            Pattern pattern = Pattern.compile("[[A-F][a-f]\\d]*");
            Matcher matcher = pattern.matcher(pNodeId);
            if (!matcher.matches()) {
                throw new InvalidPropertyException("NodeId and prefix must be hexadecimal numbers");
            }
        }

        if (properties.getTypedProperty(PASSWORD.getName()) != null) {
            String pwd = properties.getTypedProperty(PASSWORD.getName(), "    ");
            pPassword = new byte[4];
            pPassword[0] = pwd.getBytes()[0];
            pPassword[1] = pwd.getBytes()[1];
            pPassword[2] = pwd.getBytes()[2];
            pPassword[3] = pwd.getBytes()[3];
        }

        if (properties.getTypedProperty(TIMEOUT.getName()) != null) {
            pTimeout = properties.getTypedProperty(TIMEOUT.getName());
        }

        if (properties.getTypedProperty(RETRIES.getName()) != null) {
            pRetries = properties.getTypedProperty(RETRIES.getName());
        }

        if (properties.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
            pRountTripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName());
        }

        if (properties.getTypedProperty(CORRECTTIME.getName()) != null) {
            pCorrectTime = properties.getTypedProperty(CORRECTTIME.getName());
        }

        if (properties.getTypedProperty(PK_FORCE_DELAY) != null) {
            pForceDelay = properties.getTypedProperty(PK_FORCE_DELAY);
        }

        if (properties.getTypedProperty(EXTENDED_LOGGING.getName()) != null) {
            pExtendedLogging = properties.getTypedProperty(EXTENDED_LOGGING.getName());
        }

        if (properties.getTypedProperty(PK_SHOULD_DISCONNECT) != null) {
            pShouldDisconnect = "1".equals(properties.getTypedProperty(PK_SHOULD_DISCONNECT, PD_SHOULD_DISCONNECT));
        }

        readUnit1SerialNumber = "1".equals(properties.getTypedProperty(PK_READ_UNIT1_SERIALNUMBER));
        readProfileDataBeforeConfigChange = !"0".equals(properties.getTypedProperty(PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE));
    }

    /**
     * Getter for the pNodePrefix, which is the prefix to add to the node address
     * for daisy chain addressing
     *
     * Possible prefixes:
     * <ul>
     *     <li>2 -> MSU Master</li>
     *     <li>3 -> MSU Slave (used in EU, doesn't work in US)</li>
     *     <li>4 -> Modem 2400</li>
     *     <li>5 -> Slave (this allows us in the US to connect to the slave device)</li>
     *     <li>6 -> SM1 Modem</li>
     *     <li>7 -> Multitech Modem</li>
     *     <li>9 -> Modem 2400 Master</li>
     *     <li>A -> Modem 2400 Slave</li>
     *     <li>D -> Master (this allows us in the US to address master device)</li>
     *     <liF -> Standalone (this allows us in the US to interrogate standalone meters)></li>
     * </ul>
     */
    protected String getpNodePrefix(TypedProperties p) {
         return p.getTypedProperty(PK_NODE_PREFIX, PD_NODE_PREFIX);
    }

    Date getBeginningOfRecording() throws IOException {
        TableAddress ta = new TableAddress(this, 2, 30);
        byte[] values = ta.readBytes(6);
        return TypeDateTimeRcd.parse(new Assembly(this, new ByteArray(values))).toDate();
    }

    private void sendNodeId() throws IOException {
        try {
            if ((this.pNodeId != null) && !"".equals(pNodeId)) {
                XCommand xCommand = commandFactory.createX(nextCrn(), 0x00, 0x0b); // 0b => slave
                byte arg1 = (byte) Integer.parseInt(pNodeId.substring(0, 2), 16);
                byte arg2 = (byte) Integer.parseInt(pNodeId.substring(2, 4), 16);
                byte arg3 = (byte) Integer.parseInt(pNodeId.substring(4, 6), 16);
                byte arg4 = (byte) Integer.parseInt(pNodeId.substring(6, 8), 16);
                byte[] arg = {arg1, arg2, arg3, arg4};
                xCommand.setArgumnt(arg);
                linkLayer.send(xCommand);
            }
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid node address: " + pNodeId);
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {

            commandFactory = new CommandFactory();
            linkLayer =
                    new LinkLayer(inputStream, outputStream, 0, 0, pRetries, pForceDelay, this);
            sendNodeId();
            obisCodeMapper = new ObisCodeMapper(this);

        } catch (ConnectionException e) {
            logger.severe("MAXSys 2510, " + e.getMessage());
            throw e;
        }

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg = "MaxSys protocol init \n";
            infoMsg += " SerialNr = " + pSerialNumber + ",";
            infoMsg += " Psswd = " + new String(pPassword) + ",";
            infoMsg += " Timeout = " + pTimeout + ",";
            infoMsg += " Retries = " + pRetries + ",";
            infoMsg += " Ext. Logging = " + pExtendedLogging + ",";
            infoMsg += " RoundTripCorr = " + pRountTripCorrection + ",";
            infoMsg += " Correct Time = " + pCorrectTime + ",";
            infoMsg += " TimeZone = " + timeZone.getID();
            logger.info(infoMsg);
        }
    }

    @Override
    public void connect() throws IOException {
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {
            linkLayer.send(commandFactory.createX(nextCrn(), 0x00, 0x0e)); // 0e: return unit id
            getTable0();
            doExtendedLogging();
        } catch (NumberFormatException nex) {
            throw new ProtocolException(nex.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (pShouldDisconnect) {
            this.getLogger().info("disconnect " + pSerialNumber);
            linkLayer.send(commandFactory.createX(nextCrn(), 0x00, 0x10), 1);
        } else {
            ProtocolTools.delay(4000);
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getTable11().getTypeStoreCntrlRcd().getNoOfChnls();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = ProtocolUtils.getCalendar(timeZone);

        Date to = c.getTime();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        Date from = c.getTime();

        return getProfileData(from, to, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getTable12(lastReading, includeEvents).getProfile();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getTable11().getTypeStoreCntrlRcd().getIntvlInMins() * 60;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    private void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }

    protected String getSerialNumber(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int bKar = data[i] & 0xFF;
            builder.append(String.valueOf((char) ProtocolUtils.convertHexLSB(bKar)));
            builder.append(String.valueOf((char) ProtocolUtils.convertHexMSB(bKar)));
        }
        return builder.toString();
    }

    @Override
    public String getProtocolDescription() {
        return "Landis&Gyr MaxSys 2510 SMD";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getTable0().getTypeMaximumValues().getVersionNumber() +
                " " +
                getTable0().getTypeMaximumValues().getRevisionNumber();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Date getTime() throws IOException {
        return getTable1().getTypeMaximumValues().getClockCalendar().toDate();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);

        long nowMilli = calendar.getTimeInMillis();

        /* meterDate */
        Date mDate = getTable1().getTypeMaximumValues().getClockCalendar().toDate();
        long mMilli = mDate.getTime();

        short secondsDelta = (short) ((nowMilli - mMilli) / 1000);
        byte b1 = (byte) (secondsDelta & 0x00FF);
        byte b2 = (byte) ((secondsDelta & 0xFF00) >> 8);

        XCommand xCommand = commandFactory.createX(nextCrn(), 0x00, 0x0d);
        xCommand.setArgumnt(new byte[]{0, 0, b2, b1});
        linkLayer.send(xCommand);
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    public boolean isRequestHeader() {
        return false;
    }

    public byte[] getPassword() {
        return pPassword;
    }

    public byte[] getDataReadout() {
        return null;
    }

    /**
     * for easy debugging
     */
    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * for easy debugging
     */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

    LinkLayer getLinkLayer() {
        return linkLayer;
    }

    Firmware getFirmware() {
        return firmware;
    }

    private int nextCrn() {
        crn = crn + 1;
        return crn;
    }

    Table0 getTable0() throws IOException {
        if (table0 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 0);
            ByteArray ba = linkLayer.send(command);
            table0 = Table0.parse(new Assembly(this, ba));
        }
        return table0;
    }

    private Table1 getTable1() throws IOException {
        //if (table1 == null) {
        StandardCommand command = commandFactory.createY(nextCrn(), 1);
        ByteArray ba = linkLayer.send(command);
        table1 = Table1.parse(new Assembly(this, ba));
        //}
        return table1;
    }

    Table3 getTable3() throws IOException {
        if (table3 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 3);
            ByteArray ba = linkLayer.send(command);
            table3 = Table3.parse(this, new Assembly(this, ba));
        }
        return table3;
    }

    Table4 getTable4() throws IOException {
        if (table4 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 4);
            ByteArray ba = linkLayer.send(command);
            table4 = Table4.parse(this, new Assembly(this, ba));
        }
        return table4;
    }

    Table8 getTable8() throws IOException {
        if (table8 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 8);
            ByteArray ba = linkLayer.send(command);
            table8 = Table8.parse(new Assembly(this, ba));
        }
        return table8;
    }

    Table11 getTable11() throws IOException {
        if (table11 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 11);
            ByteArray ba = linkLayer.send(command);
            table11 = Table11.parse(this, new Assembly(this, ba));
        }
        return table11;
    }

    Table12 getTable12(Date from, boolean includeEvents) throws IOException {
        int noOfChnls = getTable11().getTypeStoreCntrlRcd().getNoOfChnls();
        int dataSize = getTable11().getTypeStoreCntrlRcd().getDataSize();
        int intervalMinutes = getTable11().getTypeStoreCntrlRcd().getIntvlInMins();
        int headerSize = (noOfChnls * 2) + 6;
        int intervalSize = dataSize * noOfChnls;

        Calendar fCal = Calendar.getInstance(timeZone);
        fCal.setTime(from);

        //This was bringing back an extra interval due to the
        //fact that when the before comparison happens, the
        //calendars would have non-zero seconds/milliseconds

        //Here we want to round the to calendar to the next interval in case
        //we jump the interval boundary while reading

        Calendar tCal = Calendar.getInstance(timeZone);
        tCal.setTime(getTime());
        ParseUtils.roundDown2nearestInterval(tCal, intervalMinutes * 60);
        tCal.set(Calendar.MILLISECOND, 0);
        tCal.set(Calendar.SECOND, 0);
        fCal.set(Calendar.MILLISECOND, 0);
        fCal.set(Calendar.SECOND, 0);
        int nrIntervals = 0;
        Date lastReadDate = fCal.getTime();
        while (fCal.before(tCal)) {
            fCal.add(Calendar.MINUTE, intervalMinutes);
            nrIntervals = nrIntervals + 1;
        }
        nrIntervals += 2;
        int totalSize = ((headerSize + (nrIntervals * intervalSize)) / 256) + 1; // KV_CHANGED, add +1 to avoid 0, that is what the doc tells...
        // If bytes 7 and 8 are both
        // zero then the SMD will transmit the number of bytes
        // remaining in the specified table starting from the
        // specified displacement


        StandardCommand command = commandFactory.createY(nextCrn(), 12);
        command.setNbls(totalSize & 0x000000FF);
        command.setNbms(totalSize & 0x0000FF00);
        ByteArray ba = linkLayer.send(command);
        return Table12.parse(new Assembly(this, ba), includeEvents, nrIntervals, readProfileDataBeforeConfigChange, lastReadDate);
    }

    Table13 getTable13() throws IOException {
        if (table13 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 13);
            ByteArray ba = linkLayer.send(command);
            table13 = Table13.parse(new Assembly(this, ba));
        }
        return table13;
    }

    Table14 getTable14() throws IOException {
        if (table14 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 14);
            ByteArray ba = linkLayer.send(command);
            table14 = Table14.parse(new Assembly(this, ba));
        }
        return table14;
    }

    Table15 getTable15() throws IOException {
        if (table15 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 15);
            ByteArray ba = linkLayer.send(command);
            table15 = Table15.parse(new Assembly(this, ba));
        }
        return table15;
    }

    Table16 getTable16() throws IOException {
        if (table16 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 16);
            ByteArray ba = linkLayer.send(command);
            table16 = Table16.parse(new Assembly(this, ba));
        }
        return table16;
    }

    Table18 getTable18() throws IOException {
        if (table18 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 18);
            ByteArray ba = linkLayer.send(command);
            table18 = Table18.parse(new Assembly(this, ba));
        }
        return table18;
    }

    ByteArray read(TableAddress tableAddress) throws IOException {
        StandardCommand command = commandFactory.createY(nextCrn(), tableAddress);
        return linkLayer.send(command);
    }

}