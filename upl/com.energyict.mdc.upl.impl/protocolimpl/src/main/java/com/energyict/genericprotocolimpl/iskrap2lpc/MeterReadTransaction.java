package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedByte;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedShort;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DatabaseException;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.cpo.SqlBuilder;
import com.energyict.cpo.Transaction;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ScalerUnit;
import com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator.XmlException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.amrimpl.RtuRegisterReadingImpl;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

/**
 * Meter handling: 
 *  - find or create meter 
 *  - read meter 
 *  - export message 
 * Transaction: all operations for a meter fail or all succeed.
 * 
 * NOTE:
 * In several methods you will see an IF-ELSE structure with a TESTING variable, this is only necessary for UnitTesting so we can actually 
 * store meterData in the database, sometimes it is used to set configuration which we normally should have read from the meter.
 */
class MeterReadTransaction implements CacheMechanism {
	
//	1.8.0+9:2.8.0+9:1.8.1+9d:1.8.2+9d:2.8.1+9d:2.8.2+9d:1.8.1+9m:1.8.2+9m:2.8.1+9m:2.8.2+9m
	
	protected boolean TESTING = false;
	protected String billingMonthly = "";
	protected String billingDaily = "";
	private String[] profileTestName;
	protected boolean DEBUG = false;
    
    static final int ELECTRICITY 	= 0x00;
    static final int MBUS 			= 0x01;
	
    /**
	 * a private instance of the concentrator class
	 */
	private final Concentrator concentrator;

    /** Cached Objects */
	public int confProgChange;
	public int loadProfilePeriod1;
	public int loadProfilePeriod2;
	public boolean changed;
	public ObjectDef[] loadProfileConfig1;
	public ObjectDef[] loadProfileConfig2;
	public ObjectDef[] loadProfileConfig3;
	public ObjectDef[] loadProfileConfig4;
	public CosemDateTime billingReadTime;
	public CosemDateTime captureObjReadTime;
	
	private ProtocolChannelMap protocolChannelMap = null;
	private CommunicationProfile 	communicationProfile;
	private StoreObject storeObject;
    private Rtu rtuConcentrator;
    private Rtu meter;
    private String serial;
    private String mbusSerial[] = {null, null, null, null};
    private String mSerial;
	private Cache dlmsCache;
	private boolean initCheck = false;
	
	protected MbusDevice[]			mbusDevices = {null, null, null, null};
	
    public MeterReadTransaction(Concentrator concentrator, Rtu rtuConcentrator, String serial, CommunicationProfile communicationProfile) {
        
        this.concentrator = concentrator;
		this.rtuConcentrator = rtuConcentrator;
        this.serial = serial;
        this.communicationProfile = communicationProfile;
        this.storeObject = new StoreObject();
        this.dlmsCache = new Cache();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.cpo.Transaction#doExecute()
     */
    public void doExecute() throws BusinessException, SQLException {
    	boolean succes = false;
        
    	ProfileData[] pd = {new ProfileData(), new ProfileData()};
    	XmlHandler dataHandler;		// the dataHandler constructs the loadProfile as well as the billing profiles with the given channelMap
    	
        try {
        	
        	meter = findOrCreate(rtuConcentrator, serial, ELECTRICITY);
            if (getMeter() != null) {
            	
//            	doTheCheckMethods();	// enable this for quick cache reading
            	
                // Import profile
                if( communicationProfile.getReadDemandValues() ) {
                	doTheCheckMethods();
                	dataHandler = new XmlHandler( getLogger(), getChannelMap() );
                	dataHandler.setChannelUnit(Unit.get(BaseUnit.WATTHOUR, 3));
                	importProfile(meter, dataHandler, communicationProfile.getReadMeterEvents());
                	if(mbusCheck()){
                		dataHandler = new XmlHandler( getLogger(), mbusDevices[0].getChannelMap() );
                		dataHandler.setChannelUnit(mbusDevices[0].getMbusUnit());
                		importProfile(mbusDevices[0].getRtu(), dataHandler, false);	//MBus device does not have events
                		dataHandler.clearChannelUnit();
                	}
                }
                
                // Import Daily and Monthly registers
                if( communicationProfile.getReadMeterReadings() ){
                	doTheCheckMethods();
                	dataHandler = new XmlHandler(getLogger(), getChannelMap());
                	dataHandler.setDailyMonthlyProfile(true);
                	dataHandler.setChannelUnit(Unit.get(BaseUnit.WATTHOUR, 3));
                	importDailyMonthly(getMeter(), dataHandler, serial);
                	dataHandler.setDailyMonthlyProfile(false);
                	if(mbusCheck()){
                		dataHandler = new XmlHandler(getLogger(), mbusDevices[0].getChannelMap());
                    	dataHandler.setDailyMonthlyProfile(true);
                    	dataHandler.setChannelUnit(mbusDevices[0].getMbusUnit());
                    	importDailyMonthly(mbusDevices[0].getRtu(), dataHandler, serial);
                    	dataHandler.setDailyMonthlyProfile(false);
                    	dataHandler.clearChannelUnit();
                	}
                }
                
                // Send messages
                if( communicationProfile.getSendRtuMessage() ){
                	if(!initCheck){			// otherwise the MBus messages will not be executed
                		checkMbusDevices();
                	}
                	dataHandler = new XmlHandler( getLogger(), getChannelMap() );
                	sendMeterMessages(rtuConcentrator, getMeter(), dataHandler);
                	if(mbusCheck()){
                    	if ( mbusDevices[0].getRtu().getMessages().size() != 0 ){
                    		sendMeterMessages(rtuConcentrator, getMeter(), mbusDevices[0].getRtu(), dataHandler);
                    	}
                	}
                }
                
                succes = true;
                
            }
            
        } catch (ServiceException thrown) {
            getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
            
        } catch (NumberFormatException thrown) {
			getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
        	
		} catch (InvalidPropertyException thrown) {
			getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
	            
		}  catch (IOException thrown) {
            getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
            
        } catch (BusinessException thrown) {
            getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
            
        } finally {
        	if(succes){
        		Environment.getDefault().execute(getStoreObjects());
        		getLogger().log(Level.INFO, "Meter with serialnumber " + serial + " has completely finished");
        	}
        }
        
//        return getMeter(); /* return whatever */
        
    }
    
    private Rtu getMeter(){
    	return meter;
    }
    
    protected void setMeter(Rtu meter){
    	this.meter = meter;
    }
    
	/**
     * @param
     * Import:
     *   (1) ProfileData
     *   (2) If events enabled -> Events
	 * @throws SQLException 
     */
    protected void importProfile(Rtu meter, XmlHandler dataHandler, boolean bEvents) throws ServiceException, IOException, BusinessException, SQLException {
    
        String xml = null;        
        String profile = null;
        String mtr = getMeter().getSerialNumber();
        
        String from = Constant.getInstance().format( new Date() );
        String to = Constant.getInstance().format( new Date() );
        
        String lpString1 = "99.1.0";
        String lpString2 = "99.2.0";
        
        /*
         * Read profile data 
         */
    	getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + meter.getSerialNumber() + ".");
        
        ObjectDef[] lp1 = null;
        ObjectDef[] lp2 = null;
        ObjectDef[] loadProfileDef = null;
        int lpPeriod1 = -1;
        int lpPeriod2 = -1;
        if ( TESTING ){
        	FileReader inFile = new FileReader(Utils.class.getResource(Constant.profileConfig1).getFile());
        	xml = getConcentrator().readWithStringBuffer(inFile);
        	lp1 = getlpConfigObjectDefFromString(xml);
        	lpPeriod1 = 900;
        	inFile = new FileReader(Utils.class.getResource(Constant.profileConfig2).getFile());
        	xml = getConcentrator().readWithStringBuffer(inFile);
        	lp2 = getlpConfigObjectDefFromString(xml);
        	lpPeriod2 = 3600;
        }
        else{
        	lp1 = loadProfileConfig1;
        	lp2 = loadProfileConfig2;
        	lpPeriod1 = loadProfilePeriod1;
        	lpPeriod2 = loadProfilePeriod2;
        }
        
        if(meter.getIntervalInSeconds() == lpPeriod1){
        	loadProfileDef = lp1;
        	profile = lpString1;
        }
        else if(meter.getIntervalInSeconds() == lpPeriod2){
        	loadProfileDef = lp2;
        	profile = lpString2;
        }
        else {
        	getLogger().log(Level.SEVERE, "Interval didn't match - ProfileInterval EIServer: " + getMeter().getIntervalInSeconds());
        	throw new BusinessException("Interval didn't match");
        }
        Channel chn;
        for( int i = 0; i < dataHandler.getChannelMap().getNrOfProtocolChannels(); i ++ ) {
        
            ProtocolChannel pc = dataHandler.getChannelMap().getProtocolChannel(i);
            xml = "";
            chn = getMeterChannelWithIndex(meter, i+1);
            if(chn != null){
            	from = Constant.getInstance().format( getLastChannelReading(chn) );
            	if(!pc.containsDailyValues() && !pc.containsMonthlyValues()){
            		dataHandler.setProfileChannelIndex(i);
                	if(TESTING){
                		FileReader inFile = new FileReader(Utils.class.getResource(getProfileTestName()[i]).getFile());
                		xml = getConcentrator().readWithStringBuffer(inFile);
                	} else{
                		xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), profile, pc.getRegister(), from, to);
                	}

            	}
            }
            if(!xml.equalsIgnoreCase("")){

            	dataHandler.setChannelIndex( i );
            	getConcentrator().importData(xml, dataHandler);
            	
//            	File file = new File("c://TEST_FILES/NullPointer" + mtr + "_" + i + ".xml");
//            	FileOutputStream fos = new FileOutputStream(file);
//            	ObjectOutputStream oos = new ObjectOutputStream(fos);
//            	oos.writeObject(xml);
//            	oos.close();
//            	fos.close();
            	
            }
            
        }
        
        getLogger().log(Level.INFO, "Done reading PROFILE.");
        
        /*
         * Read logbook
         */
        if( bEvents ) {
        	
        	getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + mtr + ".");
            
            from = Constant.getInstance().format(getLastLogboog(getMeter()));
            String events, powerFailures;
            if(TESTING){
            	FileReader inFile = new FileReader(Utils.class.getResource(Constant.eventsFile).getFile());
        		events = getConcentrator().readWithStringBuffer(inFile);
        		inFile = new FileReader(Utils.class.getResource(Constant.powerDownFile).getFile());
        		powerFailures = getConcentrator().readWithStringBuffer(inFile);
            }
            else{
            	events = getConnection().getMeterEvents(mtr, from, to);
            	powerFailures = getConnection().getMeterPowerFailures(mtr, from, to);
            }
            getConcentrator().importData(events, dataHandler);
            getConcentrator().importData(powerFailures, dataHandler);
            
            getLogger().log(Level.INFO, "Done reading EVENTS.");
        }
        
	        // if complete profile is read, store it!
//	      meter.store(dataHandler.getProfileData(), false);
        getStoreObjects().add(meter, dataHandler.getProfileData());
    }

	protected void importDailyMonthly(Rtu meter, XmlHandler dataHandler, String serialNumber) throws BusinessException, ServiceException, IOException, SQLException{
    	getLogger().log(Level.INFO, "Reading Daily/Monthly values from meter with serialnumber " + meter.getSerialNumber() + ".");
    	String xml = "";
    	String daily = null;
    	String monthly = null;
    	
    	String from = Constant.getInstance().format(new Date());
        String to = Constant.getInstance().format(new Date());
    	
    	int period;
    	CosemDateTime cdt;
    	period = loadProfilePeriod2;
    	cdt = billingReadTime;
        
    	// variable configuration
    	if(TESTING){
    		daily = "98.2.0";
    		monthly = "98.1.0";
    	} else {
    		if ( period == 86400 ){ 
    			daily = "99.2.0";
    		}else
    			daily = null;
        	
    		if ( (cdt.getDayOfMonth().intValue() == 1) && (cdt.getHour().intValue() == 0) && (cdt.getYear().intValue() == 65535) && (cdt.getMonth().intValue() == 255) ){
    			monthly = "98.1.0";
    			if (daily == null) daily = "98.2.0";
    		}else{
    			monthly = "98.2.0";
    			if (daily == null) daily = "98.1.0";
    		}
    	}
		
		try {
			Channel chn;
			ProtocolChannel pc;
			ProtocolChannelMap channelMap = dataHandler.getChannelMap();
			dataHandler.setChannelIndex(0);		// we will add channel per channel
			for(int i = 0; i < channelMap.getNrOfProtocolChannels(); i++){
				pc = channelMap.getProtocolChannel(i);
				xml = "";
				chn = getMeterChannelWithIndex(meter, i+1);
				dataHandler.setProfileChannelIndex(i);
				if(chn != null){
					from = Constant.getInstance().format( getLastChannelReading(chn) );
					if(pc.containsDailyValues()){
						if(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS){
							getLogger().log(Level.INFO, "Reading Daily values with registername: " + pc.getRegister());
							if(TESTING){
			            		FileReader inFile = new FileReader(Utils.class.getResource(getBillingDaily()).getFile());
//								FileReader inFile = new FileReader(Utils.class.getResource("/offlineFiles/iskrap2lpc/nullpointerstuff.xml").getFile());
			            		xml = getConcentrator().readWithStringBuffer(inFile);
							} else {
								xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), daily, pc.getRegister(), from, to);
							}
						}
						else
							throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
					}
					else if(pc.containsMonthlyValues()){
						if(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS){
							getLogger().log(Level.INFO, "Reading Monthly values with registername: " + pc.getRegister());
							if(TESTING){
			            		FileReader inFile = new FileReader(Utils.class.getResource(getBillingMonthly()).getFile());
			            		xml = getConcentrator().readWithStringBuffer(inFile);
							} else {
								xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), monthly, pc.getRegister(), from, to);
							}
						}
						else
							throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
					}
				} else
					throw new IOException("Channel out of bound exception: no channel with profileIndex " + i+1 + " is configured on the meter.");

				if(!xml.equalsIgnoreCase("")){
					
//		        	File file = new File("c://TEST_FILES/nullpointerstuff.xml");
//		        	FileOutputStream fos = new FileOutputStream(file);
//		        	ObjectOutputStream oos = new ObjectOutputStream(fos);
//		        	oos.writeObject(xml);
//		        	oos.close();
//		        	fos.close();
					
					getConcentrator().importData(xml, dataHandler);
//					meter.store(dataHandler.getDailyMonthlyProfile(), false);
					ProfileData pd = dataHandler.getDailyMonthlyProfile();
					pd = sortOutProfileData(pd, pc);
					getStoreObjects().add(chn, pd);
					dataHandler.clearDailyMonthlyProfile();
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new ServiceException(e);
		}
    }
    
	private ProfileData sortOutProfileData(ProfileData pd, ProtocolChannel pc) {
		ProfileData profileData = new ProfileData();
		profileData.setChannelInfos(pd.getChannelInfos());
		Iterator it = pd.getIntervalIterator();
		while(it.hasNext()){
			IntervalData id = (IntervalData)it.next();
			if(pc.containsDailyValues()){
				if(checkDailyBillingTime(id.getEndTime())){
					profileData.addInterval(id);
				}
			} else if(pc.containsMonthlyValues()){
				if(checkMonthlyBillingTime(id.getEndTime())){
					profileData.addInterval(id);
				}				
			}
		}
		profileData.sort();
		return profileData;
	}
	
	private boolean checkDailyBillingTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(cal.get(Calendar.HOUR)==0 && cal.get(Calendar.MINUTE)==0 && cal.get(Calendar.SECOND)==0 && cal.get(Calendar.MILLISECOND)==0)
			return true;
		return false;
	}
	
	private boolean checkMonthlyBillingTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH)==1)
			return true;
		return false;
	}

	private Channel getMeterChannelWithIndex(Rtu meter, int profileIndex) {
		Iterator it = meter.getChannels().iterator();
		while(it.hasNext()){
			Channel chn = (Channel)it.next();
			if(chn.getLoadProfileIndex() == profileIndex)
				return chn;
		}
		return null;
	}

	
	protected void handleRegisters(XmlHandler dataHandler, Rtu meter) throws ServiceException, BusinessException, SQLException {
		
        Iterator i = dataHandler.getMeterReadingData().getRegisterValues().iterator();
        while (i.hasNext()) {
            RegisterValue registerValue = (RegisterValue) i.next();
            RtuRegister register = getMeter().getRegister( registerValue.getObisCode() );

            if( register != null ){
            	if(register.getReadingAt(registerValue.getReadTime()) == null){
//            		register.store( registerValue );
            		getStoreObjects().add(register, registerValue);
            	}
            }
            else {
                String obis = registerValue.getObisCode().toString();
                String msg = "Register " + obis + " not defined on device";
                getLogger().info( msg );
            }
        }
	}
	
    protected CosemDateTime getCosemDateTimeFromXmlString(String xml) {
    	CosemDateTime cdt = null;
    	try {
			Element topElement = getConcentrator().toDom(xml).getDocumentElement();
			UnsignedShort year = new UnsignedShort(topElement.getElementsByTagName("Year").item(0).getFirstChild().getTextContent());
			UnsignedByte month = new UnsignedByte(topElement.getElementsByTagName("Month").item(0).getFirstChild().getTextContent());
			UnsignedByte dayOfMonth = new UnsignedByte(topElement.getElementsByTagName("DayOfMonth").item(0).getFirstChild().getTextContent());
			UnsignedByte dayOfWeek = new UnsignedByte(topElement.getElementsByTagName("DayOfWeek").item(0).getFirstChild().getTextContent());
			UnsignedByte hour = new UnsignedByte(topElement.getElementsByTagName("Hour").item(0).getFirstChild().getTextContent());
			UnsignedByte minute = new UnsignedByte(topElement.getElementsByTagName("Minute").item(0).getFirstChild().getTextContent());
			cdt = new CosemDateTime(year, month, dayOfMonth, dayOfWeek, hour, minute);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return cdt;
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
	
	protected Date getLastChannelReading(Channel chn){
		Date result = chn.getLastReading();
		if(result == null){
			result = getClearLastYearDate(chn.getRtu());
		}
		return result;
	}
    
    protected Date getLastReading(Rtu rtu) {
        Date result = rtu.getLastReading();
        if( result == null )
        	result = getClearLastYearDate(rtu);
        return result;
    }
    
    protected Date getLastLogboog(Rtu rtu) {
        Date result = rtu.getLastLogbook();
        if( result == null ) 
        	result = getClearLastYearDate(rtu);
        return result;
    }
    
    private Date getClearLastYearDate(Rtu rtu){
   		Calendar tempCalendar = Calendar.getInstance(rtu.getDeviceTimeZone());
   		tempCalendar.add(Calendar.YEAR, -1);
		tempCalendar.set(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.set(Calendar.MINUTE, 0 );
		tempCalendar.set(Calendar.SECOND, 0 );
		tempCalendar.set(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
    }
    
    public ProtocolChannelMap getChannelMap() throws InvalidPropertyException, BusinessException {
    	if (protocolChannelMap == null){
    		String sChannelMap = getMeter().getProperties().getProperty( Constant.CHANNEL_MAP );
    		if(sChannelMap != null)
    			protocolChannelMap = new ProtocolChannelMap( sChannelMap );
    		else
    			throw new BusinessException("No channelmap configured on the meter, meter will not be handled.");
    	}
        return protocolChannelMap;
    }
    
    
	private String getFirwareVersions(Rtu concentrator, String meterID, ObisCode oc) throws NumberFormatException, ServiceException, IOException, BusinessException{
		String times[] = prepareCosemGetRequest();
		
		StringBuffer strBuff = new StringBuffer();
		byte[] strCore = getConnection().cosemGetRequest(meterID, times[0], times[1], oc.toString(), new UnsignedInt(1), new UnsignedInt(2));
		for(int i = 2; i < strCore.length; i++){
			String str = Integer.toHexString(strCore[i]&0xFF);
			if(str.length() == 1)
				strBuff.append("0");
			strBuff.append(str);
		}
		return strBuff.toString();
	}
	
	public static void main(String[] args){
//		StringBuilder strBuilder = new StringBuilder();
//		byte[] b = {9, 16, -124, -41, -120, 26, 86, 96, 0, -128, -3, 109, 105, -103, -108, -1, -33, -10};
//		for(int i = 2; i < b.length; i++){
//			strBuilder.append(Integer.toHexString(b[i]&0xFF));
//			strBuilder.append(" ");
//		}
//		
//		System.out.println(strBuilder);
		
		byte[] b = {2, 2, 15, 0, 22, 30};
		try {
			ScalerUnit su = new ScalerUnit(b);
			System.out.println(su);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected Rtu findOrCreate(Rtu concentrator, String serial, int type) throws SQLException, BusinessException, IOException { 
        
        List meterList = getConcentrator().mw().getRtuFactory().findBySerialNumber(serial);

        if( meterList.size() == 1 ) {
    		
        	((Rtu)meterList.get(0)).updateGateway(concentrator);
        	
        	// We may not move the meter if he already exists
//        	int folderID = getFolderID(concentrator);
//        	if(folderID != -1){
//        		Folder result = getConcentrator().mw().getFolderFactory().find(folderID);
//        		if(result != null){
//        			((Rtu)meterList.get(0)).moveToFolder(result);
//        		} else {
//        			getLogger().log(Level.INFO, "No folder found with ID: " + folderID + ", new meter will be placed in prototype folder.");
//        		}
//        	} else {
//        		getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
//        	}
        	
            return (Rtu) meterList.get(0);
        }
        
        else if( meterList.size() > 1 ) {
            getLogger().severe( toDuplicateSerialsErrorMsg(serial) );
            return null;
        }
        
        if(getConcentrator().getRtuType(concentrator) != null)
        	return createMeter(concentrator, getConcentrator().getRtuType(concentrator), serial);
        else{
        	getLogger().severe( Constant.NO_AUTODISCOVERY ); 
        	return null;
        }
    }
	
	private String getFolderID(Rtu concentrator){
		String folderid = concentrator.getProperties().getProperty(Constant.FOLDER_EXT_NAME);
		return folderid;
	}
	
	/** Create a meter for configured RtuType 
     * @throws BusinessException 
     * @throws SQLException */
    
    private Rtu createMeter(Rtu concentrator, RtuType type, String serial) throws SQLException, BusinessException{
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date lastreading = cal.getTime();
        
        RtuShadow shadow = type.newRtuShadow();
        
        shadow.setName(serial);
        shadow.setSerialNumber(serial);
        
    	String folderExtName = getFolderID(concentrator);
    	if(folderExtName != null){
    		Folder result = getConcentrator().mw().getFolderFactory().findByExternalName(folderExtName);
    		if(result != null){
    			shadow.setFolderId(result.getId());
    		} else {
    			getLogger().log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
    		}
    	} else {
    		getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
    	}        
        
    	shadow.setGatewayId(concentrator.getId());
    	shadow.setLastReading(lastreading);
        return getConcentrator().mw().getRtuFactory().create(shadow);
        
    }
	
    private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat( Constant.DUPLICATE_SERIALS )
                    .format( new Object [] { serial } );
    }

	public Logger getLogger(){
		return getConcentrator().getLogger();
	}
	
	private ObjectDef[] getlpConfigObjectDefFromString(String xml) {
    	ObjectDef[] od = {null, null, null, null, null, null, null, null, null, null};
    	try {
			Element topElement = getConcentrator().toDom(xml).getDocumentElement();
			NodeList objects = topElement.getElementsByTagName("Object");
			
			for(int i = 0; i < objects.getLength(); i++){
				Element object = (Element) objects.item(i);
				UnsignedShort classId = new UnsignedShort(object.getElementsByTagName("ClassId").item(0).getFirstChild().getTextContent());
				String instanceId = object.getElementsByTagName("InstanceId").item(0).getFirstChild().getTextContent();
				byte attributeId = (byte)Integer.parseInt(object.getElementsByTagName("AttributeId").item(0).getFirstChild().getTextContent());
				UnsignedShort dataId = new UnsignedShort(object.getElementsByTagName("DataId").item(0).getFirstChild().getTextContent());
				od[i] = new ObjectDef(classId, instanceId, attributeId, dataId);
			}
			
		} catch (XmlException e) {
			e.printStackTrace();
		}
    	
		return od;
	}
    
//	private boolean lpContainsRegister(ObjectDef[] lp, String register) {
//		for (int i = 0; i< lp.length; i++){
//			if(lp[i]!=null){
//				String instId = lp[i].getInstanceId();
//				if (register.length() == 5){
//					if (instId.indexOf(register) == 4)
//						return true;
//				}
//				else
//					if (instId.indexOf(register.subSequence(0, register.length()).toString()) >= 0)
//						return true;
//			}
//		}
//		return false;
//	}

	protected boolean isTESTING() {
		return TESTING;
	}

	protected void setTESTING(boolean testing) {
		TESTING = testing;
	}

	private Concentrator getConcentrator() {
		return this.concentrator;
	}
	
	private void testLogging(String msg){
		if(getConcentrator().getTESTLOGGING() >= 1)
			getLogger().log(Level.INFO, msg);
	}
	
	private String[] prepareCosemGetRequest(){
		String times[] = {"", ""};
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		times[0] = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		cal.add(Calendar.MINUTE, 10);
		times[1] = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		return times;
	}
	
	private Connection getConnection(){
		return getConcentrator().getConnection();
	}
	
	protected StoreObject getStoreObjects(){
		return this.storeObject;
	}
	
    /*******************************************************************************************
    Message implementation
     * @throws IOException 
     * @throws NumberFormatException 
    *******************************************************************************************/

    protected void sendMeterMessages(Rtu concentrator, Rtu rtu, XmlHandler dataHandler) throws BusinessException, SQLException, NumberFormatException, IOException{
    	sendMeterMessages(concentrator, rtu, null, dataHandler);
    }
    
    /** Send Pending RtuMessage to meter. 
     * 	Currently we use the eRtu as a concentrator for the mbusRtu, so the serialNumber is this from the eRtu.
     * 	The messages them are those from the mbus device if this is not NULL.
     * @throws IOException 
     * @throws NumberFormatException 
     * */
    protected void sendMeterMessages(Rtu concentrator, Rtu eRtu, Rtu mbusRtu, XmlHandler dataHandler) throws BusinessException, SQLException, NumberFormatException, IOException {
    
        /* short circuit */
        if( ! communicationProfile.getSendRtuMessage() )
            return;
        
        Iterator mi = null;
        String showSerial = null;
        
        if (mbusRtu != null){	//mbus messages
        	mi = mbusRtu.getPendingMessages().iterator();
        	showSerial = mbusRtu.getSerialNumber();
        }
        else{					//eRtu messages
            mi = eRtu.getPendingMessages().iterator();
            showSerial = eRtu.getSerialNumber();
        }
        
        String serial = eRtu.getSerialNumber();     
        
        if (mi.hasNext())
        	getLogger().log(Level.INFO, "Handling MESSAGES from meter with serialnumber " + showSerial);
        else
        	return;
        
        while (mi.hasNext()) {
            
            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();
            
            boolean doReadRegister  = contents.indexOf(Constant.ON_DEMAND) != -1;
            boolean doDisconnect    = contents.indexOf(Constant.DISCONNECT_LOAD) != -1;
            boolean doConnect       = (contents.indexOf(Constant.CONNECT_LOAD) != -1) && !doDisconnect;
            
            boolean thresholdParameters	= (contents.indexOf(Constant.THRESHOLD_GROUPID) != -1) ||
            									(contents.indexOf(Constant.THRESHOLD_POWERLIMIT) != -1) ||
            									(contents.indexOf(Constant.CONTRACT_POWERLIMIT) != -1);
            
            /* A single message failure must not stop the other msgs. */
            try {
            	
                if (doReadRegister){
                	
                	dataHandler.getMeterReadingData().getRegisterValues().clear();
                    
                    List rl = new ArrayList( );
                    Iterator i = null;
                    
                    if (mbusRtu != null)
                    	i = mbusRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    else
                    	i = eRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    
                    while (i.hasNext()) {
                        
                        RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                        ObisCode oc = spec.getRegisterMapping().getObisCode();
                        if (oc.getF() == 255){
                        	
                        	if (checkManObisCodes(oc)){
                        		rl.add(oc.toString());
                        	}
                        			
                        	else if(checkOtherObisCodes(oc))
                        		rl.add( new String(oc.getC()+"."+oc.getD()+"."+oc.getE()) );
                        	
                        	else if(checkFirmwareObisCodes(oc)){
                        		Date d = new Date(System.currentTimeMillis());
                        		String fwv = getFirwareVersions(concentrator, serial, oc);
                        		dataHandler.getMeterReadingData().add(new RegisterValue(oc, null, null, null, d, d, 0, fwv));
                        	}
                        	else
                        		getLogger().log(Level.INFO, "Register with obisCode " + oc.toString() + " is not supported.");
                     
	                        dataHandler.checkOnDemands(true);
	                        dataHandler.setProfileDuration(-1);
                        }
                        
                    }
                    if (DEBUG) System.out.println(rl);
                    String registers [] = (String[]) rl.toArray(new String[0] ); 
                    String r = getConnection().getMeterOnDemandResultsList(serial, registers);
                    
                    getConcentrator().importData(r, dataHandler);
                    if (mbusRtu != null)
                    	handleRegisters(dataHandler, mbusRtu);
                    else
                    	handleRegisters(dataHandler, eRtu);
                    dataHandler.checkOnDemands(false);
                    
                }
                
                if (doConnect) {
                	getConnection().setMeterDisconnectControl(serial, true);
                }
                
                if (doDisconnect) {
                	getConnection().setMeterDisconnectControl(serial, false);
                }
                
                if (thresholdParameters){
                	
                	String groupID = getConcentrator().getMessageValue(contents, Constant.THRESHOLD_GROUPID);
                	if (groupID.equalsIgnoreCase("")){
                		msg.setFailed();
                		throw new BusinessException("No groupID was entered.");
                	}
                	
                	String thresholdPL = getConcentrator().getMessageValue(contents, Constant.THRESHOLD_POWERLIMIT);
                	String contractPL = getConcentrator().getMessageValue(contents, Constant.CONTRACT_POWERLIMIT);
                	if ( (thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase("")) ){
                		msg.setFailed();
                		throw new BusinessException("Neighter contractual nor threshold limit was given.");
                	}
                	                	
                	UnsignedInt uiGrId = new UnsignedInt();
                	UnsignedInt crPl = new UnsignedInt();
                	byte[] contractPowerLimit	= new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED,0, 0, 0, 0};

                	if (thresholdPL.equalsIgnoreCase("")){
                		msg.setFailed();
                		throw new BusinessException("No threshold powerLimit was given.");
                	}

                	try{
                		uiGrId.setValue((long)Integer.parseInt(groupID));
                		if (!thresholdPL.equalsIgnoreCase(""))
                			crPl.setValue((long)Integer.parseInt(thresholdPL));
                		if (!contractPL.equalsIgnoreCase("")){
                    		contractPowerLimit[1] = (byte)((long)Integer.parseInt(contractPL) >> 24);
                    		contractPowerLimit[2] = (byte)((long)Integer.parseInt(contractPL) >> 16);
                    		contractPowerLimit[3] = (byte)((long)Integer.parseInt(contractPL) >> 8);
                    		contractPowerLimit[4] = (byte)((long)Integer.parseInt(contractPL));
                		}
                	}
                	catch(NumberFormatException e){
                		throw new BusinessException("Invalid threshold parameters");
                	}
                	/*
                	 * Normally the webService setMeterPowerLimit should be used, but it doens't work with that,
                	 * to speed up the development we used the general setCosem method and this works fine!
                	 * 
                	 * 		port(concentrator).setMeterPowerLimit(serial, contractPl);
                	 * 
                	 */
                	if (!contractPL.equalsIgnoreCase("")){
                		Calendar cal = Calendar.getInstance();
                		cal.add(Calendar.MINUTE, 5);
                		String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
                		cal.add(Calendar.MINUTE, 10);
                		String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
                		 getConnection().cosemSetRequest(serial, startBefore, endBefore, Constant.powerLimitObisCode.toString(), new UnsignedInt(3), new UnsignedInt(2), contractPowerLimit);
                	}
                	
                	getConnection().setMeterCodeRedGroupId(serial, uiGrId);
                	if (!thresholdPL.equalsIgnoreCase(""))
                		getConnection().setMeterCodeRedPowerLimit(serial, crPl);
                }
                
                /* These are synchronous calls, so no sent state is ever used */
                msg.confirm();
                getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                
            } catch (RemoteException re) {
                msg.setFailed();
                re.printStackTrace();
                getConcentrator().severe(re, re.getMessage());
                throw new BusinessException(re);
            } catch (ServiceException se) {
                msg.setFailed();
                se.printStackTrace();
                getConcentrator().severe(se, se.getMessage());
                throw new BusinessException(se);
            }
        }
        getLogger().log(Level.INFO, "Done handling messages.");
    }
    
	private boolean checkManObisCodes(ObisCode oc) {
    	if((oc.getA()==0)&&((oc.getB()==0)||(oc.getB()==1))){
    		if(oc.getD() == 7){			// dips and swells
    			if((oc.getE()>=11)&&(oc.getE()<=17))
    				return true;
    			if((oc.getE()>=21)&&(oc.getE()<=27))
    				return true;
    			if((oc.getE()>=31)&&(oc.getE()<=37))
    				return true;
    			if((oc.getE()>=41)&&(oc.getE()<=47))
    				return true;
    			if((oc.getE()>=50)&&(oc.getE()<=51))	// voltage asymmetry
    				return true;
    		}
    		else if(oc.getD() == 8){	// daily peak and minimum
    			if((oc.getE()>=0)&&(oc.getE()<=3))
    				return true;
    			if((oc.getE()>=10)&&(oc.getE()<=13))
    				return true;
    			if((oc.getE()>=20)&&(oc.getE()<=23))
    				return true;
    			if((oc.getE()>=30)&&(oc.getE()<=33))
    				return true;
    			if(oc.getE()==50)
    				return true;
    		}
    		else if(oc.getD() == 6){		// reclosing counter
    			if(oc.getE()==1)
    				return true;
    		}
    		else if((oc.getD() == 50)&&(oc.getE() == 0)) // ondemand gas
    			return true;
    	}
    	return false;
	}
	
	private boolean checkFirmwareObisCodes(ObisCode oc){
		if(oc.getD() == 101){
			if(oc.getE() == 18 || oc.getE() == 28 || oc.getE() == 26)	// firmware versions
			return true;
		}
		return false;
	}
	
    private boolean checkOtherObisCodes(ObisCode oc) {
    	if((oc.getA()==1)&&((oc.getB()==0)||(oc.getB()==1))){
    		if((oc.getC()==1)||(oc.getC()==2)){
    			if((oc.getD()==4)&&(oc.getE()==0))
    				return true;		// Current average demand
    			if((oc.getD()==5)&&(oc.getE()==0))
    				return true;		// Last average demand
    			if((oc.getD()==6)&&((oc.getE()>=0)&&(oc.getE()<=4)))
    				return true;		// Max. demand rate E
    			if((oc.getD()==8)&&((oc.getE()>=0)&&(oc.getE()<=4)))
    				return true;		// Active energy
    		}
    	}
		return false;
	}
    
	protected String getBillingMonthly() {
		return billingMonthly;
	}

	protected void setBillingMonthly(String billingMonthly) {
		this.billingMonthly = billingMonthly;
	}

	protected String getBillingDaily() {
		return billingDaily;
	}

	protected void setBillingDaily(String billingDaily) {
		this.billingDaily = billingDaily;
	}
	
	protected void setProfileTestName(String[] name){
		this.profileTestName = name;
	}
	
	protected String[] getProfileTestName(){
		return profileTestName;
	}
	
	
    /*******************************************************************************************
    Caching and collecting
    *******************************************************************************************/
    
    private void doTheCheckMethods() throws NumberFormatException, IOException, ServiceException, SQLException, BusinessException{
    	if(!initCheck){
    		try {
    			testLogging("TESTLOGGING - Start the cache mechanism.");
    			startCacheMechanism(this);
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();	// absorb - The transaction may NOT fail, if the file is not found, then make one.
    		} catch (IOException e) {
    			e.printStackTrace();
    		} 
    		
    		collectCache();
    		saveConfiguration();
    		checkMbusDevices();
    		initCheck = true;
    	}
    }
    
	public void startCacheMechanism(Object fileSource) throws FileNotFoundException, IOException {
		
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? ");
        builder.bindInt(getMeter().getId());
        PreparedStatement stmnt;
		try {
			stmnt = builder.getStatement(Environment.getDefault().getConnection());

	        try {
	              InputStream in = null;
	              ResultSet resultSet = stmnt.executeQuery();
	              try {
	            	  if (resultSet.next()) {
	            		  Blob blob = resultSet.getBlob(1);
	            		  if (blob.length() > 0) {
	            			  in = blob.getBinaryStream();
	            			  ObjectInputStream ois = new ObjectInputStream(in);
	            			  try {
	            				  dlmsCache = (Cache)ois.readObject();
	            			  } catch (ClassNotFoundException e) {
	            				  e.printStackTrace();
	            			  } finally {
	            				  ois.close();
	            			  }
	            		  }
	            	  }
	              } finally {
	                   resultSet.close();
	              }
	        } finally {
	              stmnt.close();
        }
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}	
	}
    
    private boolean mbusCheck(){
        if ( mbusDevices[0] != null ){
        	if(mbusSerial[0].equalsIgnoreCase(mSerial)){
            	if(mbusDevices[0].getRtu() != null){
            		return true;
            	}
        	}
        	else
            	getLogger().log(Level.CONFIG, "MBus serialnumber in EIServer(" + mbusSerial[0] + ") didn't match serialnumber in meter(" + mSerial+ ")");
        }
    	return false;
    }
    
    private void checkMbusDevices() throws IOException, NumberFormatException, ServiceException, SQLException, BusinessException {
		if ((getMeter().getDownstreamRtus().size() > 0) || (concentrator.getRtuType(getMeter()) != null)){
			if ( concentrator.TESTING ){
				FileReader inFile = new FileReader(Utils.class.getResource(Constant.mbusSerialFile).getFile());
				mSerial = concentrator.readWithStringBuffer(inFile);
			}
			else{
				getLogger().log(Level.INFO, "Checking mbus configuration.");
				mSerial = getMbusSerial(serial);
			}
			
			if (( concentrator.getRtuType(getMeter()) != null ) && (getMeter().getDownstreamRtus().size() == 0)){
				mbusDevices[0] = new MbusDevice(1, mSerial, findOrCreate(getMeter(), mSerial, MBUS), getLogger());	
			}
			fillInMbusSerials(getMeter().getDownstreamRtus());
			
			if(!(( concentrator.getRtuType(getMeter()) != null ) && (getMeter().getDownstreamRtus().size() == 0)))
				mbusDevices[0] = new MbusDevice(1, mbusSerial[0], findOrCreate(getMeter(), mbusSerial[0], MBUS), getLogger());
		}
	}
    
	private void fillInMbusSerials(List downstreamRtus) {
		Iterator it = downstreamRtus.iterator();
		int count = 0;
		while(it.hasNext()){
			mbusSerial[count] = ((Rtu)it.next()).getSerialNumber();
			count++;
			if(count > mbusSerial.length){
				getLogger().log(Level.WARNING, "MBus device count exceeds maximum(4)");
			}
		}
	}
    
	private String getMbusSerial(String meterID) throws NumberFormatException, ServiceException, IOException, BusinessException {
		String times[] = prepareCosemGetRequest();
		String str = new String( getConnection().cosemGetRequest(meterID, times[0], times[1], Constant.mbusSerialObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2)));
		return str.substring(2);
	}
	
	private void saveConfiguration() throws BusinessException, SQLException {
		dlmsCache.setBillingReadTime(billingReadTime);
		dlmsCache.setCaptureObjReadTime(captureObjReadTime); // not necessary
		dlmsCache.setLoadProfileConfig1(loadProfileConfig1);
		dlmsCache.setLoadProfileConfig2(loadProfileConfig2);
		dlmsCache.setLoadProfileConfig3(loadProfileConfig3);
		dlmsCache.setLoadProfileConfig4(loadProfileConfig4);
		dlmsCache.setLoadProfilePeriod1(loadProfilePeriod1);
		dlmsCache.setLoadProfilePeriod2(loadProfilePeriod2);
		stopCacheMechanism();
	}
	
	public void stopCacheMechanism() throws BusinessException, SQLException {
		Transaction tr = new Transaction() {
			public Object doExecute() throws SQLException, BusinessException {
				createOrUpdateDeviceCache();
				updateCacheContent();
				return null;
			}
		};
		try {
			MeteringWarehouse.getCurrent().execute(tr);
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Failed to execute the stopCacheMechanism." + e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Failed to execute the stopCacheMechanism." + e);
		}
	}
	
	private void updateCacheContent() throws SQLException {
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? for update");
		builder.bindInt(getMeter().getId());
		PreparedStatement stmnt = builder.getStatement(Environment.getDefault().getConnection());		
		try {
			ResultSet rs = stmnt.executeQuery();
			if (!rs.next()) {
				throw new SQLException("Record not found");
			}
			try {
				java.sql.Blob blob = (java.sql.Blob) rs.getBlob(1);
				ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(0L));
				out.writeObject(dlmsCache);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				rs.close();
			}
		} finally {
			stmnt.close();
		}
	}

	private void createOrUpdateDeviceCache() throws SQLException {
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ?");
		builder.bindInt(getMeter().getId());
		PreparedStatement stmnt = builder.getStatement(Environment.getDefault().getConnection());		
		try {
			ResultSet rs = stmnt.executeQuery();
			if (!rs.next()) {
				builder = new SqlBuilder("insert into eisdevicecache (rtuid, content, mod_date) values (?,empty_blob(),sysdate)");
				builder.bindInt(getMeter().getId());
				PreparedStatement insertStmnt = builder.getStatement(Environment.getDefault().getConnection());
				try {
					insertStmnt.executeUpdate();
				}
				finally {
					insertStmnt.close();
				}
			}
		} finally {
			stmnt.close();
		}
	}

	private void collectCache() throws BusinessException, IOException {
		int iConf;
		try{
			if( dlmsCache.getLoadProfileConfig1() != null ){
				testLogging("TESTLOGGING - Collect1/ cache file is not empty");
				setCachedObjects(dlmsCache.getBillingReadTime(), dlmsCache.getCaptureObjReadTime(), dlmsCache.getLoadProfileConfig1(),
						dlmsCache.getLoadProfileConfig2(), dlmsCache.getLoadProfileConfig3(), dlmsCache.getLoadProfileConfig4(),
						dlmsCache.getLoadProfilePeriod1(), dlmsCache.getLoadProfilePeriod2());
				
				try{
					getLogger().log(Level.INFO, "Checking configuration parameters.");
					iConf = requestConfigurationChanges(rtuConcentrator, serial);
					
				}catch (NumberFormatException e) {
					iConf = -1;
					getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
					getLogger().log(Level.INFO, "(This will take several minutes.)");
					requestConfigurationParameters(rtuConcentrator, serial);
					dlmsCache.setConfProgChange(iConf);
					e.printStackTrace();
				} catch (ServiceException e) {
					iConf = -1;
					getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
					getLogger().log(Level.INFO, "(This will take several minutes.)");
					requestConfigurationParameters(rtuConcentrator, serial);
					dlmsCache.setConfProgChange(iConf);
					e.printStackTrace();
				} catch (RemoteException e) {
					iConf = -1;
					getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
					getLogger().log(Level.INFO, "(This will take several minutes.)");
					requestConfigurationParameters(rtuConcentrator, serial);
					dlmsCache.setConfProgChange(iConf);
					e.printStackTrace();
				}
				
				if (iConf != dlmsCache.getConfProgChange()){
					getLogger().log(Level.INFO, "Iskra Mx37x: Configuration changed, requesting configuration parameters...");
					getLogger().log(Level.INFO, "(This will take several minutes.)");
					requestConfigurationParameters(rtuConcentrator, serial);
					dlmsCache.setConfProgChange(iConf);
				}
			}
			
			else{ 	//if cache doesn't exist
				getLogger().log(Level.INFO, "Iskra Mx37x: Cache does not exist, requesting configuration parameters...");
				getLogger().log(Level.INFO, "(This will take several minutes.)");
				requestConfigurationParameters(rtuConcentrator, serial);
				
				try{
					iConf = requestConfigurationChanges(rtuConcentrator, serial);
					dlmsCache.setConfProgChange(iConf);
					
				}catch (NumberFormatException e) {
					iConf = -1;
					e.printStackTrace();
				} catch (ServiceException e) {
					iConf = -1;
					e.printStackTrace();
				} catch (RemoteException e) {
					iConf = -1;
					e.printStackTrace();
				}
			}
		}  catch (RemoteException e1) {
			e1.printStackTrace();
			throw new BusinessException(e1); /* roll back */
		} catch (ServiceException e1) {
			e1.printStackTrace();
			throw new BusinessException(e1); /* roll back */
		}
	}
	
	public String getFileName() {
		Calendar calendar = Calendar.getInstance();
	    return calendar.get(Calendar.YEAR) + "_" + serial + "_IskraMx37x.cache";
	}

	public Object getCache() {
		return dlmsCache;
	}

	public void setCache(Object cacheObject) {
		this.dlmsCache=(Cache)cacheObject;
	}
	
    protected void requestConfigurationParameters(Rtu concentrator, String serial) throws ServiceException, IOException, BusinessException {
        String loadProfile1 = "LoadProfile1";
        String loadProfile2 = "LoadProfile2";
        String billingProfile = "BillingProfile";
        String scheduledProfile = "ScheduledProfile";
        
		try {
			testLogging("TESTLOGGING - Requesting1/ lp period1");
			this.loadProfilePeriod1 = getConnection().getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile1)).intValue();
			testLogging("TESTLOGGING - Requesting2/ lp period2");
			this.loadProfilePeriod2 = getConnection().getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile2)).intValue();
			testLogging("TESTLOGGING - Requesting3/ lp config1");
			this.loadProfileConfig1 = getConnection().getMeterProfileConfig(serial, new ProfileType(loadProfile1));
			testLogging("TESTLOGGING - Requesting4/ lp config2");
			this.loadProfileConfig2 = getConnection().getMeterProfileConfig(serial, new ProfileType(loadProfile2));
			testLogging("TESTLOGGING - Requesting5/ lp config3");
			this.loadProfileConfig3 = getConnection().getMeterProfileConfig(serial, new ProfileType(billingProfile));
			testLogging("TESTLOGGING - Requesting6/ lp config4");
			this.loadProfileConfig4 = getConnection().getMeterProfileConfig(serial, new ProfileType(scheduledProfile));
			testLogging("TESTLOGGING - Requesting7/ billing readTime");
			this.billingReadTime = getConnection().getMeterBillingReadTime(serial);
			
		} catch (RemoteException e) {
			getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
			e.printStackTrace();
			throw new RemoteException( "No parameters could be retrieved.", e );
		} catch (ServiceException e) {
			getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
			e.printStackTrace();
			throw new ServiceException( "No parameters could be retrieved.", e );
		}
	}
    
	protected void setCachedObjects(CosemDateTime billingReadTime,
			CosemDateTime captureObjReadTime, ObjectDef[] loadProfileConfig1,
			ObjectDef[] loadProfileConfig2, ObjectDef[] loadProfileConfig3, 
			ObjectDef[] loadProfileConfig4, int loadProfilePeriod1, int loadProfilePeriod2) {
		this.billingReadTime = billingReadTime;
		this.captureObjReadTime = captureObjReadTime;
		this.loadProfileConfig1 = loadProfileConfig1;
		this.loadProfileConfig2 = loadProfileConfig2;
		this.loadProfileConfig3 = loadProfileConfig3;
		this.loadProfileConfig4 = loadProfileConfig4;
		this.loadProfilePeriod1 = loadProfilePeriod1;
		this.loadProfilePeriod2 = loadProfilePeriod2;
	}
	
	protected int requestConfigurationChanges(Rtu concentrator, String serial) throws NumberFormatException, ServiceException, IOException, BusinessException {
		String times[] = prepareCosemGetRequest();
		byte[] byteStrs = getConnection().cosemGetRequest(serial, times[0], times[1], Constant.confChangeObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2));
		int changes = byteStrs[2]&0xFF;
		changes = changes + ((byteStrs[1]&0xFF)<<8);
		return changes;
	}
	
}