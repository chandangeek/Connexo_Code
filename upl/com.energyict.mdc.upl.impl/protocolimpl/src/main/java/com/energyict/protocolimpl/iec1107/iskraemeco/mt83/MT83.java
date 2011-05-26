package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83RegisterConfig;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83Registry;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author  jme
 * 
 * <B>Description :</B><BR>
 * Class that implements the Iskra MT83 meter IEC1107 protocol.
 * <BR>
 *
 * Changes:
 * JME	|07042009|	Added support for more registers
 * JME	|14042009|	Removed channelmap property. The protocol now reads this data from the meter.
 * JME	|20042009|	Fixed nullpointer exception when there is no profile data.
 * 
 */
public class MT83 implements MeterProtocol, ProtocolLink, HHUEnabler, MeterExceptionInfo, RegisterProtocol, DemandResetProtocol, MessageProtocol {
    
    private static final byte DEBUG=0;
        
    private static final int LOADPROFILES_FIRST = 1;
    private static final int LOADPROFILES_LAST = 2;
    
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
    private ProtocolChannelMap channelMap = null;
    private int extendedLogging;
    
    private TimeZone timeZone;
    private static Logger logger;
    
    int readCurrentDay;
    int loadProfileNumber;
    
    FlagIEC1107Connection flagIEC1107Connection=null;
    MT83Registry mt83Registry=null;
    MT83Profile mt83Profile=null;
    MT83RegisterConfig mt83RegisterConfig = new MT83RegisterConfig(); // we should use an infotype property to determine the registerset
    MT83MeterMessages meterMessages = new MT83MeterMessages(this);

    byte[] dataReadout=null;
    private boolean software7E1;
    
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
		ProfileData mt83profile = getMT83Profile().getProfileData(fromCalendar,
				toCalendar, getNumberOfChannels(), loadProfileNumber, includeEvents,
				isReadCurrentDay());
		
		mt83profile.applyEvents(getProfileInterval()/60);
		
		return mt83profile;
    }
    
//    // Only for debugging
//    public ProfileData getProfileData(Calendar from,Calendar to) throws IOException {
//        return getMT83Profile().getProfileData(from,
//        to,
//        getNumberOfChannels(),
//        1,
//        false, isReadCurrentDay());
//    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
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
        getMT83Registry().setRegister(MT83Registry.TIME_AND_DATE_READWRITE,date);
    } // public void setTime() throws IOException
    
    public Date getTime() throws IOException {
        Date date =  (Date)getMT83Registry().getRegister(MT83Registry.TIME_AND_DATE_READONLY);
        sendDebug("getTime() result: METER: " + date.toString() + " METER-ROUNDTRIP: " + new Date(date.getTime()-iRoundtripCorrection).toString(), DEBUG);
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
            iProfileInterval=Integer.parseInt(properties.getProperty("ProfileInterval","900").trim());
//            channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap","1.5:2.5:5.5:6.5:7.5:8.5").trim());
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim()); 
            readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay","0").trim());
            loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber","1").trim());
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
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
    	return ProtocolUtils.obj2String(getMT83Registry().getRegister(name));
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
        getMT83Registry().setRegister(name,value);
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
        result.add("ExtendedLogging");
        result.add("ReadCurrentDay");
        result.add("LoadProfileNumber");
        result.add("Software7E1");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        try {
            String fwversion = ""; 
        	fwversion += "Version: " + (String)getMT83Registry().getRegister(MT83Registry.SOFTWARE_REVISION) + " - ";
        	fwversion += "Device date: " + (String)getMT83Registry().getRegister(MT83Registry.SOFTWARE_DATE) + " - ";
        	fwversion += "Device Type: " + (String)getMT83Registry().getRegister(MT83Registry.DEVICE_TYPE);
        	
        	return fwversion;
        }
        catch(IOException e) {
            throw new IOException("MT83, getFirmwareVersion, "+e.getMessage());
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
            flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible,software7E1);
            flagIEC1107Connection.setErrorSignature("ER");
            mt83Registry = new MT83Registry(this,this);
            mt83Profile = new MT83Profile(this,this,mt83Registry);
        }
        catch(ConnectionException e) {
            logger.severe("MT83: init(...), "+e.getMessage());
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
        return mt83RegisterConfig.getRegisterInfo();
    }
    
    protected void validateSerialNumber() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getMT83Registry().getRegister(MT83Registry.SERIAL);
        if (sn.compareTo(serialNumber) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
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
        
    	return getProtocolChannelMap().getNrOfProtocolChannels();
    	//return channelMap.getNrOfChannels();
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        Object obj = getMT83Registry().getRegister(MT83Registry.PROFILE_INTERVAL);
        if (obj == null)
            return iProfileInterval;
        else
            return ProtocolUtils.obj2int(obj)*60;
    }
    
    private MT83Registry getMT83Registry() {
        return mt83Registry;
    }
    private MT83Profile getMT83Profile() {
        return mt83Profile;
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
        if (channelMap == null) {
        	try {
				channelMap = getMT83Profile().buildChannelMap(getProfileInterval(), loadProfileNumber);
			} catch (Exception e) {
				try {channelMap = new ProtocolChannelMap("");} catch (InvalidPropertyException e1) {}
				e.printStackTrace();
			}
        }
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
        RegisterInfo regInfo = new RegisterInfo(this.mt83RegisterConfig.getRegisterDescription(obisCode));
    	return regInfo;
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	RegisterValue regvalue;
    	sendDebug("readRegister() obiscode = "  + obisCode.toString(), DEBUG);
    	MT83ObisCodeMapper ocm = new MT83ObisCodeMapper(getMT83Registry(),getTimeZone(),mt83RegisterConfig);

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
    
	public ChannelMap getChannelMap() {
        return null;
    }

    public static void sendDebug(String message, int debuglvl) {
		message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
    	if ((debuglvl > 0) && (DEBUG > 0 ) && (logger != null)) {
    		System.out.println(message);
    		logger.info(message);
    	}
    }

    protected void setConnection(final FlagIEC1107Connection connection) {
        this.flagIEC1107Connection = connection;
    }

    /**
     * Execute a billing reset on the device. After receiving the �Demand Reset�
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws java.io.IOException
     */
    public void resetDemand() throws IOException {
        mt83Registry.setRegister(MT83Registry.BILLING_RESET_COMMAND, "");
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        this.meterMessages.applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.meterMessages.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return this.meterMessages.getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return this.meterMessages.writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return this.meterMessages.writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return this.meterMessages.writeValue(value);
    }
}
