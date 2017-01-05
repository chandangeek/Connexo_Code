/*
 * PPM.java
 *
 * Created on 16 juli 2004, 8:57
 */

package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.opus.OpusConnection;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author fbo
 * @beginchanges FBL |06012005| In response to OBS150, problem with no password
 * ||
 * || fix: There MUST be a password, and it MUST be 8 characters long.
 * ||
 * || MUST BE PASSWORD reason:
 * || I deduct this from the fact that the Encryption class throws a
 * || NullPointerException when there is no password entered.
 * ||
 * || CHECK IF THE PASSWORD IS TOO SHORT reason:
 * || I deduct this the from the fact that the Encryption class throws an
 * || ArrayIndexOutOfBoundsException when the password is shorter then 8 characters.
 * ||
 * || CHECK IF THE PASSWORD IS TOO LONG reason:
 * || The Powermaster Unit software allows maximum 8 characters.
 * ||
 * || I have added 3 checks in the init method:
 * || - 1) check if there is a password
 * || - 2) check if it is not too short
 * || - 3) check if it is not too long
 * ||
 * ||
 * KV |23032005| Changed header to be compatible with protocol version tool
 * FBL|30032005| Changes to ObisCodeMapper.
 * ||
 * || The old way of mapping obis codes to Max Demand and Cum Max Demand was
 * || wrong: when the c-field was 2 (max demand) or 6 (cum max demand), the
 * || e-field indicated the index of respectively the max demand register, the
 * || cum max demand register.
 * ||
 * || The reason for this is that the PPM allows for tarifs to be defined on
 * || max demand and cum max demand, but the protocol does not provide a way
 * || to read these definitions.  So the protocol does not know wich of the
 * || max demand / cum max demand registers is holding the requested obis
 * || code.
 * ||
 * || To work around this an obiscode with e-field = 0 now returns the maximum
 * || of all registers that are available.  For instance, a meter is configured
 * || with
 * || max demand register 1 = import W value= 10000
 * || max demand register 2 = import W value= 15000
 * || max demand register 3 = empty
 * || max demand register 4 = empty
 * ||
 * || an obiscode 1.1.1.2.0.F would return max demand register 2, since that
 * || contains the highest value for the requested phenonemon: import W.  The
 * || same is true for cum max demand.
 * ||
 * || Also, to allow for specific registers to be retrieved, manufacturer
 * || specific obis codes are used.
 * || for instance
 * || 1.1.1.2.128.F
 * || would return the first import W max demand register
 * ||
 * || For a completely different matter: the sorting of historical data
 * || has changed.  The sorting is no longer done with dates but with the
 * || billing counter.  For more info see HistoricalDataParser.
 * FBL|28022005| Fix for data spikes
 * || When retrieving profile data on or around an interval boundary, the meter
 * || can return invalid data.  These are intervals that are not yet correctly
 * || closed. (So in fact they should not appear in the profile data at all.)
 * || Such an invalid interval shows up in EIServer as a spike, because the value
 * || contains a lot of '9' characters (e.g. '99990.0').
 * || To make sure that only properly closed intervals are stored, intervals that
 * || are less then a minute old are ignored. The end time of an interval needs
 * || to be at least a minute before the current meter time.
 * FBL|02032007| Fix for sporadic data corruption
 * || Added extra checking error checking in communication.
 * JME|17122009| Changed method of setTime to do a adjust time. This method does not waste profile data
 * || Fixed bug in profile parser (AAAssembler) to ignore the invalid marked profile data.
 * @endchanges
 */

public class PPM extends AbstractPPM implements SerialNumberSupport {

    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * The minimum period of time that must be elapsed in order for an interval
     * to be valid/acceptable. (in millisecs) (see Fix for data spikes)
     */
    public static final int MINIMUM_INTERVAL_AGE = 60000;
    private static final int MAX_TIME_DIFF = 50000;

    /**
     * Property keys specific for PPM protocol.
     */
    private static final String PK_OPUS = "OPUS";
    private static final String PK_TIMEOUT = "Timeout";
    private static final String PK_RETRIES = "Retries";
    private static final String PK_FORCE_DELAY = "ForcedDelay";
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";

    /**
     * The historical data register contains data for 4 days
     */
    public static final int NR_HISTORICAL_DATA = 4;

    /**
     * Property Default values
     */
    //final static String PD_ADDRESS = null;
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final long PD_FORCE_DELAY = 350;
    private static final long PD_DELAY_AFTER_FAIL = 500;
    private static final String PD_OPUS = "1";
    private static final String PD_PASSWORD = "--------";
    private static final String PD_EXTENDED_LOGGING = "0";
    private static final int PD_SECURITY_LEVEL = 0;

    private static final long TIME_SHIFT_RATE = (60 * 10500) / 0x07F;
    private static final Map<String, String> EXCEPTION = new HashMap<>();

    static {
        EXCEPTION.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        EXCEPTION.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist" + " in the meter");
        EXCEPTION.put("ERR3", "Invalid Packet Number");
        EXCEPTION.put("ERR5", "Data Identity is locked - password timeout");
        EXCEPTION.put("ERR6", "General Comms error");
    }

    /**
     * Required properties will have NO default value
     */
    private String pProfileInterval = null;
    private String pSerialNumber = null;
    private String pAddress = null;

    private TimeZone timeZone = null;
    private Logger logger = null;

    /**
     * Optional properties make use of default value
     */
    private String pNodeId = PD_NODE_ID;
    private String pPassword = PD_PASSWORD;
    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;
    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    /* Delay in msec between protocol Message Sequences */
    private long pForceDelay = PD_FORCE_DELAY;
    /* Delay in msec after a protocol error */
    private long pDelayAfterFail = PD_DELAY_AFTER_FAIL;
    /* 1 if opus protocol is used, 0 if not */
    private String pOpus = PD_OPUS;
    private String pExtendedLogging = PD_EXTENDED_LOGGING;
    private int pSecurityLevel = PD_SECURITY_LEVEL;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private OpusConnection opusConnection = null;

    private RegisterFactory rFactory = null;
    private Profile profile = null;
    private ObisCodeMapper obisCodeMapper = null;

    private static final String[] REGISTERCONFIG = {
            "TotalImportKwh",
            "TotalExportKwh",
            "TotalImportKvarh",
            "TotalExportKvarh",
            "TotalKvah"
    };

    private boolean software7E1 = false;
    private MeterType meterType = null;
    private final PropertySpecService propertySpecService;

    public PPM(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String) rFactory.getRegister(RegisterFactory.R_SERIAL_NUMBER);
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getMaxRetry() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.stringSpec(PROFILEINTERVAL.getName()),
                new PasswordPropertySpec(PASSWORD.getName(), true, 8),
                this.stringSpec(PK_OPUS),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.stringSpec(PK_EXTENDED_LOGGING),
                this.integerSpec(PK_FORCE_DELAY),
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

    @Override
    public void setUPLProperties(TypedProperties p) throws InvalidPropertyException, MissingPropertyException {
        if (p.getTypedProperty(ADDRESS.getName()) != null) {
            this.pAddress = p.getTypedProperty(ADDRESS.getName());
        }

        if (p.getTypedProperty(NODEID.getName()) != null) {
            pNodeId = p.getTypedProperty(NODEID.getName());
        }

        if (p.getTypedProperty(SERIALNUMBER.getName()) != null) {
            pSerialNumber = p.getTypedProperty(SERIALNUMBER.getName());
        }

        if (p.getTypedProperty(PROFILEINTERVAL.getName()) != null) {
            pProfileInterval = p.getTypedProperty(PROFILEINTERVAL.getName());
        }

        if (p.getTypedProperty(PASSWORD.getName()) != null) {
            pPassword = p.getTypedProperty(PASSWORD.getName());
        }

        if (p.getTypedProperty(PK_OPUS) != null) {
            pOpus = p.getTypedProperty(PK_OPUS);
        }

        if (p.getTypedProperty(PK_TIMEOUT) != null) {
            pTimeout = Integer.parseInt(p.getTypedProperty(PK_TIMEOUT));
        }

        if (p.getTypedProperty(PK_RETRIES) != null) {
            pRetries = Integer.parseInt(p.getTypedProperty(PK_RETRIES));
        }

        if (p.getTypedProperty(PK_EXTENDED_LOGGING) != null) {
            pExtendedLogging = p.getTypedProperty(PK_EXTENDED_LOGGING);
        }

        if (p.getTypedProperty(PK_FORCE_DELAY) != null) {
            pForceDelay = Integer.parseInt(p.getTypedProperty(PK_FORCE_DELAY));
        }

        this.software7E1 = !"0".equalsIgnoreCase(p.getTypedProperty("Software7E1", "0"));
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "PPM protocol init \n" +
                            "- Password         = " + pPassword + "\n" +
                            "- ProfileInterval  = " + pProfileInterval + "\n" +
                            "- SerialNumber     = " + pSerialNumber + "\n" +
                            "- Node Id          = " + pNodeId + "\n" +
                            "- Timeout          = " + pTimeout + "\n" +
                            "- Retries          = " + pRetries + "\n" +
                            "- Extended Logging = " + pExtendedLogging + "\n" +
                            "- RoundTripCorr    = " + pRountTripCorrection + "\n" +
                            "- TimeZone         = " + timeZone + "\n" +
                            "- Force Delay      = " + pForceDelay;

            logger.info(infoMsg);
        }

        if (isOpus()) {
            opusConnection = new OpusConnection(inputStream, outputStream, this);
        } else {
            try {
                this.flagIEC1107Connection = new FlagIEC1107Connection(
                        inputStream,
                        outputStream,
                        this.pTimeout,
                        this.pRetries,
                        this.pForceDelay,
                        0,
                        0,
                        new com.energyict.protocolimpl.iec1107.ppmi1.Encryption(),
                        this.software7E1,
                        logger
                );
            } catch (ConnectionException e) {
                logger.severe("PPM: init(...), " + e.getMessage());
            }
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            if (!isOpus()) {
                this.meterType = this.flagIEC1107Connection.connectMAC(this.pAddress, this.pPassword, this.pSecurityLevel, this.pNodeId);
                String ri = this.meterType.getReceivedIdent().substring(10, 13);
                int version = Integer.parseInt(ri);
                this.logger.log(Level.INFO, "Meter " + this.meterType.getReceivedIdent());
                this.logger.log(Level.INFO, "MeterType version = " + ri + " - " + version);
                rFactory = new OpticalRegisterFactory(this, this);
                validatePassword();
            } else {
                rFactory = new OpusRegisterFactory(this, this);
            }

            obisCodeMapper = new ObisCodeMapper(rFactory);
            profile = new Profile(this, rFactory);
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw e;
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }

        doExtendedLogging();

    }

    private void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            this.logger.info(rFactory.getRegisterInformation().getExtendedLogging() + " \n");
        }
    }

    private void validatePassword() throws IOException {
        String pw = (String) rFactory.getRegister(RegisterFactory.R_OPUS_PASSWORD);
        if ((pw != null) && (pPassword != null) && (pw.equals(pPassword))) {
            return;
        }
        throw new InvalidPropertyException("Configured password does not match the device opus password!");
    }

    @Override
    public void disconnect() throws IOException {
        if (!isOpus()) {
            try {
                this.flagIEC1107Connection.disconnectMAC();
            } catch (FlagIEC1107ConnectionException e) {
                this.logger.severe("disconnect() error, " + e.getMessage());
            }
        } else {
            logger.info("errorCount=" + opusConnection.getErrorCount());
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:41 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData = profile.getProfileData(from, to, includeEvents);
        return ProtocolTools.clipProfileData(from, to, profileData);
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        LoadProfileDefinition lpd = rFactory.getLoadProfileDefinition();
        List l = lpd.toChannelInfoList();
        ChannelInfo ci = (ChannelInfo) l.get(channelId);
        if (ci == null) {
            logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId);
            return null;
        } else {
            logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId + " " + rFactory.getRegister(REGISTERCONFIG[ci.getChannelId() - 1]));
            return (Quantity) rFactory.getRegister(REGISTERCONFIG[ci.getChannelId() - 1]);
        }
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return (Quantity) rFactory.getRegister(name);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return rFactory.getLoadProfileDefinition().getNrOfChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return rFactory.getSubIntervalPeriod().intValue() * SECONDS_PER_MINUTE;
    }

    @Override
    public Date getTime() throws IOException {
        return rFactory.getTimeDate();
    }

    /*
      * Important: A timeset can only be done if the difference is less than
      * 50 seconds.
      *
      * @see com.energyict.protocol.MeterProtocol#setTime()
      */
    @Override
    public void setTime() throws IOException {
        logger.log(Level.INFO, "Setting time ...");

        Date meterTime = getTime();

        Calendar sysCalendar = ProtocolUtils.getCalendar(timeZone);
        sysCalendar.add(Calendar.MILLISECOND, pRountTripCorrection);

        long diff = meterTime.getTime() - sysCalendar.getTimeInMillis();

        if (Math.abs(diff) > MAX_TIME_DIFF) {

            String msg = "Time difference exceeds maximum difference allowed.";
            msg += " ( difference=" + Math.abs(diff) + " ms ).";
            msg += "The time will only be corrected with ";
            msg += MAX_TIME_DIFF + " ms.";
            logger.severe(msg);

            sysCalendar.setTime(meterTime);
            if (diff < 0) {
                sysCalendar.add(Calendar.MILLISECOND, MAX_TIME_DIFF);
            } else {
                sysCalendar.add(Calendar.MILLISECOND, -MAX_TIME_DIFF);
            }

        }

        try {
            if (isOpus()) {
                rFactory.setRegister(OpusRegisterFactory.R_TIME_ADJUSTMENT_RS232, sysCalendar.getTime());
            } else {
                rFactory.setRegister(OpticalRegisterFactory.R_TIME_ADJUSTMENT_OPTICAL, getAdjustmentValue(diff));
            }
        } catch (IOException ex) {
            String msg = "Could not do a timeset, probably wrong password.";
            logger.severe(msg);
            throw new NestedIOException(ex);
        }

    }

    private static String getAdjustmentValue(final long clockDiff) {
        long shiftValue = Math.abs(clockDiff) / TIME_SHIFT_RATE;
        shiftValue = shiftValue > 0x07F ? 0x07F : shiftValue;
        shiftValue |= clockDiff > 0 ? 0x080 : 0x000;
        String returValue = Long.toHexString(shiftValue).toUpperCase();
        returValue = (returValue.length() < 2 ? "0" : "") + returValue;
        return returValue;
    }

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagIEC1107Connection;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String getPassword() {
        return pPassword;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, pTimeout, pRetries, pForceDelay, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        return rFactory.getSerialNumber();
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /**
     * Getter for the {@link RegisterFactory}
     *
     * @return the {@link RegisterFactory}
     */
    protected RegisterFactory getRegisterFactory() {
        return rFactory;
    }

    /**
     * Getter for the {@link OpusConnection}
     *
     * @return the {@link OpusConnection}
     */
    public OpusConnection getOpusConnection() {
        return opusConnection;
    }

    /**
     * Check if the used protocol is OPUS. Will return false if the protocol is IEC1107
     *
     * @return true if protocol is OPUS
     */
    protected boolean isOpus() {
        return "1".equals(pOpus);
    }

    /**
     * Getter for the nodeId property
     *
     * @return the nodeId
     */
    public String getNodeId() {
        return pNodeId;
    }

    /**
     * Getter for the maximumRetries property
     *
     * @return the maximumRetry value
     */
    public int getMaxRetry() {
        return pRetries;
    }

    public long getForceDelay() {
        return pForceDelay;
    }

    public long getDelayAfterFail() {
        return pDelayAfterFail;
    }

    public long getTimeout() {
        return pTimeout;
    }

}