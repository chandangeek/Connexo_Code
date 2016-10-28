/*
 * IskraEmeco.java
 *
 * Created on 8 mei 2003, 17:56
 */

/*
 *  Changes:
 *  KV 15022005 Changed RegisterConfig to allow B field obiscodes != 1
 */
package com.energyict.protocolimpl.iec1107.iskraemeco;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the Iskra meter IEC1107 protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|29012004|Changed serial number and device id behaviour
 *         KV|17022004| extended with MeterExceptionInfo
 *         KV|23032005|Changed header to be compatible with protocol version tool
 *         KV|30032005|Improved registerreading, configuration data
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 * @version 1.0
 * @endchanges
 */
public class IskraEmeco extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, MeterExceptionInfo, RegisterProtocol, SerialNumberSupport {

    private static final byte DEBUG = 0;

    private static final String[] ISKRAEMECO_METERREADINGS_DEFAULT = {"Total Energy A+", "Total Energy R1", "Total Energy R4"};

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
    private int iProfileInterval;
    private ChannelMap channelMap = null;
    private int extendedLogging;

    private TimeZone timeZone;
    private Logger logger;

    int readCurrentDay;

    FlagIEC1107Connection flagIEC1107Connection = null;
    IskraEmecoRegistry iskraEmecoRegistry = null;
    IskraEmecoProfile iskraEmecoProfile = null;
    RegisterConfig regs = new EDPRegisterConfig(); // we should use an infotype property to determine the registerset

    byte[] dataReadout = null;

    private boolean software7E1;

    /**
     * Creates a new instance of ABBA1500, empty constructor
     */
    public IskraEmeco() {
    } // public IskraEmeco()

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.YEAR, -10);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar, toCalendar, includeEvents);
    }


    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        return getIskraEmecoProfile().getProfileData(fromCalendar, toCalendar, getNumberOfChannels(), 1, includeEvents, isReadCurrentDay());
    }

    // Only for debugging
    public ProfileData getProfileData(Calendar from, Calendar to) throws IOException {
        return getIskraEmecoProfile().getProfileData(from,
                to,
                getNumberOfChannels(),
                1,
                false, isReadCurrentDay());
    }

    public Quantity getMeterReading(String name) throws IOException {
        try {
            return (Quantity) getIskraEmecoRegistry().getRegister(name);
        } catch (ClassCastException e) {
            throw new IOException("IskraEmeco, getMeterReading, register " + name + " is not type Quantity");
        }
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        String[] ISKRAEMECO_METERREADINGS = null;
        try {
            ISKRAEMECO_METERREADINGS = ISKRAEMECO_METERREADINGS_DEFAULT;

            if (channelId >= getNumberOfChannels()) {
                throw new IOException("IskraEmeco, getMeterReading, invalid channelId, " + channelId);
            }
            return (Quantity) getIskraEmecoRegistry().getRegister(ISKRAEMECO_METERREADINGS[channelId]);
        } catch (ClassCastException e) {
            throw new IOException("IskraEmeco, getMeterReading, register " + ISKRAEMECO_METERREADINGS[channelId] + " (" + channelId + ") is not type Quantity");
        }
    }

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getIskraEmecoRegistry().setRegister("TimeDateWrite", date);
        //getIskraEmecoRegistry().setRegister("0.9.2",date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        Date date = (Date) getIskraEmecoRegistry().getRegister("TimeDateReadOnly");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    public byte getLastProtocolState() {
        return -1;
    }

    /************************************** MeterProtocol implementation ***************************************/

    @Override
    public String getSerialNumber() {
        try {
            return (String) getIskraEmecoRegistry().getRegister("meter serial number");
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            serialNumber = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "1.5:5.5:8.5"));
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay", "0"));
            this.software7E1 = !"0".equalsIgnoreCase(properties.getProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }

    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name <br>
     * @return the register value
     * @throws IOException             <br>
     * @throws UnsupportedException    <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException {
        return ProtocolUtils.obj2String(getIskraEmecoRegistry().getRegister(name));
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException             <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException    <br>
     */
    public void setRegister(String name, String value) throws IOException {
        getIskraEmecoRegistry().setRegister(name, value);
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "SecurityLevel",
                    "EchoCancelling",
                    "IEC1107Compatible",
                    "ChannelMap",
                    "ExtendedLogging",
                    "ReadCurrentDay",
                    "Software7E1");
    }

    /** Protocol Version **/
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException {
        try {
            return ((String) getIskraEmecoRegistry().getRegister("software revision number"));
        } catch (IOException e) {
            throw new IOException("IskraEmeco, getFirmwareVersion, " + e.getMessage());
        }
    } // public String getFirmwareVersion()

    /**
     * initializes the receiver
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            iskraEmecoRegistry = new IskraEmecoRegistry(this, this);
            iskraEmecoProfile = new IskraEmecoProfile(this, this, iskraEmecoRegistry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }
    }

    public void connect() throws IOException {
        try {
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo();
    }

    public void disconnect() throws NestedIOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfChannels();
    }

    public int getProfileInterval() throws IOException {
        Object obj = getIskraEmecoRegistry().getRegister("Profile Interval");
        if (obj == null) {
            return iProfileInterval;
        } else {
            return ProtocolUtils.obj2int(obj) * 60;
        }
    }

    private IskraEmecoRegistry getIskraEmecoRegistry() {
        return iskraEmecoRegistry;
    }

    private IskraEmecoProfile getIskraEmecoProfile() {
        return iskraEmecoProfile;
    }


    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    public String getPassword() {
        return strPassword;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    public byte[] getHHUDataReadout() {
        setDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
        return getDataReadout();
    }

    public void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
    }


    public void release() throws IOException {
    }

    // KV 17022004 implementation of MeterExceptionInfo
    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ER01", "Unknown command");
        EXCEPTION_INFO_MAP.put("ER02", "Invalid command");
        EXCEPTION_INFO_MAP.put("ER03", "Command format failure");
        EXCEPTION_INFO_MAP.put("ER04", "Read-only code");
        EXCEPTION_INFO_MAP.put("ER05", "Write-only code");
        EXCEPTION_INFO_MAP.put("ER06", "Command => no R/W allowed");
        EXCEPTION_INFO_MAP.put("ER07", "Access denied");
        EXCEPTION_INFO_MAP.put("ER08", "Non R3 variable");
        EXCEPTION_INFO_MAP.put("ER09", "Variable is not command");
        EXCEPTION_INFO_MAP.put("ER10", "Command not executed");
        EXCEPTION_INFO_MAP.put("ER11", "Code format error");
        EXCEPTION_INFO_MAP.put("ER12", "Non data-read-out variable");
        EXCEPTION_INFO_MAP.put("ER13", "Variable without unit");
        EXCEPTION_INFO_MAP.put("ER14", "Wrong previous values index");
        EXCEPTION_INFO_MAP.put("ER15", "Code without offset");
        EXCEPTION_INFO_MAP.put("ER16", "Wrong offset or number of elements");
        EXCEPTION_INFO_MAP.put("ER17", "No value for write");
        EXCEPTION_INFO_MAP.put("ER18", "Array index too high");
        EXCEPTION_INFO_MAP.put("ER19", "Wrong offset format or field length");
        EXCEPTION_INFO_MAP.put("ER20", "No response from master");
        EXCEPTION_INFO_MAP.put("ER21", "Invalid character in R3 blocks");
        EXCEPTION_INFO_MAP.put("ER22", "Variable without previous values");
        EXCEPTION_INFO_MAP.put("ER23", "Code does not exist");
        EXCEPTION_INFO_MAP.put("ER24", "Invalid variable subtag");
        EXCEPTION_INFO_MAP.put("ER25", "Non-existing register");
        EXCEPTION_INFO_MAP.put("ER26", "EE write failure");
        EXCEPTION_INFO_MAP.put("ER27", "Invalid value");
        EXCEPTION_INFO_MAP.put("ER28", "Invalid time");
        EXCEPTION_INFO_MAP.put("ER29", "Previous value not valid");
        EXCEPTION_INFO_MAP.put("ER30", "Previous value empty");
        EXCEPTION_INFO_MAP.put("ER36", "MD reset lockout active - comm.");
        EXCEPTION_INFO_MAP.put("ER37", "Load profile value not valid");
        EXCEPTION_INFO_MAP.put("ER38", "Load profile is empty (no values)");
        EXCEPTION_INFO_MAP.put("ER39", "No load profile function in the meter");
        EXCEPTION_INFO_MAP.put("ER40", "Non-existing channel of load profile");
        EXCEPTION_INFO_MAP.put("ER41", "Load profile start time > end time");
        EXCEPTION_INFO_MAP.put("ER43", "Error in time format");
        EXCEPTION_INFO_MAP.put("ER44", "Invalid data-read-out");
        EXCEPTION_INFO_MAP.put("ER47", "Meter not in auto-scroll mode");
        EXCEPTION_INFO_MAP.put("ER49", "Error on cumulative maximum");
        EXCEPTION_INFO_MAP.put("ER53", "Non R5 variable");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * ****************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getIskraEmecoRegistry(), getTimeZone(), regs);
        return ocm.getRegisterValue(obisCode);
    }

    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    public boolean isRequestHeader() {
        return false;
    }

    public boolean isReadCurrentDay() {
        return readCurrentDay == 1;
    }

}