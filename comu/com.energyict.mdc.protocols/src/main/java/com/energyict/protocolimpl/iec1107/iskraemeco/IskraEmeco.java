/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import javax.inject.Inject;
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
public class IskraEmeco extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, MeterExceptionInfo, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco MT851 IEC1107 (VDEW)";
    }

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

    @Inject
    public IskraEmeco(PropertySpecService propertySpecService) {
        super(propertySpecService);
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
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "1.5:5.5:8.5"));
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay", "0"));
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
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

    public List getOptionalKeys() {
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
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
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
    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        try {
            validateSerialNumber(); // KV 15122003
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo();
    }

    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber) == 0)) {
            return;
        }
        String sn = (String) getIskraEmecoRegistry().getRegister("meter serial number");
        if (sn.compareTo(serialNumber) == 0) {
            return;
        }
        throw new IOException("SerialNiumber mismatch! meter sn=" + sn + ", configured sn=" + serialNumber);
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

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
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
    static Map<String, String> exceptionInfoMap = new HashMap<>();

    static {
        exceptionInfoMap.put("ER01", "Unknown command");
        exceptionInfoMap.put("ER02", "Invalid command");
        exceptionInfoMap.put("ER03", "Command format failure");
        exceptionInfoMap.put("ER04", "Read-only code");
        exceptionInfoMap.put("ER05", "Write-only code");
        exceptionInfoMap.put("ER06", "Command => no R/W allowed");
        exceptionInfoMap.put("ER07", "Access denied");
        exceptionInfoMap.put("ER08", "Non R3 variable");
        exceptionInfoMap.put("ER09", "Variable is not command");
        exceptionInfoMap.put("ER10", "Command not executed");
        exceptionInfoMap.put("ER11", "Code format error");
        exceptionInfoMap.put("ER12", "Non data-read-out variable");
        exceptionInfoMap.put("ER13", "Variable without unit");
        exceptionInfoMap.put("ER14", "Wrong previous values index");
        exceptionInfoMap.put("ER15", "Code without offset");
        exceptionInfoMap.put("ER16", "Wrong offset or number of elements");
        exceptionInfoMap.put("ER17", "No value for write");
        exceptionInfoMap.put("ER18", "Array index too high");
        exceptionInfoMap.put("ER19", "Wrong offset format or field length");
        exceptionInfoMap.put("ER20", "No response from master");
        exceptionInfoMap.put("ER21", "Invalid character in R3 blocks");
        exceptionInfoMap.put("ER22", "Variable without previous values");
        exceptionInfoMap.put("ER23", "Code does not exist");
        exceptionInfoMap.put("ER24", "Invalid variable subtag");
        exceptionInfoMap.put("ER25", "Non-existing register");
        exceptionInfoMap.put("ER26", "EE write failure");
        exceptionInfoMap.put("ER27", "Invalid value");
        exceptionInfoMap.put("ER28", "Invalid time");
        exceptionInfoMap.put("ER29", "Previous value not valid");
        exceptionInfoMap.put("ER30", "Previous value empty");
        exceptionInfoMap.put("ER36", "MD reset lockout active - comm.");
        exceptionInfoMap.put("ER37", "Load profile value not valid");
        exceptionInfoMap.put("ER38", "Load profile is empty (no values)");
        exceptionInfoMap.put("ER39", "No load profile function in the meter");
        exceptionInfoMap.put("ER40", "Non-existing channel of load profile");
        exceptionInfoMap.put("ER41", "Load profile start time > end time");
        exceptionInfoMap.put("ER43", "Error in time format");
        exceptionInfoMap.put("ER44", "Invalid data-read-out");
        exceptionInfoMap.put("ER47", "Meter not in auto-scroll mode");
        exceptionInfoMap.put("ER49", "Error on cumulative maximum");
        exceptionInfoMap.put("ER53", "Non R5 variable");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(id);
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
} // public class IskraEmeco implements MeterProtocol {
