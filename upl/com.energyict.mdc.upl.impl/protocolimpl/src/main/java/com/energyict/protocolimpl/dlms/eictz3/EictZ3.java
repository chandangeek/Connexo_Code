/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the DLMS COSEM meter protocol of the Actaris SL7000 meter with LN referencing. 
 * <BR>
 * <B>@beginchanges</B><BR>
	KV|14052002|Initial version
	KV|25102002|Re-engineered to MeterProtocol interface
	KV|28082003|Password variable length
	KV||bugfix, change of interface signature getValuesIterator in IntervalData 
	KV|29102003|bugfix, did not request meterreading unit
	KV|16012004|changed powerfail handling...
	KV|06102004| reengineer using cosem package and add obiscode register mapping
	KV|17112004|add logbook implementation
	KV|17032005|improved registerreading
	KV|23032005|Changed header to be compatible with protocol version tool
	KV|31032005|Handle DataContainerException
	GN|25042008|Missing hour values with a profileInterval of 10min
	GN|04022009|Added the possibility to make a request with a from/to date. The request must be in the form: 0.0.99.1.0.255:7:2-01/02/2009 00:00:00-04/02/2009 12:00:00
	KV|11022009|Cleanup and refactored as NTA compatible EICT Z3 protocol
 * @endchanges
 */
package com.energyict.protocolimpl.dlms.eictz3;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocolimpl.dlms.Z3.AARQ;

public class EictZ3 implements DLMSCOSEMGlobals, MeterProtocol, HHUEnabler, ProtocolLink, CacheMechanism, RegisterProtocol {
    private static final byte DEBUG=0;  // KV 16012004 changed all DEBUG values  
    
    
    String version=null;
    String serialnr=null;
    String nodeId;    

    private String strID=null;
    private String strPassword=null;
    private String serialNumber=null;
    
    private int hDLCTimeoutProperty;
    private int protocolRetriesProperty;
    private int securityLevel;
    private int requestTimeZone;
    private int roundtripCorrection; 
    private int clientMacAddress;
    private int serverUpperMacAddress;
    private int serverLowerMacAddress;
    private String firmwareVersion;
    private String loadProfileObisCode;
    private int fullLogbook;
    
    DLMSConnection dlmsConnection=null;
    CosemObjectFactory cosemObjectFactory=null;
    StoredValuesImpl storedValuesImpl=null;
    
    ObisCodeMapper ocm=null;
    
    // Lazy initializing
    private int numberOfChannels=-1;
    private int configProgramChanges=-1;
    private int profileInterval=-1;
    CapturedObjectsHelper capturedObjectsHelper=null;
    
    // DLMS PDU offsets
    private static final byte DL_COSEMPDU_DATA_OFFSET=0x07;

    // Added for MeterProtocol interface implementation
    private Logger logger=null;
    private TimeZone timeZone=null;
    
    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("ECT");
    private DLMSCache dlmsCache=new DLMSCache();     
    private int extendedLogging;
    int addressingMode;
    int connectionMode;
    int informationFieldSize;
    
    /** Creates a new instance of EictZ3, empty constructor*/
    public EictZ3()
    {
    } // public EictZ3(...)

    public DLMSConnection getDLMSConnection() {
       return dlmsConnection;
    }
    
    /** initializes the receiver
     * @param inputStream <br>
     * @param outputStream <br>
     * @param timeZone <br>
     * @param logger <br>
     */    
    public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;     
        
        try {
            cosemObjectFactory = new CosemObjectFactory(this);
            storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
            if (connectionMode == 0)
                dlmsConnection=new HDLCConnection(inputStream,outputStream,hDLCTimeoutProperty,100,protocolRetriesProperty,clientMacAddress,serverLowerMacAddress,serverUpperMacAddress,addressingMode,informationFieldSize);
            else
                dlmsConnection=new TCPIPConnection(inputStream,outputStream,hDLCTimeoutProperty,100,protocolRetriesProperty,clientMacAddress,serverLowerMacAddress);
            
            getDLMSConnection().setIskraWrapper(1);
        }
        catch(DLMSConnectionException e) {
           //logger.severe ("dlms: Device clock is outside tolerance window. Setting clock");
           throw new IOException(e.getMessage());
        }
        //boolAbort = false;
    }
    
    
    
    private CapturedObjectsHelper getCapturedObjectsHelper()  throws UnsupportedException, IOException {
        if (capturedObjectsHelper == null) {
           ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString(loadProfileObisCode));
           capturedObjectsHelper = profileGeneric.getCaptureObjectsHelper();
        } // if (capturedObjects == null) 
        return capturedObjectsHelper;
    } // private CapturedObjectsHelper getCapturedObjectsHelper()  throws UnsupportedException, IOException {
    
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
    	try {
	        if (numberOfChannels == -1) {
	            numberOfChannels = getCapturedObjectsHelper().getNrOfchannels();
	        }
	        return numberOfChannels;
    	}
    	catch(IOException e) {
    		getLogger().severe(e.getMessage());
    		return 0;
    	}
    } // public int getNumberOfChannels() throws IOException
    

/**
 * Method that requests the recorder interval in sec.
 * Hardcoded for SL7000 meter to 15 min.
 * @return Remote meter 'recorder interval' in min.
 * @exception IOException
 */
    public int getProfileInterval() throws IOException,UnsupportedException{
    	try {
	        if (profileInterval == -1) {
	           ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString(loadProfileObisCode));
	           profileInterval = profileGeneric.getCapturePeriod();
	        }
	        return profileInterval;
    	}
    	catch(IOException e) {
    		return 0;
    	}
    }
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.set(Calendar.YEAR,2009);        
        fromCalendar.set(Calendar.MONTH,0);
        fromCalendar.set(Calendar.DATE,1);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }

    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(timeZone),includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }
    
    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar,boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte)getNumberOfChannels();
        return doGetDemandValues(fromCalendar,
                                 bNROfChannels,
                                 includeEvents);
    }
    
    private ProfileData doGetDemandValues(Calendar fromCalendar, byte bNROfChannels,  boolean includeEvents) throws IOException {
        
        ProfileData profileData = new ProfileData();
        DataContainer dataContainer = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString(loadProfileObisCode)).getBuffer(fromCalendar,Calendar.getInstance());
        
        for (int i=0;i<bNROfChannels;i++) {
       	   ScalerUnit scalerunit = getRegisterScalerUnit(i);
           profileData.addChannel(new ChannelInfo(i,"EictZ3_channel_"+i,scalerunit.getUnit()));
        }
        buildProfileData(bNROfChannels,dataContainer,profileData);
        
        if (includeEvents) {
            profileData.getMeterEvents().addAll(getLogbookData(fromCalendar));
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval()/60); 
        }
        
        return profileData;
    }
    
    
/*    
    {{  0  ,  0    ,  99  ,  98 ,  0   ,  255  },  7   ,  1  ,  STD_EVLOG ,&getEventLog                   , PUBLICACCESS },           ///< Std Event Log
    {{  0  ,  0    ,  99  ,  98 ,  1   ,  255  },  7   ,  1  ,  FRAUD_EVLOG ,&getEventLog                 , PUBLICACCESS },           ///< Faud Event Log
    {{  0  ,  0    ,  99  ,  98 ,  2   ,  255  },  7   ,  0  ,  DISCONNECT_CTRL_EVLOG ,&getEventLog       , PUBLICACCESS },           ///< Control Log
    {{  0  ,  0    ,  99  ,  98 ,  3   ,  255  },  7   ,  0  ,  MBUS_EVLOG ,&getEventLog                  , PUBLICACCESS },           ///< Mbus Event Log
    {{  0  ,  1    ,  24  ,  5  ,  0   ,  255  },  7  ,   1 , MBUS_CTRL_EVLOG1 ,&getEventLog              , PUBLICACCESS },
    {{  0  ,  2    ,  24  ,  5  ,  0   ,  255  },  7   ,  1 ,   MBUS_CTRL_EVLOG2       , &getEventLog     , PUBLICACCESS },           ///<  Mbus Control Log Channel 2
    {{  0  ,  3    ,  24  ,  5  ,  0   ,  255  },  7   ,  1 ,   MBUS_CTRL_EVLOG3       , &getEventLog     , PUBLICACCESS },           ///<  Mbus Control Log Channel 2
    {{  0  ,  4    ,  24  ,  5  ,  0   ,  255  },  7   ,  1  ,  MBUS_CTRL_EVLOG4  ,&getEventLog           , PUBLICACCESS },           ///<  Mbus Control Log Channel 4
    {{  1  ,  0    ,  99  , 97  ,  0   ,  255  },  7  ,  0  ,  POWER_FAIL_LOG ,&getEventLog               , PUBLICACCESS },           ///<  Power fail Log
*/    
    
    private List getLogbookData(Calendar from) throws IOException {
        List meterEvents = new ArrayList();
        if (DEBUG>=1) getCosemObjectFactory().getProfileGeneric(ObisCode.fromByteArray(LOGBOOK_PROFILE_LN)).getBuffer().printDataContainer();
        Logbook logbook = new Logbook(timeZone);
        Calendar to = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        if (fullLogbook == 0) {
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromByteArray(LOGBOOK_PROFILE_LN)).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.1.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.2.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.3.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.1.24.5.0.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.2.24.5.0.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.3.24.5.0.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.4.24.5.0.255")).readBufferAttr(from,to)));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("1.0.99.97.0.255")).readBufferAttr(from,to)));
        }
        else {
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromByteArray(LOGBOOK_PROFILE_LN)).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.1.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.2.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.3.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.1.24.5.0.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.2.24.5.0.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.3.24.5.0.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.4.24.5.0.255")).readBufferAttr()));
	        meterEvents.addAll(logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("1.0.99.97.0.255")).readBufferAttr()));
        }
        Collections.sort(meterEvents);
        return meterEvents;
    }
    
    
    private Calendar setCalendar(Calendar cal, DataStructure dataStructure,byte btype) throws IOException {
        
        Calendar calendar = (Calendar)cal.clone();
        
        if (dataStructure.getOctetString(0).getArray()[0] != -1)
              calendar.set(Calendar.YEAR,(((int)dataStructure.getOctetString(0).getArray()[0]&0xff)<<8)|
                                         (((int)dataStructure.getOctetString(0).getArray()[1]&0xff)));
        
        
        if (dataStructure.getOctetString(0).getArray()[2] != -1)
              calendar.set(Calendar.MONTH,((int)dataStructure.getOctetString(0).getArray()[2]&0xff)-1);
        
        
        if (dataStructure.getOctetString(0).getArray()[3] != -1)
              calendar.set(Calendar.DAY_OF_MONTH,((int)dataStructure.getOctetString(0).getArray()[3]&0xff));
        
        
        if (dataStructure.getOctetString(0).getArray()[5] != -1)
              calendar.set(Calendar.HOUR_OF_DAY,((int)dataStructure.getOctetString(0).getArray()[5]&0xff));
        else
              calendar.set(Calendar.HOUR_OF_DAY,0);
        
        
        if (btype == 0)
        {
            if (dataStructure.getOctetString(0).getArray()[6] != -1)
                  calendar.set(Calendar.MINUTE,(((int)dataStructure.getOctetString(0).getArray()[6]&0xff)/(getProfileInterval()/60))*(getProfileInterval()/60));
            else
                  calendar.set(Calendar.MINUTE,0);
            
            calendar.set(Calendar.SECOND,0);
        }
        else
        {
            if (dataStructure.getOctetString(0).getArray()[6] != -1)
                  calendar.set(Calendar.MINUTE,((int)dataStructure.getOctetString(0).getArray()[6]&0xff));
            else
                  calendar.set(Calendar.MINUTE,0);
            
            if (dataStructure.getOctetString(0).getArray()[7] != -1)
                  calendar.set(Calendar.SECOND,((int)dataStructure.getOctetString(0).getArray()[7]&0xff));
            else
                  calendar.set(Calendar.SECOND,0);
        }
        
        // if DSA, add 1 hour
        if (dataStructure.getOctetString(0).getArray()[11] != -1)
           if ((dataStructure.getOctetString(0).getArray()[11] & (byte)0x80) == 0x80)
               calendar.add(Calendar.HOUR_OF_DAY,-1);
        
        return calendar;
        
    } // private void setCalendar(Calendar calendar, DataStructure dataStructure,byte bBitmask)
    
    private void buildProfileData(byte bNROfChannels, DataContainer dataContainer,ProfileData profileData)  throws IOException
    {
        byte bDOW;
        Calendar calendar=null,calendarEV=null;
        int i,t;
        boolean currentAdd=true,previousAdd=true;
        IntervalData previousIntervalData=null,currentIntervalData;

        if (dataContainer.getRoot().element.length == 0)
           throw new IOException("No entries in object list.");
        
        if (requestTimeZone != 0)
            calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
        else
            calendar = ProtocolUtils.initCalendar(false,timeZone);

        if (DEBUG >=1) dataContainer.printDataContainer();
        //dataContainer.printDataContainer();
        
        for (i=0;i<dataContainer.getRoot().element.length;i++) { // for all retrieved intervals
            calendar = setCalendar(calendar,dataContainer.getRoot().getStructure(i),(byte)0x00);
            profileData.addInterval(getIntervalData(dataContainer.getRoot().getStructure(i), calendar));
        } // for (i=0;i<dataContainer.getRoot().element.length;i++) // for all retrieved intervals

    } // private void buildProfileData(byte bNROfChannels, DataContainer dataContainer)  throws IOException
    

    private IntervalData addIntervalData(IntervalData currentIntervalData,IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int current,i;
        for (i=0;i<currentCount;i++) {
            current = ((Number)currentIntervalData.get(i)).intValue()+((Number)previousIntervalData.get(i)).intValue();
            intervalData.addValue(new Integer(current));
        }
        return intervalData;
    }
    /*
     * VDEW status flags
     *
     */
    
    // appears only in the logbook
    protected static final int CLEAR_LOADPROFILE = 0x4000;
    protected static final int CLEAR_LOGBOOK = 0x2000;
    protected static final int END_OF_ERROR = 0x0400;
    protected static final int BEGIN_OF_ERROR = 0x0200;    
    protected static final int VARIABLE_SET = 0x0100;    
    
    // appears in the logbook and the intervalstatus
    protected static final int POWER_FAILURE = 0x0080;
    protected static final int POWER_RECOVERY = 0x0040;
    protected static final int DEVICE_CLOCK_SET_INCORRECT = 0x0020;  // Changed KV 12062003
    protected static final int DEVICE_RESET = 0x0010;
    protected static final int SEASONAL_SWITCHOVER = 0x0008;
    protected static final int DISTURBED_MEASURE = 0x0004;
    protected static final int RUNNING_RESERVE_EXHAUSTED = 0x0002;
    protected static final int FATAL_DEVICE_ERROR = 0x0001;
    
    private int map2IntervalStateBits(int protocolStatus) {
    	int eiStatus=0;
    		
		if ((protocolStatus&CLEAR_LOADPROFILE) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&CLEAR_LOGBOOK) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&END_OF_ERROR) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&BEGIN_OF_ERROR) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&VARIABLE_SET) != 0)
			eiStatus |= IntervalStateBits.CONFIGURATIONCHANGE; 
		if ((protocolStatus&DEVICE_CLOCK_SET_INCORRECT) != 0)
			eiStatus |= IntervalStateBits.SHORTLONG; 
		if ((protocolStatus&SEASONAL_SWITCHOVER) != 0)
			eiStatus |= IntervalStateBits.SHORTLONG; 
		if ((protocolStatus&FATAL_DEVICE_ERROR) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&DISTURBED_MEASURE) != 0)
			eiStatus |= IntervalStateBits.CORRUPTED; 
		if ((protocolStatus&POWER_FAILURE) != 0)
			eiStatus |= IntervalStateBits.POWERDOWN; 
		if ((protocolStatus&POWER_RECOVERY) != 0)
			eiStatus |= IntervalStateBits.POWERUP; 
		if ((protocolStatus&DEVICE_RESET) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
		if ((protocolStatus&RUNNING_RESERVE_EXHAUSTED) != 0)
			eiStatus |= IntervalStateBits.OTHER; 
    	return eiStatus;
    } // private void map2IntervalStateBits(int protocolStatus)
    
    private IntervalData getIntervalData(DataStructure dataStructure,Calendar calendar) throws UnsupportedException, IOException {
        // Add interval data...
    	int eiStatus=map2IntervalStateBits(dataStructure.getInteger(1));
    	int protocolStatus=dataStructure.getInteger(1);
        IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()),eiStatus,protocolStatus);
        
        for (int t=0;t<getCapturedObjectsHelper().getNrOfCapturedObjects();t++)
            if (getCapturedObjectsHelper().isChannelData(t))
                intervalData.addValue(new Integer(dataStructure.getInteger(t)));
        return intervalData;
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    } 
     
     private ScalerUnit getRegisterScalerUnit(int channelId) throws IOException {
        if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSCOSEMGlobals.ICID_REGISTER) {
        	return getCosemObjectFactory().getRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        }
        else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSCOSEMGlobals.ICID_DEMAND_REGISTER) {
        	return getCosemObjectFactory().getDemandRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        }
        else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSCOSEMGlobals.ICID_EXTENDED_REGISTER) {
        	return getCosemObjectFactory().getExtendedRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        }
        else throw new IOException("EictZ3, getRegisterScalerUnit(), invalid channelId, "+channelId);
     }
     
/**
 * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
 * @exception IOException
 */
    public void setTime() throws IOException {
       Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
       calendar.add(Calendar.MILLISECOND,roundtripCorrection);           
       byte[] byteTimeBuffer = new byte[14];

       byteTimeBuffer[0]=TYPEDESC_OCTET_STRING;
       byteTimeBuffer[1]=12; // length
       byteTimeBuffer[2]=(byte)(calendar.get(calendar.YEAR) >> 8);
       byteTimeBuffer[3]=(byte)calendar.get(calendar.YEAR);
       byteTimeBuffer[4]=(byte)(calendar.get(calendar.MONTH)+1);
       byteTimeBuffer[5]=(byte)calendar.get(calendar.DAY_OF_MONTH);
       byte bDOW = (byte)calendar.get(calendar.DAY_OF_WEEK);
       byteTimeBuffer[6]=bDOW--==1?(byte)7:bDOW;
       byteTimeBuffer[7]=(byte)calendar.get(calendar.HOUR_OF_DAY);
       byteTimeBuffer[8]=(byte)calendar.get(calendar.MINUTE);
       byteTimeBuffer[9]=(byte)calendar.get(calendar.SECOND);
       byteTimeBuffer[10]=(byte)0xFF;
       byteTimeBuffer[11]=(byte)0xFF; //0x80;
       byteTimeBuffer[12]=(byte)0xFF; //0x00;
       if (timeZone.inDaylightTime(calendar.getTime()))
           byteTimeBuffer[13]=(byte)0x80; //0x00;
       else
           byteTimeBuffer[13]=(byte)0x00; //0x00;
       
       getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"),8,2, byteTimeBuffer);
        
    } // public void setTime() throws IOException
    
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        //dstFlag = clock.getDstFlag();
        return date;
    }
    
    private boolean verifyMeterID() throws IOException {
        if ((strID == null) || ("".compareTo(strID)==0) || (strID.compareTo(getSerialNumber()) == 0))
            return true;
        else 
            return false;
    }
    
    // KV 19012004
    private boolean verifyMeterSerialNR() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0) || (serialNumber.compareTo(getSerialNumber()) == 0))
            return true;
        else 
            return false;
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (configProgramChanges == -1)
           configProgramChanges = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        return configProgramChanges;
    } // public int requestConfigurationProgramChanges() throws IOException
    
    
    /**
     * This method requests for the COSEM object SAP.
     * @exception IOException
     */
    public void requestSAP() throws IOException {
        String devID =  (String)getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
        if ((strID != null) && ("".compareTo(strID) != 0)) {
            if (strID.compareTo(devID) != 0) {
                throw new IOException("DLMSSN, requestSAP, Wrong DeviceID!, settings="+strID+", meter="+devID);
            }
        }
    } // public void requestSAP() throws IOException
    
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
        }
        catch(DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }
        try {
        	
        	AARQ aarq = new AARQ(securityLevel,strPassword,getDLMSConnection());

            try {
    
               // requestSAP();  // KV 08102004 R/W denied to read SAP!!!!!
               //System.out.println("cache="+dlmsCache.getObjectList()+", confchange="+dlmsCache.getConfProgChange()+", ischanged="+dlmsCache.isChanged());
                try { // conf program change and object list stuff
                    int iConf;
                    
                    if (dlmsCache.getObjectList() != null) {
                        meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                        try {
                            
                            iConf = requestConfigurationProgramChanges();
                        }
                        catch(IOException e) {
                            iConf=0; //-1;
                            // KV_TO_DO temporary hardcode confchange to 0 and left out requesting objectlist from the exception
//                            logger.severe("DLMSZMD: Configuration change count not accessible, request object list.");
//                            requestObjectList();
//                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                        }

                        if (iConf != dlmsCache.getConfProgChange()) {
                        // KV 19112003 ************************** DEBUGGING CODE ********************************
                        //System.out.println("!!!!!!!!!! DEBUGGING CODE FORCED DLMS CACHE UPDATE !!!!!!!!!!");
                        //if (true) {
                        // ****************************************************************************   
                            logger.severe("DLMSZMD: Configuration changed, request object list.");
                            requestObjectList();           // request object list again from rtu
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                        }
                    }
                    else { // Cache not exist
                        logger.info("GenericGetSet: Cache does not exist, request object list.");
                        requestObjectList();
                        try {
                        	iConf = requestConfigurationProgramChanges();
                        }
                        catch(IOException e) {
                        	// KV_TO_DO temporary catch this exception 
                            iConf=0;
                        }
                      
                        dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                        dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                    }
                    
                    if (!verifyMeterID()) 
                        throw new IOException("DLMSLN7000, connect, Wrong DeviceID!, settings="+strID+", meter="+getSerialNumber());

                    // KV 19012004
                    if (!verifyMeterSerialNR()) 
                        throw new IOException("DLMSLN7000, connect, Wrong SerialNR!, settings="+serialNumber+", meter="+getSerialNumber());
                    
                    
                    if (extendedLogging >= 1) 
                       logger.info(getRegistersInfo(extendedLogging));
                    
                }
                catch(IOException e) {
                    throw new IOException("connect() error, "+e.getMessage());
                }
                
            }
            catch(IOException e) {
                throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            throw new IOException(e.getMessage());
        }
        
        validateSerialNumber(); // KV 19012004
        
    } // public void connect() throws IOException
    
    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.toString()+" "+uo.getObisCode().getDescription()+"\n");
        }
        //strBuff.append(getSerialNumber()+"\n");
        //strBuff.append(AXDRDecoder.decode(getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.1.1.255")).getData()).toString()+"\n"); // utility equipment identifier
        return strBuff.toString();
    }
    
    public void disconnect() throws IOException {
       try {
          if (dlmsConnection != null) getDLMSConnection().disconnectMAC();
       }
       catch(DLMSConnectionException e) {
          logger.severe("DLMSLN: disconnect(), "+e.getMessage());
       }
    } // public void disconnect() throws IOException
    
    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     * @exception IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    } // public void requestObjectList() throws IOException

    public String requestAttribute(short sIC,byte[] LN,byte bAttr) throws IOException {
        return doRequestAttribute(sIC,LN, bAttr).print2strDataContainer();
    } // public String requestAttribute(short sIC,byte[] LN,byte bAttr ) throws IOException
    
    
    private DataContainer doRequestAttribute(int classId,byte[] ln,int lnAttr) throws IOException {
       DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln),DLMSUtils.attrLN2SN(lnAttr),classId).getDataContainer(); 
       return dc;
    } // public DataContainer doRequestAttribute(short sIC,byte[] LN,byte bAttr ) throws IOException
    
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getSerialNumber();
        if ((sn != null) && (sn.compareTo(serialNumber) == 0)) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }
    
    public String getSerialNumber() throws IOException {
        if (serialnr==null) {
        	serialnr = AXDRDecoder.decode(getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.1.0.255")).getData()).getOctetString().stringValue();
        }
        return serialnr;
    } // public String getSerialNumber() throws IOException  
    
    public String getProtocolVersion() {
        return "$Revision: 1.39 $";
    }
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        if (version == null) {
           version = AXDRDecoder.decode(getCosemObjectFactory().getData(ObisCode.fromString("1.0.0.2.0.255")).getData()).getOctetString().stringValue();
    	}
        return version;
    }
    
    /** this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see AbstractMeterProtocol#validateProperties
     */    
    public void setProperties(Properties properties) throws MissingPropertyException , InvalidPropertyException {
        validateProperties(properties);
        //this.properties = properties;
    }
    
    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */    
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException
    {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            if ((strID != null) && (strID.length()>16)) throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            hDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            protocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            //iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            securityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            requestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            roundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            
            clientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","1").trim());
            serverUpperMacAddress=Integer.parseInt(properties.getProperty("iServerUpperMacAddress","17").trim());
            serverLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","17").trim());
            firmwareVersion=properties.getProperty("FirmwareVersion","ANY");
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            // KV 19012004 get the serialNumber
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));  
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","-1"));  
            connectionMode = Integer.parseInt(properties.getProperty("Connection","0")); // 0=HDLC, 1= TCP/IP
            
            //default obis code is the obiscode for the elec meter attached to the uart2 port
            loadProfileObisCode = properties.getProperty("LoadProfileObisCode","1.0.99.1.0.255");
            fullLogbook = Integer.parseInt(properties.getProperty("FullLogbook","0"));              
            informationFieldSize = Integer.parseInt(properties.getProperty("InformationFieldSize","-1"));
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
        return doGetRegister(name);
    }
    
    private String doGetRegister(String name) throws IOException {
        boolean classSpecified=false;
        if (name.indexOf(':') >= 0)
            classSpecified=true;
        DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            if (classSpecified){
            	return requestAttribute(ln.getDLMSClass(),ln.getLN(), (byte)ln.getOffset());
            } else {
               UniversalObject uo = getMeterConfig().getObject(ln);
               return getCosemObjectFactory().getGenericRead(uo).getDataContainer().print2strDataContainer();
            }
        } else if(name.indexOf("-") >= 0){	// you get a from/to
			DLMSObis ln2 = new DLMSObis(name.substring(0, name.indexOf("-")));
			if(ln2.isLogicalName()){
				String from = name.substring(name.indexOf("-")+1, name.indexOf("-", name.indexOf("-")+1));
				String to = name.substring(name.indexOf(from) + from.length() +1);
				if(ln2.getDLMSClass() == 7){
					return getCosemObjectFactory().getProfileGeneric(getMeterConfig().getObject(ln2).getObisCode()).getBuffer(convertStringToCalendar(from), convertStringToCalendar(to)).print2strDataContainer();
				} else{
					throw new NoSuchRegisterException("GenericGetSet,getRegister, register "+name+" is not a profile.");
				}
			} else {
				throw new NoSuchRegisterException("GenericGetSet,getRegister, register "+name+" does not exist.");
			}
        }else{
        	throw new NoSuchRegisterException("GenericGetSet,getRegister, register "+name+" does not exist.");
        }
    }
    
    private Calendar convertStringToCalendar(String strDate){
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.set(Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" ")))&0xFFFF,
				(Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))&0xFF) -1,
				Integer.parseInt(strDate.substring(0, strDate.indexOf("/")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":")))&0xFF,
				Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length()))&0xFF);
		return cal;
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */    
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        boolean classSpecified=false;
        if (name.indexOf(':') >= 0)
            classSpecified=true;
        DLMSObis ln = new DLMSObis(name);
        if ((ln.isLogicalName()) && (classSpecified)) {
           getCosemObjectFactory().getGenericWrite(ObisCode.fromByteArray(ln.getLN()),ln.getOffset(),ln.getDLMSClass()).write(convert(value));
        }
        else throw new NoSuchRegisterException("GenericGetSet, setRegister, register "+name+" does not exist.");    	
    }    

    
	byte[] convert(String s) throws IOException {
		if ((s.length() % 2) != 0)
			throw new IOException("String length is not a modulo 2 hex representation!");
		else {
			byte[] data = new byte[s.length()/2];
			for (int i=0;i<(s.length()/2);i++) {
				data[i] = (byte)Integer.parseInt(s.substring(i*2, (i*2)+2),16);
			}
			return data;
		}
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
        List result = new ArrayList();
        
        return result; 
    }
    
    /** this implementation returns an empty list
     * @return a list of strings
     */    
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("iServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("Connection");        
        result.add("LoadProfileObisCode");
        result.add("FullLogbook");
        result.add("InformationFieldSize");
        return result;
    }
    
    public int requestTimeZone() throws IOException {
       // All time reporting is UTC for the SL7000
       return (0);
    }
  
    public void setCache(Object cacheObject) {
        this.dlmsCache=(DLMSCache)cacheObject;
    }
    public Object getCache() {
        return dlmsCache;
    }
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
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
    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException,com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache)cacheObject;
            if (dc.isChanged()) {
                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
                RtuDLMS rtu = new RtuDLMS(rtuid);
                rtuCache.saveObjectList(dc.getObjectList());
                rtu.setConfProgChange(dc.getConfProgChange());
            }
        }
        else throw new com.energyict.cbo.BusinessException("invalid RtuId!");
    }
    
    public void release() throws IOException {
    }
    
    // implementation oh HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = 
              (HHUSignOn)new IEC1107HHUConnection(commChannel,hDLCTimeoutProperty,protocolRetriesProperty,300,0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn,nodeId);
    }
     public byte[] getHHUDataReadout() {
         return getDLMSConnection().getHhuSignOn().getDataReadout();   
     }
     
    public Logger getLogger() {
        return logger;
    }
    
    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }
    
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }
    
    public int getRoundTripCorrection() {
        return roundtripCorrection;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public boolean isRequestTimeZone() {
        return (requestTimeZone != 0);
    }
    
    /**
     * Getter for property cosemObjectFactory.
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }
    
    public String getFileName() {
        
        
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+calendar.get(Calendar.DAY_OF_MONTH)+"_"+strID+"_"+strPassword+"_"+serialNumber+"_"+serverUpperMacAddress+"_DLMSSL7000.cache";
    }    
    
    public StoredValues getStoredValues() {
        return (StoredValues)storedValuesImpl;
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	
    	
    	UniversalObject uo = getMeterConfig().findObject(obisCode);
    	if (uo.getClassID() == DLMSCOSEMGlobals.ICID_REGISTER) {
    		Register register = getCosemObjectFactory().getRegister(obisCode);
    		return new RegisterValue(obisCode,register.getQuantityValue());
    	}
    	else if (uo.getClassID() == DLMSCOSEMGlobals.ICID_DEMAND_REGISTER) {
    		DemandRegister register = getCosemObjectFactory().getDemandRegister(obisCode);
    		return new RegisterValue(obisCode,register.getQuantityValue());
    	}
    	else if (uo.getClassID() == DLMSCOSEMGlobals.ICID_EXTENDED_REGISTER) {
    		ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
    		return new RegisterValue(obisCode,register.getQuantityValue());
    	}
    	else if (uo.getClassID() == DLMSCOSEMGlobals.ICID_DISCONNECT_CONTROL) {
    		Disconnector register = getCosemObjectFactory().getDisconnector(obisCode);
    		return new RegisterValue(obisCode,""+register.getState());
    	}
        if (ocm == null)
            ocm = new ObisCodeMapper(getCosemObjectFactory());
        return ocm.getRegisterValue(obisCode);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
} // public class DLMSProtocolLN extends MeterProtocol
