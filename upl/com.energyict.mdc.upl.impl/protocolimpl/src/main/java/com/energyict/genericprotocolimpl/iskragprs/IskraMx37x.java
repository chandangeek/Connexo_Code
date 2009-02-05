/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.cosem.PPPSetup.PPPAuthenticationType;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.genericprotocolimpl.common.GenericCache;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader;
import com.energyict.genericprotocolimpl.common.tou.CosemActivityCalendarBuilder;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
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
import com.energyict.protocolimpl.dlms.HDLCConnection;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;

/**
 * @author gna
 *
 * Changes:
 * GNA |29012009| Added force clock
 * GNA |02022009| Mad some changes to the sendTOU message. No activationDate is immediate activation using the Object method
 */
public class IskraMx37x implements GenericProtocol, ProtocolLink, CacheMechanism, Messaging{
	
	private int DEBUG = 0;
	private int TESTLOGGING = 0; 
	private boolean initCheck = false;
	
    private Logger 					logger;
    private Properties 				properties;
    private CommunicationProfile 	communicationProfile;
	private Link 					link;	
    private DLMSConnection        	dlmsConnection;
    private CosemObjectFactory 		cosemObjectFactory;
    private SecureConnection		secureConnection;
    private Rtu                    	rtu;
    private Clock					clock;
    private Cache 					dlmsCache;
    private DLMSMeterConfig 		meterConfig;
    private ObisCodeMapper 			ocm = null;
    private MbusDevice[]			mbusDevices = {null, null, null, null};				// max. 4 MBus meters
    public static ScalerUnit[] 		demandScalerUnits = {new ScalerUnit(0,30), new ScalerUnit(0,255), new ScalerUnit(0,255), new ScalerUnit(0,255), new ScalerUnit(0,255)};
    private CommunicationScheduler 	scheduler;
    
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
	private int configProgramChanges;
	
	private boolean forcedMbusCheck = false;
	
    private String strID;
	private String strPassword;
	private String firmwareVersion;
	private String nodeId;
	private String serialNumber;
	private String rtuType;
	private String serialnr = null;
	private String devID = null;
	
    private ObisCode genericProfile1 		= 	ObisCode.fromString("1.0.99.1.0.255");		// mostly considered as intervalProfile
    private ObisCode genericProfile2 		= 	ObisCode.fromString("1.0.99.2.0.255");		// mostly considered as MBus profile of daily profile
    private ObisCode genericProfile3 		= 	ObisCode.fromString("1.0.98.1.0.255");		// mostly considered as monthly or daily profile
    private ObisCode genericProfile4 		=	ObisCode.fromString("1.0.98.2.0.255");		// mostly considered as daily or monthly profile
    private ObisCode loadProfileObisCode97 	= 	ObisCode.fromString("1.0.99.97.0.255");
    private ObisCode breakerObisCode 		= 	ObisCode.fromString("0.0.128.30.21.255");
    private ObisCode eventLogObisCode 		= 	ObisCode.fromString("1.0.99.98.0.255");
//    private ObisCode mbusScalerUnit 		=	ObisCode.fromString("0.1.128.50.0.255");
    private ObisCode deviceLogicalName		= 	ObisCode.fromString("0.0.42.0.0.255");
    private ObisCode status 				= 	ObisCode.fromString("1.0.96.240.0.255");
    private ObisCode endOfBilling			=	ObisCode.fromString("0.0.15.0.0.255");
    private ObisCode endOfCapturedObjects	=	ObisCode.fromString("0.0.15.1.0.255");
    private ObisCode[] mbusPrimaryAddress	= 	{ObisCode.fromString("0.1.128.50.20.255"),
    											ObisCode.fromString("0.2.128.50.20.255"),
    											ObisCode.fromString("0.3.128.50.20.255"),
    											ObisCode.fromString("0.4.128.50.20.255")};
    private ObisCode[] mbusCustomerID		= 	{ObisCode.fromString("0.1.128.50.21.255"),
    											ObisCode.fromString("0.2.128.50.21.255"),
    											ObisCode.fromString("0.3.128.50.21.255"),
    											ObisCode.fromString("0.4.128.50.21.255")};
    private ObisCode[] mbusUnit				=	{ObisCode.fromString("0.1.128.50.30.255"),
												ObisCode.fromString("0.2.128.50.30.255"),
												ObisCode.fromString("0.3.128.50.30.255"),
												ObisCode.fromString("0.4.128.50.30.255")};
    private ObisCode[] mbusMedium			=	{ObisCode.fromString("0.1.128.50.23.255"),
												ObisCode.fromString("0.2.128.50.23.255"),
												ObisCode.fromString("0.3.128.50.23.255"),
												ObisCode.fromString("0.4.128.50.23.255")};
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
    
    private ArrayList monthlyProfilConfig	= new ArrayList(20);
    
    private static final int ELECTRICITY 	= 0x00;
    private static final int MBUS 			= 0x01;
    public static final int MBUS_MAX		= 0x04;
    
    private final static String RTU_TYPE 		= "RtuType";
    
    private byte[] connectMsg 				= new byte[] { DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x01 };
    private byte[] disconnectMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x00 };
    private byte[] contractPowerLimitMsg 	= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crPowerLimitMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crDurationMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0 };
    private byte[] crMeterGroupIDMsg 		= new byte[] { DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0 };
    private byte[] crGroupIDMsg 			= new byte[] { DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0 };
    
    private final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    private final static String FOLDER_EXT_NAME = "FolderExtName";

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
    	
    	cal.set(Calendar.DATE, Integer.parseInt(startDate.substring(0, startDate.indexOf("/"))));
    	cal.set(Calendar.MONTH, (Integer.parseInt(startDate.substring(startDate.indexOf("/") + 1, startDate.lastIndexOf("/")))) - 1);
    	cal.set(Calendar.YEAR, Integer.parseInt(startDate.substring(startDate.lastIndexOf("/") + 1, startDate.indexOf(" "))));
    	
    	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startDate.substring(startDate.indexOf(" ") + 1, startDate.indexOf(":"))));
    	cal.set(Calendar.MINUTE, Integer.parseInt(startDate.substring(startDate.indexOf(":") + 1, startDate.lastIndexOf(":"))));
    	cal.set(Calendar.SECOND, Integer.parseInt(startDate.substring(startDate.lastIndexOf(":") + 1, startDate.length())));
    	
    	System.out.println(cal.getTime());
		
	}

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.logger = logger;
		this.communicationProfile = scheduler.getCommunicationProfile();
		this.link = link;
		this.scheduler = scheduler;
		
		rtu = scheduler.getRtu();
		validateProperties();
		init(link.getInputStream(),link.getOutputStream());
		
        try {
        	connect();
        	
        	/**
        	 * TODO Just To TEST TODO
        	 */
//        	justATestMethod();
//        	doTheCheckMethods();
//        	handleMbusMeters();
//        	DailyMonthly dm = new DailyMonthly(this);
//        	dm.getDailyValues(dailyObisCode);
//        	dm.getMonthlyValues(monthlyObisCode);
        	    	
        	// Set clock or Force clock... if necessary
        	if( communicationProfile.getForceClock() ){
        		getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + Calendar.getInstance(getTimeZone()).getTime());
        		setTimeClock();
        	}else if( communicationProfile.getWriteClock() ) {
        		setTime();
        	}
        	
        	// Read profiles and events ... if necessary
    		if( (communicationProfile.getReadDemandValues()) && (communicationProfile.getReadMeterEvents()) ){
    			doTheCheckMethods();
    			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + rtu.getSerialNumber());
    			ElectricityProfile ep = new ElectricityProfile(this);
    			ep.getProfile(loadProfileObisCode, communicationProfile.getReadMeterEvents());
    		}
    		
    		// Read registers ... if necessary
    		/**
    		 * Here we are assuming that the daily and monthly values should be read.
    		 * In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to indicate whether you
    		 * want to read the actual registers or the daily/monthly registers ...
    		 */
    		if( communicationProfile.getReadMeterReadings() ) {
    			doTheCheckMethods();
    			
    			getLogger().log(Level.INFO, "Getting daily and monthly values for meter with serialnumber: " + rtu.getSerialNumber());
    			DailyMonthly dm = new DailyMonthly(this);
            	dm.getDailyValues(dailyObisCode);
            	dm.getMonthlyValues(monthlyObisCode);
    		}

    		// Send messages ... if there are messages
    		if( communicationProfile.getSendRtuMessage() ){
    			sendMeterMessages();
    		}
    		
    		// Handle the MBus meters
    		if(mbusCheck()){
    			getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
    			handleMbusMeters();
    		}
    		
    		if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Stopping the cache mechanism, saving to disk.");
    		GenericCache.stopCacheMechanism(rtu, dlmsCache);
    		disConnect();
    		
    		getLogger().log(Level.INFO, "Meter with serialnumber " + rtu.getSerialNumber() + " has completely finished.");
        	
		} catch (DLMSConnectionException e) {
			disConnect();
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (ServiceException e) {
			e.printStackTrace();
			disConnect();
			throw new BusinessException(e);
		} catch (ParseException e) {
			e.printStackTrace();
			disConnect();
			throw new BusinessException(e);
		} catch (SQLException e){
			e.printStackTrace();
			disConnect();
			
			/** Close the connection after an SQL exception, connection will startup again if requested */
        	Environment.getDefault().closeConnection();
        
			
			throw new BusinessException(e);
		}
		finally {
			if(dlmsCache.getObjectList() != null){
				GenericCache.stopCacheMechanism(rtu, dlmsCache);
			}
		}
	}
	
    private void justATestMethod() {
    	try {
//			PPPSetup pppSetup = getCosemObjectFactory().getPPPSetup();
//			
//			PPPAuthenticationType pppat = pppSetup.getPPPAuthenticationType();
//			System.out.println("PPP Username: " + pppSetup.getPPPAuthenticationType().getUsername().toString());
//			System.out.println("PPP Password: " + pppSetup.getPPPAuthenticationType().getPassword().toString());
//			
//			String newUsername = "essent";
//			String newPassword = "essentPassword";
//			
//			pppat.setUserName(newUsername);
//			pppat.setPassWord(newPassword);
//			
//			pppSetup.writePPPAuthenticationType(pppat);
    		
    		GPRSModemSetup gprsSetup = getCosemObjectFactory().getGPRSModemSetup();
    		
    		System.out.println(gprsSetup.getAPN().stringValue());
    		System.out.println(gprsSetup.getPinCod().getValue());
    		System.out.println(gprsSetup.getQualityOfService());
    		System.out.println(gprsSetup.getTheDefaultQualityOfService());
    		System.out.println(gprsSetup.getRequestedQualityOfService());
    		
    		gprsSetup.writeAPN("testAPN");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
	}

	/**
     * Checks if there is in fact an MBus meter configured on the E-meter
     * @return true or false
     */
    private boolean mbusCheck(){

		for(int i = 0; i < MBUS_MAX; i++){
			if ( mbusDevices[i] != null ){
				if(mbusDevices[i].getMbus() != null){
					return true;
				}
			}
		}
		return false;
	
    }
    
    private void handleMbusMeters(){
    	for(int i = 0; i < MBUS_MAX; i++){
    		if(mbusDevices[i] != null){
    			try {
    				mbusDevices[i].setIskraDevice(this);
					mbusDevices[i].execute(scheduler, null, null);
				} catch (BusinessException e) {
		            /*
		             * A single MBusMeter failed: log and try next MBusMeter.
		             */
					e.printStackTrace();
					getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");
					
				} catch (SQLException e) {
					
		        	/** Close the connection after an SQL exception, connection will startup again if requested */
		        	Environment.getDefault().closeConnection();
					
		            /*
		             * A single MBusMeter failed: log and try next MBusMeter.
		             */
					e.printStackTrace();
					getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");
					
				} catch (IOException e) {
		            /*
		             * A single MBusMeter failed: log and try next MBusMeter.
		             */
					e.printStackTrace();
					getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");
					
				}
    		}
    	}
    }
	
	private Date getClearMidnightDate(){
   		Calendar tempCalendar = Calendar.getInstance(getTimeZone());
		tempCalendar.add(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.add(Calendar.MINUTE, 0 );
		tempCalendar.add(Calendar.SECOND, 0 );
		tempCalendar.add(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
	}
	
	private void doTheCheckMethods() throws IOException, SQLException, BusinessException{
    	if(!initCheck){
        	checkConfiguration();
    		initCheck = true;
    	}
	}
	
	/** Short notation for MeteringWarehouse.getCurrent() */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

	private void checkConfiguration() throws IOException {
		
//		getLogger().log(Level.INFO, "Reading configuration");
		
		byte[] dailyByte = {0, 0, 0, 0, -1, -1, -1, -1, -1};
		byte[] monthlyByte = {0, 0, 0, 0, -1, -1, -1, 1, -1};
		
		if( dlmsCache.getGenericInterval1() == -1 ){
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Reading generic profile period 1.");
			dlmsCache.setGenericInterval1(getCosemObjectFactory().getProfileGeneric(genericProfile1).getCapturePeriod());
		}
		if( dlmsCache.getGenericInterval2() == -1 ){
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Reading generic profile period 2.");
			dlmsCache.setGenericInterval2(getCosemObjectFactory().getProfileGeneric(genericProfile2).getCapturePeriod());
		}
			
		if( dlmsCache.getGenericInterval3() == null ){
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Reading end of billing period 1.");			
			dlmsCache.setGenericInterval3(getCosemObjectFactory().getSingleActionSchedule(endOfBilling).getExecutionTime().getBEREncodedByteArray());
		}
		if( dlmsCache.getGenericInterval4() == null ){
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Reading end of billing period 2.");
			dlmsCache.setGenericInterval4(getCosemObjectFactory().getSingleActionSchedule(endOfCapturedObjects).getExecutionTime().getBEREncodedByteArray());
		}
		
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
		} else{
			getLogger().log(Level.INFO, "Iskra Mx37x, checkConfiguration, no dailyProfile configured for meter " + strID);
		}
		
		if (isMonthlyArray(new Array(dlmsCache.getGenericInterval3(),0,0))){
			monthlyObisCode = genericProfile3;
		} else if (isMonthlyArray(new Array(dlmsCache.getGenericInterval4(),0,0))){
			monthlyObisCode = genericProfile4;
		} else{
			getLogger().log(Level.INFO, "Iskra Mx37x, checkConfiguration, no monthlyProfile configured for meter " + strID);
		}

		if(dlmsCache.getMonthlyProfileConfig() == null){
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Reading Monthly captured Objects");
			List capObjects = getCosemObjectFactory().getProfileGeneric(monthlyObisCode).getCaptureObjects();
			Iterator it = capObjects.iterator();
			while(it.hasNext()){
				CapturedObject co = (CapturedObject)it.next();
				if( (co.getClassId() == 3) || (co.getClassId() == 4) ){    //registers and extended registers
					monthlyProfilConfig.add(co.getLogicalName().getObisCode());
				}
			}
			dlmsCache.setMonthlyProfileConfig(monthlyProfilConfig);
		}
		
		ObisCodeMapper.setDailyObisCode(dailyObisCode);
        ObisCodeMapper.setMonthlyObisCode(monthlyObisCode);
        ObisCodeMapper.setProfileConfiguration(dlmsCache.getMonthlyProfileConfig());
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
		String mSerial = "";
		if(!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))){
			for(int i = 0; i < MBUS_MAX; i++){
				int mbusAddress = (int)getCosemObjectFactory().getCosemObject(mbusPrimaryAddress[i]).getValue();
				if(mbusAddress > 0){
					mSerial = getMbusSerial(mbusCustomerID[i]);
					if(!mSerial.equals("")){
						Unit mUnit = getMbusUnit(mbusUnit[i]);
						int mMedium = (int)getCosemObjectFactory().getCosemObject(mbusMedium[i]).getValue();
						Rtu mbusRtu = findOrCreateNewMbusDevice(mSerial);
						if(mbusRtu != null){
							mbusDevices[i] = new MbusDevice(mbusAddress, i, mSerial, mMedium, mbusRtu, mUnit, getLogger());
						} else {
							mbusDevices[i] = null;
						}
					} else {
						mbusDevices[i] = null;
					}
				} else {
					mbusDevices[i] = null;
				}
			}
		}
		updateMbusDevices(rtu.getDownstreamRtus());
	}
	
	private void updateMbusDevices(List<Rtu> downstreamRtus) throws SQLException, BusinessException{
		Iterator<Rtu> it = downstreamRtus.iterator();
		int count = 0;
		boolean delete = true;
		while(it.hasNext()){
			Rtu mbus = it.next();
			delete = true;
			for(int i = 0; i < mbusDevices.length; i++){
    			if(mbusDevices[i] != null){
    				if(mbus.getSerialNumber().equalsIgnoreCase(mbusDevices[i].getCustomerID())){
    					delete = false;
    				}
    			}
    		}
    		if(delete){
//    			mbus.updateGateway(null);	 // you can do this in the latest build of EIServer
    			RtuShadow shadow = mbus.getShadow();
    			shadow.setGatewayId(0);
    			mbus.update(shadow);
    		}
		}
	}

	private Unit getMbusUnit(ObisCode obisCode) throws IOException {
		try {
			String vifResult = Integer.toString((int)getCosemObjectFactory().getData(obisCode).getData()[2], 16);
			ValueInformationfieldCoding vif = ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding(Integer.parseInt(vifResult, 16), -1);
			return vif.getUnit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the MBus Unit");
		}
	}

	private Rtu findOrCreateNewMbusDevice(String customerID) throws SQLException, BusinessException, IOException {
		List mbusList = mw().getRtuFactory().findBySerialNumber(customerID);
		if( mbusList.size() == 1 ) {
			((Rtu)mbusList.get(0)).updateGateway(rtu);
			return (Rtu)mbusList.get(0);
		}
        if( mbusList.size() > 1 ) {
            getLogger().severe( toDuplicateSerialsErrorMsg(customerID) );
            return null;
        }
        RtuType rtuType = getRtuType();
        if (rtuType == null)
        	return null;
        else
        	return createMeter(rtu, getRtuType(), customerID);
	}
	
    private Rtu createMeter(Rtu rtu2, RtuType type, String customerID) throws SQLException, BusinessException {
        RtuShadow shadow = type.newRtuShadow();
        
        Date lastreading = shadow.getLastReading();
        
    	shadow.setName(customerID);
        shadow.setSerialNumber(customerID);

        String folderExtName = getFolderID();
    	if(folderExtName != null){
    		Folder result = mw().getFolderFactory().findByExternalName(folderExtName);
    		if(result != null){
    			shadow.setFolderId(result.getId());
    		} else {
    			getLogger().log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
    		}
    	} else {
    		getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
    	}    
    	
    	shadow.setGatewayId(rtu.getId());
    	shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);
	}
    
	/**
	 * @param concentrator
	 * @return the folderID of the given rtu
	 */
	private String getFolderID(){
		String folderid = getProperty(FOLDER_EXT_NAME);
		return folderid;
	}

	private RtuType getRtuType() throws IOException {
    	String type = getProperty(RTU_TYPE);
    	if (Utils.isNull(type)) {
    		getLogger().warning("No automatic meter creation: no property RtuType defined.");
    		return null;
    	}
    	else {
           RtuType rtuType = mw().getRtuTypeFactory().find(type);
           if (rtuType == null)
        	   throw new IOException("Iskra Mx37x, No rtutype defined with name '" + type + "'");
           if (rtuType.getPrototypeRtu() == null)
        	   throw new IOException("Iskra Mx37x, rtutype '" + type + "' has not prototype rtu");
           return rtuType;
        }
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
		
		try {
			if (connectionMode == 0){
				dlmsConnection = new HDLCConnection(is, os,	iHDLCTimeoutProperty, 100, iProtocolRetriesProperty,iClientMacAddress, iServerLowerMacAddress,iServerUpperMacAddress, addressingMode);
			} else {
				dlmsConnection = new TCPIPConnection(is, os, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty,iClientMacAddress, iServerLowerMacAddress);
			}
			
			dlmsConnection.setIskraWrapper(1);
			
		} catch (DLMSConnectionException e) {
			throw new IOException(e.getMessage());
		}
	}

	private void connect() throws IOException, DLMSConnectionException, SQLException, BusinessException {
			getDLMSConnection().connectMAC();
			secureConnection = new SecureConnection(iSecurityLevelProperty,
					firmwareVersion, strPassword, getDLMSConnection());
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Starting the Cache mechanism(checking if cache exists, reading cache if it doesn't exist)");
			if(TESTLOGGING >= 1) getLogger().log(Level.INFO, "GN - TESTLOG: Starting the Cache mechanism does not mean saving to disk.");
			Object temp = GenericCache.startCacheMechanism(rtu);
			dlmsCache = (temp == null)?new Cache():(Cache)temp;
			collectCache();
			if(meterConfig.getCapturedObjectList() == null)
				meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
			if (!verifyMeterID())
				throw new IOException("Iskra Mx37x, connect, Wrong DeviceID!, settings=" + strID + ", meter=" + getDeviceAddress());
			if (!verifyMeterSerialNR())
				throw new IOException("Iskra Mx37x, connect, Wrong SerialNR!, settings=" + serialNumber + ", meter=" + getSerialNumber());
			if (extendedLogging >= 1)
				logger.info(getRegistersInfo(extendedLogging));
	}
	
	private void disConnect(){
	       try {
	         	  secureConnection.disConnect();
	         	  getDLMSConnection().disconnectMAC();
	        } catch(DLMSConnectionException e) {
	           logger.severe("DLMSLN: disconnect(), "+e.getMessage());
	        } catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void mbusMeterDeletionCheck() throws SQLException, BusinessException, IOException{
		if(!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))){
			for(int i = 0; i < dlmsCache.getMbusCount(); i++){
				if(rtuExists(dlmsCache.getMbusCustomerID(i))){
					mbusDevices[i] = new MbusDevice((int)dlmsCache.getMbusAddress(i), dlmsCache.getMbusPhysicalAddress(i), dlmsCache.getMbusCustomerID(i),
							dlmsCache.getMbusMedium(i), findOrCreateNewMbusDevice(dlmsCache.getMbusCustomerID(i)), dlmsCache.getMbusUnit(i), getLogger());
				} else {
					forcedMbusCheck = true;
					break;
				}
			}
		}
	}
	
	/**
	 * Checks if the Rtu with the serialnumber exists in database
	 * @param serial
	 * @return true or false
	 */
	protected boolean rtuExists(String serial){
		List meterList = mw().getRtuFactory().findBySerialNumber(serial);
		if(meterList.size() == 1)
			return true;
		else if (meterList.size() == 0)
			return false;
		else{	// should never get here, no multiple serialNumbers can be allowed
			getLogger().severe(toDuplicateSerialsErrorMsg(serial));
		}
		return false;
	}
	
	private void collectCache() throws IOException, SQLException, BusinessException {
        int iConf;
        getLogger().log(Level.INFO, "Reading configuration");
        
        if (dlmsCache.getObjectList() != null) {
            meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
            mbusMeterDeletionCheck();
            
            try {
                iConf = requestConfigurationProgramChanges();
            }
            catch(IOException e) {
                e.printStackTrace();
                iConf=-1;
                logger.severe("Iskra Mx37x: Configuration change is not accessible, request object list...");
                requestObjectList();
                checkMbusDevices();		
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                dlmsCache.setMbusParameters(mbusDevices);
            }

            if ((iConf != dlmsCache.getConfProgChange())) {
                
            	if (DEBUG>=1) System.out.println("iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());    
                
            	logger.severe("Iskra Mx37x: Configuration changed, request object list...");
                requestObjectList();	// request object list again from rtu
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                dlmsCache.setMbusParameters(mbusDevices);
                
                
                if (DEBUG>=1) System.out.println("after requesting objectlist (conf changed)... iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());  
            }
            if(forcedMbusCheck){	// you do not need to read the whole cache if you just changed the mbus meters
            	checkMbusDevices();		
            }
        }
        
        else { // Cache not exist
            logger.info("Iskra Mx37x: Cache does not exist, request object list.");
            requestObjectList();
            checkMbusDevices();		
            try {
                iConf = requestConfigurationProgramChanges();
              
                dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                dlmsCache.setMbusParameters(mbusDevices);
                
                if (DEBUG>=1) System.out.println("after requesting objectlist... iConf="+iConf+", dlmsCache.getConfProgChange()="+dlmsCache.getConfProgChange());  
            }
            catch(IOException e) {
                iConf=-1;
            }
        }
	}
	
	private Clock getClock() throws IOException{
		if(this.clock == null){
			this.clock = getCosemObjectFactory().getClock();
		}
		return this.clock;
	}
	
    public Date getTime() throws IOException {
        return getClock().getDateTime();
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
        	getLogger().log(Level.INFO, "Setting meterTime");
            setTimeClock();
        }
    }
    
    public void setTimeClock() throws IOException
    {
//       Calendar calendar=null;
//       if (iRequestTimeZone != 0)
//           calendar = ProtocolUtils.getCalendar(false,requestTimeZone());
//       else
//           calendar = ProtocolUtils.initCalendar(false,getTimeZone());
//       calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);           
//       doSetTime(calendar);
    	doSetTime(Calendar.getInstance(getTimeZone()));
    }
    
//    public int requestTimeZone() throws IOException {
//        if (deviation == -1) { 
//            Clock clock = getCosemObjectFactory().getClock();
//            deviation = clock.getTimeZone();
//        }
//        return (deviation);
//     }
    
    private void doSetTime(Calendar calendar) throws IOException
    {
//    	byte[] byteTimeBuffer = createByteDate(calendar);
//       
//       getCosemObjectFactory().writeObject(clockObisCode,8,2, byteTimeBuffer);
    	getClock().setTimeAttr(new DateTime(calendar));
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
	
	public Calendar getToCalendar(){
		return ProtocolUtils.getCalendar(getTimeZone());
	}
	
	/**
	 * @param channel
	 * @return a Calendar object from the lastReading of the given channel, if the date is NULL,
	 * a date from one month ago is created at midnight.
	 */
	public Calendar getFromCalendar(Channel channel){
		Date lastReading = channel.getLastReading();
		if(lastReading == null){
			lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastReading);
		return cal;
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
        
        // check if the customerID from the meter matches the customerID from the cache
        if(!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))){
        	String meterCustomerID;
        	String customerID;
        	for(int i = 0; i < MBUS_MAX; i++){
        		customerID = dlmsCache.getMbusCustomerID(i);
        		meterCustomerID = getMbusSerial(mbusCustomerID[i]);
        		if(customerID != null){
        			if(!customerID.equalsIgnoreCase(meterCustomerID)){
        				forcedMbusCheck = true;
        				break;
        			}
        		} else {
        			if(!meterCustomerID.equals("")){
        				forcedMbusCheck = true;
        				break;
        			}
        		}
        	}
        }
        
        return configProgramChanges;
    }
    
    private String getMbusSerial(ObisCode oc) throws IOException{
    	try {
			String str = "";
			byte[] data = getCosemObjectFactory().getData(oc).getData();
			byte[] parseStr = new byte[data.length - 2];
			System.arraycopy(data, 2, parseStr, 0, parseStr.length);
			if(com.energyict.genericprotocolimpl.common.ParseUtils.checkIfAllAreChars(parseStr)){
				str = new String(parseStr);
			} else{
				str = com.energyict.genericprotocolimpl.common.ParseUtils.decimalByteToString(parseStr);
			}
			return str;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the MBus serialNumber");
		}
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
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","17").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","1").trim());
            firmwareVersion=properties.getProperty("FirmwareVersion","ANY");
            nodeId = rtu.getNodeAddress();
            serialNumber = rtu.getSerialNumber();
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0"));            
            addressingMode=Integer.parseInt(properties.getProperty("AddressingMode","2"));              
            connectionMode = Integer.parseInt(properties.getProperty("Connection","1")); // 0=HDLC, 1= TCP/IP
            rtuType = properties.getProperty("RtuType","");
            TESTLOGGING = Integer.parseInt(properties.getProperty("TestLogging" , "0"));
            	 
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
		return "$Date$";
	}

	public List getOptionalKeys() {
        List result = new ArrayList(16);
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
        result.add("TestLogging");
        result.add("FolderExtName");
        return result;
	}

	public List getRequiredKeys() {
        List result = new ArrayList();
//        result.add("Connection");
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
		try {
			return isRequestTimeZone()?TimeZone.getTimeZone(Integer.toString(getClock().getTimeZone())):rtu.getDeviceTimeZone();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().log(Level.INFO, "Could not verify meterTimeZone so EIServer timeZone is used.");
			return rtu.getDeviceTimeZone();
		}
	}

	public boolean isRequestTimeZone() {
		return (this.iRequestTimeZone==1)?true:false;
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
	private void sendMeterMessages() throws IOException, BusinessException, SQLException {
		
		Iterator mi = rtu.getPendingMessages().iterator();
		if(mi.hasNext())
			getLogger().log(Level.INFO, "Handling messages for meter with serialnumber: " + rtu.getSerialNumber());
		
		while(mi.hasNext()){
            RtuMessage msg = (RtuMessage) mi.next();
            String msgString = msg.getContents();
            String contents = msgString.substring(msgString.indexOf("<")+1, msgString.indexOf(">"));
            if (contents.endsWith("/"))
            	contents = contents.substring(0, contents.length()-1);
            BigDecimal breakerState = null;
            
            boolean disconnect 	= contents.equalsIgnoreCase(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connect 	= contents.equalsIgnoreCase(RtuMessageConstant.CONNECT_LOAD);
            boolean ondemand 	= contents.equalsIgnoreCase(RtuMessageConstant.READ_ON_DEMAND);
            boolean threshpars  = contents.equalsIgnoreCase(RtuMessageConstant.PARAMETER_GROUPID);
            boolean threshold  	= contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_GROUPID);
            boolean thresholdcl = contents.equalsIgnoreCase(RtuMessageConstant.CLEAR_THRESHOLD);
            boolean falsemsg	= contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_STARTDT) || contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_STOPDT);
            boolean tou			= contents.equalsIgnoreCase(RtuMessageConstant.TOU_SCHEDULE);
            boolean apnUnPw		= contents.equalsIgnoreCase(RtuMessageConstant.GPRS_APN) ||
            							contents.equalsIgnoreCase(RtuMessageConstant.GPRS_USERNAME) ||
            							contents.equalsIgnoreCase(RtuMessageConstant.GPRS_PASSWORD);
            
            if (falsemsg){
            	msg.setFailed();
        		AMRJournalManager amrJournalManager = 
        			new AMRJournalManager(rtu, scheduler);
        		amrJournalManager.journal(
        				new AmrJournalEntry(AmrJournalEntry.DETAIL, "No groupID was entered."));
        		amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
        		amrJournalManager.updateRetrials();
        		getLogger().severe("No groupID was entered.");
            }
            
            if (connect || disconnect){
                if (disconnect){
                	getLogger().log(Level.INFO, "Sending disconnect message for meter with serialnumber: " + rtu.getSerialNumber());
                	cosemObjectFactory.writeObject(breakerObisCode, 1, 2, disconnectMsg);
                	breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }
                
                if (connect){
                	getLogger().log(Level.INFO, "Sending connect message for meter with serialnumber: " + rtu.getSerialNumber());
                	cosemObjectFactory.writeObject(breakerObisCode, 1, 2, connectMsg);
                	breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }
        		
        		switch(breakerState.intValue()){
        		
        		case 0: {
        			if ( contents.indexOf(RtuMessageConstant.DISCONNECT_LOAD) != -1 )
        				msg.confirm();
        			else 
        	            msg.setFailed();          
        		}break;
        		
        		case 1: {
        			if ( contents.indexOf(RtuMessageConstant.CONNECT_LOAD) != -1 )
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

			if(tou) {
				sendActivityCalendar(contents,msg);
			}
            if (ondemand) {
            	onDemand(rtu,msg);
            }
            
            if (threshpars){
            	thresholdParameters(msg);
            }
            
            if (threshold){
            	applyThresholdValue(msg);
            }
            
            if (thresholdcl){
            	clearThreshold(msg);
            }
            
            if(apnUnPw){
            	changeApnUserNamePassword(msg);
            }
		}
	}
	
	private void changeApnUserNamePassword(RtuMessage msg) throws BusinessException, SQLException {
		String description = "Changing apn/username/password for meter with serialnumber: " + rtu.getSerialNumber();
		
			try {
				String apn = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_APN);
				if(apn.equalsIgnoreCase("")){
					throw new ApplicationException("APN value is required for message " + msg.displayString());
				}
				String userName = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_USERNAME);
				String pass = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_PASSWORD);
				
				PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
				pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
				pppat.setUserName(userName);
				pppat.setPassWord(pass);
				
				getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
				
				getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
				
				msg.confirm();
				
			} catch (Exception e) {
				e.printStackTrace();
				fail(e, msg, description);
			}
			
			
		
	}
	
	private void clearThreshold(RtuMessage msg) throws BusinessException, SQLException {
		String description = "Clear threshold for meter with serialnumber: " + rtu.getSerialNumber();
		try{
			
			String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.CLEAR_THRESHOLD);
			if(groupID.equalsIgnoreCase(""))
				throw new BusinessException("No groupID was entered.");
			
			int grID = 0;
			
        	try{
		    	grID = Integer.parseInt(groupID);
		    	crGroupIDMsg[1]=(byte)(grID >> 8);
		    	crGroupIDMsg[2]=(byte)grID;
		    	
        	} catch(NumberFormatException e){
        		throw new BusinessException("Invalid groupID");
        	}
			
	    	String startDate = "";
	    	String stopDate = "";
	    	Calendar startCal = null; 
	    	Calendar stopCal = null;
			
    		startCal = Calendar.getInstance(getTimeZone());
    		stopCal = startCal;
    		
	    	long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis()))/1000;
	    	crDurationMsg[1]=(byte)(crDur >> 24);
	    	crDurationMsg[2]=(byte)(crDur >> 16);
	    	crDurationMsg[3]=(byte)(crDur >> 8);
	    	crDurationMsg[4]=(byte)crDur;
	    	byte[] byteDate = createByteDate(startCal);
	    	
	    	getLogger().log(Level.INFO, description);
			getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
			getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
			getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);
			
			msg.confirm();
			
		} catch(Exception e) {
			fail(e, msg, description);
    	}
	}

	private void applyThresholdValue(RtuMessage msg) throws BusinessException, SQLException {
		String description = "Setting threshold value for meter with serialnumber: " + rtu.getSerialNumber();
		try{
			
			String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_GROUPID);
			if(groupID.equalsIgnoreCase(""))
				throw new BusinessException("No groupID was entered.");
			
			int grID = 0;
			
        	try{
		    	grID = Integer.parseInt(groupID);
		    	crGroupIDMsg[1]=(byte)(grID >> 8);
		    	crGroupIDMsg[2]=(byte)grID;
		    	
        	} catch(NumberFormatException e){
        		throw new BusinessException("Invalid groupID");
        	}
			
	    	String startDate = "";
	    	String stopDate = "";
	    	Calendar startCal = null; 
	    	Calendar stopCal = null;
			
        	startDate = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_STARTDT);
        	stopDate = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_STOPDT);
        	startCal = (startDate.equalsIgnoreCase(""))?Calendar.getInstance(getTimeZone()):getCalendarFromString(startDate);
        	if (stopDate.equalsIgnoreCase("")){
        		stopCal = Calendar.getInstance();
        		stopCal.setTime(startCal.getTime());
        		stopCal.add(Calendar.YEAR, 1);
        	}else{
        		stopCal = getCalendarFromString(stopDate);
        	}
        	
	    	long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis()))/1000;
	    	crDurationMsg[1]=(byte)(crDur >> 24);
	    	crDurationMsg[2]=(byte)(crDur >> 16);
	    	crDurationMsg[3]=(byte)(crDur >> 8);
	    	crDurationMsg[4]=(byte)crDur;
	    	byte[] byteDate = createByteDate(startCal);
	    	
	    	getLogger().log(Level.INFO, description);
			getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
			getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
			getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);
			
			msg.confirm();
			
		} catch(Exception e) {
			fail(e, msg, description);
    	}
	}

	private void thresholdParameters(RtuMessage msg) throws BusinessException, SQLException {
		String description = "Sending threshold configuration for meter with serialnumber: " + rtu.getSerialNumber();
		try {
			
			String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.PARAMETER_GROUPID);
			if(groupID.equalsIgnoreCase(""))
				throw new BusinessException("No groupID was entered.");
			
			String thresholdPL = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_POWERLIMIT);
			String contractPL = getMessageValue(msg.getContents(), RtuMessageConstant.CONTRACT_POWERLIMIT);
        	if ( (thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase("")) )
    			throw new BusinessException("Neighter contractual nor threshold limit was given.");
        	
        	long conPL = 0;
        	long limit = 0;
        	int grID = -1;
        	
        	try{
		    	grID = Integer.parseInt(groupID);
		    	crMeterGroupIDMsg[1]=(byte)(grID >> 8);
		    	crMeterGroupIDMsg[2]=(byte)grID;
		    	
		    	if(!contractPL.equalsIgnoreCase("")){
		    		conPL = Integer.parseInt(contractPL);
		    		contractPowerLimitMsg[1]=(byte)(conPL >> 24);
		    		contractPowerLimitMsg[2]=(byte)(conPL >> 16);
		    		contractPowerLimitMsg[3]=(byte)(conPL >> 8);
		    		contractPowerLimitMsg[4]=(byte)conPL;
		    	}
		    	
		    	if(!thresholdPL.equalsIgnoreCase("")){
		    		limit = Integer.parseInt(thresholdPL);
			    	crPowerLimitMsg[1]=(byte)(limit >> 24);
			    	crPowerLimitMsg[2]=(byte)(limit >> 16);
			    	crPowerLimitMsg[3]=(byte)(limit >> 8);
			    	crPowerLimitMsg[4]=(byte)limit;
		    	}
		    	
        	} catch(NumberFormatException e){
        		throw new BusinessException("Invalid groupID");
        	}
        	getLogger().log(Level.INFO, description);
        	getCosemObjectFactory().writeObject(crMeterGroupID, 1, 2, crMeterGroupIDMsg);
        	if(!contractPL.equalsIgnoreCase(""))
        		getCosemObjectFactory().writeObject(contractPowerLimit, 3, 2, contractPowerLimitMsg);
        	if(!thresholdPL.equalsIgnoreCase(""))
        		getCosemObjectFactory().writeObject(crPowerLimit, 3, 2, crPowerLimitMsg);
        	
        	msg.confirm();
        	
		} catch (Exception e) {
			fail(e, msg, description);
    	}
		
	}
	
	protected void onDemand(Rtu rtu, RtuMessage msg) throws IOException, SQLException, BusinessException {
		String description = "Getting ondemand registers for meter with serialnumber: " + rtu.getSerialNumber();
		try {
			getLogger().log(Level.INFO, description);
			MeterReadingData mrd = new MeterReadingData();
	    	Iterator i = rtu.getRtuType().getRtuRegisterSpecs().iterator();
	        while (i.hasNext()) {
	        	
	            RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
	            ObisCode oc = spec.getObisCode();
	            RtuRegister register = rtu.getRegister( oc );
	            
	            if (register != null){
	            	
	            	if (oc.getF() == 255){
	                	RegisterValue rv = readRegister(oc);
	                	rv.setRtuRegisterId(register.getId());
	                	mrd.add(rv);
	                }
	            }
				else {
					String obis = oc.toString();
					String msgError = "Register " + obis + " not defined on device";
					getLogger().info(msgError);
				}
	        }
	        rtu.store(mrd);
	    	msg.confirm();
		}
		catch (Exception e) {
			fail(e, msg, description);
    	}
	}
	
	protected void fail(Exception e, RtuMessage msg, String description) throws BusinessException, SQLException {
		msg.setFailed();
		AMRJournalManager amrJournalManager = 
			new AMRJournalManager(rtu, scheduler);
		amrJournalManager.journal(
				new AmrJournalEntry(AmrJournalEntry.DETAIL, description + ": " + e.toString()));
		amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
		amrJournalManager.updateRetrials();
		getLogger().severe(e.toString());
	}
	
	public void sendActivityCalendar(String contents, RtuMessage msg) throws SQLException, BusinessException, IOException  {
		String description = 
			"Sending new Tariff Program message to meter with serialnumber: " + rtu.getSerialNumber();
		try {
			getLogger().log(Level.INFO, description);
			UserFile userFile = getUserFile(msg.getContents());
	    	
	    	ActivityCalendar activityCalendar =
	    		getCosemObjectFactory().getActivityCalendar(ObisCode.fromString("0.0.13.0.0.255"));
	    
	    	com.energyict.genericprotocolimpl.common.tou.ActivityCalendar calendarData = 
	    		new com.energyict.genericprotocolimpl.common.tou.ActivityCalendar();
	    	ActivityCalendarReader reader = new IskraActivityCalendarReader(calendarData, getTimeZone(), getMeter().getTimeZone());
	    	calendarData.setReader(reader);
	    	calendarData.read(new ByteArrayInputStream(userFile.loadFileInByteArray()));
	    	CosemActivityCalendarBuilder builder = new 
	    		CosemActivityCalendarBuilder(calendarData);
    	
	        activityCalendar.writeCalendarNamePassive(builder.calendarNamePassive());
	        activityCalendar.writeSeasonProfilePassive(builder.seasonProfilePassive());
	        activityCalendar.writeWeekProfileTablePassive(builder.weekProfileTablePassive());
	        activityCalendar.writeDayProfileTablePassive(builder.dayProfileTablePassive());
	        if (calendarData.getActivatePassiveCalendarTime() != null){
	        	activityCalendar.writeActivatePassiveCalendarTime(builder.activatePassiveCalendarTime());
	        } else {
	        	activityCalendar.activateNow();
	        }
	        
	        // check if xml file contains special days
	        int newSpecialDays = calendarData.getSpecialDays().size();
	        if (newSpecialDays > 0) {
		        SpecialDaysTable specialDaysTable =
		    		getCosemObjectFactory().getSpecialDaysTable(ObisCode.fromString("0.0.11.0.0.255"));
		        // delete old special days
		        Array array = specialDaysTable.readSpecialDays();
		        int currentMaxSpecialDayIndex = array.nrOfDataTypes();
		        for (int i = newSpecialDays; i < currentMaxSpecialDayIndex; i++) 
		        	calendarData.addDummyDay(i);
		        specialDaysTable.writeSpecialDays(builder.specialDays());
	        }
	        msg.confirm();
    	}
    	catch (Exception e) {
    		fail(e, msg, description);
    	}
    }
	
	protected UserFile getUserFile(String contents)throws BusinessException {
        int id = getTouFileId(contents);
        UserFile userFile = 
        	MeteringWarehouse.getCurrent().getUserFileFactory().find(id);
        if (userFile == null)
        	throw new BusinessException("No userfile found with id " + id);
        return userFile;
	}
	
	protected int getTouFileId(String contents) throws BusinessException {
		int startIndex = 2 + RtuMessageConstant.TOU_SCHEDULE.length();  // <TOU>
		int endIndex = contents.indexOf("</" + RtuMessageConstant.TOU_SCHEDULE + ">");
		String value = contents.substring(startIndex, endIndex);
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new BusinessException("Invalid userfile id: " + value);
		}
	}

	private Calendar getCalendarFromString(String strDate) throws BusinessException {
		try{
			Calendar cal = Calendar.getInstance(getTimeZone());
	    	cal.set(Calendar.DATE, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))));
	    	cal.set(Calendar.MONTH, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
	    	cal.set(Calendar.YEAR, Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));
	    	
	    	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
	    	cal.set(Calendar.MINUTE, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
	    	cal.set(Calendar.SECOND, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
	    	cal.clear(Calendar.MILLISECOND);
	    	return cal;
		}
    	catch(NumberFormatException e){
    		throw new BusinessException("Invalid dateTime format for the applyThreshold message.");
    	}
		
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
        MessageCategorySpec cat2 = new MessageCategorySpec("ThresholdMessages");
        
        MessageSpec msgSpec = addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("ReadOnDemand", RtuMessageConstant.READ_ON_DEMAND, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addTouMessage("Set new tariff program", RtuMessageConstant.TOU_SCHEDULE, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addThresholdParameters("Threshold parameters", RtuMessageConstant.THRESHOLD_PARAMETERS, false);
        cat2.addMessageSpec(msgSpec);
        msgSpec = addThresholdMessage("Apply Threshold", RtuMessageConstant.APPLY_THRESHOLD, false);
        cat2.addMessageSpec(msgSpec);
        msgSpec = addClearThresholdMessage("Clear Threshold", RtuMessageConstant.CLEAR_THRESHOLD, false);
        cat2.addMessageSpec(msgSpec);
        msgSpec = addGPRSModemSetup("Change GPRS Modem setup", RtuMessageConstant.GPRS_MODEM_SETUP, false);
        cat.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        theCategories.add(cat2);
        return theCategories;
	}

	private MessageSpec addClearThresholdMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
	    MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.CLEAR_THRESHOLD);
	    tagSpec.add(new MessageValueSpec());
	    msgSpec.add(tagSpec);
		return msgSpec;
	}
	
	private MessageSpec addGPRSModemSetup(String keyId, String tagName, boolean advanced){
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_APN);
		tagSpec.add(new MessageValueSpec());
		msgSpec.add(tagSpec);
		tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_USERNAME);
	    tagSpec.add(new MessageValueSpec());
	    msgSpec.add(tagSpec);
		tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_PASSWORD);
	    tagSpec.add(new MessageValueSpec());
	    msgSpec.add(tagSpec);
	    return msgSpec;
	}

	private MessageSpec addThresholdParameters(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
	    MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.PARAMETER_GROUPID);
	    tagSpec.add(new MessageValueSpec());
	    msgSpec.add(tagSpec);
	    tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_POWERLIMIT);
	    tagSpec.add(new MessageValueSpec());
	    msgSpec.add(tagSpec);
	    tagSpec = new MessageTagSpec(RtuMessageConstant.CONTRACT_POWERLIMIT);
	    tagSpec.add(new MessageValueSpec());
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

	private MessageSpec addThresholdMessage(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
    	MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_GROUPID);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_STARTDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_STOPDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
	}

	public Rtu getMeter() {
		return this.rtu;
	}
	
	public ObisCode getMbusLoadProfile(int address){
		return mbusLProfileObisCode[address];
	}

	public ObisCode getDailyLoadProfile() {
		return dailyObisCode;
	}

	public ObisCode getMonthlyLoadProfile() {
		return monthlyObisCode;
	}
}
