package com.energyict.protocolimpl.iec1107.a140;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * @beginchanges
  FBL |03112005| bugfix for DST transition, timechange in past and serialnr
  || DST transition, a single value was lost. 
  || (due to generation of bad unique id for entry)
  || timechange in past, overwrite behaviour for bad values.
  || the newest value is now saved, unless it is an init value
  || serialnr does not take dashes into account like the other Elster protocols.
  FBL |24112005| bugfix TimeDate was buffered by protocol.  Must be 
  || reloaded every time.
  FBL |24112006| bugfix for 0xE4 byte in date field.  A new day in Load
  || Profile starts with "0xE4-date-demand period".  Within such a date 
  || an 0xE4 character can occur again.  Solution: when an 0xE4 byte is 
  || found, skip next 5 bytes (date=4 bytes and demand period = 1 byte).
 * @endchanges
 * @author fbo
 */

public class A140 implements MeterProtocol, ProtocolLink, HHUEnabler,
        SerialNumber, MeterExceptionInfo, RegisterProtocol {

    private int dbg = 0;
    
    final static long FORCE_DELAY = 350;
    
    /** Property keys specific for A140 protocol. */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";

    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 2;
    final static String PD_EXTENDED_LOGGING = "0";

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
    

    public A140() {
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

        if (p.getProperty(PK_TIMEOUT) != null)
            pTimeout = new Integer( p.getProperty(PK_TIMEOUT) ).intValue(); 

        if (p.getProperty(PK_RETRIES) != null)
            pRetries = new Integer(p.getProperty(PK_RETRIES) ).intValue();

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null)
            pRountTripCorrection = new Integer(p.getProperty(MeterProtocol.ROUNDTRIPCORR) ).intValue();

        if (p.getProperty(MeterProtocol.CORRECTTIME) != null)
            pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));

        if (p.getProperty(PK_EXTENDED_LOGGING) != null)
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        
        this.software7E1 = !p.getProperty("Software7E1", "0").equalsIgnoreCase("0");
        validateProperties();

    }
    
    /** the implementation returns both the address and password key
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }
    
    /** this implementation returns an empty list
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add( MeterProtocol.ADDRESS );
        result.add( PK_TIMEOUT );
        result.add( PK_RETRIES );
        result.add( PK_EXTENDED_LOGGING );
        result.add("Software7E1");
        return result;
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
                    new CAI700(), software7E1 );
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
    
    void connect( int baudRate ) throws IOException {
        try {

            meterType = flagConnection.connectMAC(pAddress, pPassword,
                    pSecurityLevel, pNodeId, baudRate );

            logger.log(Level.INFO, "Meter " + meterType.getReceivedIdent());
            rFactory = new RegisterFactory(this);
            dataType = new DataType( timeZone );
            obisCodeMapper = new ObisCodeMapper(this,rFactory);
            
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
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 1;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return rFactory.getLoadProfile().getProfileData( );
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
        return rFactory.getLoadProfile().getProfileData( from, to );
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
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
        HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel,
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
        p.setProperty("SecurityLevel","0");
        p.setProperty(MeterProtocol.NODEID,nodeId==null?"":nodeId);
        p.setProperty("IEC1107Compatible","1");
        setProperties(p);
        
        init(cChannel.getInputStream(),cChannel.getOutputStream(),null,null);
        enableHHUSignOn(cChannel);
        connect(baudrate);
        String serialNumber = rFactory.getSerialNumber().getSerialNumber();
        disconnect();
        return serialNumber;
    }

    static Map exception = new HashMap();

    static {
        exception.put("ERR1",
                "Invalid Command/Function type e.g. other than W1, R1 etc");

        exception.put("ERR2",
                "Invalid Data Identity Number e.g. Data id does not exist"
                        + " in the meter");
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
        if (exceptionInfo != null)
            return id + ", " + exceptionInfo;
        else
            return "No meter specific exception info for " + id;
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
        if( "1".equals(pExtendedLogging ) ) {
            logger.log( Level.INFO, obisCodeMapper.getExtendedLogging() + "\n" );
            if( dbg > 0)
                logger.log(Level.INFO, obisCodeMapper.getDebugLogging()+"\n");
        }
    }

    public String getProtocolVersion() {
        return "$Revision: 1.11 $";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException,
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Quantity getMeterReading(String name) throws UnsupportedException,
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public Date getTime() throws IOException {
        Date date = rFactory.getTimeAndDate().getTime();
        return date;
    }

    public void setTime() throws IOException {
        getFlagIEC1107Connection().authenticate();
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection );
        rFactory.getTimeAndDate().setTime( calendar.getTime() );
        rFactory.getTimeAndDate().write();
    }

    public String getRegister(String name) throws IOException,
            UnsupportedException, NoSuchRegisterException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRegister(String name, String value) throws IOException,
            NoSuchRegisterException, UnsupportedException {
        // TODO Auto-generated method stub

    }

    public void initializeDevice() throws IOException, UnsupportedException {
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
    
    public DataType getDataType( ){
        return dataType;
    }
    
    /* ___ Private property checking ___ */
    
    private void validateSerialNumber( ) throws IOException {
        if ((pSerialNumber == null) || ("".equals(pSerialNumber)))
            return;
        // at this point pSerialNumber can not be null any more
        
        String sn = (String) rFactory.getSerialNumber().getSerialNumber();
        if( sn!= null ) {
            
            String snNoDash = sn.replaceAll( "-+", "" );
            
            String pSerialNumberNoDash = pSerialNumber.replaceAll( "-+", "" );
            
            if( pSerialNumberNoDash.equals( snNoDash ) )
                return;
        }
        
        throw new IOException("SerialNumber mismatch! meter sn=" + sn
        + ", configured sn=" + pSerialNumber);
    }    
    
    private void validateProperties() throws MissingPropertyException,
            InvalidPropertyException {
    	
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

    public void updateCache(int rtuid, Object cacheObject) throws SQLException,
            BusinessException {
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
    
    /** for easy debugging */
    void setTimeZone( TimeZone timeZone ){
        this.timeZone = timeZone;
    }
    
    /** for easy debugging */
    void setLogger( Logger logger ){
        this.logger = logger;
    }
    
}
