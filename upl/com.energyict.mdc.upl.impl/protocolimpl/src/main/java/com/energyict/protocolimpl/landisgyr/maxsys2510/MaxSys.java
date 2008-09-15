package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.ShadowList;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuTypeShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterSpecShadow;
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

/**
 * @beginchanges
 * @endchanges
 * 
 * Table 15 contains all the register values.  It has 2 + x nr of places in
 * memory to make a copy of all register data.  
 * It is divided:
 *  - current season:   registers currently updated 
 *  - previous season:  registers from previous season
 *  - self read data:   registers copy from previous rate reset.  
 * 
 * From the (S4)manual:
 * "When actuated, the self-read function will save a copy of all energy and 
 * demand information, including TOU metrics, and mark the current date and 
 * time." 
 * 
 * And
 * 
 * "The S4 is capable of storing up to six sets of self-read data. A self-read 
 * initiates the transfer of current data into the next available self-read 
 * memory block, followed by a demand reset. Only the most recent six self-reads 
 * are retained. The oldest self-read is replaced by the most recent when the 
 * programmable number has been reached."
 * 
 * There are 2 (independent) types of billing periods:
 *  - Seasons
 *  - Rate resets
 * 
 * A season has a week schedule.  For every day there are several time of uses
 * defined.  A time of use defines which datablocks are active.
 * 
 * @author fbo
 */

public class MaxSys implements MeterProtocol, RegisterProtocol {

    /** Property keys */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_FORCE_DELAY = "ForceDelay";
    final static String PK_READ_UNIT1_SERIALNUMBER = "ReadUnit1SerialNumber";
    final static String PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE = "ReadProfileDataBeforeConfigChange";
    final static String PK_COMMAND_DELAY = "CommandDelay";

    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 2;
    final static String PD_EXTENDED_LOGGING = "0";
    final static int PD_FORCE_DELAY = 250;
    
    /**
     * Property values Required properties will have NO default value Optional 
     * properties make use of default value
     */
    String pAddress = null;
    String pNodeId = PD_NODE_ID;
    String pSerialNumber = null;
    int pProfileInterval;
    byte[] pPassword;

    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pSecurityLevel = PD_SECURITY_LEVEL;
    int pCorrectTime = 0;
    int pForceDelay = PD_FORCE_DELAY;
    
    String pExtendedLogging = PD_EXTENDED_LOGGING;

    LinkLayer linkLayer;
    CommandFactory commandFactory;
    
    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    private Logger logger = null;

    private Firmware firmware;
    
    private boolean readUnit1SerialNumber = false;
    private boolean readProfileDataBeforeConfigChange = true;
    private int commandDelay = 100;
    
    public MaxSys() { }
    
    public Logger getLogger() {
    	return this.logger;
    }

    /* ___ Implement interface MeterProtocol ___ */

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol# setProperties(java.util.Properties)
     */
    public void setProperties(Properties p) throws InvalidPropertyException, MissingPropertyException {

        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null)
            pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
        
        if (p.getProperty(MeterProtocol.NODEID) != null) {
        	pNodeId = p.getProperty(MeterProtocol.NODEID);
        	if (pNodeId.length() == 7)
        		pNodeId = "3" + pNodeId;
        	if (pNodeId.length() != 8)
        		throw new InvalidPropertyException( "NodeId must be a string of 7 or 8 numbers long");
        	try {
        		Integer.parseInt(pNodeId);
        	}
        	catch (NumberFormatException e) {
        		throw new InvalidPropertyException( "NodeId: only numbers allowed");
        	}
        }

        if (p.getProperty(MeterProtocol.PROFILEINTERVAL) != null)
            pProfileInterval = Integer.parseInt(p.getProperty(MeterProtocol.PROFILEINTERVAL));

        if (p.getProperty(MeterProtocol.PASSWORD) != null) {
            
            String pwd = p.getProperty(MeterProtocol.PASSWORD);
            if( pwd == null ) {
                pwd = "    ";
            }
            
            if( pwd.length() != 4 ) {
                String msg = "Password must be a string of 4 characters long. eg 0000";
                throw new InvalidPropertyException( msg );
            }
            
            for( int i = pwd.length(); i < 4; i ++ )
                pwd += " ";

            pPassword = new byte [4];
            pPassword[0] = pwd.getBytes()[0]; 
            pPassword[1] = pwd.getBytes()[1]; 
            pPassword[2] = pwd.getBytes()[2];
            pPassword[3] = pwd.getBytes()[3];
            
        }

        if (p.getProperty(PK_TIMEOUT) != null)
            pTimeout = new Integer(p.getProperty(PK_TIMEOUT)).intValue();

        if (p.getProperty(PK_RETRIES) != null)
            pRetries = new Integer(p.getProperty(PK_RETRIES)).intValue();

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null)
            pRountTripCorrection = Integer.parseInt(p.getProperty(MeterProtocol.ROUNDTRIPCORR));

        if (p.getProperty(MeterProtocol.CORRECTTIME) != null)
            pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));

        if (p.getProperty(PK_FORCE_DELAY) != null)
            pForceDelay = Integer.parseInt(p.getProperty(PK_FORCE_DELAY));        
        
        if (p.getProperty(PK_EXTENDED_LOGGING) != null)
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        
         readUnit1SerialNumber = 
        	"1".equals(p.getProperty(PK_READ_UNIT1_SERIALNUMBER));
         readProfileDataBeforeConfigChange = 
         	!"0".equals(p.getProperty(this.PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE));
         
         if (p.getProperty(this.PK_COMMAND_DELAY) != null) {
        	 try {
        		 this.commandDelay = Integer.parseInt(p.getProperty(PK_COMMAND_DELAY));   
        	 }
        	 catch (NumberFormatException e) {
        		 throw new InvalidPropertyException("CommandDelay must be a number");
        	 }
         }
         
    }
    
    public int getCommandDelay() {
    	return this.commandDelay;
    }

    /**
     * the implementation returns both the address and password key
     * 
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    /**
     * this implementation returns an empty list
     * 
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(MeterProtocol.NODEID);
        result.add(PK_TIMEOUT);
        result.add(PK_RETRIES);
        result.add(PK_EXTENDED_LOGGING);
        result.add(PK_READ_UNIT1_SERIALNUMBER);
        result.add(PK_READ_PROFILE_DATA_BEFORE_CONIG_CHANGE);
        return result;
    }
    
    protected void sendNodeId() throws IOException {
    	try {
	    	if ((this.pNodeId != null) && !"".equals(pNodeId)) {
	        	XCommand xCommand = commandFactory.createX( nextCrn(), 0x00, 0x0b ); // 0b => slave
	    		byte arg1 = (byte) Integer.parseInt(pNodeId.substring(0, 2), 16);     
	    		byte arg2 = (byte) Integer.parseInt(pNodeId.substring(2, 4), 16);   
	    		byte arg3 = (byte) Integer.parseInt(pNodeId.substring(4, 6), 16);   
		        byte arg4 = (byte) Integer.parseInt(pNodeId.substring(6, 8), 16);   
	    		byte[] arg = {arg1, arg2, arg3, arg4};
	    		xCommand.setArgumnt(arg);
	            linkLayer.send( xCommand );
	    	}
    	}
    	catch (NumberFormatException e) {
    		throw new IOException("Invalid node address: " + pNodeId);
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#init( java.io.InputStream, java.io.OutputStream, java.util.TimeZone,
     *      java.util.logging.Logger)
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger)
            throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        try {

            commandFactory = new CommandFactory();
            linkLayer = 
                new LinkLayer( inputStream, outputStream, 0, 0, pRetries, pForceDelay, this);
            
            
            sendNodeId();
            obisCodeMapper = new ObisCodeMapper(this);

        } catch (ConnectionException e) {
            logger.severe("MAXSys 2510, " + e.getMessage());
            throw e;
        }

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg = "MaxSys protocol init \n";
            infoMsg += " SerialNr = " + pSerialNumber + ",";
            infoMsg += " Psswd = " + new String(pPassword) + ","; 
            infoMsg += " Timeout = " + pTimeout + ",";
            infoMsg += " Retries = " + pRetries + ",";
            infoMsg += " Ext. Logging = " + pExtendedLogging + ",";
            infoMsg += " RoundTripCorr = " + pRountTripCorrection + ",";
            infoMsg += " Correct Time = " + pCorrectTime + ",";
            infoMsg += " TimeZone = " + timeZone.getID();
            logger.info(infoMsg);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#connect()
     */
    public void connect() throws IOException {
    	this.getLogger().info("connect");
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {
        	linkLayer.send( commandFactory.createX( nextCrn(), 0x00, 0x0e ) ); // oe: return unit id
            getTable0();
            
            doExtendedLogging();
            validateSerialNumber( );
            
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }
    
    protected void sleep(int millisec) throws IOException{
    	try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public void disconnect() throws IOException {
    	sleep(4000);
    	//this.getLogger().info("disconnect " + pSerialNumber);
        /*linkLayer.send( commandFactory.createX( nextCrn(), 0x00, 0x10 ), 1);
        try {
        	getLogger().info("start sleeping: " + new Date());
        	Thread.sleep(3000);
        	getLogger().info("end sleeping: " + new Date());
        }
        catch (InterruptedException e) {
        	e.printStackTrace();
        }*/
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getTable11().getTypeStoreCntrlRcd().getNoOfChnls();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = ProtocolUtils.getCalendar(timeZone);

        Date to = c.getTime();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        Date from = c.getTime();

        return getProfileData(from, to, includeEvents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        
        ProfileData pd = getTable12(lastReading, includeEvents).getProfile();
        return pd;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {

        throw new UnsupportedException();
        
    }

    /* 
     * (non-Javadoc)
     * 
     * @see com.energyict.protocol.MeterProtocol#getProfileInterval()
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        
        return getTable11().getTypeStoreCntrlRcd().getIntvlInMins() * 60;
    
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
    void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }
    
    public Date getBeginningOfRecording() throws IOException {
    	TableAddress ta = new TableAddress( this, 2, 30 );
    	byte[] values = ta.readBytes(6);
    	return TypeDateTimeRcd.parse(new Assembly(this, new ByteArray(values))).toDate();
    }

    private void validateSerialNumber( ) throws IOException {
    	this.getLogger().info("validateSerialNumber");
        if ((pSerialNumber == null) || ("".equals(pSerialNumber)))
            return;
     
        String sn = null;
        
        // initial implementatation: serialnumber = unit_id3 (this is the default!)
        // implementation for Imserv: serialnumber = unit_id1
        if (!readUnit1SerialNumber) {
        	TableAddress ta = new TableAddress( this, 2, 19 );
        	sn = ta.readString(11);
        }
        else {
        	TableAddress ta = new TableAddress( this, 2, 0 );
        	byte[] values = ta.readBytes(4);
        	sn = getSerialNumber(values).substring(1);
        }
        
        if( sn!= null ) {
            if( pSerialNumber.equals( sn ) )
                return;
        }
        String msg =
                "SerialNumber mismatch! meter sn=" + sn +
                ", configured sn=" + pSerialNumber;
        throw new IOException(msg);
    }
    
    protected String getSerialNumber(byte[] data) {
    	StringBuffer strBuff=new StringBuffer(); 
        for (int i=0;i<data.length;i++) {
        	int bKar = data[i]&0xFF;
        	strBuff.append(String.valueOf((char) ProtocolUtils.convertHexLSB(bKar)));
        	strBuff.append(String.valueOf((char) ProtocolUtils.convertHexMSB(bKar)));
        }
        return strBuff.toString();
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.11 $";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        StringBuffer rslt = new StringBuffer();
        rslt.append( getTable0().getTypeMaximumValues().getVersionNumber() );
        rslt.append( " " );
        rslt.append( getTable0().getTypeMaximumValues().getRevisionNumber() );
        return rslt.toString();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Date getTime() throws IOException {
        return getTable1().getTypeMaximumValues().getClockCalendar().toDate();
    }

    /**
     * Send the time delta in milliseconds.
     * (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#setTime()
     */
    public void setTime() throws IOException {
        
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);

        long nowMilli = calendar.getTimeInMillis();
        
        /* meterDate */
        Date mDate = getTable1().getTypeMaximumValues().getClockCalendar().toDate();
        long mMilli = mDate.getTime();
        
        short secondsDelta = (short)((nowMilli-mMilli)/1000);
        byte b1 = (byte)(secondsDelta&0x00FF);
        byte b2 = (byte)((secondsDelta&0xFF00)>>8);

        XCommand xCommand = commandFactory.createX( nextCrn(), 0x00, 0x0d );
        xCommand.setArgumnt( new byte [] { 0, 0, b2, b1  } );
        linkLayer.send( xCommand );
        
    }

    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException,
            UnsupportedException {
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
    
    public byte [] getPassword( ){
        return pPassword;
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

    /** for easy debugging */
    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    TimeZone getTimeZone() {
        return timeZone;
    }

    /** for easy debugging */
    void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    LinkLayer getLinkLayer() {
        return linkLayer;
    }

    Firmware getFirmware() {
        return firmware;
    }

    
    int crn = 0;

    int nextCrn() {
        crn = crn + 1;
        return crn;
    }

    Table0 table0;
    Table1 table1;
    Table3 table3;
    Table4 table4;
    Table8 table8;
    Table11 table11;
    Table13 table13;
    Table14 table14;
    Table15 table15;
    Table16 table16;
    Table18 table18;
    
    
    Table0 getTable0() throws IOException {
        if (table0== null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 0);
            ByteArray ba = linkLayer.send( command );
            table0 = Table0.parse( new Assembly( this, ba ) );
        }
        return table0;
    }
    
    Table1 getTable1() throws IOException {
        //if (table1 == null) {
            StandardCommand command = commandFactory.createY(nextCrn(), 1);
            ByteArray ba = linkLayer.send( command );
            table1 = Table1.parse( new Assembly( this, ba));
        //}
        return table1;
    }
    
    Table3 getTable3() throws IOException {
        if( table3 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 3);
            ByteArray ba = linkLayer.send( command );
            table3 = Table3.parse( this, new Assembly( this, ba ) );
        }
        return table3;
    }
    
    Table4 getTable4() throws IOException {
        if( table4 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 4);
            ByteArray ba = linkLayer.send( command );
            table4 = Table4.parse( this, new Assembly( this, ba ) );
        }
        return table4;
    }
    
    Table8 getTable8() throws IOException {
        if( table8 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 8);
            ByteArray ba = linkLayer.send( command );
            table8 = Table8.parse( new Assembly( this, ba ) );            
        }
        return table8;
    }
    
    Table11 getTable11() throws IOException {
        if( table11 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 11);
            ByteArray ba = linkLayer.send( command );
            table11 = Table11.parse( this, new Assembly( this, ba ) );
        }
        return table11;
    }    
    
    Table12 getTable12( Date from, boolean includeEvents ) throws IOException {
    	
        int noOfChnls = getTable11().getTypeStoreCntrlRcd().getNoOfChnls();
        int dataSize = getTable11().getTypeStoreCntrlRcd().getDataSize();
        int intervalMinutes = getTable11().getTypeStoreCntrlRcd().getIntvlInMins();
        int headerSize = (noOfChnls * 2) + 6;
        int intervalSize = dataSize * noOfChnls;
        
        Calendar fCal = Calendar.getInstance(timeZone);
        fCal.setTime(from);
        
        Calendar tCal = Calendar.getInstance(timeZone);
        tCal.setTime( getTime() );
        
        int nrIntervals = 0;
        while( fCal.before(tCal) ) {
            fCal.add( Calendar.MINUTE, intervalMinutes );
            nrIntervals = nrIntervals + 1;
        }    
        int totalSize = (( headerSize + ( nrIntervals * intervalSize ) ) / 256)+1; // KV_CHANGED, add +1 to avoid 0, that is what the doc tells...
                                                                                   // If bytes 7 and 8 are both
                                                                                   // zero then the SMD will transmit the number of bytes
                                                                                   // remaining in the specified table starting from the
                                                                                   // specified displacement

        
        
        
        StandardCommand command = commandFactory.createY(nextCrn(), 12);
        command.setNbls( totalSize & 0x000000FF );
        command.setNbms( totalSize & 0x0000FF00 );
        ByteArray ba = linkLayer.send( command );
        return Table12.parse( new Assembly( this, ba ), includeEvents, nrIntervals, readProfileDataBeforeConfigChange );
    }
    
    Table13 getTable13() throws IOException {
        if( table13 == null ) {
            StandardCommand command = commandFactory.createY(nextCrn(), 13);
            ByteArray ba = linkLayer.send( command );
            table13 = Table13.parse( new Assembly( this, ba ) );
        }
        return table13;
    }
    
    Table14 getTable14() throws IOException {
        if( table14 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 14);
            ByteArray ba = linkLayer.send( command );
            table14 = Table14.parse( new Assembly( this, ba ) );            
        }
        return table14;
    }
    
    Table15 getTable15( ) throws IOException {
        if( table15 == null ) {
            StandardCommand command = commandFactory.createY(nextCrn(), 15);
            ByteArray ba = linkLayer.send( command );
            table15 = Table15.parse( new Assembly( this, ba ) );
        }
        return table15;
    }

    Table16 getTable16() throws IOException {
        if( table16 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 16);
            ByteArray ba = linkLayer.send( command );
            table16 = Table16.parse( new Assembly( this, ba ) );
        } 
        return table16;
    }
    
    Table18 getTable18() throws IOException {
        if( table18 == null ){
            StandardCommand command = commandFactory.createY(nextCrn(), 18);
            ByteArray ba = linkLayer.send( command );
            table18 = Table18.parse( new Assembly( this, ba ) );
        } 
        return table18;
    } 

    ByteArray read( TableAddress tableAddress ) throws IOException {
        StandardCommand command = commandFactory.createY(nextCrn(), tableAddress); 
        return linkLayer.send( command ); 
    }
    
    public static void main(String[] args) throws Exception {
    	
    	String pNodeId = "9700189";
    	pNodeId = "3" + pNodeId;
		byte arg1 = Byte.parseByte(pNodeId.substring(0, 2), 16);     
		byte arg2 = Byte.parseByte(pNodeId.substring(2, 4), 16);   
		byte arg3 = Byte.parseByte(pNodeId.substring(4, 6), 16);   

		byte arg4 = (byte) Integer.parseInt(pNodeId.substring(6, 8), 16); 
		
		byte[] arg = {arg1, arg2, arg3, arg4};
		System.out.println(ProtocolUtils.outputHexString(arg));
		
//		int value = 
		//byte d = (byte)Integer.get
		//byte b = 0x(int) 
    	//System.out.println(0x80 & 0xC0);
    	
    	
        /*ialer dialer = null;
        MaxSys max = new MaxSys();
        try {

            dialer = DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("00019163535977", 60000);
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();

            Properties properties = new Properties();
            properties.setProperty(MeterProtocol.PASSWORD, "0000");

            
//            properties.setProperty(PK_EXTENDED_LOGGING, "2");

            properties.setProperty(MeterProtocol.SERIALNUMBER, "DEVICE_ID_3");
            properties.setProperty("Retries", "5");
            properties.setProperty("ProfileInterval", "300");

            max.setProperties(properties);

            dialer
                .getSerialCommunicationChannel()
                .setParamsAndFlush( 9600, SerialCommunicationChannel.DATABITS_8,
                                    SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);

            TimeZone ect = TimeZone.getTimeZone("ECT");
            max.init(is, os, ect, Logger.getLogger("name"));
            max.connect();
            
        } catch (LinkException e) {
            System.out.println("MaxSys, DialerException, " + e.getMessage());
            max.disconnect();
        } catch (IOException e) {
            System.out.println("MaxSys, IOException, " + e.getMessage());
            max.disconnect();
        }*/
		

    }
    

}
