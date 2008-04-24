/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader;
import com.energyict.genericprotocolimpl.common.tou.CosemActivityCalendarBuilder;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.amrimpl.RtuRegisterReadingImpl;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.dlms.HDLCConnection;

/**
 * @author gna
 *
 */
public class IskraMx37x implements GenericProtocol, ProtocolLink, CacheMechanism, Messaging{
	
	private int DEBUG = 0;
	
    private Logger 					logger;
    private Properties 				properties;
    private CommunicationProfile 	communicationProfile;
	private Link 					link;	
    private DLMSConnection        	dlmsConnection;
    private CosemObjectFactory 		cosemObjectFactory;
    private SecureConnection		secureConnection;
    private Rtu                    	rtu;
    private Cache 					dlmsCache;
    private DLMSMeterConfig 		meterConfig;
    private Object 					source;
    private ObisCodeMapper 			ocm = null;
    private CapturedObjects[] 		capturedObjects = {null, null, null, null, null};	// max. 5 (1E-meter + 4MBus-meters)
    private MbusDevice[]			mbusDevices = {null, null, null, null};				// max. 4 MBus meters
    public static ScalerUnit[] 		demandScalerUnits = {new ScalerUnit(0,30), new ScalerUnit(0,0), new ScalerUnit(0,0), new ScalerUnit(0,0), new ScalerUnit(0,0)};
    
    private int iHDLCTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
	private int iSecurityLevelProperty;
	private int iRequestTimeZone;
	private int iRoundtripCorrection;
	private int extendedLogging;
	private int addressingMode;
	private int connectionMode;
	private int numberOfChannels[] = {-1, -1, -1, -1, -1};
	private int configProgramChanges;
	private int iInterval[] = {-1, -1, -1, -1, -1};
	private int metertype;
	private int dataContainerOffset = -1;
	private int deviation = -1;
	
	private long maxValue = -1;
	
    private String strID;
	private String strPassword;
	private String firmwareVersion;
	private String nodeId;
	private String serialNumber;
	private String rtuType;
	private String version;
	private String serialnr = null;
	private String devID = null;
	
    private ObisCode genericProfile1 		= 	ObisCode.fromString("1.0.99.1.0.255");		// mostly considered as intervalProfile
    private ObisCode genericProfile2 		= 	ObisCode.fromString("1.0.99.2.0.255");		// mostly considered as MBus profile of daily profile
    private ObisCode genericProfile3 		= 	ObisCode.fromString("1.0.98.1.0.255");		// mostly considered as monthly or daily profile
    private ObisCode genericProfile4 		=	ObisCode.fromString("1.0.98.2.0.255");		// mostly considered as daily or monthly profile
    private ObisCode loadProfileObisCode97 	= 	ObisCode.fromString("1.0.99.97.0.255");
    private ObisCode breakerObisCode 		= 	ObisCode.fromString("0.0.128.30.21.255");
    private ObisCode eventLogObisCode 		= 	ObisCode.fromString("1.0.99.98.0.255");
    private ObisCode mbusScalerUnit 		=	ObisCode.fromString("0.1.128.50.0.255");
    private ObisCode deviceLogicalName		= 	ObisCode.fromString("0.0.42.0.0.255");
    private ObisCode clock					=	ObisCode.fromString("0.0.1.0.0.255");
    private ObisCode status 				= 	ObisCode.fromString("1.0.96.240.0.255");
    private ObisCode endOfBilling			=	ObisCode.fromString("0.0.15.0.0.255");
    private ObisCode endOfCapturedObjects	=	ObisCode.fromString("0.0.15.1.0.255");
    private ObisCode mbusPrimaryAddress		= 	ObisCode.fromString("0.1.128.50.20.255");
    private ObisCode mbusCustomerID			= 	ObisCode.fromString("0.1.128.50.21.255");
    private ObisCode crGroupID				= 	ObisCode.fromString("0.0.128.62.0.255");
    private ObisCode crStartDate			= 	ObisCode.fromString("0.0.128.62.1.255");
    private ObisCode crDuration				= 	ObisCode.fromString("0.0.128.62.2.255");
    private ObisCode crPowerLimit			= 	ObisCode.fromString("0.0.128.62.3.255");
    private ObisCode crMeterGroupID			= 	ObisCode.fromString("0.0.128.62.6.255");
    private ObisCode contractPowerLimit		=	ObisCode.fromString("0.0.128.61.1.255");
    private ObisCode dailyObisCode 			= 	null;
    private ObisCode monthlyObisCode 		= 	null;
    private ObisCode loadProfileObisCode 	= 	null;
    private ObisCode[] mbusLProfileObisCode	=	{null, null, null, null};
    
    private static String CONNECT 			= "CONNECT";
    private static String DISCONNECT 		= "DISCONNECT";
    private static String ONDEMAND 			= "ONDEMAND";
    private static String CODERED 			= "CODERED";
    private static String CODERED_END		= "END_CODERED";
    private static String CONPOWERLIMIT		= "Contractual Power Limit (W)";
    private static String CODERED_LIMIT 	= "CodeRed Power Limit (W)";
    private static String CODERED_START_DT	= "CodeRed StartDate (dd/mm/yyyy HH:MM:SS)";
    private static String CODERED_STOP_DT	= "CodeRed EndDate (dd/mm/yyyy HH:MM:SS)";
    private static String TOU = "TOU";

    private static final int ELECTRICITY 	= 0x00;
    private static final int MBUS 			= 0x01;
    
    private final static String CONNECT_LOAD 	= "connectLoad";
    private final static String DISCONNECT_LOAD = "disconnectLoad";
    private final static String RTU_TYPE 		= "RtuType";
    
//    private List messages = new ArrayList(9);
    
    private byte[] connectMsg 				= new byte[] { DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x01 };
    private byte[] disconnectMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x00 };
    private byte[] contractPowerLimitMsg 	= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crPowerLimitMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crDurationMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crMeterGroupIDMsg 		= new byte[] { DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0 };
    private byte[] crGroupIDMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0 };
    
    private final int PROFILE_STATUS_DEVICE_DISTURBANCE=0x01;
    private final int PROFILE_STATUS_RESET_CUMULATION=0x10;
    private final int PROFILE_STATUS_DEVICE_CLOCK_CHANGED=0x20;
    private final int PROFILE_STATUS_POWER_RETURNED=0x40;
    private final int PROFILE_STATUS_POWER_FAILURE=0x80;
    
    private final static String AUTO_CREATE_ERROR_1 =
        "No automatic meter creation: no property RtuType defined.";

    private final static String AUTOCREATE_ERROR_2 =
        "No automatic meter creation: property RtuType has no prototype.";

    private final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";

	/**
	 * 
	 */
	public IskraMx37x() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String startDate = new String("18/04/2008 11:39:40");
		
    	Calendar cal = Calendar.getInstance();
    	Date date = new Date();
    	
    	cal.set(Calendar.DATE, Integer.valueOf(startDate.substring(0, startDate.indexOf("/"))));
    	cal.set(Calendar.MONTH, (Integer.valueOf(startDate.substring(startDate.indexOf("/") + 1, startDate.lastIndexOf("/")))) - 1);
    	cal.set(Calendar.YEAR, Integer.valueOf(startDate.substring(startDate.lastIndexOf("/") + 1, startDate.indexOf(" "))));
    	
    	cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(startDate.substring(startDate.indexOf(" ") + 1, startDate.indexOf(":"))));
    	cal.set(Calendar.MINUTE, Integer.valueOf(startDate.substring(startDate.indexOf(":") + 1, startDate.lastIndexOf(":"))));
    	cal.set(Calendar.SECOND, Integer.valueOf(startDate.substring(startDate.lastIndexOf(":") + 1, startDate.length())));
    	
    	System.out.println(cal.getTime());
		
	}

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.logger = logger;
		this.communicationProfile = scheduler.getCommunicationProfile();
		this.link = link;
		
		rtu = scheduler.getRtu();
		validateProperties();
		init(link.getInputStream(),link.getOutputStream());
		
		connect();
		
        try {
        	
        	checkMbusDevices();
        	checkConfiguration();
        	stopCacheMechanism();
        	
        	// Set clock ... if necessary
        	if( communicationProfile.getWriteClock() ) {
        		setTime();
        	}
        	
        	// Read profiles and events ... if necessary
    		if( (communicationProfile.getReadDemandValues()) && (communicationProfile.getReadMeterEvents()) ){
    			getProfileData(rtu.getLastLogbook(), true, loadProfileObisCode);
    			// no events on the MBus meters
    			getProfileData(mbusDevices[0].getMbus().getLastReading(), false, mbusLProfileObisCode[0]);
    		}
    		else if( (communicationProfile.getReadDemandValues()) && !(communicationProfile.getReadMeterEvents()) ){
    			getProfileData(rtu.getLastLogbook(), false, loadProfileObisCode);
    			getProfileData(mbusDevices[0].getMbus().getLastReading(), false, mbusLProfileObisCode[0]);
    		}
    		
    		// Read registers ... if necessary
    		if( communicationProfile.getReadMeterReadings() ) {
    			getRegisters(ELECTRICITY);
    			getRegisters(MBUS);
    		}
    		
    		// Send messages ... if there are messages
    		if( communicationProfile.getSendRtuMessage() ){
    			sendMeterMessages();
    			mbusDevices[0].sendMeterMessages(this);
    		}
        	
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/** Short notation for MeteringWarehouse.getCurrent() */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

	private void checkConfiguration() throws IOException {
		byte[] dailyByte = {0, 0, 0, 0, -1, -1, -1, -1, -1};
		byte[] monthlyByte = {0, 0, 0, 0, -1, -1, -1, 1, -1};
		
		if( dlmsCache.getGenericInterval1() == -1 )
			dlmsCache.setGenericInterval1(getCosemObjectFactory().getProfileGeneric(genericProfile1).getCapturePeriod());
		if( dlmsCache.getGenericInterval2() == -1 )
			dlmsCache.setGenericInterval2(getCosemObjectFactory().getProfileGeneric(genericProfile2).getCapturePeriod());
		if( dlmsCache.getGenericInterval3() == null )
			dlmsCache.setGenericInterval3(getCosemObjectFactory().getSingleActionSchedule(endOfBilling).getExecutionTime().getBEREncodedByteArray());
		if( dlmsCache.getGenericInterval4() == null )
			dlmsCache.setGenericInterval4(getCosemObjectFactory().getSingleActionSchedule(endOfCapturedObjects).getExecutionTime().getBEREncodedByteArray());
		
		if (rtu.getIntervalInSeconds() == dlmsCache.getGenericInterval1())
			loadProfileObisCode = genericProfile1;
		else if (rtu.getIntervalInSeconds() == dlmsCache.getGenericInterval2())
			loadProfileObisCode = genericProfile2;
		
		for (int i = 0; i < mbusCount(); i++){
			if (mbusDevices[i] != null){
				if (mbusDevices[i].getMbus().getIntervalInSeconds() == dlmsCache.getGenericInterval1()){
					mbusLProfileObisCode[i] = genericProfile1;
				}
				else if (mbusDevices[i].getMbus().getIntervalInSeconds() == dlmsCache.getGenericInterval2()){
					mbusLProfileObisCode[i] = genericProfile2;
				}
			}
		}
		
		if (isDailyArray(new Array(dlmsCache.getGenericInterval3(),0,0))){
			dailyObisCode = genericProfile3;
		} else if (isDailyArray(new Array(dlmsCache.getGenericInterval4(),0,0))){
			dailyObisCode = genericProfile4;
		} else if (dlmsCache.getGenericInterval2() == 84600){
			dailyObisCode = genericProfile2;
		} else
			throw new IOException("Iskra Mx37x, checkConfiguration, no dailyProfile configured for meter " + strID);
		
		if (isMonthlyArray(new Array(dlmsCache.getGenericInterval3(),0,0))){
			monthlyObisCode = genericProfile3;
		} else if (isMonthlyArray(new Array(dlmsCache.getGenericInterval4(),0,0))){
			monthlyObisCode = genericProfile4;
		} else
			throw new IOException("Iskra Mx37x, checkConfiguration, no monthlyProfile configured for meter " + strID);

		ObisCodeMapper.setDailyObisCode(dailyObisCode);
        ObisCodeMapper.setMonthlyObisCode(monthlyObisCode);
	}
	
	private boolean isDailyArray(Array genericInterval) {
		byte[] time = genericInterval.getDataType(0).getStructure().getDataType(0).getOctetString().getOctetStr();
		byte[] date = genericInterval.getDataType(0).getStructure().getDataType(1).getOctetString().getOctetStr();
		if((time[0]+time[1]+time[2]+time[3]) != 0x00)
			return false;
		
		if((date[0]==-1)&&(date[1]==-1)&&(date[2]==-1)&&(date[3]==-1)&&(date[4]==-1))
			return true;
		else
			return false;
	}
	
	private boolean isMonthlyArray(Array genericInterval){
		byte[] time = genericInterval.getDataType(0).getStructure().getDataType(0).getOctetString().getOctetStr();
		byte[] date = genericInterval.getDataType(0).getStructure().getDataType(1).getOctetString().getOctetStr();
		if((time[0]+time[1]+time[2]+time[3]) != 0x00)
			return false;
		
		if((date[3]==1)&&(date[1]==-1)&&(date[2]==-1)&&(date[0]==-1)&&(date[4]==-1))
			return true;
		else
			return false;
	}

	private int mbusCount() {
		int count =0;
		for (int i = 0; i < mbusDevices.length; i++){
			if (mbusDevices[i] != null)
				count++;
		}
		return count;
	}

	private void checkMbusDevices() throws IOException, SQLException, BusinessException {
		long pAddress1 = getCosemObjectFactory().getCosemObject(mbusPrimaryAddress).getValue();
		if ( pAddress1 != 0){
			String customerID = getCosemObjectFactory().getData(mbusCustomerID).getString();
			mbusDevices[0] = new MbusDevice(pAddress1, customerID, findOrCreateNewMbusDevice(pAddress1, customerID), logger);
		}
		// TODO complete this if there are more than one Mbus devices on the meters
	}

	private Rtu findOrCreateNewMbusDevice(long address1, String customerID) throws SQLException, BusinessException, IOException {
		
		List mbusList = mw().getRtuFactory().findBySerialNumber(customerID);
		
		if( mbusList.size() == 1 ) {
			((Rtu)mbusList.get(0)).updateGateway(rtu);
			return (Rtu)mbusList.get(0);
		}
		
        if( mbusList.size() > 1 ) {
            getLogger().severe( toDuplicateSerialsErrorMsg(customerID) );
            return null;
        }
        
        if( getRtuType() == null ) {
            getLogger().severe( AUTO_CREATE_ERROR_1 );
            return null;
        }
        
        if( getRtuType().getPrototypeRtu() == null ) {
            getLogger().severe( AUTOCREATE_ERROR_2 );    
            return null;
        }
        
        return createMeter(rtu, getRtuType(), customerID);
	}
	
    private Rtu createMeter(Rtu rtu2, RtuType type, String customerID) throws SQLException, BusinessException {
        RtuShadow shadow = type.newRtuShadow();
        
        Date lastreading = shadow.getLastReading();
        
    	shadow.setName(customerID);
        shadow.setSerialNumber(customerID);

    	shadow.setGatewayId(rtu.getId());
    	shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);
	}

	private RtuType getRtuType() throws IOException {
    	String type = getProperty(RTU_TYPE);
    	
        if (type != null){
           return mw().getRtuTypeFactory().find(type);
        }
        else throw new IOException("Iskra Mx37x, No or wrong rtuType defined om E-meter.");
	}
    
    private String getProperty(String key){
        return (String)properties.get(key);
    }

	private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat( DUPLICATE_SERIALS ).format( new Object [] { serial } );
    }

	private void init(InputStream is, OutputStream os) throws IOException {
		
        configProgramChanges 	= 	-1;
        
        dlmsCache 			= 	new Cache();
		cosemObjectFactory 	= 	new CosemObjectFactory((ProtocolLink)this);
		meterConfig 		=	DLMSMeterConfig.getInstance("ISK");
		ocm					= 	new ObisCodeMapper(getCosemObjectFactory());
		
		if (rtuType.equalsIgnoreCase("mbus"))
			metertype = MBUS;
		else
			metertype = ELECTRICITY;
		
		try {
			//		dlmsConnection = new TCPIPConnection(is, os, getTimeout(), 0, 0, 17, 16);
			if (connectionMode == 0)
				dlmsConnection = new HDLCConnection(is, os,	iHDLCTimeoutProperty, 100, iProtocolRetriesProperty,iClientMacAddress, iServerLowerMacAddress,iServerUpperMacAddress, addressingMode);
			else
				dlmsConnection = new TCPIPConnection(is, os, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty,iClientMacAddress, iServerLowerMacAddress);
			
			dlmsConnection.setIskraWrapper(1);
			
			startCacheMechanism(this);
			
		} catch (DLMSConnectionException e) {
			throw new IOException(e.getMessage());
		}
	}

	private void connect() {
		try {
			getDLMSConnection().connectMAC();
			
			new SecureConnection(iSecurityLevelProperty, firmwareVersion, strPassword, getDLMSConnection());
			
			collectCache();
			
            if (!verifyMeterID()) 
                throw new IOException("Iskra Mx37x, connect, Wrong DeviceID!, settings="+strID+", meter="+getDeviceAddress());

            if (!verifyMeterSerialNR()) 
                throw new IOException("Iskra Mx37x, connect, Wrong SerialNR!, settings="+serialNumber+", meter="+getSerialNumber());
            
            if (extendedLogging >= 1) 
                logger.info(getRegistersInfo(extendedLogging));
             
             if ( demandScalerUnits[MBUS].getUnitCode() == 0 ){
             	RegisterValue scalerRegister = readRegister(mbusScalerUnit);
             	demandScalerUnits[MBUS] = new ScalerUnit(scalerRegister.getQuantity().getUnit().getScale(),scalerRegister.getQuantity().getUnit());
             }
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DLMSConnectionException e) {
			e.printStackTrace();
		}
		
	}
	
	public void startCacheMechanism(Object fileSource) throws FileNotFoundException, IOException {
		
		this.source = fileSource;
		ObjectInputStream ois = null;
        try {
            File file = new File(((CacheMechanism) source).getFileName());
            ois = new ObjectInputStream(new FileInputStream(file));
            ((CacheMechanism) source).setCache(ois.readObject());
         }
         catch(ClassNotFoundException e) {
             e.printStackTrace();
         }
         catch(FileNotFoundException e) {
             // absorb
         }
         finally {
            if (ois != null) 
                ois.close();
         }
	}

	public void stopCacheMechanism() {
        File file = new File(((CacheMechanism) source).getFileName());
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(((CacheMechanism) source).getCache());
	        oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void collectCache() throws IOException {
        int iConf;
        
        if (dlmsCache.getObjectList() != null) {
            meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
            
            try {
                iConf = requestConfigurationProgramChanges();
            }
            catch(IOException e) {
                e.printStackTrace();
                iConf=-1;
                logger.severe("Iskra Mx37x: Configuration change is not accessible, request object list...");
                requestObjectList();
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
            }

            if (iConf != dlmsCache.getConfProgChange()) {
                
            	if (DEBUG>=1) System.out.println("iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());    
                
            	logger.severe("Iskra Mx37x: Configuration changed, request object list...");
                requestObjectList();	// request object list again from rtu
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                
                if (DEBUG>=1) System.out.println("after requesting objectlist (conf changed)... iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());  
            }
        }
        
        else { // Cache not exist
            logger.info("Iskra Mx37x: Cache does not exist, request object list.");
            requestObjectList();
            try {
                iConf = requestConfigurationProgramChanges();
              
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                if (DEBUG>=1) System.out.println("after requesting objectlist... iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());  
            }
            catch(IOException e) {
                iConf=-1;
            }
        }
	}
	
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        return date;
    }
	
    private void setTime() throws ServiceException, ParseException, IOException {
        
        /* Don't worry about clock sets over interval boundaries, Iskra
         * will (probably) handle this. 
         */
        Date cTime = getTime();
        
        Date now = new Date();
        
        long sDiff = ( now.getTime() - cTime.getTime() ) / 1000;
        long sAbsDiff = Math.abs( sDiff );
        
        getLogger().info("Difference between metertime and systemtime is " + sDiff * 1000 + " ms");
        
        long max = communicationProfile.getMaximumClockDifference();
        long min = communicationProfile.getMinimumClockDifference();
        
        if( ( sAbsDiff < max ) && ( sAbsDiff > min ) ) { 
            setTimeClock();
        }
    }
    
    public void setTimeClock() throws IOException
    {
       Calendar calendar=null;
       if (iRequestTimeZone != 0)
           calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
       else
           calendar = ProtocolUtils.initCalendar(false,getTimeZone());
       calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);           
       doSetTime(calendar);
    }
    
    public int requestTimeZone() throws IOException {
        if (deviation == -1) { 
            Clock clock = getCosemObjectFactory().getClock();
            deviation = clock.getTimeZone();
        }
        return (deviation);
     }
    
    private void doSetTime(Calendar calendar) throws IOException
    {
    	byte[] byteTimeBuffer = createByteDate(calendar);
       
       getCosemObjectFactory().writeObject(clock,8,2, byteTimeBuffer);
    }
    
	private byte[] createByteDate(Calendar calendar) {
		byte[] byteStartDateBuffer = new byte[14];
		
		byteStartDateBuffer[0]=DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
		byteStartDateBuffer[1]=12; // length
		byteStartDateBuffer[2]=(byte)(calendar.get(calendar.YEAR) >> 8);
		byteStartDateBuffer[3]=(byte)calendar.get(calendar.YEAR);
		byteStartDateBuffer[4]=(byte)(calendar.get(calendar.MONTH)+1);
		byteStartDateBuffer[5]=(byte)calendar.get(calendar.DAY_OF_MONTH);
		byte bDOW = (byte)calendar.get(calendar.DAY_OF_WEEK);
		byteStartDateBuffer[6]=bDOW--==1?(byte)7:bDOW;
		byteStartDateBuffer[7]=(byte)calendar.get(calendar.HOUR_OF_DAY);
		byteStartDateBuffer[8]=(byte)calendar.get(calendar.MINUTE);
		byteStartDateBuffer[9]=(byte)calendar.get(calendar.SECOND);
		byteStartDateBuffer[10]=(byte)0x0; // hundreds of seconds
		                
		byteStartDateBuffer[11]=(byte)(0x80); 
		byteStartDateBuffer[12]=(byte)0;
		                
		if (getTimeZone().inDaylightTime(calendar.getTime()))
			byteStartDateBuffer[13]=(byte)0x80; //0x00;
		else
			byteStartDateBuffer[13]=(byte)0x00; //0x00;
		
		return byteStartDateBuffer;
	}
	
	private void getRegisters(int deviceType) throws UnsupportedException, NoSuchRegisterException, IOException, SQLException, BusinessException {
		
		Rtu nRtu = null;
		
		if(deviceType == ELECTRICITY)
			nRtu = rtu;
		else if (deviceType == MBUS)
			nRtu = mbusDevices[0].getMbus();
		
		List registerGroups = communicationProfile.getRtuRegisterGroups();
		
        Iterator i = nRtu.getRtuType().getRtuRegisterSpecs().iterator();
        while (i.hasNext()) {
        	
            RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
            ObisCode oc = spec.getObisCode();
            RtuRegister register = nRtu.getRegister( oc );
            
            if (register != null){
            	
            	if (oc.getF() != 255){
            		
            		if (oc.getF() == 0)
            			ocm.setDaily();
            		else if (oc.getF() == -1)
            			ocm.setMonthly();
            		
            		RtuRegisterReadingImpl rrri = null;
            		Date lastRegisterDate = null;
            		Calendar registerCalendar = Calendar.getInstance(getTimeZone());
            		Calendar systemCalendar = Calendar.getInstance(getTimeZone());
                	List registerValues = mw().getRtuRegisterReadingFactory().findByRegister(register.getId());
                	
                	if (registerValues.size() != 0){
                		lastRegisterDate = getLastRegisterDate(registerValues);
                		registerCalendar.setTime(lastRegisterDate);
                	}else{
                		registerCalendar.add(Calendar.MONTH, -1);
                		lastRegisterDate = registerCalendar.getTime();
                	}
            		
            		int previousCount = getDayDifferenc(systemCalendar, registerCalendar);
            		
            		for (int j = previousCount; j >= 0; j--){
            			ObisCode previousOc = new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), oc.getE(), 0-j, true);
            			RegisterValue rv = readRegister(previousOc);
            			if ((rv != null)&&(rv.getToTime().after(lastRegisterDate))){
            				System.out.println(rv.getToTime() + " " + rv.getQuantity());
        					register.add(rv.getQuantity().getAmount(), rv.getEventTime(), rv.getFromTime(), rv.getToTime(),rv.getReadTime());
        					try {
								Thread.sleep(750);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
            			}
            		}
            		if (oc.getF() == 0)
            			ocm.clearDaily();
            		else if (oc.getF() == -1)
            			ocm.clearMonthly();
                }
            }
			else {
				String obis = oc.toString();
				String msg = "Register " + obis + " not defined on device";
				getLogger().info(msg);
			}
        }
	}
	
    private int getDayDifferenc(Calendar systemCalendar, Calendar registerCalendar) {
		long diff = Math.abs(systemCalendar.getTimeInMillis() - registerCalendar.getTimeInMillis());
		return (int) (diff/(1000*60*60*24));
	}

	private Date getLastRegisterDate(List registerValues) {
    	Date lastDate = ((RtuRegisterReadingImpl) registerValues.get(0)).getToTime();
    	Iterator it = registerValues.iterator();
    	while(it.hasNext()){
    		Date dateRrri = ((RtuRegisterReadingImpl)it.next()).getToTime();
    		if (dateRrri.after(lastDate))
    			lastDate = dateRrri;
    	}
    	return lastDate;
	}

	public ProfileData getProfileData(Date lastReading,boolean includeEvents, ObisCode lProfileObisCode) throws IOException, SQLException, BusinessException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(getTimeZone()),includeEvents, lProfileObisCode);
    }
    
    private ProfileData doGetProfileData(Calendar fromCalendar,Calendar toCalendar,boolean includeEvents, ObisCode lProfileObisCode) throws IOException, SQLException, BusinessException {
        byte bNROfChannels = (byte)getNumberOfChannels(lProfileObisCode);
        return doGetDemandValues(fromCalendar, bNROfChannels, includeEvents, lProfileObisCode);
    }
    
    private ProfileData doGetDemandValues(Calendar fromCalendar, byte bNROfChannels,  boolean includeEvents, ObisCode profileObisCode) throws IOException, SQLException, BusinessException {
        
    	String nRtuType = null;
    	
    	if (profileObisCode.equals(loadProfileObisCode))
    		nRtuType = rtuType;
    	else if (profileObisCode.equals(mbusLProfileObisCode[0]))
    		nRtuType = mbusDevices[0].getMbus().getProperties().getProperty(RTU_TYPE);
    	
    	ProfileData profileData = new ProfileData( );
        DataContainer dataContainer = getCosemObjectFactory().getProfileGeneric(profileObisCode).getBuffer(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()));
        for (int channelId=0;channelId<bNROfChannels;channelId++) {
    		if ( !nRtuType.equalsIgnoreCase("mbus")){
            	
    			RegisterValue scalerRegister = readRegister(getCapturedObjects(profileObisCode).getProfileDataChannel(channelId));
				demandScalerUnits[0] = new ScalerUnit(scalerRegister.getQuantity().getUnit().getScale(),scalerRegister.getQuantity().getUnit());
				ChannelInfo ci = new ChannelInfo(channelId, "IskraMx37x_channel_"+channelId, demandScalerUnits[0].getUnit());
				ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                profileData.addChannel(ci);
        	}
            	
    		else if ( bytesToObisString(getCapturedObjects(profileObisCode).getProfileDataChannel(channelId).getLN()).indexOf("0.1.128.50.0.255") == 0 ){
        		// don't show the events on the mbus meter
        		includeEvents = false;
        		RegisterValue scalerRegister = readRegister(getCapturedObjects(profileObisCode).getProfileDataChannel(channelId));
				demandScalerUnits[1] = new ScalerUnit(scalerRegister.getQuantity().getUnit().getScale(),scalerRegister.getQuantity().getUnit());
				ChannelInfo ci2 = new ChannelInfo(channelId, "IskraMx37x_channel_"+channelId, demandScalerUnits[1].getUnit());
				ci2.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
	            profileData.addChannel(ci2);
        	}
        }
        
        buildProfileData(bNROfChannels,dataContainer,profileData,profileObisCode);
        
        if (includeEvents) {
            profileData.getMeterEvents().addAll(getLogbookData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone())));
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval(profileObisCode)/60); 
        }
        
    	if (profileObisCode.equals(loadProfileObisCode))
    		rtu.store(profileData);
    	else if (profileObisCode.equals(mbusLProfileObisCode[0]))
    		mbusDevices[0].getMbus().store(profileData);
        
        return profileData;
    }
    
	private List getLogbookData(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        Logbook logbook = new Logbook(getTimeZone());
        return logbook.getMeterEvents(getCosemObjectFactory().getProfileGeneric(eventLogObisCode).getBuffer(fromCalendar, toCalendar));
    }
	
    public int getProfileInterval(ObisCode profileObisCode) throws IOException,UnsupportedException{
    	
    	int item = -1;
    	
    	if (profileObisCode.equals(loadProfileObisCode))
    		item = 0;
    	else if (profileObisCode.equals(mbusLProfileObisCode[0]))
    		item = 1;
    	
        if (iInterval[item] == -1) {
           iInterval[item] = getCosemObjectFactory().getProfileGeneric(profileObisCode).getCapturePeriod();
        }
        return iInterval[item];
    }
    
    private void buildProfileData(byte bNROfChannels, DataContainer dataContainer,ProfileData profileData, ObisCode profileObisCode)  throws IOException
    {
        Calendar calendar=null,calendarEV=null;
        int i,t,protocolStatus=0;
        boolean currentAdd=true,previousAdd=true;
        IntervalData previousIntervalData=null,currentIntervalData;

        if (DEBUG >=1) dataContainer.printDataContainer();
        
        if (dataContainer.getRoot().element.length == 0)
           throw new IOException("No entries in object list.");
        
        for (i=0;i<dataContainer.getRoot().element.length;i++) { // for all retrieved intervals
            try {    
                calendar = dataContainer.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(profileObisCode)).toCalendar(getTimeZone());        
            }
            catch(ClassCastException e) {
                // absorb
                if (DEBUG>=1)  System.out.println ("DEBUG> buildProfileData, ClassCastException ,"+e.toString());
                if (calendar != null) calendar.add(calendar.MINUTE,(getProfileInterval(profileObisCode)/60));
            }
            if (calendar != null) {
                if (getProfileStatusChannelIndex(profileObisCode)!=-1)
                    protocolStatus = dataContainer.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(profileObisCode));   
                else
                    protocolStatus=0;

                currentIntervalData = getIntervalData(dataContainer.getRoot().getStructure(i), calendar, protocolStatus, profileObisCode);

                // KV 16012004
                if (DEBUG >=1) { 
                    dataContainer.getRoot().getStructure(i).print();
                    System.out.println();
                }

                if (currentAdd & !previousAdd) {
                   if (DEBUG>=1) System.out.println ("add intervals together...");
                   currentIntervalData = addIntervalData(currentIntervalData,previousIntervalData);
                }

                // Add interval data...
                if (currentAdd) {
                    profileData.addInterval(currentIntervalData);
                }

                previousIntervalData=currentIntervalData;
                previousAdd=currentAdd;
                
            } // if (calendar != null)
            
        } // for (i=0;i<dataContainer.getRoot().element.length;i++) // for all retrieved intervals

        if (DEBUG>=1) System.out.println(profileData);
        
    }
    
    private IntervalData getIntervalData(DataStructure dataStructure,Calendar calendar,int protocolStatus, ObisCode profileObisCode) throws UnsupportedException, IOException {
        // Add interval data...
        IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()),map(protocolStatus),protocolStatus);
        
        for (int t=0;t<getCapturedObjects(profileObisCode).getNROfChannels();t++){
        	
                if (getCapturedObjects(profileObisCode).isChannelData(getObjectNumber(t, profileObisCode)))
                    intervalData.addValue(new Integer(dataStructure.getInteger(getObjectNumber(t, profileObisCode) + dataContainerOffset)));
        }
        	
        return intervalData;
    }
    
    private int getObjectNumber(int t, ObisCode profileObisCode) throws IOException {
		for(int i = 0; i < getCapturedObjects(profileObisCode).getNROfObjects(); i++){
			if ( getCapturedObjects(profileObisCode).getChannelNR(i) == t)
				return i;
		}
		throw new UnsupportedException();
	}
    
    private int map(int protocolStatus) {
        
        int eiStatus=0;
        
        if ((protocolStatus & PROFILE_STATUS_DEVICE_DISTURBANCE) == PROFILE_STATUS_DEVICE_DISTURBANCE) {
            eiStatus |= IntervalStateBits.DEVICE_ERROR; 
        }
        if ((protocolStatus & PROFILE_STATUS_RESET_CUMULATION) == PROFILE_STATUS_RESET_CUMULATION) {
            eiStatus |= IntervalStateBits.OTHER; 
        } 
        if ((protocolStatus & PROFILE_STATUS_DEVICE_CLOCK_CHANGED) == PROFILE_STATUS_DEVICE_CLOCK_CHANGED) {
            eiStatus |= IntervalStateBits.SHORTLONG; 
        } 
        if ((protocolStatus & PROFILE_STATUS_POWER_RETURNED) == PROFILE_STATUS_POWER_RETURNED) {
            eiStatus |= IntervalStateBits.POWERUP; 
        } 
        if ((protocolStatus & PROFILE_STATUS_POWER_FAILURE) == PROFILE_STATUS_POWER_FAILURE) {
            eiStatus |= IntervalStateBits.POWERDOWN; 
        } 
        
        return eiStatus;
        
    }
    
    private IntervalData addIntervalData(IntervalData currentIntervalData,IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime(),currentIntervalData.getEiStatus(),currentIntervalData.getProtocolStatus());
        int current,i;
        for (i=0;i<currentCount;i++) {
            current = ((Number)currentIntervalData.get(i)).intValue()+((Number)previousIntervalData.get(i)).intValue();
            intervalData.addValue(new Integer(current));
        }
        return intervalData;
    }
    
    private int getProfileClockChannelIndex(ObisCode profileObisCode) throws IOException {
        for (int i=0;i<getCapturedObjects(profileObisCode).getNROfObjects();i++) {
            if (!getCapturedObjects(profileObisCode).isChannelData(i)) {
                if (ObisCode.fromByteArray(getCapturedObjects(profileObisCode).getLN(i)).equals(clock))
                    return i;
            }
        }
        throw new IOException("Iskra Mx37x, no clock channel found in captureobjects!");
    }
    
    private int getProfileStatusChannelIndex(ObisCode profileObisCode) throws UnsupportedException, IOException {
        for (int i=0;i<getCapturedObjects(profileObisCode).getNROfObjects();i++) {
            if (!getCapturedObjects(profileObisCode).isChannelData(i)) {
                if (ObisCode.fromByteArray(getCapturedObjects(profileObisCode).getLN(i)).equals(status))
                    return i;
            }
        }
        return -1;
    }
    
    public int getNumberOfChannels(ObisCode profileObisCode) throws UnsupportedException, IOException {
    	
    	int item = -1;
    	
    	if (profileObisCode.equals(loadProfileObisCode))
    		item = 0;
    	else if (profileObisCode.equals(mbusLProfileObisCode[0]))
    		item = 1;
    	
        if (numberOfChannels[item] == -1) {
            numberOfChannels[item] = getCapturedObjects(profileObisCode).getNROfChannels();
        }
        return numberOfChannels[item];
    }
    
    private CapturedObjects getCapturedObjects(ObisCode profileObisCode)  throws UnsupportedException, IOException {
    	
    	int item = -1;
    	
    	if (profileObisCode.equals(loadProfileObisCode))
    		item = 0;
    	else if (profileObisCode.equals(mbusLProfileObisCode[0]))
    		item = 1;
    	
        if (capturedObjects[item] == null) {
           int i;
           int j = 0;
           DataContainer dataContainer = null;
           try {
               ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(profileObisCode);
               meterConfig.setCapturedObjectList(profileGeneric.getCaptureObjectsAsUniversalObjects());               
               dataContainer = profileGeneric.getCaptureObjectsAsDataContainer();
               
               capturedObjects[item] = new CapturedObjects(dataContainer.getRoot().element.length);
               for (i=0;i<dataContainer.getRoot().element.length;i++) {
            	   
            	   if ( i >= 2){
            		   
            		   if ( rtuType.equalsIgnoreCase("mbus") ){
                		   if ( bytesToObisString(dataContainer.getRoot().getStructure(i).getOctetString(1).getArray()).indexOf("0.1.128.50.0.255") == 0 ){
                               capturedObjects[item].add(j,
                                       dataContainer.getRoot().getStructure(i).getInteger(0),
                                       dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                       dataContainer.getRoot().getStructure(i).getInteger(2));
                               if (dataContainerOffset== -1) dataContainerOffset  = i - 2;
                               j++;
                		   }
                	   }
                	   else{
                		   if ( bytesToObisString(dataContainer.getRoot().getStructure(i).getOctetString(1).getArray()).indexOf("0.1.128.50.0.255") != 0 ){
                               capturedObjects[item].add(j,
                                       dataContainer.getRoot().getStructure(i).getInteger(0),
                                       dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                                       dataContainer.getRoot().getStructure(i).getInteger(2));
                               if (dataContainerOffset== -1) dataContainerOffset  = i - 2;
                               j++;
                		   }
                	   }
            	   }
            	   
            	   else{
                       capturedObjects[item].add(j,
                               dataContainer.getRoot().getStructure(i).getInteger(0),
                               dataContainer.getRoot().getStructure(i).getOctetString(1).getArray(),
                               dataContainer.getRoot().getStructure(i).getInteger(2));
                       j++;
            	   }
               }
           }
           catch (java.lang.ClassCastException e) {
               System.out.println("Error retrieving object: "+e.getMessage());   
           }
           catch(java.lang.ArrayIndexOutOfBoundsException e) {
               System.out.println("Index error: "+e.getMessage());   
           }
           
        } 
        return capturedObjects[item];
    }
    
    private String bytesToObisString(byte[] channelLN) {
    	String str = "";
		for(int i = 0; i < channelLN.length; i++){
			if (i>0) str+=".";
				str += ""+((int)channelLN[i]&0xff);
		}
		return str;
	}
	
    private boolean verifyMeterID() throws IOException {
        if ((strID == null) || ("".compareTo(strID)==0) || (strID.compareTo(getDeviceAddress()) == 0))
            return true;
        else 
            return false;
    }
    
    public String getDeviceAddress() throws IOException {
    	if (devID == null)
    		devID = getCosemObjectFactory().getGenericRead(deviceLogicalName,DLMSUtils.attrLN2SN(2),1).getString();
        return devID;
    }
    
    private boolean verifyMeterSerialNR() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0) || (serialNumber.compareTo(getSerialNumber()) == 0))
            return true;
        else 
            return false;
    }
    
    public String getSerialNumber() throws IOException {
        if (serialnr==null) {
            UniversalObject uo = meterConfig.getSerialNumberObject();
            serialnr = getCosemObjectFactory().getGenericRead(uo).getString();
        }
        return serialnr;
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;
        
        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i=0;i<getMeterConfig().getInstantiatedObjectList().length;i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString()+" "+uo.getObisCode().getDescription()+"\n");
        }
        
        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode).getCaptureObjects().iterator();
        while(it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject)it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString()+" "+capturedObject.getLogicalName().getObisCode().getDescription()+" (load profile)\n");
        }
        
        return strBuff.toString();
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (ocm == null)
            ocm = new ObisCodeMapper(getCosemObjectFactory());
        return ocm.getRegisterValue(obisCode);
    }
    
	public static ScalerUnit getScalerUnit(ObisCode obisCode) {
		
		if (obisCode.toString().indexOf("1.0") == 0)
			return demandScalerUnits[ELECTRICITY];
		else
			return demandScalerUnits[MBUS];
	}
	
    public int requestConfigurationProgramChanges() throws IOException {
        if (configProgramChanges == -1)
           configProgramChanges = (int)getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        return configProgramChanges;
    } 

    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    } 
    
    public String requestAttribute(short sIC,byte[] LN,byte bAttr) throws IOException {
        return doRequestAttribute(sIC,LN, bAttr).print2strDataContainer();
    }
    
    private DataContainer doRequestAttribute(int classId,byte[] ln,int lnAttr) throws IOException {
        DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln),DLMSUtils.attrLN2SN(lnAttr),classId).getDataContainer(); 
        return dc;
     }
    
	public void addProperties(Properties properties) {
		this.properties = properties;
	}

	private void validateProperties() throws MissingPropertyException, InvalidPropertyException {
		try{
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            
            strID = rtu.getDeviceId();
            if ((strID != null) && (strID.length()>16)) 
            	throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            
            strPassword = rtu.getPassword();
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","10").trim());
            iSecurityLevelProperty=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","100").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","1").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","17").trim());
            firmwareVersion=properties.getProperty("FirmwareVersion","ANY");
            nodeId = rtu.getNodeAddress();
            serialNumber = rtu.getSerialNumber();
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));            
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","2"));              
            connectionMode = Integer.parseInt(properties.getProperty("Connection","0")); // 0=HDLC, 1= TCP/IP
            rtuType = properties.getProperty("RtuType","");
            
            if (Integer.parseInt(properties.getProperty("LoadProfileId","1")) == 1)
                loadProfileObisCode = genericProfile1;
            else if (Integer.parseInt(properties.getProperty("LoadProfileId","1")) == 2)
                loadProfileObisCode = genericProfile2;
            else if (Integer.parseInt(properties.getProperty("LoadProfileId","1")) == 97)
                loadProfileObisCode = loadProfileObisCode97;
            else throw new InvalidPropertyException("IskraMx37x, validateProperties, invalid LoadProfileId, "+Integer.parseInt(properties.getProperty("LoadProfileId","1"))); 
            
		}
		catch(NumberFormatException e){
			
		}
	}

	public String getVersion() {
		return "$Revision: 1.1 $";
	}

	public List getOptionalKeys() {
        List result = new ArrayList(9);
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("LoadProfileId");
        result.add("AddressingMode");
        result.add("Connection");
        result.add("RtuType");
        return result;
	}

	public List getRequiredKeys() {
        List result = new ArrayList(3);
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("Connection");
        return result; 
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
		return 0;
	}

	public int getRoundTripCorrection() {
		return iRoundtripCorrection;
	}

	public StoredValues getStoredValues() {
		// TODO Auto-generated method stub
		return null;
	}

	public TimeZone getTimeZone() {
		return rtu.getTimeZone();
	}

	public boolean isRequestTimeZone() {
		return false;
	}

	public CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

	public void setCosemObjectFactory(CosemObjectFactory cosemObjectFactory) {
		this.cosemObjectFactory = cosemObjectFactory;
	}

	public Object getCache() {
		return dlmsCache;
	}

	public String getFileName() {
		Calendar calendar = Calendar.getInstance();
	    return calendar.get(Calendar.YEAR) + "_" + strID + "_" + iServerUpperMacAddress + "_IskraMx37x.cache";
	}

	public void setCache(Object cacheObject) {
		this.dlmsCache=(Cache)cacheObject;
	}
	
    /*******************************************************************************************
    M e s s a g e P r o t o c o l  i n t e r f a c e 
     * @throws IOException 
     * @throws SQLException 
     * @throws BusinessException 
    *******************************************************************************************/
   // message protocol

	private void sendMeterMessages() throws IOException, BusinessException, SQLException {
		Iterator mi = rtu.getPendingMessages().iterator();
		
		while(mi.hasNext()){
            RtuMessage msg = (RtuMessage) mi.next();
            String msgString = msg.getContents();
            String contents = msgString.substring(msgString.indexOf("<")+1, msgString.indexOf(">"));
            if (contents.endsWith("/"))
            	contents = contents.substring(0, contents.length()-1);
            BigDecimal breakerState = null;
            
            boolean disconnect 	= contents.equalsIgnoreCase(DISCONNECT);
            boolean connect 	= contents.equalsIgnoreCase(CONNECT);
            boolean ondemand 	= contents.equalsIgnoreCase(ONDEMAND);
            boolean codered		= contents.equalsIgnoreCase(CONPOWERLIMIT)||
            					contents.equalsIgnoreCase(CODERED_LIMIT)||
            					contents.equalsIgnoreCase(CODERED_START_DT)||
            					contents.equalsIgnoreCase(CODERED_STOP_DT);
            boolean endcodered 	= contents.equalsIgnoreCase(CODERED_END);
            boolean tou = contents.equalsIgnoreCase(TOU);
            
            if (connect || disconnect){
                if (disconnect){
                	cosemObjectFactory.writeObject(breakerObisCode, 1, 2, disconnectMsg);
                	breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }
                
                if (connect){
                	cosemObjectFactory.writeObject(breakerObisCode, 1, 2, connectMsg);
                	breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }
        		
        		switch(breakerState.intValue()){
        		
        		case 0: {
        			if ( contents.indexOf(DISCONNECT) != -1 )
        				msg.confirm();
        			else 
        	            msg.setFailed();          
        		}break;
        		
        		case 1: {
        			if ( contents.indexOf(CONNECT) != -1 )
        				msg.confirm();
        			else 
        				msg.setFailed();    
        		}break;
        		
        		default:{
        			msg.setFailed();
        			break;
        		}
        		}
            }
            
            /*if (tou)
            	sendActivityCalendar(contents, msg);*/
            
            if (ondemand){
            	Iterator i = rtu.getRtuType().getRtuRegisterSpecs().iterator();
                while (i.hasNext()) {
                	
                    RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                    ObisCode oc = spec.getObisCode();
                    RtuRegister register = rtu.getRegister( oc );
                    
                    if (register != null){
                    	
                    	if (oc.getF() == 255){
                        	RegisterValue rv = readRegister(oc);
        					register.add(rv.getQuantity().getAmount(), rv
        							.getEventTime(), rv.getFromTime(), rv.getToTime(),
        							rv.getReadTime());
                        }
                    	
                    }
        			else {
        				String obis = oc.toString();
        				String msgError = "Register " + obis + " not defined on device";
        				getLogger().info(msgError);
        			}
                }
            	msg.confirm();
            }
            
            if (codered || endcodered){
            	
            	long contractPL = 0;
            	long limit = 0;
            	String startDate = "";
            	String stopDate = "";
            	Calendar startCal = null; 
            	Calendar stopCal = null;
            	
            	if(codered){
            		
            		// the contractual Power Limit
                	if (getMessageValue(msgString, CONPOWERLIMIT).equalsIgnoreCase("")){
                		contractPL = readRegister(contractPowerLimit).getQuantity().getAmount().longValue();
                	}else
                		contractPL = Integer.parseInt(getMessageValue(msgString, CONPOWERLIMIT));
                	
                	// the CodeRed Power Limit
                	if (getMessageValue(msgString, CODERED_LIMIT).equalsIgnoreCase("")){
                		throw new IOException("No Code Red Limit was entered for the CodeRed message.");
                	}else
                		limit = Integer.parseInt(getMessageValue(msgString, CODERED_LIMIT));
                	
                	// the Start- and EndDates to calculate the duration
                	startDate = getMessageValue(msgString, CODERED_START_DT);
                	stopDate = getMessageValue(msgString, CODERED_STOP_DT);
                	startCal = (startDate.equalsIgnoreCase(""))?Calendar.getInstance(getTimeZone()):getCalendarFromString(startDate);
                	if (stopDate.equalsIgnoreCase("")){
                		stopCal = Calendar.getInstance();
                		stopCal.setTime(startCal.getTime());
                		stopCal.add(Calendar.YEAR, 1);
                	}else{
                		stopCal = getCalendarFromString(stopDate);
                	}
            	}
            	
            	if(endcodered){
            		
            		// read the Contractual Power Limit from the meter
            		contractPL = readRegister(contractPowerLimit).getQuantity().getAmount().longValue();
            		
            		// read the CodeRed Power Limit from the meter
            		limit = readRegister(crPowerLimit).getQuantity().getAmount().longValue();
            		
            		startCal = Calendar.getInstance(getTimeZone());
            		stopCal = startCal;
            	}
            	
            	contractPowerLimitMsg[1]=(byte)(contractPL >> 24);
            	contractPowerLimitMsg[2]=(byte)(contractPL >> 16);
            	contractPowerLimitMsg[3]=(byte)(contractPL >> 8);
            	contractPowerLimitMsg[4]=(byte)contractPL;
            	
            	int rtuID = rtu.getId();
            	crMeterGroupIDMsg[1]=(byte)(rtuID >> 8);
            	crMeterGroupIDMsg[2]=(byte)rtuID;
            	crGroupIDMsg = crMeterGroupIDMsg;

            	crPowerLimitMsg[1]=(byte)(limit >> 24);
            	crPowerLimitMsg[2]=(byte)(limit >> 16);
            	crPowerLimitMsg[3]=(byte)(limit >> 8);
            	crPowerLimitMsg[4]=(byte)limit;

            	long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis()))/1000;
            	crDurationMsg[1]=(byte)(crDur >> 24);
            	crDurationMsg[2]=(byte)(crDur >> 16);
            	crDurationMsg[3]=(byte)(crDur >> 8);
            	crDurationMsg[4]=(byte)crDur;
            	byte[] byteDate = createByteDate(startCal);
            	
            	getCosemObjectFactory().writeObject(contractPowerLimit, 3, 2, contractPowerLimitMsg);
            	getCosemObjectFactory().writeObject(crMeterGroupID, 1, 2, crMeterGroupIDMsg);
            	getCosemObjectFactory().writeObject(crPowerLimit, 3, 2, crPowerLimitMsg);
            	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// absorb
					e.printStackTrace();
				}
				getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
				getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
            	getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);
            	
            	msg.confirm();
            }
		}
	}
	
	public void sendActivityCalendar(String contents, RtuMessage msg) throws SQLException, BusinessException, IOException  {
    	UserFile userFile = getUserFile(contents);
    	
    	ActivityCalendar activityCalendar =
    		getCosemObjectFactory().getActivityCalendar(ObisCode.fromString("0.0.13.0.0.255"));
    
    	com.energyict.genericprotocolimpl.common.tou.ActivityCalendar calendarData = 
    		new com.energyict.genericprotocolimpl.common.tou.ActivityCalendar();
    	ActivityCalendarReader reader = new IskraActivityCalendarReader(calendarData);
    	calendarData.setReader(reader);
    	calendarData.read(new ByteArrayInputStream(userFile.loadFileInByteArray()));
    	CosemActivityCalendarBuilder builder = new 
    		CosemActivityCalendarBuilder(calendarData);
    	
        activityCalendar.writeCalendarNamePassive(builder.calendarNamePassive());
        activityCalendar.writeDayProfileTablePassive(builder.dayProfileTablePassive());
        activityCalendar.writeWeekProfileTablePassive(builder.weekProfileTablePassive());
        activityCalendar.writeSeasonProfilePassive(builder.seasonProfilePassive());
        activityCalendar.writeActivatePassiveCalendarTime(builder.activatePassiveCalendarTime());
        
        // read calendar, if error
        msg.confirm();
    	
    }
	
	protected UserFile getUserFile(String contents)throws BusinessException {
        int id = getTouFileId(contents);
        UserFile userFile = 
        	MeteringWarehouse.getCurrent().getUserFileFactory().find(id);
        if (userFile == null)
        	throw new BusinessException("No userfile found with id " + id);
        return null;
	}
	
	protected int getTouFileId(String contents) throws BusinessException {
		int startIndex = 2 + TOU.length();  // <TOU>
		int endIndex = contents.indexOf("</" + TOU + ">") - 1;
		String value = contents.substring(startIndex, endIndex);
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new BusinessException("Invalid userfile id: " + value);
		}
	}


	private Calendar getCalendarFromString(String strDate) {
		Calendar cal = Calendar.getInstance(getTimeZone());
    	cal.set(Calendar.DATE, Integer.valueOf(strDate.substring(0, strDate.indexOf("/"))));
    	cal.set(Calendar.MONTH, (Integer.valueOf(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
    	cal.set(Calendar.YEAR, Integer.valueOf(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));
    	
    	cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
    	cal.set(Calendar.MINUTE, Integer.valueOf(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
    	cal.set(Calendar.SECOND, Integer.valueOf(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
    	cal.clear(Calendar.MILLISECOND);
		return cal;
	}

	private String getMessageValue(String msgStr, String str) {
		try {
			return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
					+ 1, msgStr.indexOf("</" + str));
		} catch (Exception e) {
			return "";
		}
	}

	public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        MessageCategorySpec cat2 = new MessageCategorySpec("CodeRedMessages");
        
        MessageSpec msgSpec = addBasicMsg("Disconnect meter", DISCONNECT, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", CONNECT, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("ReadOnDemand", ONDEMAND, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addTouMessage("Set Time of use", "TOU", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addCodeRedMessage("CodeRed: Enter parameters", CODERED, false);
        cat2.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("CodeRed: Stop", CODERED_END, false);
        cat2.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        theCategories.add(cat2);
        return theCategories;
	}

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

	public String writeTag(MessageTag msgTag) {
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

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    private MessageSpec addTouMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

	private MessageSpec addCodeRedMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(CONPOWERLIMIT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(CODERED_LIMIT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(CODERED_START_DT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(CODERED_STOP_DT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
	}

}
