package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
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
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLCConnection;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.dlms.flex.Logbook;

public class EK2xx implements DLMSCOSEMGlobals, MeterProtocol, HHUEnabler, ProtocolLink, RegisterProtocol {
	
	private static final int DEBUG 			= 1;
	private static final String DEVICE_ID 	= "ELS"; 

    protected String strID;
    protected String strPassword;
    
    protected int iHDLCTimeoutProperty;
    protected int iProtocolRetriesProperty;
    protected int iDelayAfterFailProperty;
    protected int iSecurityLevelProperty;
    protected int iRequestTimeZone;
    protected int iRoundtripCorrection;
    protected int iClientMacAddress;
    protected int iServerUpperMacAddress;
    protected int iServerLowerMacAddress;
    protected int iRequestClockObject;
    protected String nodeId;
    private String serialNumber;
    private int iInterval=-1;
    private int iNROfIntervals=-1;
    private int extendedLogging;
    private int profileInterval = -1;
    
    //private boolean boolAbort=false;
    
    DLMSConnection dlmsConnection=null;
    CosemObjectFactory cosemObjectFactory=null;
    StoredValuesImpl storedValuesImpl=null;
    
    // lazy initializing
    private int iNumberOfChannels=-1;
    private int iMeterTimeZoneOffset=255;
    private int iConfigProgramChange=-1;
    
    private DLMSMeterConfig meterConfig 	= null;
    private EK2xxAarq ek2xxAarq 			= null;
    private EK2xxRegisters ek2xxRegisters	= null;
    private EK2xxProfile ek2xxProfile		= null;
    private int numberOfChannels 			= -1;

    // Added for MeterProtocol interface implementation
    private Logger logger=null;
    private TimeZone timeZone=null;
    //private Properties properties=null;
    
    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    int addressingMode;
    int connectionMode;

	/*
	 * Constructors
	 */

	public EK2xx() {
		this.meterConfig = DLMSMeterConfig.getInstance(getDeviceID());
		this.ek2xxAarq = new EK2xxAarq(this);
		this.ek2xxRegisters = new EK2xxRegisters();
		this.ek2xxProfile = new EK2xxProfile(this);
	}

	/*
	 * Private getters, setters and methods
	 */

	private String getDeviceID() {
		return DEVICE_ID;
	}
	
	private EK2xxProfile getEk2xxProfile() {
		return ek2xxProfile;
	}
	
	public EK2xxRegisters getEk2xxRegisters() {
		return ek2xxRegisters;
	}
	
    public void requestSAP() throws IOException {
        String devID =  (String)getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
        if ((strID != null) && ("".compareTo(strID) != 0)) {
            if (strID.compareTo(devID) != 0) {
                throw new IOException("DLMSSN, requestSAP, Wrong DeviceID!, settings="+strID+", meter="+devID);
            }
        }
    } // public void requestSAP() throws IOException

	public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
	}

    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException(key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((strID != null) &&(strID.length()>16)) throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            
            
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            // KV 19012004 get the serialNumber
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","-1"));  
            connectionMode = Integer.parseInt(properties.getProperty("Connection","0")); // 0=HDLC, 1= TCP/IP
            strPassword = properties.getProperty(MeterProtocol.PASSWORD, "");
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            iRequestClockObject=Integer.parseInt(properties.getProperty("RequestClockObject","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevelProperty=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","32").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","1").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","0").trim());
            
            if (DEBUG >= 1){
            	System.out.println();
            	properties.list(System.out);
            	System.out.println();
            }
                       
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("EK2xx, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }
	
	public int getProfileInterval() throws UnsupportedException, IOException {
		if (this.profileInterval == -1) {
			this.profileInterval = (int) (getCosemObjectFactory().getData(EK2xxRegisters.PROFILE_INTERVAL).getValue() & 0xEFFFFFFF);
		}
		return profileInterval;
	}
	
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (numberOfChannels == -1) {
            numberOfChannels = getCapturedObjects().getNROfChannels();
        }
        return numberOfChannels;
    } // public int getNumberOfChannels() throws IOException

	public void disconnect() throws IOException {
        try {
            if (getDLMSConnection() != null) getDLMSConnection().disconnectMAC();
        }
        catch(DLMSConnectionException e) {
            logger.severe("DLMSLN: disconnect(), "+e.getMessage()); 
        }
	}

	public void connect() throws IOException {
		try {
			getDLMSConnection().connectMAC();
			ek2xxAarq.requestApplAssoc(iSecurityLevelProperty);
			requestSAP();
			requestObjectList();

			logger.info(getRegistersInfo(extendedLogging));

		} catch (IOException e) {
			throw new IOException("connect(): " + e.getMessage());
		} catch (DLMSConnectionException e) {
			throw new IOException("connect() DLMSConnectionException: "
					+ e.getMessage());
		}

		validateSerialNumber(); // KV 19012004

	} // public void connect() throws IOException

    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getSerialNumber();
        if ((sn != null) && (sn.compareTo(serialNumber) == 0)) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }
	
    private String getSerialNumber() throws IOException {
        UniversalObject uo = meterConfig.getSerialNumberObject();
        return getCosemObjectFactory().getGenericRead(uo.getBaseName(),uo.getValueAttributeOffset()).getString();
    } // public String getSerialNumber()   
    
    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }    
    
    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1)
           iConfigProgramChange = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        return iConfigProgramChange;
    } // public int requestConfigurationProgramChanges() throws IOException

    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    } // public void requestObjectList() throws IOException

    protected String getRegistersInfo(int extendedLogging) {
        StringBuffer strBuff = new StringBuffer();
        String regInfo = "";
        if (extendedLogging < 1) return "";

        try {
			strBuff.append("********************* All instantiated objects in the meter *********************\n");
			for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
			    UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
			    ObisCode obis = uo.getObisCode();
				regInfo = obis.toString() + " = " + translateRegister(obis).toString();
				strBuff.append(regInfo + "\n");
			}
			strBuff.append("*********************************************************************************\n");
			for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
			    UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
			    ObisCode obis = uo.getObisCode();
			    if (getEk2xxRegisters().isProfileObject(obis)) {
			    	ProfileGeneric profile = getCosemObjectFactory().getProfileGeneric(obis);
			    	
			    	strBuff.append(
			    			"profile generic = " + profile.toString() + "\n"	+
			    			"\t" + "obisCode = " + obis.toString() + "\n" +
			    			"\t" + "getCapturePeriod = " + profile.getCapturePeriod() + "\n" +
			    			"\t" + "getNumberOfProfileChannels = " + profile.getNumberOfProfileChannels() + "\n" +
			    			"\t" + "getProfileEntries = " + profile.getProfileEntries() + "\n" +
			    			"\t" + "getResetCounter = " + profile.getResetCounter() + "\n" +
			    			"\t" + "getScalerUnit = " + profile.getScalerUnit() + "\n" +
			    			"\t" + "containsCapturedObjects = " + profile.containsCapturedObjects() + "\n"+
							"\t" + "getEntriesInUse = " + profile.getEntriesInUse() + "\n"

			    	);
			    	
			    }
			}
			strBuff.append("*********************************************************************************\n\n");

        } catch (Exception e) {
			e.printStackTrace();
		}
        
        return strBuff.toString();
    }

	public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
            RtuDLMS rtu = new RtuDLMS(rtuid);
            try {
               return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            }
            catch(NotFoundException e) {
               return new DLMSCache(null,-1);  
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
	}
    
	public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getCosemObjectFactory().getData(EK2xxRegisters.SOFTWARE_VERSION).getString();
	}

	public Quantity getMeterReading(int channelId) throws UnsupportedException,	IOException {
		throw new UnsupportedException("getMeterReading(int channelId) is not suported!!!");
	}

	public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
		throw new UnsupportedException("getMeterReading(String name) is not suported!!!");
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH,-2);
        return getProfileData(calendar.getTime(),includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		Calendar calendar = Calendar.getInstance(getTimeZone());
		return getProfileData(lastReading, calendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		Date now = new Date();
		if (to.compareTo(now) >= 0) {
			System.out.println("now = " + now);
			System.out.println("now = " + to);
			to = now; 
		}
		
		List dataContainers = new ArrayList(0);
		ProfileData profileData = new ProfileData();
		DataContainer dc;
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		Calendar fromDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
		Calendar toDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
		Date profileDate = null;
		boolean lastRead = false;
		boolean dataReceived = false;
		
		ek2xxProfile.setGenerateEvents(includeEvents);
		
		fromCalendar.setTime(from);
		toCalendar.setTime(to);
		
		toDate_ptr.setTime(toCalendar.getTime());
		fromDate_ptr.setTime(toCalendar.getTime());

		do {
			fromDate_ptr.add(Calendar.HOUR, -24);
			if (fromDate_ptr.getTime().compareTo(fromCalendar.getTime()) <= 0) fromDate_ptr.setTime(fromCalendar.getTime());

			if (DEBUG >= 1) System.out.println(" ################ fromDate_ptr = " + fromDate_ptr.getTime().toString());
			if (DEBUG >= 1) System.out.println(" ################ toDate_ptr   = " + toDate_ptr.getTime().toString());
			if (DEBUG >= 1) System.out.println(" ################ fromCalendar = " + fromCalendar.getTime().toString());
			if (DEBUG >= 1) System.out.println(" ################ toCalendar   = " + toCalendar.getTime().toString());

			dc = getCosemObjectFactory().getProfileGeneric(EK2xxRegisters.PROFILE).getBuffer(fromDate_ptr, toDate_ptr);
			profileDate = ek2xxProfile.getDateFromDataContainer(dc);
			
			System.out.println(" ####################################### profileDate = " + profileDate);
			
			if (profileDate == null) {
				if (dataReceived == true) lastRead = true;
			} else {
				System.out.println(" ####################################### profileDate = " + profileDate);
				dataContainers.add(dc);
				dataReceived = true;	
			}

			toDate_ptr.setTime(fromDate_ptr.getTime());

		} while ((fromDate_ptr.getTime().compareTo(fromCalendar.getTime()) > 0 ) && !lastRead);

		ek2xxProfile.parseDataContainers(dataContainers);
		profileData.setChannelInfos(ek2xxProfile.getChannelInfos());
		profileData.getIntervalDatas().addAll(ek2xxProfile.getIntervalDatas());

		if (includeEvents){
			List meterEvents = ek2xxProfile.getMeterEvents();
			meterEvents = ProtocolUtils.checkOnOverlappingEvents(meterEvents);
			profileData.getMeterEvents().addAll(meterEvents);
		}

//		profileData.sort();
//		if (includeEvents) profileData.applyEvents(getProfileInterval());
		
		return profileData;
	}

    private CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException {
        return getEk2xxProfile().getCapturedObjects();
    } // private CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException
	
	public String getProtocolVersion() {
		return "$Revision: 1.0$";
	}

	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        throw new UnsupportedException();
	}

	public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock(EK2xxRegisters.CLOCK);
        Date date = clock.getDateTime();
        dstFlag = clock.getDstFlag();
        return date;
	}

	public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;
        
        dstFlag=-1;
        iNumberOfChannels = -1; // Lazy initializing
        iMeterTimeZoneOffset = 255; // Lazy initializing
        iConfigProgramChange = -1; // Lazy initializing
        
        try {
            cosemObjectFactory = new CosemObjectFactory(this);
            storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
            // KV 19092003 set forcedelay to 100 ms for the optical delay when using HHU?
            if (connectionMode==0)
                dlmsConnection=new HDLCConnection(inputStream,outputStream,iHDLCTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress,iServerUpperMacAddress,addressingMode);
            else
                dlmsConnection=new TCPIPConnection(inputStream,outputStream,iHDLCTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
        }
        catch(DLMSConnectionException e) {
            //logger.severe ("DLMSZMD: init(...), "+e.getMessage());
            throw new IOException(e.getMessage());
        }
        
        iInterval=-1;
        iNROfIntervals=-1;
        
	}

	public void initializeDevice() throws IOException, UnsupportedException {
		throw new UnsupportedException("initializeDevice() is not suported!!!");
	}

	public void release() throws IOException {}

	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
		throw new UnsupportedException("setRegister() not suported!");
	}

	public void setTime() throws IOException {
	       Calendar calendar=null;
	       if (iRequestTimeZone != 0) {
	           calendar = ProtocolUtils.getCalendar(getTimeZone());
	       }
	       else {
	    	   calendar = ProtocolUtils.initCalendar(false,timeZone);
	       }
	       calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);           
	       doSetTime(calendar);

		//		DateTime nowDateTime = new DateTime(new Date());
		//		getCosemObjectFactory().getClock(EK2xxRegisters.CLOCK).setTimeAttr(nowDateTime);
		//		throw new UnsupportedException("setTime() not suported!");
	}

	private void doSetTime(Calendar calendar) throws IOException
	{
		byte[] byteTimeBuffer = new byte[15];

		byteTimeBuffer[0]=1;
		byteTimeBuffer[1]=TYPEDESC_OCTET_STRING;
		byteTimeBuffer[2]=12; // length
		byteTimeBuffer[3]=(byte)(calendar.get(calendar.YEAR) >> 8);
		byteTimeBuffer[4]=(byte)calendar.get(calendar.YEAR);
		byteTimeBuffer[5]=(byte)(calendar.get(calendar.MONTH)+1);
		byteTimeBuffer[6]=(byte)calendar.get(calendar.DAY_OF_MONTH);
		byte bDOW = (byte)calendar.get(calendar.DAY_OF_WEEK);
		byteTimeBuffer[7]=bDOW--==1?(byte)7:bDOW;
		byteTimeBuffer[8]=(byte)calendar.get(calendar.HOUR_OF_DAY);
		byteTimeBuffer[9]=(byte)calendar.get(calendar.MINUTE);
		byteTimeBuffer[10]=(byte)calendar.get(calendar.SECOND);
		byteTimeBuffer[11]=(byte)0xFF;
		byteTimeBuffer[12]=(byte)0x80;
		byteTimeBuffer[13]=0x00;

		if (isRequestTimeZone()) { 
			if (dstFlag == 0) byteTimeBuffer[14]=0x00;
				else if (dstFlag == 1) byteTimeBuffer[14]=(byte)0x80;
				else throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
		}
		else {
			if (getTimeZone().inDaylightTime(calendar.getTime())) byteTimeBuffer[14]=(byte)0x80;
				else byteTimeBuffer[14]=0x00;
		}

		getCosemObjectFactory().getGenericWrite((short)meterConfig.getClockSN(),TIME_TIME).write(byteTimeBuffer);

	} // private void doSetTime(Calendar calendar)


	/*
	 * Public methods
	 */

	public void enableHHUSignOn(SerialCommunicationChannel commChannel)	throws ConnectionException {
        enableHHUSignOn(commChannel,false);
	}

	public void enableHHUSignOn(SerialCommunicationChannel commChannel,	boolean enableDataReadout) throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn)new IEC1107HHUConnection(commChannel,iHDLCTimeoutProperty,iProtocolRetriesProperty,300,0);
		hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
		hhuSignOn.enableDataReadout(enableDataReadout);
		getDLMSConnection().setHHUSignOn(hhuSignOn,nodeId);
	}

	public List getOptionalKeys() {
        List result = new ArrayList(0);
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("RequestClockObject");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("EventIdIndex");
        return result;
	}

	public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
	}

	/*
	 * Public getters and setters
	 */

	public String getPassword() {
		return strPassword;
	}

	public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();   
	}

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

	public Logger getLogger() {
		return logger;
	}

	public DLMSMeterConfig getMeterConfig() {
		return meterConfig;
	}

	public int getReference() {
        return ProtocolLink.SN_REFERENCE;
	}

	public int getRoundTripCorrection() {
		return iRoundtripCorrection;
	}

	public StoredValues getStoredValues() {
        return (StoredValues)storedValuesImpl;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {

		/*
		 * Obiscode refers to an DLMS DATA OBJECT
		 */
		if (getEk2xxRegisters().isDataObject(obisCode)) {

			Data data = getCosemObjectFactory().getData(obisCode);
			AbstractDataType valueAttr = data.getValueAttr();
			
			if (DEBUG >= 1) 
				System.out.println("ObisCode = " + obisCode + " value = " + valueAttr.toString());

			if (valueAttr.isOctetString()) 
				return new RegisterValue(obisCode, data.getString());
			if (valueAttr.isUnsigned32()) {
				return new RegisterValue(obisCode, data.getQuantityValue());
			}			

		}

		/*
		 *  Obiscode refers to an DLMS CLOCK OBJECT
		 */
		if (getEk2xxRegisters().isClockObject(obisCode)) {
			Clock clockObject = getCosemObjectFactory().getClock(obisCode);
			Date rtuDateTime = clockObject.getDateTime();
			Calendar cal = ProtocolUtils.getCalendar(getTimeZone());
			cal.setTime(rtuDateTime);
			return new RegisterValue(obisCode, cal.getTime().toString());
		}
		
		/*
		 *  Obiscode refers to an DLMS REGISTER OBJECT
		 */
		if (getEk2xxRegisters().isRegisterObject(obisCode)) {
			Register reg = getCosemObjectFactory().getRegister(obisCode);
			Date readTime = reg.getCaptureTime(); 
			Date toTime = reg.getBillingDate();
			Quantity value = null;
			String text = null;
			
			try {
				value = reg.getQuantityValue();
			} catch (Exception e) {
				text = reg.getText();
			}

			if (value != null) {
				return new RegisterValue(obisCode, value, readTime, null, toTime, new Date());
			} else {
				return new RegisterValue(obisCode, text);
			}
			
		}

		/*
		 * Obiscode refers to an DLMS PROFILE GENERIC OBJECT
		 */
		if (getEk2xxRegisters().isProfileObject(obisCode)) {
			ProfileGeneric pg = getCosemObjectFactory().getProfileGeneric(obisCode);
			if (DEBUG >= 1) {
				System.out.println(
						"profile generic = " + pg.toString() + "\n"	+
						"obisCode = " + obisCode.toString() + "\n" +
						"getCapturePeriod = " + pg.getCapturePeriod() + "\n" +
						"getNumberOfProfileChannels = " + pg.getNumberOfProfileChannels() + "\n" +
						"getProfileEntries = " + pg.getProfileEntries() + "\n" +
						"getResetCounter = " + pg.getResetCounter() + "\n" +
						"getScalerUnit = " + pg.getScalerUnit() + "\n"
				);
			}
		}
		
		throw new NoSuchRegisterException(obisCode.toString() + " is not supported.");

	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		String regType = getEk2xxRegisters().getObjectType(obisCode);
		String regName = getEk2xxRegisters().getObjectName(obisCode);
		RegisterInfo regInfo = new RegisterInfo(regName + " - Type: " + regType);
		return regInfo;
	}

	public void setCache(Object cacheObject) {}
	public void updateCache(int rtuid, Object cacheObject) throws SQLException,	BusinessException {}
	public Object getCache() {return null;}

}
