/* 
 * ABBA230.java
 *
 * <B>Description :</B><BR>
 * Class that implements the Elster AS230 meter protocol.
 * <BR>
 * <B>@beginchanges</B><BR>
FBO|02022006|Initial version
FBO|29052006|Fix profile data: data was fetched as energy values, but the meter 
stores power/demand values.
FBO|30052006|Fix profile data: When a time set occurs and the time meter is set 
back for more then one interval period, there will be double entries in the 
profile data.  These entries will not get a SL flag from the meter.  Since these 
entries occur twice or more they need an SL flag.
 *@endchanges
 */

package com.energyict.protocolimpl.iec1107.abba230;

import java.io.*; 
import java.util.*; 
import java.util.logging.*;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocolimpl.base.*; 
import com.energyict.protocolimpl.iec1107.*;

/** @author  fbo */
/*
 * 
 * KV	25112008 	Changed authentication mechanism with new security level
 * KV	02122008 	Add intervalstate bits to logbook
 * JME	23012009	Fixed Java 1.5 <=> 1.4 issues to port from 8.1 to 7.5, 7.3 or 7.1
 * 
 */

public class ABBA230 implements
        MeterProtocol, ProtocolLink, HHUEnabler, SerialNumber, MeterExceptionInfo,
        RegisterProtocol, MessageProtocol, EventMapper {
    
	final int DEBUG=0;
	
	boolean firmwareUpgrade=false;
	
    private static String CONNECT 			= "ConnectLoad";
    private static String DISCONNECT 		= "DisconnectLoad";
    private static String ARM 				= "ArmMeter";
    private static String TARIFFPROGRAM 	= "UploadMeterScheme";
    private static String FIRMWAREPROGRAM 	= "UpgradeMeterFirmware";
    private static String MDRESET			= "MDReset";
	
    private static String CONNECT_DISPLAY 			= "Connect Load";
    private static String DISCONNECT_DISPLAY 		= "Disconnect Load";
    private static String ARM_DISPLAY 				= "Arm Meter";
    private static String TARIFFPROGRAM_DISPLAY 	= "Upload Meter Scheme";
    private static String FIRMWAREPROGRAM_DISPLAY 	= "Upgrade Meter Firmware";
	private static String REGISTERRESET_DISPLAY 	= "Reset registers";
	
    final static long FORCE_DELAY = 300;
    
    /** Property keys specific for AS230 protocol. */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    final static String PK_ECHO_CANCELING = "EchoCancelling";
    
    
    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 2;
    final static int PD_EXTENDED_LOGGING = 0;
    final static int PD_IEC1107_COMPATIBLE = 1;
    final static int PD_ECHO_CANCELING = 0;
    
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
    int pRoundTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pSecurityLevel = PD_SECURITY_LEVEL;
    int pCorrectTime = 0;
    int pExtendedLogging = PD_EXTENDED_LOGGING;
    private int pEchoCancelling = PD_ECHO_CANCELING;
    private int pIEC1107Compatible = PD_IEC1107_COMPATIBLE;
    
    private ABBA230MeterType abba230MeterType=null;
    private TimeZone timeZone;
    private Logger logger;
    private FlagIEC1107Connection flagConnection=null;
    private ABBA230RegisterFactory rFactory=null;
    private ABBA230Profile profile=null;
    
    public ABBA230() { }
    
    /* ________ Impelement interface MeterProtocol ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#setProperties(java.util.Properties)
     */
    public void setProperties(Properties p) throws MissingPropertyException , InvalidPropertyException {
        try {
            
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (p.getProperty(key) == null) {
                    String msg = key + " key missing";
                    throw new MissingPropertyException(msg);
                }
            }
            
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
                pRoundTripCorrection = new Integer(p.getProperty(MeterProtocol.ROUNDTRIPCORR) ).intValue();
            
            if (p.getProperty(MeterProtocol.CORRECTTIME) != null)
                pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));
            
            if (p.getProperty(PK_EXTENDED_LOGGING) != null)
                pExtendedLogging = Integer.parseInt(p.getProperty(PK_EXTENDED_LOGGING));
            
            
            pSecurityLevel = Integer.parseInt(p.getProperty("SecurityLevel","3").trim());
            if (pSecurityLevel != 0) {
                if ("".equals(pPassword)) {
                    String msg = "Password field is empty! correct first!";
                    throw new InvalidPropertyException(msg);
                }
                if (pPassword==null) {
                    String msg = "Password must be filled in!, correct first!";
                    throw new InvalidPropertyException(msg);
                }
                if (pPassword.length()!=8) {
                    String msg = "Password must have a length of 8 characters!, correct first!";
                    throw new InvalidPropertyException(msg);
                }
            }
            
            if (p.getProperty(PK_ECHO_CANCELING) != null)
                pEchoCancelling = Integer.parseInt(p.getProperty(PK_ECHO_CANCELING));
            
            if (p.getProperty(PK_IEC1107_COMPATIBLE) != null)
                pIEC1107Compatible = Integer.parseInt(p.getProperty(PK_IEC1107_COMPATIBLE));
            
            
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Elster A230, validateProperties, NumberFormatException, "+e.getMessage());
        }
        
    }
    
    
    
    
    public List map2MeterEvent(String event) throws IOException {
    	EventMapperFactory emf = new EventMapperFactory();
    	return emf.getMeterEvents(event);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getRequiredKeys()
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        
        
        
        
        
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getOptionalKeys()
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ExtendedLogging");
        return result;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol
     * #init(java.io.InputStream, java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
     */
    public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        
        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "A230 protocol init \n"
                    + " Address = " + pAddress + ","
                    + " Node Id = " + pNodeId + ","
                    + " SerialNr = " + pSerialNumber + ","
                    + " Psswd = " + pPassword + ",\n"
                    + " Timeout = " + pTimeout + ","
                    + " Retries = " + pRetries + ","
                    + " Ext. Logging = " + pExtendedLogging + ","
                    + " RoundTripCorr = " + pRoundTripCorrection + ","
                    + " Correct Time = " + pCorrectTime + ","
                    + " TimeZone = " + timeZone.getID();
            
            logger.info(infoMsg);
        }
        
        try {
            flagConnection=
                    new FlagIEC1107Connection(
                    inputStream,outputStream,pTimeout,pRetries,
                    FORCE_DELAY,pEchoCancelling,pIEC1107Compatible,
                    new CAI700());
        } catch(ConnectionException e) {
            logger.severe("Elster A230: init(...), " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#connect()
     */
    public void connect() throws IOException { connect(0); }
    
    /**
     * @param baudrate
     * @throws IOException
     */
    public void connect(int baudrate) throws IOException {
        try {
            getFlagIEC1107Connection().connectMAC(pAddress,pPassword,pSecurityLevel,pNodeId,baudrate);
            
            rFactory = new ABBA230RegisterFactory((ProtocolLink)this,(MeterExceptionInfo)this);
            profile=new ABBA230Profile(this,rFactory);
            
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        } catch(IOException e) {
            disconnect();
            throw e;
        }
        
        try {
            validateSerialNumber();
        } catch(FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }
        
        if ( pExtendedLogging > 0 ) getRegistersInfo();
        
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#disconnect()
     */
    public void disconnect() throws NestedIOException {
        try {
        	if (!firmwareUpgrade)
        		getFlagIEC1107Connection().disconnectMAC();
        } catch(FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, "+e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNumberOfChannels()
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        ABBA230Register r = rFactory.getLoadProfileConfiguration();
        LoadProfileConfigRegister lpcr = (LoadProfileConfigRegister) rFactory.getRegister( r );
        return lpcr.getNumberRegisters();
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return profile.getProfileData(includeEvents);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
    throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return profile.getProfileData(lastReading,calendar.getTime(), includeEvents);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
    throws IOException,UnsupportedException {
        return profile.getProfileData(from,to, includeEvents);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getTime()
     */
    public Date getTime() throws IOException {
        return (Date)rFactory.getRegister("TimeDate");
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#setTime()
     */
    public void setTime() throws IOException {
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND,pRoundTripCorrection);
        getFlagIEC1107Connection().authenticate();
        rFactory.setRegister("TimeDate",calendar.getTime());
    } 
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getProtocolVersion()
     */
    public String getProtocolVersion() {
    	String rev = "$Revision$"+" - "+"$Date$";
    	String manipulated = "Revision "+rev.substring(rev.indexOf("$Revision: ")+"$Revision: ".length(), rev.indexOf("$ -"))
    						+"at "
    						 +rev.substring(rev.indexOf("$Date: ")+"$Date: ".length(), rev.indexOf("$Date: ")+"$Date: ".length()+19);
    	return manipulated; 
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getFirmwareVersion() 
     */
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        String str="unknown";
        // KV 15122003 only if pAddress is filled in
        if ((pAddress!=null) && (pAddress.length()>5)) {
            str = pAddress.substring(5,pAddress.length());
        }
        return str;
    }
    

    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#release()
     */
    public void release() throws IOException {}
    
    
    /* ________ Impelement interface ProtocolLink ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProfileInterval()
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return ((Integer)rFactory.getRegister("IntegrationPeriod")).intValue();
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getFlagIEC1107Connection()
     */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagConnection;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getLogger()
     */
    public Logger getLogger() {
        return logger;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getPassword()
     */
    public String getPassword() {
        return pPassword;
    }
    
    /* (non-Javadoc)
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
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#isIEC1107Compatible()
     */
    public boolean isIEC1107Compatible() {
        return pIEC1107Compatible == 1;
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#isRequestHeader()
     */
    public boolean isRequestHeader() {
        return false;
    }
    
    /* ________ Implement interface HHUEnabler ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel, boolean)
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                (HHUSignOn)new IEC1107HHUConnection(commChannel,pTimeout,pRetries,300,pEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.HHUEnabler#getHHUDataReadout()
     */
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }
    
    
    /* ________ Impelement interface SerialNumber ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.SerialNumber#
     * getSerialNumber(com.energyict.protocol.meteridentification.DiscoverInfo)
     */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel","0");
        properties.setProperty(MeterProtocol.NODEID,nodeId==null?"":nodeId);
        properties.setProperty("IEC1107Compatible","1");
        setProperties(properties);
        init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
        enableHHUSignOn(commChannel);
        connect(baudrate);
        String serialNumber =  getRegister("SerialNumber");
        disconnect();
        return serialNumber;
    }
    
    /* ________ Impelement interface MeterProtocol ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return rFactory.readRegister(obisCode);
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.RegisterProtocol#translateRegister(com.energyict.obis.ObisCode)
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    /* ________ Impelement interface MeterExceptionInfo ___________ */
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterExceptionInfo#getExceptionInfo(java.lang.String)
     */
    public String getExceptionInfo(String id) {
        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
            return id+", "+exceptionInfo;
        else
            return "No meter specific exception info for "+id;
    }
    
    
    static Map exceptionInfoMap = new HashMap();
    static {
        exceptionInfoMap.put("ERR1","Invalid Command/Function type e.g. other than W1, R1 etc");
        exceptionInfoMap.put("ERR2","Invalid Data Identity Number e.g. Data id does not exist in the meter");
        exceptionInfoMap.put("ERR3","Invalid Packet Number");
        exceptionInfoMap.put("ERR5","Data Identity is locked - pPassword timeout");
        exceptionInfoMap.put("ERR6","General Comms error");
    }
    
    private void validateSerialNumber( ) throws IOException {
        if ((pSerialNumber == null) || ("".equals(pSerialNumber)))
            return;
        String sn = (String) rFactory.getRegister("SerialNumber");
        if( sn!= null ) {
            String snNoDash = sn.replaceAll( "-+", "" );
            String pSerialNumberNoDash = pSerialNumber.replaceAll( "-+", "" );
            if( pSerialNumberNoDash.equals( snNoDash ) )
                return;
        }
        String msg =
                "SerialNumber mismatch! meter sn=" + sn +
                ", configured sn=" + pSerialNumber;
        throw new IOException(msg);
    }
    
    public ABBA230MeterType getAbba230MeterType() {
        return abba230MeterType;
    }
    
    /* ________ Not supported methods ___________ */
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#getMeterReading(java.lang.String)
     */
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        String msg = "method getMeterReading( String name ) is not supported.";
        throw new UnsupportedException( msg );
    }
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#getMeterReading(int)
     */
    public Quantity getMeterReading(int channelID) throws UnsupportedException, IOException {
        String msg = "method getMeterReading( int channelID ) is not supported.";
        throw new UnsupportedException( msg );
    }
    
    /* method not supported
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
     */
    public byte[] getDataReadout() { return null; }
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#getCache()
     */
    public Object getCache() { return null; }
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#fetchCache(int)
     */
    public Object fetchCache(int rtuid)
    throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null; }
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object)
     */
    public void setCache(Object cacheObject) {}
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#updateCache(int, java.lang.Object)
     */
    public void updateCache(int rtuid, Object cacheObject)
    throws java.sql.SQLException, com.energyict.cbo.BusinessException {}
    
    /* method not supported
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getChannelMap()
     */
    public ChannelMap getChannelMap() { return null; }
    
    /* method not supported
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProtocolChannelMap()
     */
    public ProtocolChannelMap getProtocolChannelMap() { return null; }
    
    /* method not supported
     * @see com.energyict.protocol.MeterProtocol#initializeDevice()
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String, java.lang.String)
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
    	if (name.compareTo("FIRMWAREPROGRAM")==0) {
    		try {
	    		blankCheck();
	    		File file = new File(value);
	    		byte[] data = new byte[(int)file.length()];
	    		FileInputStream fis = new FileInputStream(file);
	    		fis.read(data);
	    		fis.close();
	    		programFirmware(new String(data));
	    		activate();
	    		//firmwareUpgrade=true;
	    		try {
        		   Thread.sleep(20000);
	    		   disconnect();
	    		}
	    		catch(Exception e) {
	    			System.out.println(e.getMessage());
	    		}
	    		connect();
	    		
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	throw new UnsupportedException();
    }    
    
    
    private void blankCheck() throws IOException {
    	long timeout=0;
    	final int AUTHENTICATE_REARM_FIRMWARE=60000; 
    	ABBA230DataIdentity di = new ABBA230DataIdentity("002", 1, 64, false, rFactory.getABBA230DataIdentityFactory());
    	for (int set=0;set<64;set++) {
    		int retries = 0;
    		while(true) {
				try {
					
		            if (((long) (System.currentTimeMillis() - timeout)) > 0) {
		                timeout = System.currentTimeMillis() + AUTHENTICATE_REARM_FIRMWARE; // arm again...
		    			if (DEBUG>=1) System.out.println("Authenticate...");
		                getFlagIEC1107Connection().authenticate();
		            }		
					
		            if (DEBUG>=1) System.out.println("Blankcheck set "+set);
		    		byte[] data = di.read(false,1,set);
		    		if (data[0] == 0) {
		    			if (DEBUG>=1) System.out.println("Erase set "+set);
		    			di.writeRawRegisterHex(set+1,"1");
		    			
		    		}
		    		break;
				} catch (FlagIEC1107ConnectionException e) {
					if (retries++>=1)
						throw e;
					e.printStackTrace();
				} catch (IOException e) {
					if (retries++>=1)
						throw e;
					e.printStackTrace();
				}
    		}
    	}
    }
    
    private void programFirmware(String firmwareXMLData) {
		FirmwareSaxParser o = new FirmwareSaxParser(rFactory.getABBA230DataIdentityFactory());
		o.start(firmwareXMLData,false); 
    }
    
    private void activate() throws IOException {
    	ABBA230DataIdentity di = new ABBA230DataIdentity("005", 1, 1, false, rFactory.getABBA230DataIdentityFactory());
    	int retries = 0;
    	while(true) {
	    	try {
				di.writeRawRegister(1,"0");
				break;
			} catch (FlagIEC1107ConnectionException e) {
				if (retries++>=1)
					throw e;
				e.printStackTrace();
			} catch (IOException e) {
				if (retries++>=1)
					throw e;
				e.printStackTrace();
			}
    	}
    }
    
    private void doMDReset() {
    	
    	try {
			rFactory.setRegister("ResetRegister",new byte[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /* (non-Javadoc)
     * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
    	
    	//rFactory.getRegister("BlankCheck");
    	
    	//byte[] data = rFactory.getABBA230DataIdentityFactory().getDataIdentity("002", false, 1, 0);
    	
//    	ABBA230DataIdentity di = new ABBA230DataIdentity("002", 1, 1, false, rFactory.getABBA230DataIdentityFactory());
//    	byte[] data = di.read(false,1,0);
    	
/*    	
    	long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
    	System.out.println("status -> "+val);
    	
    	if (val == 0) {
	    	rFactory.setRegister("ContactorStatus",new byte[]{1});
	    	try { Thread.sleep(5000); } catch(InterruptedException e) {}
	    	System.out.println("should read 1, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
    	}
    	else {
    		rFactory.setRegister("ContactorCloser",new byte[]{0});
	    	try { Thread.sleep(5000); } catch(InterruptedException e) {}
	    	System.out.println("should read 0, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
    	}
    	
    	rFactory.setRegister("ContactorStatus",new byte[]{0});
    	rFactory.setRegister("ContactorCloser",new byte[]{0});
    	try { Thread.sleep(5000); } catch(InterruptedException e) {}
    	System.out.println("should read 0, closed contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
    	
    	rFactory.setRegister("ContactorStatus",new byte[]{1});
    	try { Thread.sleep(5000); } catch(InterruptedException e) {}
    	System.out.println("should read 1, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
    	
    	rFactory.setRegister("ContactorStatus",new byte[]{0});
    	rFactory.setRegister("ContactorCloser",new byte[]{0});
    	try { Thread.sleep(5000); } catch(InterruptedException e) {}
    	System.out.println("should read 0, closed contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
    	
    	return "";
*/    	
        throw new UnsupportedException( "getRegister() is not supported" );
    }
    
    
    /* ________ Get Register Info, extended logging ___________ */
    
    private void getRegistersInfo() throws IOException {
        
        StringBuffer rslt = new StringBuffer();
        String obisCodeString;
        ObisCode obisCode;
        RegisterInfo obisCodeInfo;
        
        logger.info("************************* Extended Logging *************************");
        
        int [] billingPoint = { 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ,12,13,14,15,16,17,18,19,20,21,22,23,24,25};
        
        for (int bpi = 0; bpi < billingPoint.length; bpi ++) {
            
            TariffSources ts = (TariffSources)rFactory.getRegister("TariffSources");
            ArrayList tarifRegisters = new ArrayList();
//            if (billingPoint[bpi] == 255) {
//                ts = (TariffSources)rFactory.getRegister("TariffSources");
//            } else {
//            	
//            	if ((billingPoint[bpi]>=0) && (billingPoint[bpi]<=11)) {
//	                HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister( "HistoricalRegister", billingPoint[bpi] );
//	                if( hv.getBillingDate() == null ) continue;
//	                ts = hv.getTariffSources();
//            	}
//            	else if ((billingPoint[bpi]>=12) && (billingPoint[bpi]<=25)) {
//	                HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister( "DailyHistoricalRegister", billingPoint[bpi] );
//	                if( hv.getBillingDate() == null ) continue;
//	                ts = hv.getTariffSources();
//            	}
//                
//            }
            
            rslt.append( "Billing point: " + billingPoint[bpi] + "\n" );
            
            if( bpi > 0 ) {
                obisCodeString = "1.1.0.1.2."+ billingPoint[bpi];
                obisCode = ObisCode.fromString( obisCodeString );
                obisCodeInfo = ObisCodeMapper.getRegisterInfo( obisCode );
                rslt.append( " " + obisCodeString + ", " + obisCodeInfo + "\n" );
                if( pExtendedLogging == 2 )
                    rslt.append( " " + rFactory.readRegister( obisCode ).toString() + "\n" );
            }
            
            rslt.append( "Cumulative registers: \n" );
            List list = EnergyTypeCode.getEnergyTypeCodes();
            Iterator it = list.iterator();
            while(it.hasNext()) {
                EnergyTypeCode etc = (EnergyTypeCode)it.next();
                obisCodeString = "1.1."+etc.getObisC()+".8.0."+ billingPoint[bpi];
                obisCode = ObisCode.fromString( obisCodeString );
                obisCodeInfo = ObisCodeMapper.getRegisterInfo( obisCode );
                
                rslt.append( " " + obisCodeString + ", " + obisCodeInfo + "\n" );
                if( pExtendedLogging == 2)
                    rslt.append( " " + rFactory.readRegister( obisCode ).toString() + "\n" );
                
                for (int i=0;i<ts.getRegSource().length;i++) {
                    if (ts.getRegSource()[i] == etc.getRegSource()) {
                        obisCodeString = "1.1."+etc.getObisC()+".8."+(i+1)+"."+billingPoint[bpi];
                        tarifRegisters.add(obisCodeString);
                    }
                }
                
            }
            rslt.append( "\n" );
            
            rslt.append( "Tou Registers: \n" );
            it = tarifRegisters.iterator();
            while( it.hasNext() ){
                obisCodeString = (String) it.next();
                obisCode = ObisCode.fromString( obisCodeString );
                obisCodeInfo = ObisCodeMapper.getRegisterInfo( obisCode );
                
                rslt.append( " " + obisCodeString + ", " + obisCodeInfo + "\n" );
                if( pExtendedLogging == 2 )
                    rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
            }
            
            rslt.append( "\n" );
            
            rslt.append("Cumulative Maximum Demand registers:\n");
            int [] md = { 0, 1 };
            for (int i=0;i<md.length;i++) {
                try {
                    CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)rFactory.getRegister("CumulativeMaximumDemand"+i,billingPoint[bpi]);
                    int c = EnergyTypeCode.getObisCFromRegSource( cmd.getRegSource(), false );
                    obisCodeString = "1." + md[i] + "." + c  + ".2.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString( obisCodeString );
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo( obisCode );
                    
                    rslt.append( " " + obisCodeString + ", " + obisCodeInfo + "\n" );
                    if(  pExtendedLogging == 2 )
                        rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
                } catch(NoSuchRegisterException e) {
                    // the register is not configured in the meter, so it can not be fetched
                }
            }
            
            rslt.append( "\n" );
            
            rslt.append("Maximum demand registers:\n");
            
            int [] cmd = { 0, 1 };
            for (int i=0;i<cmd.length;i++) {
                try {
                    MaximumDemand mdRegister = (MaximumDemand)rFactory.getRegister("MaximumDemand"+i,billingPoint[bpi]);
                    int c = EnergyTypeCode.getObisCFromRegSource( mdRegister.getRegSource(), false );
                    if( mdRegister.getQuantity() == null )
                        continue;
                    
                    obisCodeString = "1.1." + c  + ".6.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString( obisCodeString );
                    //System.out.println("obisCode " + obisCodeString );
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                    //System.out.println("obisCodeInfo " + obisCodeInfo );
                    
                    rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
                    if(  pExtendedLogging == 2 )
                        rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
                    
                } catch(NoSuchRegisterException e) {
                    // the register is not configured in the meter, so it can not be fetched
                }
            }
            
            rslt.append( "\n" );
        }
            
        rslt.append("\n");
        obisCodeString = "1.1.0.4.2.255";
        obisCode = ObisCode.fromString( obisCodeString );
        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
        rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
        if(  pExtendedLogging == 2 )
            rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );

        obisCodeString = "1.1.0.4.5.255";
        obisCode = ObisCode.fromString( obisCodeString );
        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
        rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
        if(  pExtendedLogging == 2 )
            rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );

        obisCodeString = "1.0.0.0.1.255";
        obisCode = ObisCode.fromString( obisCodeString );
        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
        rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
        if(  pExtendedLogging == 2 )
            rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );

        obisCodeString = "0.0.96.50.0.255";
        obisCode = ObisCode.fromString( obisCodeString );
        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
        rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
        if(  pExtendedLogging == 2 )
            rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
        
        rslt.append("************************* End Extended Logging *********************");
        logger.info(rslt.toString());
        
    }
    
	public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        
        MessageSpec msgSpec = addBasicMsg(DISCONNECT_DISPLAY, DISCONNECT, false);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg(ARM_DISPLAY, ARM, false);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg(CONNECT_DISPLAY, CONNECT, false);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg(TARIFFPROGRAM_DISPLAY, TARIFFPROGRAM, false);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addBasicMsg(REGISTERRESET_DISPLAY, MDRESET, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(FIRMWAREPROGRAM_DISPLAY, FIRMWAREPROGRAM, true);
        cat.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        return theCategories;
	}

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
	public String writeMessage(Message msg) {
		return msg.write(this);
	}

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append( msgTag.getName() );
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute)it.next();
            if (att.getValue()==null || att.getValue().length()==0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");
        
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag())
                buf.append( writeTag((MessageTag)elt) );
            else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if (value==null || value.length()==0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("</");
        buf.append( msgTag.getName() );
        buf.append(">");
        
        return buf.toString();    
    }	
	
	public String writeTag2(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag())
                buf.append(writeTag((MessageTag) elt));
            else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");
        
        return buf.toString();
	}

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	public void applyMessages(List messageEntries) throws IOException {
		
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		
		try {
			if (messageEntry.getContent().indexOf("<"+DISCONNECT)>=0) {
				logger.info("************************* DISCONNECT CONTACTOR *************************");
		    	long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
		    	if (val == 0) {
		        	rFactory.setRegister("ContactorStatus",new byte[]{1}); // open the contactor
		    	}
			}
			else if (messageEntry.getContent().indexOf("<"+CONNECT)>=0) {
				logger.info("************************* CONNECT CONTACTOR *************************");
		    	long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
		    	if (val > 0) {
		        	rFactory.setRegister("ContactorStatus",new byte[]{0}); // arm the contactor for closing
		        	rFactory.setRegister("ContactorCloser",new byte[]{0}); // close the armed contactor
		    	}
			}
			else if (messageEntry.getContent().indexOf("<"+ARM)>=0) {
				logger.info("************************* ARM CONTACTOR *************************");
		    	long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
		    	if (val == 1) {
		        	rFactory.setRegister("ContactorStatus",new byte[]{0}); // arm the contactor for closing
		    	}
			}
			else if (messageEntry.getContent().indexOf("<"+TARIFFPROGRAM)>=0) {
				logger.info("************************* PROGRAM TARIFF *************************");
				int start = messageEntry.getContent().indexOf(TARIFFPROGRAM)+TARIFFPROGRAM.length()+1;
				int end = messageEntry.getContent().lastIndexOf(TARIFFPROGRAM)-2;
				String tariffXMLData = messageEntry.getContent().substring(start,end);
				TariffSaxParser o = new TariffSaxParser(rFactory.getABBA230DataIdentityFactory());
				o.start(tariffXMLData,false); 
			}
			else if (messageEntry.getContent().indexOf("<"+FIRMWAREPROGRAM)>=0) {
				logger.info("************************* FIRMWARE UPGRADE *************************");
				int start = messageEntry.getContent().indexOf(FIRMWAREPROGRAM)+FIRMWAREPROGRAM.length()+1;
				int end = messageEntry.getContent().lastIndexOf(FIRMWAREPROGRAM)-2;
				String firmwareXMLData = messageEntry.getContent().substring(start,end);
				blankCheck();
				
//	    		File file = new File("C:/Documents and Settings/kvds/My Documents/projecten/ESB/sphasefw.xml");
//	    		byte[] data = new byte[(int)file.length()];
//	    		FileInputStream fis = new FileInputStream(file);
//	    		fis.read(data);
//	    		fis.close();
//	    		programFirmware(new String(data));
				
				programFirmware(firmwareXMLData);
				
				activate();
				firmwareUpgrade=true;
			}
			else if (messageEntry.getContent().indexOf("<"+MDRESET)>=0) {
				logger.info("************************* MD RESET *************************");
				int start = messageEntry.getContent().indexOf(MDRESET)+MDRESET.length()+1;
				int end = messageEntry.getContent().lastIndexOf(MDRESET)-2;
				String mdresetXMLData = messageEntry.getContent().substring(start,end);
				
				doMDReset();
				
			}
			return MessageResult.createSuccess(messageEntry);
		}
		catch(IOException e) {
			return MessageResult.createFailed(messageEntry);
		}
	}
	
	
	
	
}
