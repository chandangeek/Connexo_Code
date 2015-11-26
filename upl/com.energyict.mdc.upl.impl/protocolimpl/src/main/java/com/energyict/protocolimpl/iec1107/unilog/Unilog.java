/*
 * Unilog.java
 *
 * Created on 10 januari 2005, 09:19
 */

package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author fbo
 * @beginchanges FB|01022005|Initial version
 * KV|23032005|Changed header to be compatible with protocol version tool
 * KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 * @endchanges
 */
public class Unilog extends AbstractUnilog {

    private final static String KAMSTRUP_ID = "/KAM5";

    /**
     * Property keys specific for PPM protocol.
     */
    private final static String PK_TIMEOUT = "Timeout";
    private final static String PK_RETRIES = "Retries";
    private final static String PK_FORCE_DELAY = "ForceDelay";
    private final static String PK_ECHO_CANCELLING = "EchoCancelling";
    private final static String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    private static final String PK_CHANNEL_MAP = "ChannelMap";

    /**
     * Property Default values
     */
    private final static String PD_PASSWORD = "kamstrup";
    private final static int PD_TIMEOUT = 10000;
    private final static int PD_RETRIES = 5;
    private final static int PD_PROFILE_INTERVAL = 3600;
    private final static long PD_FORCE_DELAY = 170;
    private final static int PD_ECHO_CANCELING = 0;
    private final static int PD_IEC1107_COMPATIBLE = 1;
    private final static int PD_ROUNDTRIP_CORRECTION = 0;
    private final static int PD_SECURITY_LEVEL = 1;
    private final static String PD_CHANNEL_MAP = "0,0";

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    private String pAddress = null;
    private String pNodeId = null;
    private String pSerialNumber = null;
    private String pPassword = PD_PASSWORD;

    private String pChannelMap = PD_CHANNEL_MAP;
    private int pProfileInterval = PD_PROFILE_INTERVAL;
    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;
    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Delay in msec between protocol Message Sequences */
    private long pForceDelay = PD_FORCE_DELAY;
    private int pEchoCanceling = PD_ECHO_CANCELING;
    private int pIec1107Compatible = PD_IEC1107_COMPATIBLE;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pSecurityLevel = PD_SECURITY_LEVEL;

    private MeterType meterType;
    private FlagIEC1107Connection flagIEC1107Connection = null;
    private UnilogRegistry registry = null;
    private UnilogProfile profile = null;
    private ProtocolChannelMap protocolChannelMap = null;

    private boolean software7E1;
    private static final String PK_SOFTWARE_7E1 = "Software7E1";

    /**
     * Creates a new instance of Unilog, empty constructor
     */
    public Unilog() {
    }


    /**
     * Validate the properties
     *
     * @param properties
     * @throws MissingPropertyException
     * @throws InvalidPropertyException
     */
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

        if (properties.getProperty(MeterProtocol.ADDRESS) != null) {
            pAddress = properties.getProperty(MeterProtocol.ADDRESS);
        }

        if (properties.getProperty(MeterProtocol.NODEID) != null) {
            pNodeId = properties.getProperty(MeterProtocol.NODEID);
        }

        if (properties.getProperty(MeterProtocol.SERIALNUMBER) != null) {
            pSerialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
        }

        if (properties.getProperty(MeterProtocol.PASSWORD) != null) {
            pPassword = properties.getProperty(MeterProtocol.PASSWORD);
        }

        if (properties.getProperty(MeterProtocol.PROFILEINTERVAL) != null) {
            pProfileInterval = Integer.parseInt(properties
                    .getProperty(MeterProtocol.PROFILEINTERVAL));
        }

        if (properties.getProperty(PK_TIMEOUT) != null) {
            pTimeout = Integer.parseInt(properties.getProperty(PK_TIMEOUT));
        }

        if (properties.getProperty(PK_RETRIES) != null) {
            pRetries = Integer.parseInt(properties.getProperty(PK_RETRIES));
        }

        if (properties.getProperty(PK_FORCE_DELAY) != null) {
            pForceDelay = Integer.parseInt(properties.getProperty(PK_FORCE_DELAY));
        }

        if (properties.getProperty(PK_ECHO_CANCELLING) != null) {
            pEchoCanceling = Integer
                    .parseInt(properties.getProperty(PK_ECHO_CANCELLING));
        }

        if (properties.getProperty(PK_IEC1107_COMPATIBLE) != null) {
            pIec1107Compatible = Integer.parseInt(properties
                    .getProperty(PK_IEC1107_COMPATIBLE));
        }

        if (properties.getProperty(MeterProtocol.ROUNDTRIPCORR) != null) {
            pRountTripCorrection = Integer.parseInt(properties
                    .getProperty(MeterProtocol.ROUNDTRIPCORR));
        }

        this.software7E1 = !properties.getProperty(PK_SOFTWARE_7E1, "0").equalsIgnoreCase("0");

        if (properties.getProperty(Unilog.PK_CHANNEL_MAP) != null) {
            this.pChannelMap = properties.getProperty(Unilog.PK_CHANNEL_MAP);
        }

        protocolChannelMap = new ProtocolChannelMap(pChannelMap);

    }

    /**
     * The required keys of the protocol
     *
     * @return
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    /**
     * The optional keys of the protocol
     *
     * @return
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(PK_TIMEOUT);
        result.add(PK_RETRIES);
        result.add(MeterProtocol.ROUNDTRIPCORR);
        result.add(PK_SOFTWARE_7E1);
        result.add(PK_CHANNEL_MAP);
        return result;
    }

    /**
     * Init the protocol with all the given parameters
     *
     * @param inputStream
     * @param outputStream
     * @param timeZone
     * @param logger
     * @throws IOException
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        setTimeZone(timeZone);
        setLogger(logger);
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, pTimeout, pRetries, pForceDelay, pEchoCanceling, pIec1107Compatible, software7E1, logger);
            registry = new UnilogRegistry(this, this);
            profile = new UnilogProfile(this, registry);
        } catch (ConnectionException e) {
            logger.severe("Unilog: init(...), " + e.getMessage());
        }
    }

    /**
     * Connect to the meter
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            meterType = flagIEC1107Connection.connectMAC(pAddress, pPassword, pSecurityLevel, pNodeId);
            validateSerialNumber();
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    /**
     * Disconnect from the meter
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        }
    }

    /**
     * Validate the serial number with the serial number in EIServer if available
     *
     * @throws IOException
     */
    void validateSerialNumber() throws IOException {
        if (pSerialNumber != null) {
            String meterSerial = meterType.getReceivedIdent();
            if (meterSerial == null) {
                throw new IOException("SerialNumber mismatch! configured serial = " + pSerialNumber + " meter serial unknown");
            } else {
                String trimMeterSerial = meterSerial.substring(5, meterSerial.length());
                trimMeterSerial = trimMeterSerial.replace(new String(new byte[]{0x0D, 0x0A}), "");  // Remove \r\n, but only if necessary

                if (!trimMeterSerial.equals(pSerialNumber)) {
                    throw new IOException("SerialNumber mismatch! configured serial = " + pSerialNumber + " meter serial = " + trimMeterSerial);
                }
            }
        }
    }

    /**
     * Getter for the profile interval field
     *
     * @return
     * @throws UnsupportedException
     * @throws IOException
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return pProfileInterval;
    }

    /**
     * Read the profile data
     *
     * @param from
     * @param to
     * @param includeEvents
     * @return
     * @throws IOException
     * @throws UnsupportedException
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return profile.getProfileData(fromCalendar, toCalendar, getNumberOfChannels(), 1);
    }

    /**
     * Set the device time to the current system time
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        Date date = calendar.getTime();
        registry.setRegister("0.9.1", date);
        registry.setRegister("0.9.2", date);
    }

    /**
     * Read the device time from the connected meter
     *
     * @return
     * @throws IOException
     */
    public Date getTime() throws IOException {
        Date date = (Date) registry.getRegister(registry.R_TIME_DATE);
        return new Date(date.getTime() - pRountTripCorrection);
    }

    /**
     * Get the number of channels. This value is extracted from the channelMap propertie
     *
     * @return
     * @throws UnsupportedException
     * @throws IOException
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return protocolChannelMap.getNrOfProtocolChannels();
    }

    /**
     * Getter for the password field
     *
     * @return
     */
    public String getPassword() {
        return pPassword;
    }

    /**
     * Getter for the number of retries
     *
     * @return
     */
    public int getNrOfRetries() {
        return pRetries;
    }

    /**
     * Get the protocol version
     *
     * @return
     */
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    /**
     * If possible, read the firmware version from the device. At the moment, this is not possible yet
     * with the Unilog protcol, so this method allways returns "Unknown"
     *
     * @return
     * @throws IOException
     * @throws UnsupportedException
     */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return ("Unknown");
    }

    /**
     * Getter for the protocol connection
     *
     * @return
     */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    /**
     * Not supported in the Unigas300 protocol
     *
     * @return
     */
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    /**
     * Checks if the protocol should behave like IEC1107 or manufacturer specific
     *
     * @return
     */
    public boolean isIEC1107Compatible() {
        return (pIec1107Compatible == 1);
    }

    /**
     * Getter for the data readout bytes.
     * Not used in the Unilog protocol, so always returns null
     *
     * @return
     */
    public byte[] getDataReadout() {
        return null;
    }


    /**
     * Check if we should request the header while reading profile data.
     * Not used in the Unilog protocol, so always returns false
     *
     * @return
     */
    public boolean isRequestHeader() {
        return false;
    }

    /**
     * Read a register identified by its obiscode
     *
     * @param obisCode
     * @return
     * @throws IOException
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            Object register = registry.getRegister(obisCode);
            if (register instanceof Quantity) {
                return new RegisterValue(obisCode, (Quantity) register);
            } else {
                return new RegisterValue(obisCode, register.toString());
            }
        } catch (IOException e) {
            throw new NoSuchRegisterException("Problems while reading register " + obisCode + ": " + e.getMessage());
        }
    }
}
