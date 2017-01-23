package com.energyict.protocolimpl.iec1107.a140;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocols.util.ProtocolUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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
 * @beginchanges FBL |03112005| bugfix for DST transition, timechange in past and serialnr
 * || DST transition, a single value was lost.
 * || (due to generation of bad unique id for entry)
 * || timechange in past, overwrite behaviour for bad values.
 * || the newest value is now saved, unless it is an init value
 * || serialnr does not take dashes into account like the other Elster protocols.
 * FBL |24112005| bugfix TimeDate was buffered by protocol.  Must be
 * || reloaded every time.
 * FBL |24112006| bugfix for 0xE4 byte in date field.  A new day in Load
 * || Profile starts with "0xE4-date-demand period".  Within such a date
 * || an 0xE4 character can occur again.  Solution: when an 0xE4 byte is
 * || found, skip next 5 bytes (date=4 bytes and demand period = 1 byte).
 * @endchanges
 */

public class A140 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler,
        SerialNumber, MeterExceptionInfo, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elster/ABB A140 IEC1107";
    }

    private int dbg = 0;

    static final long FORCE_DELAY = 350;

    /**
     * Property keys specific for A140 protocol.
     */
    static final String PK_TIMEOUT = "Timeout";
    static final String PK_RETRIES = "Retries";
    static final String PK_EXTENDED_LOGGING = "ExtendedLogging";

    /**
     * Property Default values
     */
    static final String PD_NODE_ID = "";
    static final int PD_TIMEOUT = 10000;
    static final int PD_RETRIES = 5;
    static final int PD_ROUNDTRIP_CORRECTION = 0;
    static final int PD_SECURITY_LEVEL = 2;
    static final String PD_EXTENDED_LOGGING = "0";

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    String pAddress = null;
    String pNodeId = PD_NODE_ID;
    String pSerialNumber = null;
    String pPassword = null;

    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pSecurityLevel = PD_SECURITY_LEVEL;
    int pCorrectTime = 0;

    String pExtendedLogging = PD_EXTENDED_LOGGING;

    private MeterType meterType = null;
    private RegisterFactory rFactory = null;
    private ObisCodeMapper obisCodeMapper = null;
    private FlagIEC1107Connection flagConnection = null;
    private TimeZone timeZone = null;
    private Logger logger = null;
    private DataType dataType = null;

    private boolean software7E1;

    @Inject
    public A140(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /* ___ Implement interface MeterProtocol ___ */

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#
     *      setProperties(java.util.Properties)
     */
    public void setProperties(Properties p) throws InvalidPropertyException,
            MissingPropertyException {

        if (p.getProperty(MeterProtocol.ADDRESS) != null) {
            pAddress = p.getProperty(MeterProtocol.ADDRESS);
        }

        if (p.getProperty(MeterProtocol.NODEID) != null) {
            pNodeId = p.getProperty(MeterProtocol.NODEID);
        }

        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null) {
            pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
        }

        if (p.getProperty(MeterProtocol.PASSWORD) != null) {
            pPassword = p.getProperty(MeterProtocol.PASSWORD);
        }

        if (p.getProperty(PK_TIMEOUT) != null) {
            pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
        }

        if (p.getProperty(PK_RETRIES) != null) {
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
        }

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null) {
            pRountTripCorrection = Integer.parseInt(p.getProperty(MeterProtocol.ROUNDTRIPCORR));
        }

        if (p.getProperty(MeterProtocol.CORRECTTIME) != null) {
            pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));
        }

        if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        }

        this.software7E1 = !"0".equals(p.getProperty("Software7E1", "0"));
        validateProperties();

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
                MeterProtocol.ADDRESS,
                PK_TIMEOUT,
                PK_RETRIES,
                PK_EXTENDED_LOGGING,
                "Software7E1");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#init( java.io.InputStream,
     *      java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
     */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "A140 protocol init \n"
                            + " Address = " + pAddress + ","
                            + " Node Id = " + pNodeId + ","
                            + " SerialNr = " + pSerialNumber + ","
                            + " Psswd = " + pPassword + ","
                            + " Timeout = " + pTimeout + ","
                            + " Retries = " + pRetries + ","
                            + " Ext. Logging = " + pExtendedLogging + ","
                            + " RoundTripCorr = " + pRountTripCorrection + ","
                            + " Correct Time = " + pCorrectTime + ","
                            + " TimeZone = " + timeZone.getID();

            logger.info(infoMsg);

        }

        try {
            flagConnection = new FlagIEC1107Connection(inputStream,
                    outputStream, pTimeout, pRetries, FORCE_DELAY, 0, 1,
                    new CAI700(), software7E1, logger);
        } catch (ConnectionException e) {
            logger.severe("A140: init(...), " + e.getMessage());
        }

    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.MeterProtocol#connect()
    */
    public void connect() throws IOException {
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {

            meterType = flagConnection.connectMAC(pAddress, pPassword,
                    pSecurityLevel, pNodeId, baudRate);

            logger.log(Level.INFO, "Meter " + meterType.getReceivedIdent());
            rFactory = new RegisterFactory(this);
            dataType = new DataType(timeZone);
            obisCodeMapper = new ObisCodeMapper(this, rFactory);

            validateSerialNumber();
            doExtendedLogging();

        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    public void disconnect() throws IOException {
        meterType = null;
        rFactory = null;
        obisCodeMapper = null;
        flagConnection.disconnectMAC();
    }

    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    /* (non-Javadoc)
    * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
    */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return rFactory.getLoadProfile().getProfileData();
    }

    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return rFactory.getLoadProfile().getProfileData(lastReading, new Date());
    }

    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {
        return rFactory.getLoadProfile().getProfileData(from, to);
    }

    public int getProfileInterval() throws IOException {
        return rFactory.getLoadProfileConfig().getDemandPeriod();
    }

    /* ___ Implement interface ProtocolLink ___ */

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.
     *      ProtocolLink#getFlagIEC1107Connection()
     */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagConnection;
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
        return pPassword;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getTimeZone()
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /* (non-Javadoc)
    * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNrOfRetries()
    */
    public int getNrOfRetries() {
        return pRetries;
    }

    /* ___ Implement interface HHUEnabler ___ */

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel,
     *      boolean)
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,
                                boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel,
                pTimeout, pRetries, FORCE_DELAY, 0);
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
    public void enableHHUSignOn(SerialCommunicationChannel commChannel)
            throws ConnectionException {
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
        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();

        Properties p = new Properties();
        p.setProperty("SecurityLevel", "0");
        p.setProperty(MeterProtocol.NODEID, nodeId == null ? "" : nodeId);
        p.setProperty("IEC1107Compatible", "1");
        setProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        enableHHUSignOn(cChannel);
        connect(baudrate);
        String serialNumber = rFactory.getSerialNumber().getSerialNumber();
        disconnect();
        return serialNumber;
    }

    static Map<String, String> exception = new HashMap<>();

    static {
        exception.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");

        exception.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
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
        String exceptionInfo = exception.get(id);
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
        return obisCodeMapper.getRegisterValue(obisCode);
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
        return rFactory;
    }

    /**
     * @throws IOException
     */
    public void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
            if (dbg > 0) {
                logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
            }
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quantity getMeterReading(int channelId) throws
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quantity getMeterReading(String name) throws
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Date getTime() throws IOException {
        return rFactory.getTimeAndDate().getTime();
    }

    public void setTime() throws IOException {
        getFlagIEC1107Connection().authenticate();
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        rFactory.getTimeAndDate().setTime(calendar.getTime());
        rFactory.getTimeAndDate().write();
    }

    public String getRegister(String name) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRegister(String name, String value) throws IOException {
        // TODO Auto-generated method stub

    }

    public void initializeDevice() throws IOException {
        // TODO Auto-generated method stub
    }

    public void release() throws IOException {
        // TODO Auto-generated method stub
    }

    public boolean isIEC1107Compatible() {
        return true;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isRequestHeader() {
        // TODO Auto-generated method stub
        return false;
    }

    public DataType getDataType() {
        return dataType;
    }

    /* ___ Private property checking ___ */

    private void validateSerialNumber() throws IOException {
        if ((pSerialNumber == null) || ("".equals(pSerialNumber))) {
            return;
        }
        // at this point pSerialNumber can not be null any more

        String sn = rFactory.getSerialNumber().getSerialNumber();
        if (sn != null) {

            String snNoDash = sn.replaceAll("-+", "");

            String pSerialNumberNoDash = pSerialNumber.replaceAll("-+", "");

            if (pSerialNumberNoDash.equals(snNoDash)) {
                return;
            }
        }

        throw new IOException("SerialNumber mismatch! meter sn=" + sn
                + ", configured sn=" + pSerialNumber);
    }

    private void validateProperties() {

    }

    /* ___ Unsupported methods ___ */

    public void setCache(Object cacheObject) {
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws SQLException {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

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

    /**
     * for easy debugging
     */
    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * for easy debugging
     */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

}
