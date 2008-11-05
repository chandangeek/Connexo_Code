/*
 * IskraEmeco.java
 *
 * Created on 8 mei 2003, 17:56
 */

/*
 *  Changes:
 *  KV 15022005 Changed RegisterConfig to allow B field obiscodes != 1 
 */
package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.*;
import java.util.logging.*;

import sun.security.action.GetLongAction;

import com.energyict.cbo.*;

import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.connection.HHUSignOn;

/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Iskra meter IEC1107 protocol.
 * <BR>
 * <B>@beginchanges</B><BR>
KV|29012004|Changed serial number and device id behaviour
KV|17022004| extended with MeterExceptionInfo
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Improved registerreading, configuration data
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 * @endchanges
 *
 */
public class MT83 implements MeterProtocol, ProtocolLink, HHUEnabler, MeterExceptionInfo, RegisterProtocol {
    
    private static final byte DEBUG=1;
    
    private static final String[] ISKRAEMECO_METERREADINGS_DEFAULT = {"Total Energy A+","Total Energy R1","Total Energy R4"};
    
    private static final int LOADPROFILES_FIRST = 1;
    private static final int LOADPROFILES_LAST = 5;
    
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
    private ChannelMap channelMap=null;
    private int extendedLogging;
    
    private TimeZone timeZone;
    private static Logger logger;
    
    int readCurrentDay;
    int loadProfileNumber;
    
    FlagIEC1107Connection flagIEC1107Connection=null;
    MT83Registry iskraEmecoRegistry=null;
    MT83Profile iskraEmecoProfile=null;
    RegisterConfig regs = new MT83RegisterConfig(); // we should use an infotype property to determine the registerset

    byte[] dataReadout=null;
    
    /** Creates a new instance of MT83, empty constructor*/
    public MT83() {
    } // public MT83()
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.YEAR,-10);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar,toCalendar,includeEvents);
    }
    
    
    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar,boolean includeEvents) throws IOException {
		ProfileData mt83profile = getIskraEmecoProfile().getProfileData(fromCalendar,
				toCalendar, getNumberOfChannels(), loadProfileNumber, includeEvents,
				isReadCurrentDay());
		
		mt83profile.applyEvents(getProfileInterval()/60);
		
		return mt83profile;
    }
    
    // Only for debugging
    public ProfileData getProfileData(Calendar from,Calendar to) throws IOException {
        return getIskraEmecoProfile().getProfileData(from,
        to,
        getNumberOfChannels(),
        1,
        false, isReadCurrentDay());
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        try {
            return (Quantity)getIskraEmecoRegistry().getRegister(name);
        }
        catch(ClassCastException e) {
            throw new IOException("IskraEmeco, getMeterReading, register "+name+" is not type Quantity");
        }
    }
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        String[] ISKRAEMECO_METERREADINGS=null;
        try {
            ISKRAEMECO_METERREADINGS = ISKRAEMECO_METERREADINGS_DEFAULT;
            
            if (channelId >= getNumberOfChannels())
                throw new IOException("IskraEmeco, getMeterReading, invalid channelId, "+channelId);
            return (Quantity)getIskraEmecoRegistry().getRegister(ISKRAEMECO_METERREADINGS[channelId]);
        }
        catch(ClassCastException e) {
            throw new IOException("IskraEmeco, getMeterReading, register "+ISKRAEMECO_METERREADINGS[channelId]+" ("+channelId+") is not type Quantity");
        }
    }
    
    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     * @exception IOException
     */
    public void setTime() throws IOException {
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);
        Date date = calendar.getTime();
        getIskraEmecoRegistry().setRegister(MT83Registry.TIME_AND_DATE_READWRITE,date);
        //getIskraEmecoRegistry().setRegister("0.9.2",date);
    } // public void setTime() throws IOException
    
    public Date getTime() throws IOException {
        Date date =  (Date)getIskraEmecoRegistry().getRegister(MT83Registry.TIME_AND_DATE_READONLY);
        sendDebug("getTime() Local time: " + new Date() + " GMT: " + new Date().toGMTString() + " TimeZone: " + getTimeZone().getID(), DEBUG);
        sendDebug("getTime() result: METER: " + date.toString() + " GMT: " + date.toGMTString() + " METER-ROUNDTRIP: " + new Date(date.getTime()-iRoundtripCorrection).toString(), DEBUG);
        return new Date(date.getTime()-iRoundtripCorrection);
    }
    
    public byte getLastProtocolState(){
        return -1;
    }
    
    /************************************** MeterProtocol implementation ***************************************/
    
    /** this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see AbstractMeterProtocol#validateProperties
     */
    public void setProperties(Properties properties) throws MissingPropertyException , InvalidPropertyException {
        validateProperties(properties);
    }
    
    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException(key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            iIEC1107TimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","20000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            iEchoCancelling=Integer.parseInt(properties.getProperty("EchoCancelling","0").trim());
            iIEC1107Compatible=Integer.parseInt(properties.getProperty("IEC1107Compatible","1").trim());
            iProfileInterval=Integer.parseInt(properties.getProperty("ProfileInterval","3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap","1.5:2.5:5.5:6.5:7.5:8.5").trim());
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim()); 
            readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay","0").trim());
            loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber","1").trim());
            
            if ((loadProfileNumber < LOADPROFILES_FIRST) || (loadProfileNumber > LOADPROFILES_LAST)) {
            	String exceptionmessage = "";
            	exceptionmessage += "LoadProfileNumber cannot be " + loadProfileNumber + "! ";
            	exceptionmessage += "LoadProfileNumber can be " + LOADPROFILES_FIRST + " to " + LOADPROFILES_LAST + " ";
            	exceptionmessage += "for the MT83x protocol.";
            	throw new InvalidPropertyException(exceptionmessage);
            }
            
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());
        }
        
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @return the register value
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        sendDebug("getRegister(): name = " + name, DEBUG);
    	return ProtocolUtils.obj2String(getIskraEmecoRegistry().getRegister(name));
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        sendDebug("setRegister(): name = " + name + " value = " + value, DEBUG);
        getIskraEmecoRegistry().setRegister(name,value);
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
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
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ChannelMap");
        result.add("ExtendedLogging");
        result.add("ReadCurrentDay");
        result.add("LoadProfileNumber");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.0 $";
    }
    
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        try {
            return((String)getIskraEmecoRegistry().getRegister(MT83Registry.SOFTWARE_REVISION));
        }
        catch(IOException e) {
            throw new IOException("IskraEmeco, getFirmwareVersion, "+e.getMessage());
        }
    } // public String getFirmwareVersion()
    
    /** initializes the receiver
     * @param inputStream <br>
     * @param outputStream <br>
     * @param timeZone <br>
     * @param logger <br>
     */
    public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible);
            flagIEC1107Connection.setErrorSignature("ER");
            iskraEmecoRegistry = new MT83Registry(this,this);
            iskraEmecoProfile = new MT83Profile(this,this,iskraEmecoRegistry);
        }
        catch(ConnectionException e) {
            logger.severe("ABBA1500: init(...), "+e.getMessage());
        }
    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)
    
    /**
     * @throws IOException  */
    public void connect() throws IOException {
        try {
            flagIEC1107Connection.connectMAC(strID,strPassword,iSecurityLevel,nodeId);
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
        
        try {
            validateSerialNumber(); // KV 15122003
        }
        catch(FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }
        
        if (extendedLogging >= 1) 
           logger.info(getRegistersInfo(extendedLogging));
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return regs.getRegisterInfo();
    }
    
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getIskraEmecoRegistry().getRegister(MT83Registry.SERIAL);
        if (sn.compareTo(serialNumber) == 0) return;
        throw new IOException("SerialNiumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }
    
    
    public void disconnect() throws NestedIOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        }
        catch(FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, "+e.getMessage());
        }
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return channelMap.getNrOfChannels();
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        Object obj = getIskraEmecoRegistry().getRegister(MT83Registry.PROFILE_INTERVAL);
        if (obj == null)
            return iProfileInterval;
        else
            return ProtocolUtils.obj2int(obj)*60;
    }
    
    private MT83Registry getIskraEmecoRegistry() {
        return iskraEmecoRegistry;
    }
    private MT83Profile getIskraEmecoProfile() {
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
        enableHHUSignOn(commChannel,false);
    }
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
        (HHUSignOn)new IEC1107HHUConnection(commChannel,iIEC1107TimeoutProperty,iProtocolRetriesProperty,300,iEchoCancelling);
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
        this.dataReadout=dataReadout;
    }
    
    
    public void release() throws IOException {
    }
    

    public String getExceptionInfo(String id) {
    	String exceptionInfo = (String) MT83CodeMapper.exceptionInfoMap.get(id);
    	if (exceptionInfo != null)
    		exceptionInfo = id + ", " + exceptionInfo;
    	else
    		exceptionInfo = "No meter specific exception info for " + id;
    	return exceptionInfo;
    }
    
    public Logger getLogger() {
        return logger;   
    }
    
    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return MT83ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	RegisterValue regvalue;
    	sendDebug("readRegister() obiscode = "  + obisCode.toString(), DEBUG);
    	MT83ObisCodeMapper ocm = new MT83ObisCodeMapper(getIskraEmecoRegistry(),getTimeZone(),regs);

    	try {
			regvalue = ocm.getRegisterValue(obisCode);
		} catch (IOException e) {
			sendDebug(e.getMessage(), DEBUG);
			throw e;
		}
		return regvalue;
    }
    
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }
    
    public boolean isRequestHeader() {
        return false;
    }
    
    public boolean isReadCurrentDay() {
        return readCurrentDay==1;
    }
    
    public static void sendDebug(String message, int debuglvl) {
    	if ((debuglvl > 0) && (DEBUG > 0 )) {
    		message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
    		System.out.println(message);
    		if (logger != null) logger.info(message);
    	}
    }

} // public class IskraEmeco implements MeterProtocol {
