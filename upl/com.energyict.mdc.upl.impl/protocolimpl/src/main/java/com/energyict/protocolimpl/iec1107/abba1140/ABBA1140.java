/*
 * ABBA1140.java
 *
 * <B>Description :</B><BR>
 * Class that implements the Elster A1140 meter protocol.
 * <BR>
 * <B>@beginchanges</B><BR>
 * 
 * FBO	02022006	Initial version
 * FBO	29052006	Fix profile data: data was fetched as energy values, but the meter 
 * 					stores power/demand values.
 * FBO	30052006	Fix profile data: When a time set occurs and the time meter is set 
 * 					back for more then one interval period, there will be double entries in the 
 * 					profile data.  These entries will not get a SL flag from the meter.  Since these 
 * 					entries occur twice or more they need an SL flag.
 * JME	13032009	Added support for new firmware by adding the following features:
 * 						* Added new registers: 	
 * 							- Serial number (0.0.96.1.0.255)
 * 							- Daily historical registers: Added for obisCode field F from 24 to 37
 * 							- Historical registers: Increased from 15 to 24 billing points (obis field F from 0 to 23)
 * 						* Implemented message protocol with the following messages:
 * 							- Billing reset message
 *@endchanges
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
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
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/** @author  fbo */

public class ABBA1140 implements
        MeterProtocol, ProtocolLink, HHUEnabler, SerialNumber, MeterExceptionInfo,
        RegisterProtocol, MessageProtocol {
    
    final static long FORCE_DELAY = 300;
    
    /** Property keys specific for A140 protocol. */
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    final static String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    final static String PK_ECHO_CANCELING = "EchoCancelling";
    
    private static String BILLINGRESET		= "BillingReset";
	private static String BILLINGRESET_DISPLAY 		= "Billing reset";
    
    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static int PD_SECURITY_LEVEL = 2;
    final static int PD_EXTENDED_LOGGING = 0;
    final static int PD_IEC1107_COMPATIBLE = 0;
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
    
    private ABBA1140MeterType abba1140MeterType=null;
    private TimeZone timeZone;
    private Logger logger;
    private FlagIEC1107Connection flagConnection=null;
    private ABBA1140RegisterFactory rFactory=null;
    private ABBA1140Profile profile=null;
    
    public ABBA1140() { }
    
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
            
            
            pSecurityLevel = Integer.parseInt(p.getProperty("SecurityLevel","2").trim());
            if (pSecurityLevel != 0) {
                if ("".equals(pPassword)) {
                    String msg = "Password field is empty! correct first!";
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
            throw new InvalidPropertyException("ABBA1140, validateProperties, NumberFormatException, "+e.getMessage());
        }
        
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
                    "A1140 protocol init \n"
                    + " Address = " + pAddress + ","
                    + " Node Id = " + pNodeId + ","
                    + " SerialNr = " + pSerialNumber + ","
                    + " Psswd = " + pPassword + ","
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
            logger.severe("ABBA1140: init(...), " + e.getMessage());
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
            rFactory = new ABBA1140RegisterFactory((ProtocolLink)this,(MeterExceptionInfo)this);
            profile=new ABBA1140Profile(this,rFactory);
            
            
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
            getFlagIEC1107Connection().disconnectMAC();
        } catch(FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, "+e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNumberOfChannels()
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        ABBA1140Register r = rFactory.getLoadProfileConfiguration();
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
        return "$Revision: 1.16 $";
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
     * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String, java.lang.String)
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        rFactory.setRegister(name,value);
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
    
    /* ________ Impelement interface HHUEnabler ___________ */
    
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
    
    public ABBA1140MeterType getAbba1140MeterType() {
        return abba1140MeterType;
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
     * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        throw new UnsupportedException( "getRegister() is not supported" );
    }
    
    
    /* ________ Get Register Info, extended logging ___________ */
    
    private void getRegistersInfo() throws IOException {
        
        StringBuffer rslt = new StringBuffer();
        String obisCodeString;
        ObisCode obisCode;
        RegisterInfo obisCodeInfo;
        
        logger.info("************************* Extended Logging *************************");
        
        int [] billingPoint = { 255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
        
        for (int bpi = 0; bpi < billingPoint.length; bpi ++) {
            
            TariffSources ts;
            ArrayList tarifRegisters = new ArrayList();
            if (bpi == 0) {
                ts = (TariffSources)rFactory.getRegister("TariffSources");
            } else {
                HistoricalRegister hv = (HistoricalRegister)
                rFactory.getRegister( "HistoricalRegister", billingPoint[bpi] );
                if( hv.getBillingDate() == null ) continue;
                ts = hv.getTariffSources();
                
            }
            
            rslt.append( "Billing point: " + billingPoint[bpi] + "\n" );
            
            if( bpi > 0 ) {
                obisCodeString = "1.1.0.1.2."+ billingPoint[bpi];
                obisCode = ObisCode.fromString( obisCodeString );
                try {
					obisCodeInfo = ObisCodeMapper.getRegisterInfo( obisCode );
	                rslt.append( " " + obisCodeString + ", " + obisCodeInfo + "\n" );
				} catch (Exception e) {
					e.printStackTrace();
				}
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
            int [] md = { 0, 1, 2, 3 };
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
            
            int [] cmd = { 0, 1, 2, 3 };
            for (int i=0;i<cmd.length;i++) {
                try {
                    MaximumDemand mdRegister = (MaximumDemand)rFactory.getRegister("MaximumDemand"+(i*3),billingPoint[bpi]);
                    int c = EnergyTypeCode.getObisCFromRegSource( mdRegister.getRegSource(), false );
                    if( mdRegister.getQuantity() == null )
                        continue;
                    
                    obisCodeString = "1.1." + c  + ".6.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString( obisCodeString );
                    System.out.println("obisCode " + obisCodeString );
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                    System.out.println("obisCodeInfo " + obisCodeInfo );
                    
                    rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
                    if(  pExtendedLogging == 2 )
                        rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
                    
                    obisCodeString = "1.2." + c  + ".6.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString( obisCodeString );
                    System.out.println("obisCode " + obisCodeString );
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                    System.out.println("obisCodeInfo " + obisCodeInfo );
                    
                    rslt.append( " " + obisCodeString + ", " + obisCodeInfo +"\n");
                    if(  pExtendedLogging == 2 )
                        rslt.append( " " +  rFactory.readRegister( obisCode ).toString() + "\n" );
                    
                    obisCodeString = "1.3." + c  + ".6.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString( obisCodeString );
                    System.out.println("obisCode " + obisCodeString );
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                    System.out.println("obisCodeInfo " + obisCodeInfo );
                    
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
    
    /*
     * MessageProtocol methods below this banner
     */
    
	public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        
        MessageSpec msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
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

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	public void applyMessages(List messageEntries) throws IOException {
		
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

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		
		if (messageEntry.getContent().indexOf("<"+BILLINGRESET)>=0) {
			try {
				logger.info("************************* BILLING RESET *************************");
				logger.info("Performing billing reset ...");
				int start = messageEntry.getContent().indexOf(BILLINGRESET)+BILLINGRESET.length()+1;
				int end = messageEntry.getContent().lastIndexOf(BILLINGRESET)-2;
				String mdresetXMLData = messageEntry.getContent().substring(start,end);
				doBillingReset();
				logger.info("Billing reset succes!");
				return MessageResult.createSuccess(messageEntry);
			} catch(IOException e) {
				logger.info("Billing reset failed! => " + e.getMessage());
				e.printStackTrace();
			}
		}
		return MessageResult.createFailed(messageEntry);
	}

	private void doBillingReset() throws IOException {
		rFactory.setRegister("EndOfBillingPeriod", "1");
	}

}
