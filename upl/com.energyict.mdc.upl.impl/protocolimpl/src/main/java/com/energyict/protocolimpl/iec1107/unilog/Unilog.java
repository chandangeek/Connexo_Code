/*
 * Unilog.java
 *
 * Created on 10 januari 2005, 09:19
 */

package com.energyict.protocolimpl.iec1107.unilog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * @author fbo
 * @beginchanges
FB|01022005|Initial version
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 * @endchanges 
 */
public class Unilog implements MeterProtocol, ProtocolLink, MeterExceptionInfo {

    final static String KAMSTRUP_ID = "/KAM5";
    
    /** Property keys specific for PPM protocol. */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_FORCE_DELAY = "ForceDelay";
    final static String PK_ECHO_CANCELLING = "EchoCancelling";
    final static String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_CHANNEL_MAP = "ChannelMap";

    /** Property Default values */
    final static String PD_PASSWORD = "kamstrup";
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_PROFILE_INTERVAL = 3600;
    final static long PD_FORCE_DELAY = 170;
    final static int PD_ECHO_CANCELING = 0;
    final static int PD_IEC1107_COMPATIBLE = 1;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 1;
    final static String PD_EXTENDED_LOGGING = "0";
    final static String PD_CHANNEL_MAP = "0,0";

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    String pAddress = null; 
    String pNodeId = null;
    String pSerialNumber = null;
    String pPassword = PD_PASSWORD;

    String pChannelMap = PD_CHANNEL_MAP;
    int pProfileInterval = PD_PROFILE_INTERVAL;
    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;
    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Delay in msec between protocol Message Sequences */
    long pForceDelay = PD_FORCE_DELAY;
    int pEchoCanceling = PD_ECHO_CANCELING;
    int pIec1107Compatible = PD_IEC1107_COMPATIBLE;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pSecurityLevel = PD_SECURITY_LEVEL;


    private TimeZone timeZone;
    private Logger logger;
    private MeterType meterType;
    FlagIEC1107Connection flagIEC1107Connection = null;
    UnilogRegistry registry = null;
    UnilogProfile profile = null;
    ProtocolChannelMap protocolChannelMap = null;
    
    private boolean software7E1;

    /** Creates a new instance of Unilog, empty constructor */
    public Unilog() {
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

        if (p.getProperty(MeterProtocol.ADDRESS) != null)
            pAddress = p.getProperty(MeterProtocol.ADDRESS);

        if (p.getProperty(MeterProtocol.NODEID) != null)
            pNodeId = p.getProperty(MeterProtocol.NODEID);

        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null)
            pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);

        if (p.getProperty(MeterProtocol.PASSWORD) != null)
            pPassword = p.getProperty(MeterProtocol.PASSWORD);

        if (p.getProperty(MeterProtocol.PROFILEINTERVAL) != null)
            pProfileInterval = Integer.parseInt(p
                    .getProperty(MeterProtocol.PROFILEINTERVAL));

        if (p.getProperty(PK_TIMEOUT) != null)
            pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));

        if (p.getProperty(PK_RETRIES) != null)
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));

        if (p.getProperty(PK_FORCE_DELAY) != null)
            pForceDelay = Integer.parseInt(p.getProperty(PK_FORCE_DELAY));

        if (p.getProperty(PK_ECHO_CANCELLING) != null)
            pEchoCanceling = Integer
                    .parseInt(p.getProperty(PK_ECHO_CANCELLING));

        if (p.getProperty(PK_IEC1107_COMPATIBLE) != null)
            pIec1107Compatible = Integer.parseInt(p
                    .getProperty(PK_IEC1107_COMPATIBLE));

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null)
            pRountTripCorrection = Integer.parseInt(p
                    .getProperty(MeterProtocol.ROUNDTRIPCORR));

        this.software7E1 = !p.getProperty("Software7E1", "0").equalsIgnoreCase("0");
        
        validateProperties();

    }

    private void validateProperties() throws MissingPropertyException,
            InvalidPropertyException {

        protocolChannelMap = new ProtocolChannelMap( pChannelMap );
        
    }

    /* @see com.energyict.protocol.MeterProtocol#getRequiredKeys() */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    /* @see com.energyict.protocol.MeterProtocol#getOptionalKeys() */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(PK_TIMEOUT);
        result.add(PK_RETRIES);
        result.add(MeterProtocol.ROUNDTRIPCORR);
        result.add("Software7E1");
        return result;
    }

    /*
     * @see com.energyict.protocol.MeterProtocol#init(java.io.InputStream,
     *      java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
     */
    public void init(InputStream inputStream, OutputStream outputStream,
            TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg = "PPM protocol init \n" + "- Address           = "
                    + pAddress + "\n" + "- Node Id           = " + pNodeId
                    + "\n" + "- SerialNumber      = " + pSerialNumber + "\n"
                    + "- Password          = " + pPassword + "\n"
                    + "- Timeout           = " + pTimeout + "\n"
                    + "- Retries           = " + pRetries + "\n"
                    + "- ProfileInterval   = " + pProfileInterval + "\n"
                    + "- ForceDelay        = " + pForceDelay + "\n"
                    + "- EchoCanceling     = " + pEchoCanceling + "\n"
                    + "- IEC1107Compatible = " + pIec1107Compatible + "\n"
                    + "- RoundTripCorr     = " + pRountTripCorrection + "\n";

            logger.info(infoMsg);
        }

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream,
                    outputStream, pTimeout, pRetries, pForceDelay,
                    pEchoCanceling, pIec1107Compatible,software7E1);

            registry = new UnilogRegistry(this, this);
            profile = new UnilogProfile(this, registry);

        } catch (ConnectionException e) {
            logger.severe("Unilog: init(...), " + e.getMessage());
        }

    }

    /* @see com.energyict.protocol.MeterProtocol#connect() */
    public void connect() throws IOException {
        try {
            
            meterType = 
                flagIEC1107Connection.connectMAC(pAddress, pPassword,
                                    pSecurityLevel, pNodeId);
            
            validateSerialNumber();
            
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }

    }

    /* @see com.energyict.protocol.MeterProtocol#disconnect() */
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }
    
    void validateSerialNumber( ) throws IOException {
        
        // if exceptionMsg stays null -> nobody moves, nobody gets hurt
        String exceptionMsg = null;
        String meterSerial = meterType.getReceivedIdent();
        
        if( pSerialNumber != null ) {
            if( meterSerial == null ) {
                exceptionMsg = 
                    "SerialNumber mismatch! configured serial = " + 
                    pSerialNumber + " meter serial unknown";
            } else {
                String trimMeterSerial = 
                           meterSerial.substring( 5, meterSerial.length() - 2 );
                
                if( ! trimMeterSerial.equals( pSerialNumber )  )
                    exceptionMsg =
                        "SerialNumber mismatch! configured serial = " +
                        pSerialNumber + " meter serial = " + trimMeterSerial;   
            }
        }
        
        if( exceptionMsg != null ) throw new IOException( exceptionMsg );
        
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProfileInterval() */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return pProfileInterval;
    }

    /* @see com.energyict.protocol.MeterProtocol#getProfileData(boolean) */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {

        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);

        return doGetProfileData(calendar.getTime(), includeEvents);

    }

    /*
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
     *      boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {

        return doGetProfileData(lastReading, includeEvents);

    }

    /*
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
     *      java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException, UnsupportedException {

        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);

        return profile.getProfileData(fromCalendar, toCalendar,
                getNumberOfChannels(), 1);

    }

    private ProfileData doGetProfileData(Date lastReading, boolean includeEvents)
            throws IOException {

        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);

        return profile.getProfileData(fromCalendar, ProtocolUtils
                .getCalendar(timeZone), getNumberOfChannels(), 1);

    }

    /* @see com.energyict.protocol.MeterProtocol#getMeterReading(java.lang.String) 
     * has been deprecated */
    public Quantity getMeterReading(String name) throws UnsupportedException,
            IOException {
        throw new UnsupportedException();
    }

    /* @see com.energyict.protocol.MeterProtocol#getMeterReading(int) 
     * has been deprecated */
    public Quantity getMeterReading(int channelId) throws UnsupportedException,
            IOException {
        throw new UnsupportedException();
    }

    /* @see com.energyict.protocol.MeterProtocol#setTime() */
    public void setTime() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        Date date = calendar.getTime();
        registry.setRegister("0.9.1", date);
        registry.setRegister("0.9.2", date);
    }

    /* @see com.energyict.protocol.MeterProtocol#getTime() */
    public Date getTime() throws IOException {
        Date date = (Date) registry.getRegister(registry.R_TIME_DATE);
        return new Date(date.getTime() - pRountTripCorrection);
    }

    /*
     * TODO (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#getNumberOfChannels()
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return protocolChannelMap.getNrOfProtocolChannels();
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getPassword() */
    public String getPassword() {
        return pPassword;
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNrOfRetries() */
    public int getNrOfRetries() {
        return pRetries;
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getLogger() */
    public Logger getLogger() {
        return logger;
    }

    static Map exceptionInfoMap = new HashMap();
    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
    }

    /* @see com.energyict.protocolimpl.base.MeterExceptionInfo#getExceptionInfo(java.lang.String) */
    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(id);
        if (exceptionInfo != null)
            return id + ", " + exceptionInfo;
        else
            return "No meter specific exception info for " + id;
    }

    /* @see com.energyict.protocol.MeterProtocol#initializeDevice() */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    /* @see com.energyict.protocol.MeterProtocol#getProtocolVersion() */
    public String getProtocolVersion() {
        return "$Revision: 1.8 $";
    }

    /* @see com.energyict.protocol.MeterProtocol#getFirmwareVersion() */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return ("Unknown");
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getTimeZone() */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /* @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String) */
    public String getRegister(String name) throws IOException,
            UnsupportedException, NoSuchRegisterException {
        return ProtocolUtils.obj2String(registry.getRegister(name));
    }

    /*
     * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String,
     *      java.lang.String)
     */
    public void setRegister(String name, String value) throws IOException,
            NoSuchRegisterException, UnsupportedException {
        registry.setRegister(name, value);
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getFlagIEC1107Connection() */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#isIEC1107Compatible() */
    public boolean isIEC1107Compatible() {
        return (pIec1107Compatible == 1);
    }

    /* @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object) */
    public void setCache(Object cacheObject) {
    }

    /*
     * @see com.energyict.protocol.MeterProtocol#updateCache(int,
     *      java.lang.Object)
     */
    public void updateCache(int rtuid, Object cacheObject)
            throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getChannelMap() */
    public ChannelMap getChannelMap() {
        return null;
    }

    /* @see com.energyict.protocol.MeterProtocol#release() */
    public void release() throws IOException {
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout() */
    public byte[] getDataReadout() {
        return null;
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProtocolChannelMap() */
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    /* @see com.energyict.protocolimpl.iec1107.ProtocolLink#isRequestHeader() */
    public boolean isRequestHeader() {
        return false;
    }

    /* @see com.energyict.protocol.MeterProtocol#getCache() */
    public Object getCache() {
        return null;
    }

    /* @see com.energyict.protocol.MeterProtocol#fetchCache(int) */
    public Object fetchCache(int rtuid) throws java.sql.SQLException,
            com.energyict.cbo.BusinessException {
        return null;
    }

}
