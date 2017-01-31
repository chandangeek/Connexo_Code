/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PPM.java
 *
 * Created on 16 juli 2004, 8:57
 */

package com.energyict.protocolimpl.iec1107.ppm;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppm.opus.OpusConnection;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * JME|01092009| Fixed bug in DataIdentity resulting in an exception while using IEC1107 (OPUS = 0)
 * || Argument was not copied to object property because of typo. Changed "lenght" to "length" in argument name.
 * || Added fixes for Java code quality
 * JME|09092009| Added support for setTime() while using the optical connection (OPUS = 0)
 * JME|17122009| Changed method of setTime to do a adjust time. This method does not waste profiledata
 * @endchanges
 */
public class PPM extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "ABB/GE PPM Issue2 OPUS";
    }

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

    private static final String PK_DELAY_AFTER_FAIL = "DelayAfterFail";
    private static final String PK_SECURITY_LEVEL = "SecurityLevel";
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";

    /**
     * Property Default values
     */
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final long PD_FORCE_DELAY = 350;

    private static final long PD_DELAY_AFTER_FAIL = 500;
    private static final int PD_SECURITY_LEVEL = 2;
    private static final String PD_OPUS = "1";
    private static final String PD_EXTENDED_LOGGING = "0";

    private static final long TIME_SHIFT_RATE = (60 * 10500) / 0x07F;

    /**
     * Property values
     * Required properties will have NO default value
     * Optional properties make use of default value
     */
    private String pAddress = null;
    private String pNodeId = PD_NODE_ID;
    private String pSerialNumber = null;
    private String pPassword = null;

    private TimeZone timeZone = null;
    private Logger logger = null;

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
    private int pSecurityLevel = PD_SECURITY_LEVEL;
    //String pProfileInterval = null;
    /* 1 if opus protocol is used, 0 if not */
    private String pOpus = PD_OPUS;
    private String pExtendedLogging = PD_EXTENDED_LOGGING;

    private int pCorrectTime;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private OpusConnection opusConnection = null;
    private MeterType meterType = null;
    private RegisterFactory rFactory = null;
    private Profile profile = null;
    private ObisCodeMapper obisCodeMapper = null;

    private boolean software7E1;

    private final String[] REGISTERCONFIG = {
            "TotalImportKwh",
            "TotalExportKwh",
            "TotalImportKvarh",
            "TotalExportKvarh",
            "TotalKvah"
    };

    @Inject
    public PPM(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /* ___ Implement interface MeterProtocol ___ */

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#
      *      setProperties(java.util.Properties)
      */
    public void setProperties(Properties p) throws InvalidPropertyException, MissingPropertyException {

        if (p.getProperty(MeterProtocol.ADDRESS) != null) {
            this.pAddress = p.getProperty(MeterProtocol.ADDRESS);
        }

        if (p.getProperty(MeterProtocol.NODEID) != null) {
            this.pNodeId = p.getProperty(MeterProtocol.NODEID);
        }

        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null) {
            this.pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
        }

        if (p.getProperty(MeterProtocol.PASSWORD) != null) {
            this.pPassword = p.getProperty(MeterProtocol.PASSWORD);
        }

        if (p.getProperty(PK_OPUS) != null) {
            this.pOpus = p.getProperty(PK_OPUS);
        }

        if (p.getProperty(PK_TIMEOUT) != null) {
            this.pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
        }

        if (p.getProperty(PK_RETRIES) != null) {
            this.pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
        }

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null) {
            this.pRountTripCorrection = Integer.parseInt(p.getProperty(MeterProtocol.ROUNDTRIPCORR));
        }

        if (p.getProperty(PK_DELAY_AFTER_FAIL) != null) {
            this.pDelayAfterFail = Integer.parseInt(p.getProperty(PK_DELAY_AFTER_FAIL));
        }

        if (p.getProperty(PK_SECURITY_LEVEL) != null) {
            this.pRetries = Integer.parseInt(p.getProperty(PK_SECURITY_LEVEL));
        }

        if (p.getProperty(MeterProtocol.CORRECTTIME) != null) {
            this.pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));
        }

        if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
            this.pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        }

        if (p.getProperty(PK_FORCE_DELAY) != null) {
            this.pForceDelay = Integer.parseInt(p.getProperty(PK_FORCE_DELAY));
        }

        this.software7E1 = !p.getProperty("Software7E1", "0").equalsIgnoreCase("0");

        validateProperties();

    }

    private void validateProperties() throws InvalidPropertyException {

        if (this.pPassword == null) {
            String msg = "";
            msg += "There was no password entered, stopping communication. ";
            throw new InvalidPropertyException(msg);
        }

        if (this.pPassword.length() < 8) {
            String msg = "";
            msg += "Password is too short, the length must be 8 characters, stopping communication. ";
            throw new InvalidPropertyException(msg);
        }

        if (this.pPassword.length() > 8) {
            String msg = "";
            msg += "Password is too long, the length must be 8 characters, stopping communication. ";
            throw new InvalidPropertyException(msg);
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#init( java.io.InputStream,
      *      java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
      */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg = "PPM protocol init \n"
                    + "- Address          = " + this.pAddress + "\n"
                    + "- Node Id          = " + this.pNodeId + "\n"
                    + "- SerialNumber     = " + this.pSerialNumber + "\n"
                    + "- Password         = " + this.pPassword + "\n"
                    + "- Opus             = " + this.pOpus + "\n"
                    + "- Timeout          = " + this.pTimeout + "\n"
                    + "- Retries          = " + this.pRetries + "\n"
                    + "- Extended Logging = " + this.pExtendedLogging + "\n"
                    + "- RoundTripCorr    = " + this.pRountTripCorrection + "\n"
                    + "- Correct Time     = " + this.pCorrectTime + "\n"
                    + "- TimeZone         = " + timeZone + "\n"
                    + "- Force Delay      = " + this.pForceDelay + "\n"
                    + "- Software7E1      = " + this.software7E1;

            logger.info(infoMsg);
        }

        if (isOpus()) {
            this.opusConnection = new OpusConnection(inputStream, outputStream, this);
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
                        new com.energyict.protocolimpl.iec1107.ppm.Encryption(),
                        this.software7E1,
                        logger
                );
            } catch (ConnectionException e) {
                logger.severe("PPM: init(...), " + e.getMessage());
            }
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#connect()
      */
    public void connect() throws IOException {
        try {
            if (!isOpus()) {
                this.meterType = this.flagIEC1107Connection.connectMAC(this.pAddress, this.pPassword, this.pSecurityLevel, this.pNodeId);
                String ri = this.meterType.getReceivedIdent().substring(10, 13);
                int version = Integer.parseInt(ri);
                this.logger.log(Level.INFO, "Meter " + this.meterType.getReceivedIdent());
                this.logger.log(Level.INFO, "MeterType version = " + ri + " - " + version);
            }

            this.rFactory = new RegisterFactory(this, this, PPMMeterType.ISSUE2);
            this.profile = new Profile(this, this.rFactory);

            validateSerialNumber();
            doExtendedLogging();

        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }

    }

    /*  change on: 26/01/2005.  The method should basically ignore the leading
      *  dashes (-) in the comparison.
      */
    private void validateSerialNumber() throws IOException {
        if ((this.pSerialNumber == null) || ("".equals(this.pSerialNumber))) {
            return;
        }

        String sn = (String) this.rFactory.getRegister("SerialNumber");
        if (sn != null) {
            String snNoDash = sn.replaceAll("-+", "");
            String pSerialNumberNoDash = this.pSerialNumber.replaceAll("-+", "");
            if (pSerialNumberNoDash.equals(snNoDash)) {
                return;
            }
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + this.pSerialNumber);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#disconnect()
      */
    public void disconnect() throws IOException {
        if (!isOpus()) {
            try {
                this.flagIEC1107Connection.disconnectMAC();
            } catch (FlagIEC1107ConnectionException e) {
                this.logger.severe("disconnect() error, " + e.getMessage());
            }
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    PK_OPUS,
                    PK_TIMEOUT,
                    PK_RETRIES,
                    PK_EXTENDED_LOGGING,
                    PK_FORCE_DELAY,
                    "Software7E1");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return this.pAddress;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
      */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        ProfileData profileData = this.profile.getProfileData(new Date(), new Date(), includeEvents);
        this.logger.log(Level.INFO, profileData.toString());
        return profileData;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
      *      boolean)
      */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = this.profile.getProfileData(lastReading, new Date(), includeEvents);
        this.logger.log(Level.INFO, profileData.toString());
        return profileData;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
      *      java.util.Date, boolean)
      */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData = this.profile.getProfileData(from, to, includeEvents);
        this.logger.log(Level.INFO, profileData.toString());
        return profileData;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getMeterReading(int)
      */
    public Quantity getMeterReading(int channelId) throws IOException {
        LoadProfileDefinition lpd = this.rFactory.getLoadProfileDefinition();
        List l = lpd.toChannelInfoList();
        ChannelInfo ci = (ChannelInfo) l.get(channelId);
        if (ci == null) {
            this.logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId);
            return null;
        } else {
            this.logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId + " " + this.rFactory.getRegister(this.REGISTERCONFIG[(ci.getChannelId() - 1)]));
            return (Quantity) this.rFactory.getRegister(this.REGISTERCONFIG[(ci.getChannelId() - 1)]);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getMeterReading(java.lang.String)
      */
    public Quantity getMeterReading(String name) throws IOException {
        return (Quantity) this.rFactory.getRegister(name);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNumberOfChannels()
      */
    public int getNumberOfChannels() throws IOException {
        return this.rFactory.getLoadProfileDefinition().getNrOfChannels();
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProfileInterval()
      */
    public int getProfileInterval() throws IOException {
        return this.rFactory.getSubIntervalPeriod().intValue() * 60;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
      */
    public String getRegister(String name) throws IOException {
        return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getTime()
      */
    public Date getTime() throws IOException {
        return rFactory.getTimeDate();
    }

    /*
      * Important: A timeset can only be done if the difference is less than
      * 50 seconds.
      *
      * @see com.energyict.protocol.MeterProtocol#setTime()
      */
    public void setTime() throws IOException {
        logger.log(Level.INFO, "Setting time");

        Date meterTime = getTime();

        Calendar sysCalendar = null;
        sysCalendar = ProtocolUtils.getCalendar(timeZone);
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
                rFactory.setRegister(RegisterFactory.R_TIME_ADJUSTMENT_RS232, sysCalendar.getTime());
            } else {
                rFactory.setRegister(RegisterFactory.R_TIME_ADJUSTMENT_OPTICAL, getAdjustmentValue(diff));
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

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String,
      *      java.lang.String)
      */
    public void setRegister(String name, String value) throws IOException {
    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#getCache()
      */
    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object)
      */
    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.MeterProtocol#release()
      */
    public void release() throws IOException {
    }

    /* ___ Implement interface ProtocolLink ___ */

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getChannelMap()
      */
    public ChannelMap getChannelMap() {
        return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
      */
    public byte[] getDataReadout() {
        return null;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.
      *      ProtocolLink#getFlagIEC1107Connection()
      */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagIEC1107Connection;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107. ProtocolLink#getLogger()
      */
    public Logger getLogger() {
        return this.logger;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getPassword()
      */
    public String getPassword() {
        return this.pPassword;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getTimeZone()
      */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /* ___ Implement interface HHUEnabler ___ */

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.base.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel,
      *      boolean)
      */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.pTimeout, this.pRetries, this.pForceDelay, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.base.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
      */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.base.HHUEnabler#getHHUDataReadout()
      */
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.base.SerialNumber#getSerialNumber(com.energyict.dialer.core.SerialCommunicationChannel,
      *      java.lang.String)
      */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        // KV 22072005 unused code
        //SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        //String nodeId = discoverInfo.getNodeId();

        return this.rFactory.getSerialNumber();
    }

    static Map<String, String> exception = new HashMap<>();

    static {
        exception.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        exception.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist" + " in the meter");
        exception.put("ERR3", "Invalid Packet Number");
        exception.put("ERR5", "Data Identity is locked - password timeout");
        exception.put("ERR6", "General Comms error");
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocolimpl.base.MeterExceptionInfo#getExceptionInfo(java.lang.String)
      */
    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exception.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    /* ___ Implement interface RegisterProtocol ___ */

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
      */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    public ObisCodeMapper getObisCodeMapper() throws IOException {
        if (this.obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper(this.rFactory);
        }
        return obisCodeMapper;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.energyict.protocol.RegisterProtocol#translateRegister(com.energyict.obis.ObisCode)
      */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /* ___ ___ */

    public RegisterFactory getRegisterFactory() {
        return this.rFactory;
    }

    public OpusConnection getOpusConnection() {
        return this.opusConnection;
    }

    public boolean isOpus() {
        return "1".equals(this.pOpus);
    }

    public String getNodeId() {
        return this.pNodeId;
    }

    public int getMaxRetry() {
        return this.pRetries;
    }

    public long getForceDelay() {
        return this.pForceDelay;
    }

    public long getDelayAfterFail() {
        return this.pDelayAfterFail;
    }

    public long getTimeout() {
        return this.pTimeout;
    }

    public void doExtendedLogging() throws IOException {

        if ("1".equals(this.pExtendedLogging)) {
            this.logger.info(this.rFactory.getRegisterInformation().getExtendedLogging() + " \n");
        }

    }

}
