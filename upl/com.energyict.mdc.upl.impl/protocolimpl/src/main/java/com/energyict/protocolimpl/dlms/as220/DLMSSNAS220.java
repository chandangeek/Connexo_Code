/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Base class that implements the DLMS SN (short name) protocol
 * <BR>
 * <B>@beginchanges</B><BR>
 *      KV 08042003 Initial version.<BR>
 *      KV 08102003 Save dstFlag when getTime() to be used in setTime()
 *      KV 14072004 DLMSMeterConfig made multithreaded! singleton pattern implementation removed!
 *      KV 20082004 Extended with obiscode mapping for register reading + start reengineering to use cosem package
 *      KV 30082004 Reengineered to use cosem package
 *@endchanges
 */


package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.LLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.HDLCConnection;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.protocolimpl.dlms.siemenszmd.StoredValuesImpl;

abstract public class DLMSSNAS220 implements DLMSCOSEMGlobals, MeterProtocol, HHUEnabler, ProtocolLink, CacheMechanism {

	private static final ObisCode	ENERGY_PROFILE_OBISCODE	= ObisCode.fromString("1.1.99.1.0.255");

    abstract protected void buildProfileData(byte bNROfChannels,ProfileData profileData,ScalerUnit[] scalerunit,List<LoadProfileCompactArrayEntry> values)  throws IOException;
    abstract protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;

	private boolean debug = false;

    private DLMSCache dlmsCache=new DLMSCache();

    protected String strID;
    protected String strPassword;

    protected int iTimeoutProperty;
    protected int iProtocolRetriesProperty;
    protected int iDelayAfterFailProperty;
    protected int iSecurityLevelProperty;
    protected int iRequestTimeZone;
    protected int iRoundtripCorrection;
    protected int iClientMacAddress;
    protected int iServerUpperMacAddress;
    protected int iServerLowerMacAddress;
    protected int iRequestClockObject;
    private String nodeId;
    private String serialNumber;
    private int iInterval=-1;
    private int iNROfIntervals=-1;
    private int extendedLogging;
    private ProtocolChannelMap channelMap;

    //private boolean boolAbort=false;

    private DLMSConnection dlmsConnection=null;
    private CosemObjectFactory cosemObjectFactory=null;
    private StoredValuesImpl storedValuesImpl=null;

    // lazy initializing
    private int iNumberOfChannels=-1;
    private int iMeterTimeZoneOffset=255;
    private int iConfigProgramChange=-1;

    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();

    // Added for MeterProtocol interface implementation
    private Logger logger=null;
    private TimeZone timeZone=null;
    //private Properties properties=null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    private int addressingMode;
    private int connectionMode;

    private byte[] aarqlowlevel={
            (byte)0xE6,(byte)0xE6,(byte)0x00,
            (byte)0x60,
            (byte)0x35,
            (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,
            (byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x02, //application context name
            (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80,
            (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};

    private byte[] aarqlowlevel_2={
            (byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
            (byte)0x01,
            (byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x06,  // dlms version nr
            (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x18,(byte)0x02,(byte)0x20,
            (byte)0xFF,(byte)0xFF};

    private byte[] aarqlowestlevel={
            (byte)0xE6,(byte)0xE6,(byte)0x00,
            (byte)0x60,
            (byte)0x1C, // bytes to follow
            (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,
            (byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x02, //application context name
            (byte)0xBE,(byte)0x0F,(byte)0x04,
            (byte)0x0D,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x18,(byte)0x02,
            (byte)0x20,(byte)0xFF,(byte)0xFF};

	/** Creates a new instance of DLMSSNAS220, empty constructor */
    public DLMSSNAS220() {

    }

	private String getDeviceID() {
        return "GEC";
    }

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

        dstFlag=-1;
        iNumberOfChannels = -1; // Lazy initializing
        iMeterTimeZoneOffset = 255; // Lazy initializing
        iConfigProgramChange = -1; // Lazy initializing

        try {
            cosemObjectFactory = new CosemObjectFactory(this);
            storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
            // KV 19092003 set forcedelay to 100 ms for the optical delay when using HHU?
            if (connectionMode==0) {
				dlmsConnection=new HDLCConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress,iServerUpperMacAddress,addressingMode);
			} else if (connectionMode==1) {
				dlmsConnection=new TCPIPConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
			} else if (connectionMode==2) {
				dlmsConnection=new CosemPDUConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
			} else if (connectionMode==3) {
				dlmsConnection=new LLCConnection(inputStream,outputStream,iTimeoutProperty,100,iProtocolRetriesProperty,iClientMacAddress,iServerLowerMacAddress);
			}
        }
        catch(DLMSConnectionException e) {
            //logger.severe ("DLMSSN init(...), "+e.getMessage());
            throw new IOException(e.getMessage());
        }

        iInterval=-1;
        iNROfIntervals=-1;

        //boolAbort = false;
    }

    /** Subclasses may override
     * @return the current profileinterval in seconds
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        if (iInterval == -1) {
           iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    } // public int getProfileInterval() throws UnsupportedException, IOException

    /** this implementation throws UnSupportedException. Subclasses may override
     * @return the number of channels
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (iNumberOfChannels == -1) {
             meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
            iNumberOfChannels = meterConfig.getNumberOfChannels();
        }
        return iNumberOfChannels;
    } // public int getNumberOfChannels()  throws IOException

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    private ScalerUnit getMeterDemandRegisterScalerUnit(int iChannelNR) throws IOException {
        ObisCode obisCode = meterConfig.getMeterDemandObject(iChannelNR).getObisCode();
        return getCosemObjectFactory().getCosemObject(obisCode).getScalerUnit();
        //return doGetMeterReadingScalerUnit(uo.getBaseName(), uo.getScalerAttributeOffset());
    }

    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
        }
        catch(DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }
        try {

            requestApplAssoc(iSecurityLevelProperty);
            try {
                requestSAP();
                //System.out.println("cache="+dlmsCache.getObjectList()+", confchange="+dlmsCache.getConfProgChange()+", ischanged="+dlmsCache.isChanged());
                try { // conf program change and object list stuff
                    int iConf;
                    if (dlmsCache.getObjectList() != null) {
                        meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                        try {
                            iConf = requestConfigurationProgramChanges();
                        }
                        catch(IOException e) {
                            iConf=-1;
                            logger.severe("DLMSSNAS220 Configuration change count not accessible, request object list.");
                            requestObjectList();
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                        }

                        if (iConf != dlmsCache.getConfProgChange()) {
                        // KV 19112003 ************************** DEBUGGING CODE ********************************
                        //System.out.println("!!!!!!!!!! DEBUGGING CODE FORCED DLMS CACHE UPDATE !!!!!!!!!!");
                        //if (true) {
                        // ****************************************************************************
                            logger.severe("DLMSSNAS220 Configuration changed, request object list.");
                            requestObjectList();           // request object list again from rtu
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                        }
                    }
                    else { // Cache not exist
                        logger.info("DLMSSNAS220 Cache does not exist, request object list.");
                        requestObjectList();
                        try {
                            iConf = requestConfigurationProgramChanges();
                            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                        }
                        catch(IOException e) {
                            iConf=-1;
                        }
                    }

                    if (extendedLogging >= 1) {
						logger.info(getRegistersInfo(extendedLogging));
					}

                }
                catch(IOException e) {
                    e.printStackTrace();
                    throw new IOException("connect() error, "+e.getMessage());
                }

            }
            catch(IOException e) {
                e.printStackTrace();
            	throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        	throw new IOException(e.getMessage());
        }

        validateSerialNumber(); // KV 19012004

    } // public void connect() throws IOException

    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        Iterator it;

        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString()+" "+uo.getObisCode().getDescription()+"\n");
        }

        if (getDeviceID().compareTo("EIT")!=0) {
            // all billing points values...
            strBuff.append("********************* Objects captured into billing points *********************\n");
            it = getCosemObjectFactory().getStoredValues().getProfileGeneric().getCaptureObjects().iterator();
            while(it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject)it.next();
                strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (billing point)\n");
            }
        }

        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
        }

        return strBuff.toString();
    }

    /**
     * This method initiates the MAC disconnect for the HDLC layer.
     * @exception IOException
     */
    public void disconnect() throws IOException {
        try {
            if (getDLMSConnection() != null) {
				getDLMSConnection().disconnectMAC();
			}
        }
        catch(DLMSConnectionException e) {
            logger.severe("DLMSSNAS220AS220: disconnect(), "+e.getMessage());
            //throw new IOException(e.getMessage());
        }

    } // public void disconnect() throws IOException

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     * @exception IOException
     */
    public void setTime() throws IOException {
        Calendar calendar=null;
        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
				calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
			} else if (dstFlag == 1) {
				calendar = ProtocolUtils.getCalendar(true,requestTimeZone());
			} else {
				throw new IOException("setTime(), dst flag is unknown! setTime() before getTime()!");
			}
        } else {
			calendar = ProtocolUtils.initCalendar(false,getTimeZone());
		}

        calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);
        doSetTime(calendar);
    } // public void setTime() throws IOException


    private void doSetTime(Calendar calendar) throws IOException {
        //byte[] responseData;
        byte[] byteTimeBuffer = new byte[15];

        byteTimeBuffer[0]=1;
        byteTimeBuffer[1]=TYPEDESC_OCTET_STRING;
        byteTimeBuffer[2]=12; // length
        byteTimeBuffer[3]=(byte)(calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[4]=(byte)calendar.get(Calendar.YEAR);
        byteTimeBuffer[5]=(byte)(calendar.get(Calendar.MONTH)+1);
        byteTimeBuffer[6]=(byte)calendar.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte)calendar.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[7]=bDOW--==1?(byte)7:bDOW;
        byteTimeBuffer[8]=(byte)calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[9]=(byte)calendar.get(Calendar.MINUTE);
        byteTimeBuffer[10]=(byte)calendar.get(Calendar.SECOND);
        byteTimeBuffer[11]=(byte)0xFF;
        byteTimeBuffer[12]=(byte)0x80;
        byteTimeBuffer[13]=0x00;

        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
				byteTimeBuffer[14]=0x00;
			} else if (dstFlag == 1) {
				byteTimeBuffer[14]=(byte)0x80;
			} else {
				throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
			}
        }
        else {
            if (getTimeZone().inDaylightTime(calendar.getTime())) {
				byteTimeBuffer[14]=(byte)0x80;
			} else {
				byteTimeBuffer[14]=0x00;
			}
        }

        getCosemObjectFactory().getGenericWrite((short)meterConfig.getClockSN(),TIME_TIME).write(byteTimeBuffer);

    } // private void doSetTime(Calendar calendar)

    /**
     * Method that requests the time/date in the remote meter.
     * @return Date representing the time/date of the remote meter.
     * @exception IOException
     */
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        dstFlag = clock.getDstFlag();
        return date;
    } // public Date getTime() throws IOException

    private boolean requestDaylightSavingEnabled() throws IOException {
       return getCosemObjectFactory().getClock().isDsEnabled();
    } // private boolean requestDaylightSavingEnabled() throws IOException

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     * @exception IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    } // public void requestObjectList() throws IOException

    /**
     * This method requests for the COSEM object SAP.
     * @exception IOException
     */
    public void requestSAP() throws IOException {
        String devID =  (String)getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
        if ((strID != null) && ("".compareTo(strID) != 0)) {
            if (strID.compareTo(devID) != 0) {
                throw new IOException("DLMSSNAS220, requestSAP, Wrong DeviceID!, settings="+strID+", meter="+devID);
            }
        }
    } // public void requestSAP() throws IOException

    // KV 19012004
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) {
			return;
		}
        String sn = getSerialNumber();
        if ((sn != null) && (sn.compareTo(serialNumber) == 0)) {
			return;
		}
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }

    /**
     * This method requests for the COSEM object Logical Name register.
     * @exception IOException
     */
    private String requestLNREG() throws IOException {
        return getCosemObjectFactory().getData(LNREG_OBJECT_SN).getString();
    } // public String requestLNREG() throws IOException


    /**
     * This method requests for the COSEM object Logical Name register.
     * @exception IOException
     */
    private String requestAttribute(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).getDataContainer().toString();
    } // public void requestAttribute(int iBaseName,int iOffset) throws IOException


    public String getProtocolVersion() {
    	String rev = "$Revision: 33703 $"+" - "+"$Date: 2009-06-02 17:34:52 +0200 (di, 02 jun 2009) $";
    	String manipulated = "Revision "+rev.substring(rev.indexOf("$Revision: ")+"$Revision: ".length(), rev.indexOf("$ -"))
    						+"at "
    						 +rev.substring(rev.indexOf("$Date: ")+"$Date: ".length(), rev.indexOf("$Date: ")+"$Date: ".length()+19);
    	return manipulated;
    }

    /**
     * This method requests for the version string.
     * @return String representing the version.
     * @exception IOException
     */
    public String getFirmwareVersion() throws IOException,UnsupportedException {
        StringBuffer strBuff = new StringBuffer();
    	UniversalObject uo = getMeterConfig().getVersionObject();
        byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(),uo.getValueAttributeOffset()).getResponseData();

        Array array = AXDRDecoder.decode(responsedata).getArray();
        Structure structure = array.getDataType(0).getStructure();
        strBuff.append(ProtocolUtils.outputHexString(structure.getNextDataType().getOctetString().getOctetStr()));
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().intValue());
        strBuff.append(", "+structure.getNextDataType().longValue());
        return strBuff.toString();


    } // public String getFirmwareVersion()

    private String getSerialNumber() throws IOException {
        UniversalObject uo = getMeterConfig().getSerialNumberObject();
        byte[] responsedata = getCosemObjectFactory().getGenericRead(uo.getBaseName(),uo.getValueAttributeOffset()).getResponseData();
        byte[] octetStr = AXDRDecoder.decode(responsedata).getOctetString().getOctetStr();
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<octetStr.length;i++) {
        	strBuff.append(ProtocolUtils.buildStringHex(octetStr[i]&0xff, 2));
        }
        return strBuff.toString();
    } // public String getSerialNumber()

    /**
     * This method requests for the NR of intervals that can be stored in the memory of the remote meter.
     * @return NR of intervals that can be stored in the memory of the remote meter.
     * @exception IOException
     */
    private int getNROfIntervals() throws IOException {
        if (iNROfIntervals == -1) {
            iNROfIntervals = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getProfileEntries();
        } // if (iNROfIntervals == -1)
        return iNROfIntervals;
    } // private int getNROfIntervals() throws IOException


    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        int iNROfIntervals = getNROfIntervals();
        int iInterval = getProfileInterval()/60;
        Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
        fromCalendar.add(Calendar.MINUTE,(-1)*iNROfIntervals*iInterval);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar,toCalendar,includeEvents);
    }

    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar, boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte)getNumberOfChannels(); 	//GN |13052008| otherwise this stays at -1
        return doGetDemandValues(fromCalendar,toCalendar,bNROfChannels,includeEvents);
    }



    private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels, boolean includeEvents) throws IOException {
        ProfileData profileData;
        ScalerUnit[] scalerunit;


        profileData = new ProfileData();
        scalerunit = new ScalerUnit[bNROfChannels];
        for (int i=0;i<bNROfChannels;i++) {
            scalerunit[i] = getMeterDemandRegisterScalerUnit(i);
            ChannelInfo channelInfo = new ChannelInfo(i,"dlms"+getDeviceID()+"_channel_"+i,scalerunit[i].getUnit());

            if (isDebug()) {
				System.out.println("KV_DEBUG> "+meterConfig.getChannelObject(i).toStringCo());
			}

            if (meterConfig.getChannelObject(i).isCapturedObjectCumulative()) {

                if (meterConfig.getChannelObject(i).isCapturedObjectPulses()) {
                	if(channelMap != null){
                		channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null)?channelMap.getProtocolChannel(i).getWrapAroundValue():BigDecimal.valueOf(Long.MAX_VALUE));
                	} else {
                		channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(Long.MAX_VALUE));
                		if (isDebug()) {
							System.out.println("KV_DEBUG> channel "+i+" is cumulative 64 bit");
						}
                	}
                }
                else {
                	if(channelMap != null){
                		channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null)?channelMap.getProtocolChannel(i).getWrapAroundValue():BigDecimal.valueOf(2^32));
                	} else {
                		channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(2^32));
                		if (isDebug()) {
							System.out.println("KV_DEBUG> channel "+i+" is cumulative 32 bit");
						}
                	}
                }
            }
            profileData.addChannel(channelInfo);
        }

        ProfileGeneric pg = getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE);
        byte[] data = pg.getBufferData(fromCalendar, toCalendar);
        // decode the compact array here and convert to a universallist...
        LoadProfileCompactArray o = new LoadProfileCompactArray();
        o.parse(data);
        List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = o.getLoadProfileCompactArrayEntries();

        buildProfileData(bNROfChannels,profileData,scalerunit,loadProfileCompactArrayEntries);

        if (includeEvents) {
            EventLogs eventLogs = new EventLogs(this);
        	List<MeterEvent> meterEvents = eventLogs.getEventLog(fromCalendar, toCalendar);
        	profileData.setMeterEvents(meterEvents);
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval()/60);
        }

        profileData.sort();

        if (isDebug()) {
			System.out.println(profileData);
		}

        return profileData;

    } // private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException



    /**
     * This method can be used to set a specific attribute in anremote meter object.
     * @param sLNOBIS Index to the Long name OBIS reference.
     * @param sOffset Offset to the attribute.
     * @param data Byte array to send.
     * @exception IOException
     */
    private void setValue(String str,short sOffset,byte[] data) throws IOException {
        DLMSObis dlmsObis = new DLMSObis(str);
        getCosemObjectFactory().getGenericWrite((short)meterConfig.getObject(dlmsObis).getBaseName(),(dlmsObis.getOffset()-1)*8).write(data);
    } // public void setValue(...) throws IOException

	private void requestApplAssoc(int iLevel) throws IOException {
		doRequestApplAssoc(iLevel == 0 ? aarqlowestlevel : getLowLevelSecurity());
	}

	protected byte[] getLowLevelSecurity() {
		return AS220AAREUtil.buildaarq(aarqlowlevel, aarqlowlevel_2, strPassword);
	}

    private void doRequestApplAssoc(byte[] aarq) throws IOException {
        byte[] responseData;
        responseData = getDLMSConnection().sendRequest(aarq);
        AS220AAREUtil.checkAARE(responseData);
        if (isDebug()) {
			ProtocolUtils.printResponseData(responseData);
		}
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
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            // KV 19012004 get the serialNumber
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","-1"));
            connectionMode = Integer.parseInt(properties.getProperty("Connection","0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection
            if(properties.getProperty("ChannelMap", "").equalsIgnoreCase("")){
            	channelMap = null;
            } else {
            	channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap"));
            }
            doValidateProperties(properties);
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, "+e.getMessage());
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
		DLMSObis ln = new DLMSObis(name);
		if (ln.isLogicalName()) {
			String str = requestAttribute(meterConfig.getObject(ln).getBaseName(), (short) ((ln.getOffset() - 1) * 8));
			return str;
		} else if (name.compareTo("PROGRAM_CONF_CHANGES") == 0) {
			return String.valueOf(requestConfigurationProgramChanges());
		} else if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
			requestClockObject();
			return null;
		} else {
			throw new NoSuchRegisterException("DLMS,getRegister, register " + name + " does not exist.");
		}
	}

    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
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
    public List<String> getRequiredKeys() {
        List<String> result = new ArrayList<String>();
        return result;
    }

    /** this implementation returns an empty list
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<String>();
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
        result.add("ChannelMap");
        result.add("Connection");
        return result;
    }

    private void requestClockObject() {
        if (iRequestClockObject == 1) {
            try{logger.severe("DLMSSNAS220 Clock time                       : "+getTime());}catch(IOException e){logger.severe("time attribute error");}
            //try{logger.severe ("DLMSSNAS220 Clock time_zone                  : "+requestTimeZone());}catch(IOException e){logger.severe ("time_zone attribute error");}
            try{logger.severe("DLMSSNAS220 Clock time_zone                  : "+requestAttributeLong(meterConfig.getClockSN(),TIME_TIME_ZONE));}catch(IOException e){logger.severe("time_zone attribute error");}
            try{logger.severe("DLMSSNAS220 Clock status                     : "+requestAttributeLong(meterConfig.getClockSN(),TIME_STATUS));}catch(IOException e){logger.severe("status attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_begin     : "+requestAttributeString(meterConfig.getClockSN(),TIME_DS_BEGIN));}catch(IOException e){logger.severe("DS begin attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_end       : "+requestAttributeString(meterConfig.getClockSN(),TIME_DS_END));}catch(IOException e){logger.severe("DS end attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_savings_deviation : "+requestAttributeLong(meterConfig.getClockSN(),TIME_DS_DEVIATION));}catch(IOException e){logger.severe("DS deviation attribute error");}
            try{logger.severe("DLMSSNAS220 Clock daylight_saving_enabled    : "+requestDaylightSavingEnabled());}catch(IOException e){logger.severe("DS enebled attribute error");}

        } // if (iRequestClockObject == 1)

    } // private void requestClockObject()

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
			iConfigProgramChange = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
		}
        return iConfigProgramChange;
    } // public int requestConfigurationProgramChanges() throws IOException

    protected int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
			iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
		}
        return iMeterTimeZoneOffset;
    } // protected int requestTimeZone() throws IOException

    private long requestAttributeLong(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).getValue();
    } // private long requestAttributeLong(int iBaseName,int iOffset) throws IOException

    private String requestAttributeString(int iBaseName,int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName,iOffset).toString();
    } // private String requestAttributeString(int iBaseName,int iOffset) throws IOException

    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    public TimeZone getTimeZone() {
        return timeZone;
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
        } else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
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
        } else {
			throw new com.energyict.cbo.BusinessException("invalid RtuId!");
		}
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+calendar.get(Calendar.DAY_OF_MONTH)+"_"+strID+"_"+strPassword+"_"+serialNumber+"_"+iServerUpperMacAddress+"_DLMSSNAS220.cache";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel,false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
              new IEC1107HHUConnection(commChannel,iTimeoutProperty,iProtocolRetriesProperty,300,0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn,nodeId);
    }
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    public void release() throws IOException {

    }

    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Getter for property cosemObjectFactory.
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    public StoredValues getStoredValues() {
        return storedValuesImpl;
    }

    public boolean isDebug() {
		return debug;
	}

    public void setDebug(boolean debug) {
		this.debug = debug;
	}

} // public class DLMSSNAS220

