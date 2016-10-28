package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Utils;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

/**
 * Integrated Object Network (ION) architecture
 * <p/>
 * DataRecorder Energy and Demand
 *
 * @author fbl
 * @beginchanges FBL|09032007|
 * Property NodeId used to be required.  Is no longer required, default 100 is
 * used.
 * FBL|13072007|
 * Improved error handling and general robustness.
 * FBL|31082007|
 * Added Channelmap property.
 * Changed default data recorder that is used for ProfileData.
 * @endchanges
 */

public class Ion extends PluggableMeterProtocol implements RegisterProtocol, ProtocolLink,
        HHUEnabler, SerialNumber, SerialNumberSupport {

    /**
     * Property keys
     */
    private static final String PK_TIMEOUT = "Timeout";
    private static final String PK_RETRIES = "Retries";
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PK_DATA_RECORDER_NAME = "DataRecorderName";
    private static final String PK_USER_ID = "UserId";
    private static final String PK_DTR_BEHAVIOUR = "DTRBehaviour";
    private static final String PK_FORCE_DELAY = "ForcedDelay";
    private static final String PK_CHANNEL_MAP = "ChannelMap";


    /**
     * Property Default values
     */
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final String PD_EXTENDED_LOGGING = "0";
    private static final String PD_DATA_RECORDER_NAME = "Revenue Log";
    private static final int PD_DTR_BEHAVIOUR = 2;
    private static final int PD_NODE_ID = 100;
    private static final long PD_FORCE_DELAY = 200;

    /**
     * Property values Required properties will have NO default value.
     * Optional properties make use of default value.
     */
    int pNodeId;
    String pSerialNumber = null;
    int pProfileInterval;
    int dtrBehaviour = PD_DTR_BEHAVIOUR;

    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pCorrectTime = 0;

    /* Delay in msec between protocol Message Sequences */
    long pForceDelay = PD_FORCE_DELAY;

    String pUserId;
    String pPassword;

    String pExtendedLogging = PD_EXTENDED_LOGGING;
    String pDataRecorderName = PD_DATA_RECORDER_NAME;
    ProtocolChannelMap pChannelMap = null;

    private SerialCommunicationChannel commChannel;

    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    Logger logger = null;

    private OutputStream outputStream;
    private InputStream inputStream;
    private final int source = 0x4e20;
    private ApplicationLayer applicationLayer;
    private Authentication authentication;
    private IonParser parser;
    private Profile profile;

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol# setProperties(java.util.Properties)
     */

    @Override
    public String getSerialNumber() {
        Command c = toCmd(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        try {
            applicationLayer.read(c);
            return (String)c.getResponse().getValue();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    public void setProperties(Properties p) throws InvalidPropertyException, MissingPropertyException {

        Iterator iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (p.getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }

        try {
            String anId = p.getProperty(NODEID.getName());
            if (!Utils.isNull(anId)) {
                pNodeId = Integer.parseInt(anId);
            }

        } catch (NumberFormatException nfe) {
            String msg = "NodeId must be a valid number.";
            throw new InvalidPropertyException(msg);
        }

        if (p.getProperty(PK_USER_ID) != null) {
            pUserId = p.getProperty(PK_USER_ID);
        }

        if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName()) != null) {
            pPassword = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
        }

        try {
            if (!Utils.isNull(pPassword) && !Utils.isNull(pUserId)) {
                this.authentication = new Authentication(pPassword, pUserId);
            }
        } catch (InvalidPasswordException e) {
            throw new InvalidPropertyException(e.getMessage());
        }

        if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName()) != null) {
            pSerialNumber = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
        }

        if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName()) != null) {
            pProfileInterval = Integer.parseInt(p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName()));
        }

        if (p.getProperty(PK_TIMEOUT) != null) {
            pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
        }

        if (p.getProperty(PK_RETRIES) != null) {
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
        }

        if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORR.getName()) != null) {
            pRountTripCorrection = Integer.parseInt(p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORR.getName()));
        }

        if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName()) != null) {
            pCorrectTime = Integer.parseInt(p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName()));
        }

        if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        }

        if (p.getProperty(PK_DATA_RECORDER_NAME) != null) {
            pDataRecorderName = p.getProperty(PK_DATA_RECORDER_NAME);
        }

        if (p.getProperty(PK_DTR_BEHAVIOUR) != null) {
            dtrBehaviour = Integer.parseInt(p.getProperty(PK_DTR_BEHAVIOUR));
        }

        if (p.getProperty(PK_FORCE_DELAY) != null) {
            pForceDelay = Integer.parseInt(p.getProperty(PK_FORCE_DELAY));
        }

        if (p.getProperty(PK_CHANNEL_MAP) != null) {
            pChannelMap = new ProtocolChannelMap(p.getProperty(PK_CHANNEL_MAP));
        }
    }

    public List<String> getRequiredKeys() {
        return Collections.singletonList(NODEID.getName());
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    PK_TIMEOUT,
                    PK_RETRIES,
                    PK_EXTENDED_LOGGING,
                    PK_DATA_RECORDER_NAME,
                    PK_USER_ID,
                    PK_DTR_BEHAVIOUR,
                    PK_FORCE_DELAY,
                    PK_CHANNEL_MAP);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#init( InputStream,
     *          OutputStream, TimeZone, Logger)
     */

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger)
            throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        this.parser = new IonParser();
        this.obisCodeMapper = new ObisCodeMapper(this);
        this.profile = new Profile(this);

        this.inputStream = inputStream;
        this.outputStream = outputStream;

        if (logger.isLoggable(Level.INFO)) {
            String info = "Ion protocol init \n" +
                    " NodeId = " + pNodeId + "," +
                    " SerialNr = " + pSerialNumber + "," +
                    " Timeout = " + pTimeout + "," +
                    " Retries = " + pRetries + "," +
                    " Ext. Logging = " + pExtendedLogging + "," +
                    " RoundTripCorr = " + pRountTripCorrection + "," +
                    " ForceDelay = " + pForceDelay + "," +
                    " Correct Time = " + pCorrectTime + "," +
                    " TimeZone = " + timeZone.getID();
            logger.info(info);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#connect()
     */

    public void connect() throws IOException {

        if (commChannel != null) {
            this.inputStream = commChannel.getInputStream();
            this.outputStream = commChannel.getOutputStream();
        }

        this.applicationLayer = new ApplicationLayer(this, authentication);
        if (commChannel != null) {
            commChannel.setBaudrate(9600);
            if (dtrBehaviour == 0) {
                commChannel.setDTR(false);
            } else if (dtrBehaviour == 1) {
                commChannel.setDTR(true);
            }
        }
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {
            doExtendedLogging();
            validateSecurityContext();
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    /**
     * Validate whether the Security Context (password, userId, ...) is correct
     * Here we make use of the serialNumber
     */
    private void validateSecurityContext() throws IOException {

        Command c = toCmd(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);

        if (c.getResponse().isException()) {

            if (c.getResponse().isStrucure()) {
                if ((((IonStructure) c.getResponse()).get("reason").toString()).indexOf("Invalid Password") > 0) {
                    throw new ConnectionException("Incorrect password");
                }
            }
        }   // else everything is OK

    }

    public void disconnect() throws IOException {
    }

    /**
     * A DataCollector always has 16 channels
     */
    public int getNumberOfChannels() throws IOException {
        return profile.getNumberOfChannels();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return profile.getProfileData(includeEvents);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
     */

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return profile.getProfileData(lastReading, includeEvents);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
     */

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return profile.getProfileData(from, to, includeEvents);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileInterval()
     */

    public int getProfileInterval() throws IOException {
        return pProfileInterval;
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

    void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException {

        Command cRevisionNr =
                toCmd(IonHandle.FAC_1_REVISION_SR, IonMethod.READ_REGISTER_VALUE);

        applicationLayer.read(cRevisionNr);

        return cRevisionNr.getResponse().getValue().toString();

    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public Date getTime() throws IOException {
        Command c =
                toCmd(IonHandle.CLK_1_UNIVERSAL_TIME_NVR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);
        int secs = ((Integer) c.getResponse().getValue()).intValue();
        return new Date(secs * 1000l);    // must be casted to long
    }

    /**
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#setTime()
     */
    public void setTime() throws IOException {

        long milli = System.currentTimeMillis() + pRountTripCorrection;
        int sec = (int) (milli / 1000);

        ByteArray time =
                new ByteArray()
                        .add((byte) ((sec & 0xff000000) >> 24))
                        .add((byte) ((sec & 0x00ff0000) >> 16))
                        .add((byte) ((sec & 0x0000ff00) >> 8))
                        .add((byte) ((sec & 0x000000ff)));

        applicationLayer.sendTime(new Message().setData(time));

    }

    /* ___ Unsupported methods ___ */

    public void setCache(Object cacheObject) {
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
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

    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * for easy debugging
     */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

    InputStream getInputStream() {
        return inputStream;
    }

    OutputStream getOutputStream() {
        return outputStream;
    }

    int getSource() {
        return source;
    }

    int getDestination() {
        return pNodeId;
    }

    int getRetries() {
        return pRetries;
    }

    int getTimeout() {
        return pTimeout;
    }

    IonObject parse(Assembly a) {
        return parser.parse(a);
    }

    IonObject parse(byte[] b) {
        return parser.parse(new Assembly(this, new ByteArray(b)));
    }

    ApplicationLayer getApplicationLayer() {
        return applicationLayer;
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

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return null;
    }

    public boolean isIEC1107Compatible() {
        return false;
    }

    public String getPassword() {
        return null;
    }

    public ChannelMap getChannelMap() {
        return null;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public Logger getLogger() {
        return logger;
    }

    public int getNrOfRetries() {
        return pRetries;
    }

    public boolean isRequestHeader() {
        return false;
    }

    long getForceDelay() {
        return pForceDelay;
    }


    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        this.commChannel = commChannel;
    }


    public byte[] getHHUDataReadout() {
        return null;
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {

        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();

        final String nodeId = discoverInfo.getNodeId();
        final int baudrate = discoverInfo.getBaudrate();

        Properties p = new Properties();
        p.setProperty("SecurityLevel", "0");
        p.setProperty(NODEID.getName(), nodeId == null ? "" : nodeId);

        setProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        enableHHUSignOn(cChannel);
        connect(baudrate);

        Command c =
                new Command(IonHandle.FAC_1_SERIAL_NUMBER_SR, IonMethod.READ_REGISTER_VALUE);
        applicationLayer.read(c);

        String sn = (String) c.getResponse().getValue();
        disconnect();

        return sn;

    }

    List toCmd(List handles, IonMethod method) {
        List rslt = new ArrayList();
        Iterator i = handles.iterator();
        while (i.hasNext()) {
            IonHandle handle = (IonHandle) i.next();
            rslt.add(new Command(handle, method));
        }
        return rslt;
    }

    Command toCmd(IonHandle handle, IonMethod method) {
        return new Command(handle, method);
    }

    List collectHandles(List ionIntegers) {
        ArrayList rslt = new ArrayList();
        Iterator i = ionIntegers.iterator();
        while (i.hasNext()) {
            rslt.add(IonHandle.create(((IonInteger) i.next()).getIntValue()));
        }
        return rslt;
    }

}