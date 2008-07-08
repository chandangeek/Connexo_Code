package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
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

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Transaction;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator.XmlException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.amrimpl.RtuRegisterReadingImpl;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

/**
 * Meter handling: 
 *  - find or create meter 
 *  - read meter 
 *  - export message 
 * Transaction: all operations for a meter fail or all succeed.
 */
class MeterReadTransaction implements Transaction, CacheMechanism {
	
	protected boolean TESTING = false;
	protected boolean DEBUG = false;
    
    static final int ELECTRICITY 	= 0x00;
    static final int MBUS 			= 0x01;
	
    /**
	 * a private instance of the concentrator class
	 */
	private final Concentrator concentrator;

	private Object source;
	
    /** Cached Objects */
	public int confProgChange;
	public int loadProfilePeriod1;
	public int loadProfilePeriod2;
	public boolean changed;
	public ObjectDef[] loadProfileConfig1;
	public ObjectDef[] loadProfileConfig2;
	public CosemDateTime billingReadTime;
	public CosemDateTime captureObjReadTime;
	
	/** Contains the channelmap */
	private ProtocolChannelMap protocolChannelMap = null;
    
    /** Concentrator "containing" the meter */
    private Rtu rtuConcentrator;
    
    /** Serial of the meter */
    private String serial;
    
    /** Serial of the mbusMeter */
    private String mbusSerial[] = {null, null, null, null};
    private String mSerial;

	private Cache dlmsCache;
	private boolean initCheck = false;
	
	/** the communication profile of the concentrator */
	private CommunicationProfile 	communicationProfile;	
	
	protected MbusDevice[]			mbusDevices = {null, null, null, null};
    
    public MeterReadTransaction(Concentrator concentrator, Rtu rtuConcentrator, String serial, CommunicationProfile communicationProfile) {
        
        this.concentrator = concentrator;
		this.rtuConcentrator = rtuConcentrator;
        this.serial = serial;
        this.communicationProfile = communicationProfile;
        this.dlmsCache = new Cache();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.energyict.cpo.Transaction#doExecute()
     */
    public Object doExecute() throws BusinessException, SQLException {
        
    	ProfileData[] pd = {new ProfileData(), new ProfileData()};
        Rtu meter = findOrCreate(rtuConcentrator, serial, ELECTRICITY);
        
        try {
            if (meter != null) {

                XmlHandler dataHandler = new XmlHandler( getLogger(), getChannelMap(meter) );

                // Import profile
                if( communicationProfile.getReadDemandValues() ) {
                	doTheCheckMethods(meter);
                	importProfile(rtuConcentrator, meter, dataHandler, communicationProfile.getReadMeterEvents());
                	pd = dataHandler.getProfileData();
                	meter.store(pd[ELECTRICITY]);
                	if(mbusCheck())
                		mbusDevices[0].getRtu().store(pd[MBUS]);
                }
                
                // Import registers
                if( communicationProfile.getReadMeterReadings() ){
                	doTheCheckMethods(meter);
                	importRegisters(rtuConcentrator, meter, dataHandler);
                	handleRegisters(dataHandler, meter);
                	if(mbusCheck()){
                    	if ( mbusDevices[0].getRtu().getRegisters().size() != 0 ){
                    		dataHandler.getMeterReadingData().getRegisterValues().clear();
                    		importRegisters(rtuConcentrator, mbusDevices[0].getRtu(), dataHandler, meter.getSerialNumber());
                    		handleRegisters(dataHandler, mbusDevices[0].getRtu());
                    	}
                	}
                }
                
                // Send messages
                if( communicationProfile.getSendRtuMessage() ){
                	if(!initCheck){			// otherwise the MBus messages will not be executed
                		checkMbusDevices(meter);
                	}
                	sendMeterMessages(rtuConcentrator, meter, dataHandler);
                	if(mbusCheck()){
                    	if ( mbusDevices[0].getRtu().getMessages().size() != 0 ){
                    		sendMeterMessages(rtuConcentrator, meter, mbusDevices[0].getRtu(), dataHandler);
                    	}
                	}
                }
                
                getLogger().log(Level.INFO, "Meter with serialnumber " + serial + " has completely finished");
            }
            
        } catch (ServiceException thrown) {
            
            getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
            
        } catch (IOException thrown) {
            
            getConcentrator().severe( thrown, thrown.getMessage() );
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */
            
        }
        
        return meter; /* return whatever */
        
    }
    
	/**
     * @param
     * Import:
     *   (1) ProfileData
     *   (2) If events enabled -> Events
     */
    protected void importProfile(Rtu ctr, Rtu meter, XmlHandler dataHandler, boolean bEvents) throws ServiceException, IOException, BusinessException {
    
        String xml = null;        
        String profile = null;
        String mtr = meter.getSerialNumber();
        
        Date fromDate = getLastReading(meter);
        
        // if the meter has MBus meters with an earlier LastReading, then use this LastReading
        if(meter.getDownstreamRtus().size() > 0){
        	Date downDate = null;
        	Iterator i = meter.getDownstreamRtus().iterator();
        	while(i.hasNext()){
        		Rtu downRtu = (Rtu)i.next();
        		downDate = getLastReading(downRtu);
        		if (downDate.before(fromDate))
        			fromDate.setTime(downDate.getTime());
        	}
        }
        
        String from = Constant.getInstance().format( fromDate );
        String to = Constant.getInstance().format(new Date());
        
        String lpString1 = "99.1.0";
        String lpString2 = "99.2.0";
        
        /*
         * Read profile data 
         */
    	getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + mtr + ".");
        
        ProtocolChannelMap channelMap = getChannelMap(meter);
        
        ObjectDef[] lp1;
        ObjectDef[] lp2 = null;
        int lpPeriod1;
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
        
        if (meter.getIntervalInSeconds() != lpPeriod1){
        	getLogger().log(Level.SEVERE, "ProfileInterval meter: " + lpPeriod1 +  ", ProfileInterval EIServer: " + meter.getIntervalInSeconds());
        	throw new BusinessException("Interval didn't match");
        }
        
        for( int i = 0; i < channelMap.getNrOfProtocolChannels(); i ++ ) {
        
            ProtocolChannel channel = channelMap.getProtocolChannel(i);
            String register = channel.getRegister();
            
            if (lpContainsRegister(lp1, register)){
            	profile = lpString1;
            	
            	if(TESTING){
            		getLogger().log(Level.INFO, "The actual String: " + Constant.profileFiles[i]);
            		getLogger().log(Level.INFO, "FilePath to nullPointer:" + Utils.class.getResource(Constant.profileFiles[i]));
            		getLogger().log(Level.INFO, "The integer 'i': " + i);
            		FileReader inFile = new FileReader(Utils.class.getResource(Constant.profileFiles[i]).getFile());
            		xml = getConcentrator().readWithStringBuffer(inFile);
            	}
            	else xml = getPort(ctr).getMeterProfile(mtr, profile, register, from, to);
            	
                dataHandler.setChannelIndex( i );
                getConcentrator().importData(xml, dataHandler);
            }
            
            else{
                if (lpContainsRegister(lp2, register)){
                	profile = lpString2;
                	
                	if(TESTING){
                		FileReader inFile = new FileReader(Utils.class.getResource(Constant.mbusProfile).getFile());
                		xml = getConcentrator().readWithStringBuffer(inFile);
                	}
                	else xml =  getPort(ctr).getMeterProfile(mtr, profile, register, from, to);
                	
                    dataHandler.setChannelIndex( i );
                    getConcentrator().importData(xml, dataHandler);
                }
            }
        }
        getLogger().log(Level.INFO, "Done reading PROFILE.");
        
        /*
         * Read logbook
         */
        if( bEvents ) {
        	
        	getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + mtr + ".");
            
            from = Constant.getInstance().format(getLastLogboog(meter));
            String events, powerFailures;
            if(TESTING){
            	FileReader inFile = new FileReader(Utils.class.getResource(Constant.eventsFile).getFile());
        		events = getConcentrator().readWithStringBuffer(inFile);
        		inFile = new FileReader(Utils.class.getResource(Constant.powerDownFile).getFile());
        		powerFailures = getConcentrator().readWithStringBuffer(inFile);
            }
            else{
            	events =  getPort(ctr).getMeterEvents(mtr, from, to);
            	powerFailures =  getPort(ctr).getMeterPowerFailures(mtr, from, to);
            }
            getConcentrator().importData(events, dataHandler);
            getConcentrator().importData(powerFailures, dataHandler);
            
            getLogger().log(Level.INFO, "Done reading EVENTS.");
        }
    }
    
	protected void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler) throws ServiceException, IOException, BusinessException{
    	importRegisters(ctr, meter, dataHandler, meter.getSerialNumber());
    }
    
	protected void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler, String serialNumb)throws ServiceException, IOException, BusinessException {
    	
    	String xml = null;
    	String mtr = serialNumb;
//        String from = Constant.getInstance().format(getLastReading( meter ) );
    	String from = Constant.getInstance().format(new Date());
        String to = Constant.getInstance().format(new Date());
    	
        /*
         * Read registers 
         * (use lastReading as from date !!)
         */
    	
        if( communicationProfile.getReadMeterReadings() ) {
        	
        	getLogger().log(Level.INFO, "Reading REGISTERS from meter with serialnumber " + meter.getSerialNumber() + ".");
        	
        	String daily = null;
        	String monthly = null;
        	
        	int period;
        	CosemDateTime cdt;
            if ( TESTING ){
            	FileReader inFile = new FileReader(Utils.class.getResource(Constant.dateTimeFile).getFile());
            	xml = getConcentrator().readWithStringBuffer(inFile);
            	period = 3600;
            	cdt = getCosemDateTimeFromXmlString(xml);
            }
            else{
            	period = loadProfilePeriod2;
            	cdt = billingReadTime;
            }
            
    		if ( period == 86400 ){ // Profile contains daily values
    			daily = "99.2.0";
    		}
    		else
    			daily = null;
        	
    		if ( (cdt.getDayOfMonth().intValue() == 1) && (cdt.getHour().intValue() == 0) && (cdt.getYear().intValue() == 65535) && (cdt.getMonth().intValue() == 255) ){
    			monthly = "98.1.0";
    			if (daily == null) daily = "98.2.0";
    		}
    		else{
    			monthly = "98.2.0";
    			if (daily == null) daily = "98.1.0";
    		}
    		
            // set registers for the DataHandler
            dataHandler.setDailyStr(daily);
            dataHandler.setMonthlyStr(monthly);
        	
            Iterator i = meter.getRtuType().getRtuRegisterSpecs().iterator();
            while (i.hasNext()) {
                
                RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                ObisCode oc = spec.getObisCode();
                
                if((oc.getF()==0)||(oc.getF()==-1)){
                    String register = oc.toString();
                    String profile = null;
                    List registerValues = getConcentrator().mw().getRtuRegisterReadingFactory().findByRegister(meter.getRegister(oc).getId());
                    Date lastRegisterDate = null;
                	if (registerValues.size() != 0){
                		lastRegisterDate = getLastRegisterDate(registerValues);
                	}else{
                		Calendar registerCalendar = Calendar.getInstance();
                		registerCalendar.add(Calendar.DAY_OF_MONTH, -10);
                		lastRegisterDate = registerCalendar.getTime();
                	}
                    from = Constant.getInstance().format( lastRegisterDate );
                    if (oc.getF() == 0){
                        
                        /* historical - daily*/
                    	profile = daily;
                        xml =  getPort(ctr).getMeterProfile(mtr, profile, register, from, to);
                        getConcentrator().importData(xml, dataHandler);
                       
                    }
                    
                    else if (oc.getF() == -1){

                    	 /* historical - monthly*/
                    	profile = monthly;
                        xml =  getPort(ctr).getMeterProfile(mtr, profile, register, from, to);
                        getConcentrator().importData(xml, dataHandler);
                        
                    }
                }
            }
            getLogger().log(Level.INFO, "Done reading REGISTERS.");
            
        }
    }
	
	protected void handleRegisters(XmlHandler dataHandler, Rtu meter) throws ServiceException, BusinessException, SQLException {
		
        Iterator i = dataHandler.getMeterReadingData().getRegisterValues().iterator();
        while (i.hasNext()) {
            
            RegisterValue registerValue = (RegisterValue) i.next();
            RtuRegister register = meter.getRegister( registerValue.getObisCode() );

            if( register != null )
                register.store( registerValue );
            else {
                String obis = registerValue.getObisCode().toString();
                String msg = "Register " + obis + " not defined on device";
                getLogger().info( msg );
            }
        }
	}
	
    protected void sendMeterMessages(Rtu concentrator, Rtu rtu, XmlHandler dataHandler) throws BusinessException, SQLException{
    	sendMeterMessages(concentrator, rtu, null, dataHandler);
    }
    
    /** Send Pending RtuMessage to meter. 
     * 	Currently we use the eRtu as a concentrator for the mbusRtu, so the serialNumber is this from the eRtu.
     * 	The messages them are those from the mbus device if this is not NULL.
     * */
    protected void sendMeterMessages(Rtu concentrator, Rtu eRtu, Rtu mbusRtu, XmlHandler dataHandler) throws BusinessException, SQLException {
    
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
            boolean doConnect       = contents.indexOf(Constant.CONNECT_LOAD) != -1;
            boolean doDisconnect    = contents.indexOf(Constant.DISCONNECT_LOAD) != -1;
            
            boolean thresholdParameters	= (contents.indexOf(Constant.THRESHOLD_GROUPID) != -1) ||
            									(contents.indexOf(Constant.THRESHOLD_POWERLIMIT) != -1) ||
            									(contents.indexOf(Constant.CONTRACT_POWERLIMIT) != -1);
            
            /* A single message failure must not stop the other msgs. */
            try {
            	
                if (doReadRegister){
                    
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
                        	
                        	else
                        		getLogger().log(Level.INFO, "Register with obisCode " + oc.toString() + " is not supported.");
                     
	                        dataHandler.checkOnDemands(true);
	                        dataHandler.setProfileDuration(-1);
                        }
                        
                    }
                    if (DEBUG) System.out.println(rl);
                    String registers [] = (String[]) rl.toArray(new String[0] ); 
                    String r =  getPort(concentrator).getMeterOnDemandResultsList(serial, registers);
                    
                    getConcentrator().importData(r, dataHandler);
                    if (mbusRtu != null)
                    	handleRegisters(dataHandler, mbusRtu);
                    else
                    	handleRegisters(dataHandler, eRtu);
                    dataHandler.checkOnDemands(false);
                    
                }
                
                if (doConnect) {
                     getPort(concentrator).setMeterDisconnectControl(serial, true);
                }
                
                if (doDisconnect) {
                     getPort(concentrator).setMeterDisconnectControl(serial, false);
                }
                
                if (thresholdParameters){
                	
                	String groupID = getConcentrator().getMessageValue(contents, Constant.THRESHOLD_GROUPID);
                	if (groupID.equalsIgnoreCase(""))
                		throw new BusinessException("No groupID was entered.");
                	
                	String thresholdPL = getConcentrator().getMessageValue(contents, Constant.THRESHOLD_POWERLIMIT);
                	String contractPL = getConcentrator().getMessageValue(contents, Constant.CONTRACT_POWERLIMIT);
                	if ( (thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase("")) )
                			throw new BusinessException("Neighter contractual nor threshold limit was given.");
                	                	
                	UnsignedInt uiGrId = new UnsignedInt();
                	UnsignedInt crPl = new UnsignedInt();
                	byte[] contractPowerLimit	= new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED,0, 0, 0, 0};

                	if (thresholdPL.equalsIgnoreCase(""))
                		throw new BusinessException("No threshold powerLimit was given.");

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
                		 getPort(concentrator).cosemSetRequest(serial, startBefore, endBefore, Constant.powerLimitObisCode.toString(), new UnsignedInt(3), new UnsignedInt(2), contractPowerLimit);
                	}
                	
                	 getPort(concentrator).setMeterCodeRedGroupId(serial, uiGrId);
                	if (!thresholdPL.equalsIgnoreCase(""))
                		 getPort(concentrator).setMeterCodeRedPowerLimit(serial, crPl);
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
    
    protected Date getLastReading(Rtu rtu) {
        Date result = rtu.getLastReading();
        if( result == null ) {
            result = new Date( 0 );
        }
        return result;
    }
    
    protected Date getLastLogboog(Rtu rtu) {
        Date result = rtu.getLastLogbook();
        if( result == null ) {
            result = new Date( 0 );
        }
        return result;
    }
    
    public ProtocolChannelMap getChannelMap(Rtu meter) throws InvalidPropertyException {
    	if (protocolChannelMap == null){
    		String sChannelMap = meter.getProperties().getProperty( Constant.CHANNEL_MAP );
    		protocolChannelMap = new ProtocolChannelMap( sChannelMap ); 
    	}
        
        return protocolChannelMap;
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
    
    private void doTheCheckMethods(Rtu meter) throws NumberFormatException, IOException, ServiceException, SQLException, BusinessException{
    	if(!initCheck){
    		try {
    			startCacheMechanism(this);
    		} catch (FileNotFoundException e) {
    			// absorb - The transaction may NOT fail, if the file is not found, then make one.
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} 
    		
    		collectCache();
    		saveConfiguration();
    		checkMbusDevices(meter);
    		initCheck = true;
    	}
    }

		
    private void checkMbusDevices(Rtu meter) throws IOException, NumberFormatException, ServiceException, SQLException, BusinessException {
		if ((meter.getDownstreamRtus().size() > 0) || (concentrator.getRtuType(meter) != null)){
			if ( concentrator.TESTING ){
				FileReader inFile = new FileReader(Utils.class.getResource(Constant.mbusSerialFile).getFile());
				mSerial = concentrator.readWithStringBuffer(inFile);
			}
			else{
				getLogger().log(Level.INFO, "Checking mbus configuration.");
				mSerial = getMbusSerial(rtuConcentrator, serial);
			}
			
			if (( concentrator.getRtuType(meter) != null ) && (meter.getDownstreamRtus().size() == 0)){
				mbusDevices[0] = new MbusDevice(1, mSerial, findOrCreate(meter, mSerial, MBUS), getLogger());	
			}
			fillInMbusSerials(meter.getDownstreamRtus());
			
			if(!(( concentrator.getRtuType(meter) != null ) && (meter.getDownstreamRtus().size() == 0)))
				mbusDevices[0] = new MbusDevice(1, mbusSerial[0], findOrCreate(meter, mbusSerial[0], MBUS), getLogger());
		}
	}
	
	private String getMbusSerial(Rtu concentrator, String meterID) throws NumberFormatException, RemoteException, ServiceException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		cal.add(Calendar.MINUTE, 10);
		String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		String str = new String( getPort(concentrator).cosemGetRequest(meterID, startBefore, endBefore, Constant.mbusSerialObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2)));
		return str.substring(2);
	}
	
	protected Rtu findOrCreate(Rtu concentrator, String serial, int type) throws SQLException, BusinessException { 
        
        List meterList = getConcentrator().mw().getRtuFactory().findBySerialNumber(serial);

        if( meterList.size() == 1 ) {
    		
        	((Rtu)meterList.get(0)).updateGateway(concentrator);
        	
			//******************************************************************
			// this moves the rtu to his concentrator folder
        	// ((Rtu)meterList.get(0)).moveToFolder(concentrator.getFolder());
        	//******************************************************************
        	
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
        
        //*************************************************
        // this moves the new Rtu to the Concentrator folder, else it will be placed in the prototype folder
        // shadow.setFolderId(gwRtu.getFolderId());
        //*************************************************
        
    	shadow.setGatewayId(concentrator.getId());
    	shadow.setLastReading(lastreading);
        return getConcentrator().mw().getRtuFactory().create(shadow);
        
    }
	
    private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat( Constant.DUPLICATE_SERIALS )
                    .format( new Object [] { serial } );
    }

	private void saveConfiguration() {
		dlmsCache.setBillingReadTime(billingReadTime);
		dlmsCache.setCaptureObjReadTime(captureObjReadTime); // not necessary
		dlmsCache.setLoadProfileConfig1(loadProfileConfig1);
		dlmsCache.setLoadProfileConfig2(loadProfileConfig2);
		dlmsCache.setLoadProfilePeriod1(loadProfilePeriod1);
		dlmsCache.setLoadProfilePeriod2(loadProfilePeriod2);
		stopCacheMechanism();
	}

	private void collectCache() throws BusinessException {
		int iConf;
		try{
			if( dlmsCache.getLoadProfileConfig1() != null ){
				
				setCachedObjects(dlmsCache.getBillingReadTime(), dlmsCache.getCaptureObjReadTime(), dlmsCache.getLoadProfileConfig1(),
						dlmsCache.getLoadProfileConfig2(), dlmsCache.getLoadProfilePeriod1(), dlmsCache.getLoadProfilePeriod2());
				
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
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e); /* roll back */
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
        	 e.printStackTrace();
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
	
	public Logger getLogger(){
		return getConcentrator().getLogger();
	}
	
    protected void requestConfigurationParameters(Rtu concentrator, String serial) throws BusinessException {
        String loadProfile1 = "LoadProfile1";
        String loadProfile2 = "LoadProfile2";
        
		try {
			
			this.loadProfilePeriod1 =  getPort(concentrator).getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile1)).intValue();
			this.loadProfilePeriod2 =  getPort(concentrator).getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile2)).intValue();
			this.loadProfileConfig1 =  getPort(concentrator).getMeterProfileConfig(serial, new ProfileType(loadProfile1));
			this.loadProfileConfig2 =  getPort(concentrator).getMeterProfileConfig(serial, new ProfileType(loadProfile2));
			this.billingReadTime =  getPort(concentrator).getMeterBillingReadTime(serial);
			
		} catch (RemoteException e) {
			getLogger().log(Level.SEVERE, "IskraMx37x: could not retreive configuration parameters, meter will NOT be handled");
			e.printStackTrace();
			throw new BusinessException( "No parameters could be retreived.", e );
		} catch (ServiceException e) {
			getLogger().log(Level.SEVERE, "IskraMx37x: could not retreive configuration parameters, meter will NOT be handled");
			e.printStackTrace();
			throw new BusinessException( "No parameters could be retreived.", e );
		}
	}
    
	protected void setCachedObjects(CosemDateTime billingReadTime,
			CosemDateTime captureObjReadTime, ObjectDef[] loadProfileConfig1,
			ObjectDef[] loadProfileConfig2, int loadProfilePeriod1,
			int loadProfilePeriod2) {
		this.billingReadTime = billingReadTime;
		this.captureObjReadTime = captureObjReadTime;
		this.loadProfileConfig1 = loadProfileConfig1;
		this.loadProfileConfig2 = loadProfileConfig2;
		this.loadProfilePeriod1 = loadProfilePeriod1;
		this.loadProfilePeriod2 = loadProfilePeriod2;
	}
	
	protected int requestConfigurationChanges(Rtu concentrator, String serial) throws NumberFormatException, RemoteException, ServiceException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		cal.add(Calendar.MINUTE, 10);
		String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		byte[] byteStrs = getPort(concentrator).cosemGetRequest(serial, startBefore, endBefore, Constant.confChangeObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2));
		int changes = byteStrs[2]&0xFF;
		changes = changes + ((byteStrs[1]&0xFF)<<8);
		return changes;
	}
	
	private P2LPCSoapPort_PortType getPort(Rtu concentrator) throws ServiceException{
		return getConcentrator().port(concentrator);
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
    
	private boolean lpContainsRegister(ObjectDef[] lp, String register) {
		for (int i = 0; i< lp.length; i++){
			if(lp[i]!=null){
				String instId = lp[i].getInstanceId();
				if (register.length() == 5){
					if (instId.indexOf(register) == 4)
						return true;
				}
				else
					if (instId.indexOf(register.subSequence(0, register.length()).toString()) >= 0)
						return true;
			}
		}
		return false;
	}

	protected boolean isTESTING() {
		return TESTING;
	}

	protected void setTESTING(boolean testing) {
		TESTING = testing;
	}

	private Concentrator getConcentrator() {
		return this.concentrator;
	}
}