package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
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

/**
 * @beginchanges
 * @endchanges
 * @author fbo
 */

public class Ziv5Ctd implements MeterProtocol, SerialNumber, RegisterProtocol {
    
    static final BigDecimal MAX_PROFILE_VALUE = new BigDecimal( 9999999 );
    
    /** Property keys */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_FETCH_PROGRAM_PROFILE = "FetchProgramProfile";
    final static String PK_CUMULATIVE_PROFILE = "CumulativeProfile";
    
    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 2;
    final static String PD_EXTENDED_LOGGING = "0";
    final static boolean PD_CUMULATIVE_PROFILE = true;
    
    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    String pAddress = null;
    String pNodeId = PD_NODE_ID;
    String pSerialNumber = null;
    int pProfileInterval;
    int pPassword;
    
    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;
    
    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pSecurityLevel = PD_SECURITY_LEVEL;
    int pCorrectTime = 0;
    
    String pFetchProgramProfile = "0";
    boolean pCumulativeProfile = PD_CUMULATIVE_PROFILE;
    String pExtendedLogging = PD_EXTENDED_LOGGING;
    
    LinkLayer linkLayer;
    FrameFactory frameFactory;
    AsduFactory asduFactory;
    
    private RegisterFactory rFactory = null;    
    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    private Logger logger = null;
    
    public Ziv5Ctd() { }
    
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
        
        if (p.getProperty(MeterProtocol.PROFILEINTERVAL) != null)
            pProfileInterval = Integer.parseInt(p.getProperty(MeterProtocol.PROFILEINTERVAL));
        
        if (p.getProperty(MeterProtocol.PASSWORD) != null)
            pPassword = Integer.parseInt(p.getProperty(MeterProtocol.PASSWORD));
        
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
        
        if(p.getProperty(PK_FETCH_PROGRAM_PROFILE) != null)
            pFetchProgramProfile = p.getProperty(PK_FETCH_PROGRAM_PROFILE);

        if(p.getProperty(PK_CUMULATIVE_PROFILE) != null)
            pCumulativeProfile = ( "1".equals( p.getProperty(PK_CUMULATIVE_PROFILE) ) );
        
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
        result.add( PK_FETCH_PROGRAM_PROFILE );
        result.add( PK_CUMULATIVE_PROFILE );
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
        
        try {
            
            TypeIdentificationFactory tif = new TypeIdentificationFactory(timeZone);
            asduFactory = new AsduFactory(Address.DEFAULT, tif );
            frameFactory = new FrameFactory(Address.DEFAULT, asduFactory);
            linkLayer = new LinkLayer(inputStream, outputStream, 0, 0, this, pRetries );
            rFactory = new RegisterFactory(this, asduFactory);
            obisCodeMapper = new ObisCodeMapper(this, rFactory);
            
        } catch(ConnectionException e) {
            logger.severe("Ziv5Ctd, "+e.getMessage());
            throw e;
        }
        
        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "ZIV protocol init \n"
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
            
            linkLayer.connect();
            linkLayer.requestRespond(asduFactory.createType0xB7(pPassword));
            
            validateSerialNumber();
            doExtendedLogging();
            
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }
    
    public void disconnect() throws IOException {
        rFactory = null;
        obisCodeMapper = null;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 6;   // always 6 channels ...
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = Calendar.getInstance( timeZone );
        
        Date to = c.getTime();
        c.set( Calendar.YEAR, c.get( Calendar.YEAR)-1 );
        Date from = c.getTime();
        
        return getProfileData( from, to, includeEvents );
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
    throws IOException {
        
        return getProfileData( lastReading, new Date(), includeEvents );
        
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
    throws IOException {
        
        int registerAddress = 0x0b;
        if( pFetchProgramProfile.equals( "1" ) )
            registerAddress = 0x0c;
        
        Asdu a = null;
        if( pCumulativeProfile )
            a = asduFactory.create0x7a(registerAddress, from, to);
        else
            a = asduFactory.create0x7b(registerAddress, from, to);
        
        ApplicationFunction appFunction = new ApplicationFunction( this );
        ProfileData result = (ProfileData) appFunction.read( a );
        
        result.generateEvents();
        
        if( includeEvents ) {
            Iterator ei = rFactory.getMeterEvents(from, to).iterator();
            while( ei.hasNext() )
                result.addEvent((MeterEvent)ei.next());
        }
            
        return result;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        if( pFetchProgramProfile.equals( "1" ) )
            // programmed profile has a configurable integration time
            return pProfileInterval;
        else
            // standard profile always has an integration time of 1 hour
            return 3600;    
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
        p.setProperty(MeterProtocol.NODEID,nodeId==null?"":nodeId);
        setProperties(p);
        
        init(cChannel.getInputStream(),cChannel.getOutputStream(),null,null);
        connect(baudrate);
        String serialNumber = rFactory.getInfoObject47().getProductCode();
        disconnect();
        return serialNumber;
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
    
    /**
     * @throws java.io.IOException
     */
    public void doExtendedLogging() throws IOException {
        if( "1".equals(pExtendedLogging ) ) {
            logger.log( Level.INFO, obisCodeMapper.getExtendedLogging() + "\n" );
        }
        if( "2".equals(pExtendedLogging ) ) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging()+"\n");
        }
    }
    
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException,
            IOException {
        throw new UnsupportedException();
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException,
            IOException {
        throw new UnsupportedException();
    }
    
    public Date getTime() throws IOException {
        return rFactory.get48().getDate();
    }
    
    public void setTime() throws IOException {
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection );
        
        CP56Time time = new CP56Time( timeZone, calendar.getTime() );
        Asdu a = asduFactory.createType0xB5( time );
        ApplicationFunction appFunction = new ApplicationFunction( this );
        appFunction.read( a );
        
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
    
    public boolean isRequestHeader() {
        // TODO Auto-generated method stub
        return false;
    }
    
    /* ___ Private property checking ___ */
    
    private void validateSerialNumber( ) throws IOException {
        
        String sn = rFactory.getInfoObject47().getProductCode();
        
        if( pSerialNumber == null || pSerialNumber.equals( sn ) ) return;
        
        throw new IOException( "SerialNumber mismatch! meter sn=" + sn
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
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
     */
    public byte[] getDataReadout() {
        return null;
    }
    
    /** for easy debugging */
    void setTimeZone( TimeZone timeZone ){
        this.timeZone = timeZone;
    }
    
    TimeZone getTimeZone( ){
        return timeZone;
    }
    
    /** for easy debugging */
    void setLogger( Logger logger ){
        this.logger = logger;
    }
    
    public static void main( String [] args ) throws Exception {
        
        Dialer dialer = null;
        Ziv5Ctd ziv = new Ziv5Ctd();
        try {
            
            dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init( "COM1" );
            dialer.connect("",60000);
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            
            Properties properties = new Properties();
            //properties.setProperty(MeterProtocol.ADDRESS,"4");
            properties.setProperty(MeterProtocol.ADDRESS,"61");
            properties.setProperty(MeterProtocol.PASSWORD,"2");
            properties.setProperty("Retries","5");
            properties.setProperty("ProfileInterval","300");
         //   properties.setProperty(PK_EXTENDED_LOGGING,"1");
            properties.setProperty(PK_FETCH_PROGRAM_PROFILE,"1");
            properties.setProperty(MeterProtocol.SERIALNUMBER,"4d940f00");
            
            //properties.setProperty("ChannelMap","1.5:1.7:1.1:2.5:2.6:2.7");
            //properties.setProperty("ChannelMap","1.5:1.7:1.1:2.5:2.6");
            
            properties.setProperty("ChannelMap","1.2"); //:1.5");
            properties.setProperty("MeterType", "0");
            ziv.setProperties(properties);
            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                    SerialCommunicationChannel.DATABITS_8,
                    SerialCommunicationChannel.PARITY_NONE,
                    SerialCommunicationChannel.STOPBITS_1);
            
            ziv.init(is,os,TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            ziv.connect();

        Calendar c = Calendar.getInstance( ziv.getTimeZone() );
        c.set( 2006, 3, 13, 0, 0 );    
        Date start = c.getTime();
        
        c.set( 2006, 3, 15, 0, 0 );
        Date end = c.getTime();
        
            
        ArrayList list = 
            ziv.rFactory.getMeterEvents(start, end);

        
        Iterator i = list.iterator();
        while( i.hasNext() )
            System.out.println(i.next() + "\n");
            
        } catch(LinkException e) {
            System.out.println("Ziv5Ctd, DialerException, "+e.getMessage());
            ziv.disconnect();
        } catch(IOException e) {
            System.out.println("Ziv5Ctd, IOException, "+e.getMessage());
            ziv.disconnect();
        }
        
        
    }
    
}
