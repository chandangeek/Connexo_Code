package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.rpc.ServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.WebServiceLocator;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
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
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.tcpip.PPPDialer;

public class Concentrator implements Messaging, GenericProtocol {
    
    private boolean DEBUG = false;
    private boolean TESTING = false;
    
    private static final int ELECTRICITY 	= 0x00;
    private static final int MBUS 			= 0x01;
    
    private static File plpFile = new File("c://TEST_FILES/Sub_38517965_200802.plp");
    
    private static String TOUConfig = "TOU";
    private static String MBUSSTRING = "mbus";

    private Logger logger;
    private Properties properties;
    private CommunicationProfile communicationProfile;	
    
    /** RtuType is used for creating new Rtu's */
    private RtuType[] rtuType = {null, null};
	private ProtocolChannelMap protocolChannelMap = null;
    
    /** Error message for a meter error */
    private final static String METER_ERROR = 
        "Meter failed, serialnumber meter: ";
    
    /** Error message for a concentrator error */
    private final static String CONCENTRATOR_ERROR = 
        "Concentrator failed, serialnumber concentrator: ";
    
    private final static String AUTO_CREATE_ERROR_1 =
        "No automatic meter creation: no property RtuType defined.";

    private final static String AUTOCREATE_ERROR_2 =
        "No automatic meter creation: property RtuType has no prototype.";

    private final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    
    
    private final static boolean ADVANCED = true;
    

    public void execute( CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        
        this.logger = logger;
        this.communicationProfile = scheduler.getCommunicationProfile();
        
        int meterCount = -1;
        
        Rtu concentrator = scheduler.getRtu();
        String serial = concentrator.getSerialNumber();
        StringBuffer progressLog = new StringBuffer();
        PPPDialer dialer = null;
        
        try {
            
            if (useDialUp(concentrator)) {
                dialer = new PPPDialer(serial, logger);
                
                String user = getUser(concentrator);
                if( user!=null ) 
                    dialer.setUserName(user);
                
                String pwd = getPassword(concentrator);
                if( pwd!=null ) 
                    dialer.setPassword(pwd);
                
                dialer.connect();
            }
            
            /* Meters discovered by concentrator */
            String meterList = port(concentrator).getMetersList();
            List discovered = collectSerials(meterList);
            meterCount = discovered.size();
            
            getLogger().log(Level.INFO,"Discoverd meter count: " + meterCount);
            
            
            Iterator im = discovered.iterator();
            while (im.hasNext()) {
                String meterSerial = (String) im.next();
                handleMeter(concentrator, meterSerial);
                progressLog.append(meterSerial + " ");
                getLogger().log(Level.INFO, "" + --meterCount + " meters to go.");
            }
            
            handleConcentrator(concentrator);
            
            
        } catch (ServiceException thrown) {
            
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
            
            throw new BusinessException( msg, thrown );
            
        } catch (XmlException thrown) {
            
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
        
            throw new BusinessException( msg, thrown );

        } catch (Throwable thrown) {
            
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
            
            throw new BusinessException( msg, thrown );
            
        } finally {
            /** clean up, must simply ALWAYS happen */
            if (useDialUp(concentrator) && dialer != null)
                dialer.disconnect();
        }
        
        
    }

	public void addProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProtocolVersion() {
        return "$Revision: 1.9 $";
    }
    
    public String getVersion() {
        return "$Revision: 1.9 $";
    }

    public List getOptionalKeys() {
        ArrayList result = new ArrayList();
        result.add( Constant.RTU_TYPE );
        result.add( Constant.USE_DIAL_UP );
        result.add( Constant.USER );
        result.add( Constant.PASSWORD );
        return result;
    }

    public List getRequiredKeys() {
        ArrayList result = new ArrayList();
        result.add( Constant.CHANNEL_MAP );
        return result;
    }
    
    
    private Logger getLogger( ){
        return logger;
    }
    
    /** Import a single meter */
    private void handleMeter( Rtu concentrator, String serial ) {
        
        try {
            
            MeterReadTransaction mrt = 
                new MeterReadTransaction(concentrator, serial);
            
            Environment.getDefault().execute(mrt);
            
        } catch (BusinessException thrown) {
            
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */

            String msg = METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
            
        } catch (SQLException thrown) {
            
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */

            String msg = METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
            
        }
        
    }
    
    /**
     * Meter handling: 
     *  - find or create meter 
     *  - read meter 
     *  - export message 
     * Transaction: all operations for a meter fail or all succeed.
     */
    class MeterReadTransaction implements Transaction {
        
        /** Concentrator "containing" the meter */
        private Rtu concentrator;
        
        /** Serial of the meter */
        private String serial;
        
        /** Serial of the mbusMeter */
        private String mbusSerial = null;
        
        public MeterReadTransaction(Rtu concentrator, String serial) {
            
            this.concentrator = concentrator;
            this.serial = serial;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see com.energyict.cpo.Transaction#doExecute()
         */
        public Object doExecute() throws BusinessException, SQLException {
            
        	ProfileData[] pd = {new ProfileData(), new ProfileData()};
            Rtu meter = findOrCreate(concentrator, serial, "Electricity");

            try {
                
                if (meter != null) {

                    XmlHandler dataHandler = new XmlHandler( getLogger(), getChannelMap(meter) );
                   
                    importProfile(concentrator, meter, dataHandler);
                    importRegisters(concentrator, meter, dataHandler);
                    sendMeterMessages(concentrator, meter, dataHandler);
                    handleRegisters(dataHandler, meter);
                    
                    pd = dataHandler.getProfileData();
                    meter.store(pd[ELECTRICITY]);
                    
                    if ( (meter.getDownstreamRtus().size() > 0) || ( pd[MBUS].getChannelInfos().size() != 0) ){
                    	
                    	if ( mbusSerial == null ){
                    		if(DEBUG)System.out.println("Read the serialnumber of the Mbus device.");
                    		
//                    		mbusSerial = readMbusSerialNumber(dataHandler, meter, serial, concentrator);
                    		
                    	}
                    	
                    	getLogger().log(Level.INFO, "********* Handling M-Bus device for meter " + serial + " *********");
                    	Rtu mbusMeter = findOrCreate(concentrator, serial, MBUSSTRING);
                    	
                    	if ( mbusMeter.getRegisters().size() != 0 ){
                    		dataHandler.getMeterReadingData().getRegisterValues().clear();
                    		importRegisters(concentrator, mbusMeter, dataHandler, meter.getSerialNumber());
                    	}
                    	
                    	if ( mbusMeter.getMessages().size() != 0 ){
                    		sendMeterMessages(concentrator, meter, mbusMeter, dataHandler);
                    	}
                    	
                    	handleRegisters(dataHandler, mbusMeter);
                    	mbusMeter.store(pd[MBUS]);
                    	
                    	getLogger().log(Level.INFO, "***********************************************************************");
                    	
                    }
                    
                    getLogger().log(Level.INFO, "Meter with serialnumber " + serial + " has completely finished");
                    
                }
                
            } catch (ServiceException thrown) {
                
                severe( thrown, thrown.getMessage() );
                thrown.printStackTrace();
                throw new BusinessException(thrown); /* roll back */
                
            } catch (IOException thrown) {
                
                severe( thrown, thrown.getMessage() );
                thrown.printStackTrace();
                throw new BusinessException(thrown); /* roll back */
                
            }
            
            return meter; /* return whatever */
            
        }

    }
    
    private Rtu findOrCreate(Rtu concentrator, String serial, String type) throws SQLException, BusinessException { 
        
        List meterList = mw().getRtuFactory().findBySerialNumber(serial);

        if( meterList.size() == 1 ) {
        	if ( type == MBUSSTRING ){
        		List gasList = mw().getRtuFactory().findByName(serial + " - " + type);
        		
        		if ( gasList.size() == 0 )
        			return createMeter(concentrator, getRtuType(MBUS), serial, type, (Rtu)meterList.get(0));
        		else{
        			((Rtu)gasList.get(0)).updateGateway((Rtu)meterList.get(0));
        			
        			//******************************************************************
        			// this moves the rtu to his concentrator folder
//        			((Rtu)gasList.get(0)).moveToFolder(((Rtu)meterList.get(0)).getFolder());
        			//******************************************************************
        			
        			return (Rtu) gasList.get(0);
        		}
        	}
        		
        	else{
            	((Rtu)meterList.get(0)).updateGateway(concentrator);
            	
    			//******************************************************************
    			// this moves the rtu to his concentrator folder
//            	((Rtu)meterList.get(0)).moveToFolder(concentrator.getFolder());
            	//******************************************************************
            	
                return (Rtu) meterList.get(0);
        	}
        }
        
        if( meterList.size() > 1 ) {
            getLogger().severe( toDuplicateSerialsErrorMsg(serial) );
            return null;
        }
            
        if( getRtuType(ELECTRICITY) == null ) {
            getLogger().severe( AUTO_CREATE_ERROR_1 );
            return null;
        }
        
        if( getRtuType(ELECTRICITY).getPrototypeRtu() == null ) {
            getLogger().severe( AUTOCREATE_ERROR_2 );    
            return null;
        }
        
        return createMeter(concentrator, getRtuType(ELECTRICITY), serial, type);
        
    }

//	private String readMbusSerialNumber(XmlHandler dataHandler, Rtu meter, String serial, Rtu concentrator) throws RemoteException, ServiceException, BusinessException {
////		RtuRegister serialRegister = meter.getRegister(ObisCode.fromString("0.1.128.50.21.255"));
//		
//		String conctime = port(concentrator).getConcentratorSystemTime();
//		
//        List rl = new ArrayList( );
//        Date stdate = new Date();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(stdate);
////        cal.add(Calendar.YEAR,-1);
//        cal.add(Calendar.HOUR_OF_DAY, -1);
//        String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
////        String endBefore = String.valueOf(cal.getTimeInMillis());
////        String endBefore = String.valueOf(Calendar.HOUR_OF_DAY)+":"+String.valueOf(Calendar.MINUTE)+":"+String.valueOf(Calendar.SECOND)	;
//        cal.add(Calendar.HOUR_OF_DAY, -1);
//        String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
////        String startBefore = String.valueOf(cal.getTimeInMillis());
////        String startBefore = String.valueOf(Calendar.HOUR_OF_DAY)+":"+String.valueOf(Calendar.MINUTE)+":"+String.valueOf(Calendar.SECOND)	;
//
//            
//        ObisCode oc = ObisCode.fromString("0.1.128.50.21.255");
//        rl.add( oc.toString() );
//            
////        endBefore = conctime;
////        startBefore = conctime;
//        
//        UnsignedInt classId = new UnsignedInt();
//        UnsignedInt attrId = new UnsignedInt();
//        
//        classId.setValue((long)1);
//        attrId.setValue((long)2);
//        
//        if (DEBUG) System.out.println(rl);
//        String registers [] = (String[]) rl.toArray(new String[0] ); 
////        String str = port(concentrator).meterMbusConfiguration(serial, new UnsignedInt((long)1) , "http://tempuri.org/action/P2LPC.CosemGetRequest", "value");
////        String r = port(concentrator).getMeterOnDemandResults(serial, oc.toString());
////        byte[] str = port(concentrator).cosemGetRequestEx(serial, startBefore, endBefore, oc.toString(), classId, attrId, startBefore, endBefore);
//        byte[] str = port(concentrator).cosemGetRequest(serial, startBefore, endBefore, "0.1.128.50.21.255", classId, attrId);
////        importData(str, dataHandler);
//        
//        RtuRegister serialRegister = meter.getRegister(ObisCode.fromString("0.1.128.50.21.255"));
//		
//		return null;
//	}

	private void handleRegisters(XmlHandler dataHandler, Rtu meter) throws ServiceException, BusinessException, SQLException {
		
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

	/*
     * Import:
     *   (1) ProfileData
     *   (2) Registers - GN|210208| Not anymore
     *   (3) Events
     */
    private void importProfile(Rtu ctr, Rtu meter, XmlHandler dataHandler) throws ServiceException, IOException, BusinessException {
    
        String xml = null;        
        String profile = null;
        String mtr = meter.getSerialNumber();
        
        Date fromDate = getLastReading(meter);
        
        // if the meter has mbus meters with an earlier LastReading, then use this LastReading
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
        
        String[] loadProfile1 = {"LoadProfile1", "99.1.0"};
        String[] loadProfile2 = {"LoadProfile2", "99.2.0"};
        
        /*
         * Read profile data 
         */
        if( communicationProfile.getReadDemandValues() ) {
        	
        	getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + mtr + ".");
            
            ProtocolChannelMap channelMap = getChannelMap(meter);
            
            ObjectDef[] lp1 = port(ctr).getMeterProfileConfig(mtr, new ProfileType(loadProfile1[0]));

            ObjectDef[] lp2 = null;
            int lpPeriod1 = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType(loadProfile1[0])).intValue();
            int lpPeriod2 = -1;
            
            if (meter.getIntervalInSeconds() != lpPeriod1){
            	getLogger().log(Level.SEVERE, "ProfileInterval meter: " + lpPeriod1 +  ", ProfileInterval EIServer: " + meter.getIntervalInSeconds());
            	throw new BusinessException("Interval didn't match");
            }
            
            for( int i = 0; i < channelMap.getNrOfProtocolChannels(); i ++ ) {
            
                ProtocolChannel channel = channelMap.getProtocolChannel(i);
                String register = channel.getRegister();
                
                if (lpContainsRegister(lp1, register)){
                	profile = loadProfile1[1];
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    dataHandler.setChannelIndex( i );
                    importData(xml, dataHandler);
                }
                
                else{
                	if (lp2 == null){
                		lp2 = port(ctr).getMeterProfileConfig(mtr, new ProfileType(loadProfile2[0]));
                	}
                	if (lpPeriod2 == -1){
                		lpPeriod2 = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType(loadProfile2[0])).intValue();
                	}
                	
                	
                    if (lpContainsRegister(lp2, register)){
                    	profile = loadProfile2[1];
                        xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                        dataHandler.setChannelIndex( i );
                        importData(xml, dataHandler);
                    }
                	
                }
                
            }
            
            getLogger().log(Level.INFO, "Done reading PROFILE.");
            
        }
        
        /*
         * Read logbook
         */
        if( communicationProfile.getReadMeterEvents() ) {
        	
        	getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + mtr + ".");

            
            from = Constant.getInstance().format(getLastLogboog(meter));
            
            xml = port(ctr).getMeterEvents(mtr, from, to);
            importData(xml, dataHandler);
            xml = port(ctr).getMeterPowerFailures(mtr, from, to);
            importData(xml, dataHandler);
            
            getLogger().log(Level.INFO, "Done reading EVENTS.");
        }
        
        
    
    }
    
    private boolean lpContainsRegister(ObjectDef[] lp, String register) {
		for (int i = 0; i< lp.length; i++){
			String instId = lp[i].getInstanceId();
			if (register.length() == 5){
				if (instId.indexOf(register) == 4)
					return true;
			}
			else
				if (instId.indexOf(register.subSequence(0, register.length()).toString()) >= 0)
					return true;
		}
		return false;
	}

	private void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler) throws ServiceException, IOException, BusinessException{
    	importRegisters(ctr, meter, dataHandler, meter.getSerialNumber());
    }
    
    private void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler, String serialNumb)throws ServiceException, IOException, BusinessException {
    	
    	String xml = null;
    	String mtr = serialNumb;
        String from = Constant.getInstance().format(getLastReading( meter ) );
        String to = Constant.getInstance().format(new Date());
    	
        /*
         * Read registers 
         * (use lastReading as from date !!)
         */
    	
        if( communicationProfile.getReadMeterReadings() ) {
        	
        	getLogger().log(Level.INFO, "Reading REGISTERS from meter with serialnumber " + mtr + ".");
        	
        	String daily = null;
        	String monthly = null;
        	int count = 0;
        	
//            CosemDateTime cdt = port(ctr).getMeterBillingReadTime(meter.getSerialNumber());
//            CosemDateTime[] cdt2 = port(ctr).getMeterScheduledReadTimes(mtr);
//            ObjectDef[] lp1 = port(ctr).getMeterProfileConfig(mtr, new ProfileType("LoadProfile2"));
            
            while( ((daily == null ) || (monthly == null)) || count != 2 ) {
            	switch(count){
            	case 0:{
            		int period = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType("LoadProfile2")).intValue();
            		if ( period == 86400 ){ // Profile contains daily values
            			daily = "99.2.0";
            		}
            		else
            			daily = null;
            		count++;
            	}break;
            	case 1:{
            		CosemDateTime cdt = port(ctr).getMeterBillingReadTime(mtr);
            		if ( (cdt.getDayOfMonth().intValue() == 1) && (cdt.getHour().intValue() == 0) && (cdt.getYear().intValue() == 65535) && (cdt.getMonth().intValue() == 255) ){
            			monthly = "98.1.0";
            			if (daily == null) daily = "98.2.0";
            		}
            		else{
            			monthly = "98.2.0";
            			if (daily == null) daily = "98.1.0";
            		}
            		count++;
            	}break;
            	default:break;
            	
            	}
            }
            
            // set registers for the DataHandler
            dataHandler.setDailyStr(daily);
            dataHandler.setMonthlyStr(monthly);
        	
            Iterator i = meter.getRtuType().getRtuRegisterSpecs().iterator();
            while (i.hasNext()) {
                
                RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                ObisCode oc = spec.getObisCode();
                String register = oc.toString();
                String profile = null;
                
                if (oc.getF() == 0){
                    
                    /* historical - daily*/
//                    profile = "99.2.0";
                	profile = daily;
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    importData(xml, dataHandler);
                   
                }
                
                else if (oc.getF() == -1){

                	 /* historical - monthly*/
//                    profile = "98.1.0";
                	profile = monthly;
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    importData(xml, dataHandler);
//
//                    profile = "98.2.0";
//                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
//                    importData(xml, dataHandler);
                    
                }
            }
            
            getLogger().log(Level.INFO, "Done reading REGISTERS.");
            
        }
    }
    
    String readWithStringBuffer(Reader fileReader) throws IOException {
    	BufferedReader br = new BufferedReader(fileReader);
    	String line;
    	StringBuffer result = new StringBuffer();
    	while ((line = br.readLine()) != null) {
    		result.append(line);
    	}
    	return result.toString();
    }
    
    Date getLastReading(Rtu rtu) {
        Date result = rtu.getLastReading();
        if( result == null ) {
            result = new Date( 0 );
        }
        return result;
    }
    
    Date getLastLogboog(Rtu rtu) {
        Date result = rtu.getLastLogbook();
        if( result == null ) {
            result = new Date( 0 );
        }
        return result;
    }
    
    /** Generic data import procedure. All imported data is in one xml format. */
    private void importData(String data, XmlHandler dataHandler) 
        throws BusinessException {
        
        try {
            debug(data);
            
            byte[] bai = data.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, dataHandler);
            
        } catch (ParserConfigurationException thrown) {
            severe(thrown, "Parsing failed: " + data);
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (SAXException thrown) {
            severe(thrown, "Parsing failed: " + data);
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (IOException thrown) {
            severe(thrown, "Parsing failed: " + data);
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        }
    
    }
    
    private void sendMeterMessages(Rtu concentrator, Rtu rtu, XmlHandler dataHandler) throws BusinessException, SQLException{
    	sendMeterMessages(concentrator, rtu, null, dataHandler);
    }
    
    /** Send Pending RtuMessage to meter. 
     * 	Currently we use the eRtu as a concentrator for the mbusRtu, so the serialNumber is this from the eRtu.
     * 	The messages them are those from the mbus device if this is not NULL.
     * */
    private void sendMeterMessages(Rtu concentrator, Rtu eRtu, Rtu mbusRtu, XmlHandler dataHandler) throws BusinessException, SQLException {
    
        /* short circuit */
        if( ! communicationProfile.getSendRtuMessage() )
            return;
        
        Iterator mi = null;
        
        if (mbusRtu != null)	//mbus messages
        	mi = mbusRtu.getPendingMessages().iterator();
        else					//eRtu messages
            mi = eRtu.getPendingMessages().iterator();
        
        String serial = eRtu.getSerialNumber();     
        
        if (mi.hasNext())
        	getLogger().log(Level.INFO, "Handling MESSAGES from meter with serialnumber " + serial);
        else
        	return;
        
        while (mi.hasNext()) {
            
            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();
            
            boolean doReadRegister  = contents.indexOf(Constant.ON_DEMAND) != -1;
            boolean doConnect       = contents.indexOf(Constant.CONNECT_LOAD) != -1;
            boolean doDisconnect    = contents.indexOf(Constant.DISCONNECT_LOAD) != -1;
            
            boolean loadControlOn     = contents.indexOf(Constant.LOAD_CONTROL_ON) != -1;
            boolean loadControlOff    = contents.indexOf(Constant.LOAD_CONTROL_OFF) != -1;
                        
            /* A single message failure must not stop the other msgs. */
            try {
            	
                if (doReadRegister){
                    
                    List rl = new ArrayList( );
                    Iterator i = null;
                    String ocString = null;
                    
                    if (mbusRtu != null)
                    	i = mbusRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    else
                    	i = eRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    
                    while (i.hasNext()) {
                        
                        RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                        ObisCode oc = spec.getRegisterMapping().getObisCode();
                        if (oc.getF() == 255){
                        	
                        	if (mbusRtu != null)
                        		rl.add(oc.toString());
                        	else
                        		rl.add( new String(oc.getC()+"."+oc.getD()+"."+oc.getE()) );
                        	
	                        dataHandler.checkOnDemands(true);
	                        dataHandler.setProfileDuration(-1);
                        }
                        
                    }
                    if (DEBUG) System.out.println(rl);
                    String registers [] = (String[]) rl.toArray(new String[0] ); 
                    String r = port(concentrator).getMeterOnDemandResultsList(serial, registers);
                    
//                    String r = port(concentrator).getMeterOnDemandResults(serial, register);
                    
                    importData(r, dataHandler);
                    dataHandler.checkOnDemands(false);
                    
                }
                
                if (doConnect) {
                    port(concentrator).setMeterDisconnectControl(serial, true);
                }
                
                if (doDisconnect) {
                    port(concentrator).setMeterDisconnectControl(serial, false);
                }
                
                if (loadControlOn) {
                    port(concentrator).setMeterLoadControl(serial, true);
                }
                
                if (loadControlOff) {
                    port(concentrator).setMeterLoadControl(serial, false);
                }
                
                /* These are synchronous calls, so no sent state is ever used */
                msg.confirm();
                getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                
            } catch (RemoteException re) {
                msg.setFailed();
                re.printStackTrace();
                severe(re, re.getMessage());
                throw new BusinessException(re);
            } catch (ServiceException se) {
                msg.setFailed();
                se.printStackTrace();
                severe(se, se.getMessage());
                throw new BusinessException(se);
            }
            
        }
        
        getLogger().log(Level.INFO, "Done handling messages.");
    
    }
    
    /** Create a meter for configured RtuType */
    
    private Rtu createMeter(Rtu concentrator, RtuType type, String serial, String energyType) 
    throws SQLException, BusinessException {
    	return createMeter(concentrator, type, serial, energyType, concentrator);
    }
    
    private Rtu createMeter(Rtu concentrator, RtuType type, String serial, String energyType, Rtu gwRtu) 
        throws SQLException, BusinessException {
        
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MONTH, -1);
//        Date lastreading = cal.getTime();
//    	Date lastreading = type
        
        RtuShadow shadow = type.newRtuShadow();
        
        Date lastreading = shadow.getLastReading();
        
        if ( energyType == MBUSSTRING ){
        	shadow.setName(serial + " - " + energyType);
        }
        else{
        	shadow.setName(serial + " - " + energyType);
            shadow.setSerialNumber(serial);
        }
        
        //*************************************************
        // this moves the new Rtu to the Concentrator folder, else it will be placed in the prototype folder
        // shadow.setFolderId(gwRtu.getFolderId());
        //*************************************************
        
    	shadow.setGatewayId(gwRtu.getId());
    	shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);
        
    }
    
    /** Import a single concentrator. 
     * @throws ServiceException 
     * @throws RemoteException 
     * @throws ParseException */
    private void handleConcentrator(Rtu concentrator) throws BusinessException, SQLException, RemoteException, ServiceException, ParseException {
    	
    	getLogger().log(Level.INFO, "Handling the concentrator with serialnumber: " + concentrator.getSerialNumber());
        
        if( communicationProfile.getWriteClock() ) {
            setTime(concentrator);
        }
        
        
        /* short circuit */
        if( communicationProfile.getSendRtuMessage() ) {
        
            String serial = concentrator.getSerialNumber();
            Iterator i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                handleConcentratorRtuMessage(concentrator, serial, msg);
            }
            
        }
        
        getLogger().log(Level.INFO, "Concentrator " + concentrator.getSerialNumber() + " has completely finished.");
        
    }

    private void setTime(Rtu concentrator) 
        throws RemoteException, ServiceException, ParseException {
        
        /* Don't worry about clock sets over interval boundaries, Iskra
         * will (probably) handle this. 
         */
        
        String systime = port(concentrator).getConcentratorSystemTime();
        
        systime = 
            Pattern.compile(":\\d{2}$").matcher(systime).replaceFirst("00");
        Date cTime = Constant.getInstance().getDateFormat().parse(systime);
        
        Date now = new Date();
        
        long sDiff = ( now.getTime() - cTime.getTime() ) / 1000;
        long sAbsDiff = Math.abs( sDiff );
        
        getLogger().info( 
                "Difference between metertime and systemtime is " + sDiff * 1000 
                + " ms");
        
        long max = communicationProfile.getMaximumClockDifference();
        long min = communicationProfile.getMinimumClockDifference();
        
        if( ( sAbsDiff < max ) && ( sAbsDiff > min ) ) { 
            
            getLogger().severe("Adjust meter time to system time");
        
            String d = Constant.getInstance().getDateFormatFixed().format(now);
            
            port(concentrator).setConcentratorSystemTime(d);
            port(concentrator).timeSync();
            
        }
        
    }
    
    private void handleConcentratorRtuMessage(
        Rtu concentrator, String serial, RtuMessage msg)
            throws BusinessException, SQLException {
        
        boolean success = false;
        
        try {
            
            String contents = msg.getContents();
            
            if (contents.indexOf(Constant.TOU_SCHEDULE) != -1) {
            	
//            	Folder concFolder = concentrator.getContainer();
//            	List ufcon = concFolder.getUserFiles();
            	
            	int idUF = findTOUConfig(concentrator);
            	
            	if (idUF != -1){
            		getLogger().info("Sending new tariff program to concenctrator with serialnumber: " + concentrator.getSerialNumber());
            		
                    UserFile uf = mw().getUserFileFactory().find(idUF);
                    if (uf != null) {
                        
                        String xml = new String(uf.loadFileInByteArray());
                        port(concentrator).setMeterTariffSettings(xml);
                        success = true;
                        getLogger().info("New tariff program succesfully implemented.");
                        
                    } else {
                        getLogger().severe(toErrorMsg(msg) + "User file not found (id=" + idUF + ")");
                    }
                    
            	}
            	else{
            		getLogger().severe(toErrorMsg(msg) + "No Userfile with the name 'TOU' in concentrator folder");
            	}
//                Element e = (Element) toDom(contents).getFirstChild();
//                String idString = e.getAttribute(Constant.USER_FILE_ID);
//                
//                int id = Integer.parseInt(idString);
            }
            
        /* A single RtuMessage failed: log and try next msg. */
        } catch (NumberFormatException thrown) {
            severe(thrown, toErrorMsg(msg) + " Id is not a number.");
            thrown.printStackTrace();
        } catch (ServiceException thrown) {
            severe(thrown, toErrorMsg(serial, msg));
            thrown.printStackTrace();
        } catch (IOException thrown) {
            severe(thrown, toErrorMsg(serial, msg));
            thrown.printStackTrace();
        } finally {
            if (success)
                msg.confirm();
            else
                msg.setFailed();
        }
        
    }
    
    private int findTOUConfig(Rtu concentrator) {
    	Folder concFolder = concentrator.getContainer();
    	List ufcon = concFolder.getUserFiles();
    	
    	for( int i = 0; i < ufcon.size(); i++ ){
    		if ( ((UserFile)ufcon.get(0)).getName().compareToIgnoreCase(TOUConfig) == 0 )
    			return ((UserFile)ufcon.get(0)).getId();
    	}
    	
		return -1;
	}

	/** Short notation for MeteringWarehouse.getCurrent() */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }
    
    /**
     * Collect all DeviceID attributes from a meter list. Alternative is SAX, but that seems overkill.
     */
    private List collectSerials(String list) throws XmlException {
        
        List result = new ArrayList();
        NodeList nl = toDom(list).getElementsByTagName("Meter");
        
        for( int i = 0; i < nl.getLength(); i ++ ) {
            Element e = (Element)nl.item(i);
            if( "DLCMeters".equals( e.getAttribute("GroupID") ) )
                result.add( e.getAttribute( "DeviceID" ) );
        }
        
        return result;
        
    }
    
    /** Instantiate webservice stub */
    private P2LPCSoapPort_PortType port(Rtu concentrator) throws ServiceException {
        
        WebServiceLocator wsl = new WebServiceLocator();
        wsl.setP2LPCSoapPortEndpointAddress(getUrl(concentrator));
        P2LPCSoapPort_PortType port = wsl.getP2LPCSoapPort();

        return port;
        
    }
    
    /* Properties */

    /** @return Network address of concentrator */
    private String getUrl(Rtu concentrator) {
        return "http://" + concentrator.getPhoneNumber() + "/WebService.wsdl";
    }
    
    
    /** Find RtuType for creating new meters. */
//    private RtuType getRtuType() {
//        if (rtuType == null) {
//            String type = getProperty(Constant.RTU_TYPE);
//            rtuType[ELECTRICITY] = mw().getRtuTypeFactory().find(type);
//        }
//        return rtuType[ELECTRICITY];
//    }
    
    private RtuType getRtuType(int energyType) {
    	
    	
        if (rtuType[energyType] == null) {
            String type = getProperty(Constant.RTU_TYPE);
            if (type != null){
	            type = type.split(":")[energyType];
	            rtuType[energyType] = mw().getRtuTypeFactory().find(type);
            }
            else return null;
        }
        
        return rtuType[energyType];
    }
    
    /** Dial up property defined on Concentrator RTU. */
    private boolean useDialUp(Rtu concentrator) {
        return concentrator.getProperties().getProperty(Constant.USE_DIAL_UP) != null;
    }
    
    private String getProperty(String key){
        return (String)properties.get(key);
    }
    
    private String getPassword(Rtu concentrator) {
        return concentrator.getProperties().getProperty(Constant.PASSWORD);
    }
    
    private String getUser(Rtu concentrator){
        return concentrator.getProperties().getProperty(Constant.USER);
    }
    
    /* Error msgs */

    /** Convert/wrap concentrator serial to err msg */
    private String toConcetratorErrorMsg(String serial) {
        return new StringBuffer()
                .append(CONCENTRATOR_ERROR)
                .append(serial)
                .append(".")
                    .toString();
    }
    
    /** Convert/wrap concentrator serial & handled meter list to err msg */
    private String toConcetratorErrorMsg(String serial, StringBuffer progress) {
        
        StringBuffer rslt = 
            new StringBuffer()
                .append(toConcetratorErrorMsg(serial))
                .append("  ");
        
        String xtra = progress.toString().trim();
        
        if (xtra.length() == 0) {
            rslt.append("No meters processed. ");
        } else {
            rslt.append("Processed meters: ")
                .append(xtra)
                .append(". ");
        }
        
        return rslt.toString();
        
    }
    
    /** Convert an RtuMessage to an error message */
    private String toErrorMsg(RtuMessage message) {
        return new StringBuffer()
                  .append("RtuMessage failed: \"")
                  .append(message.getContents())
                  .append("\".")
                      .toString();
    }
    
    private String toErrorMsg(String serial, RtuMessage msg) {
        return toConcetratorErrorMsg(serial) + toErrorMsg(msg);
    }
    
    private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat( DUPLICATE_SERIALS )
                    .format( new Object [] { serial } );
    }
    
    private ProtocolChannelMap getChannelMap(Rtu meter) throws InvalidPropertyException {
    	if (protocolChannelMap == null){
    		String sChannelMap = meter.getProperties().getProperty( Constant.CHANNEL_MAP );
    		protocolChannelMap = new ProtocolChannelMap( sChannelMap ); 
    	}
        
        return protocolChannelMap;
    }
    
    /** log to severe */
    private void severe(Throwable thrown, String eMsg) {
        String msg = eMsg + " (" + thrown.toString() + ")";
        getLogger().log(Level.SEVERE, msg, thrown);
    }
    
    /** log to severe */
    private void severe(String eMsg) {
        getLogger().severe(eMsg);
    }
    
    /** DOM wrapping */
    private Document toDom(String data) throws XmlException  {
        
        InputSource is = new InputSource(new StringReader(data));
        try {
            
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            
        } catch (SAXException thrown) {
            severe(thrown, "Parsing failed: " + data);       
            thrown.printStackTrace();
            throw new XmlException(thrown);
        } catch (IOException thrown) {
            severe(thrown, "Parsing failed: " + data);            
            thrown.printStackTrace();
            throw new XmlException(thrown);
        } catch (ParserConfigurationException thrown) {
            severe(thrown, "Parsing failed: " + data);
            thrown.printStackTrace();
            throw new XmlException(thrown);
        } catch (FactoryConfigurationError thrown) {
            severe(thrown, "Parsing failed: " + data);
            thrown.printStackTrace();
            throw new XmlException(thrown);
        }
        
    }
    
    /** Some try/catch releaf */
    private class XmlException extends Exception {
        
        private static final long serialVersionUID = 1L;

        public XmlException(String message, Throwable cause) {
            super(message, cause);
        }

        public XmlException(Throwable cause) {
            super(cause);
        }
        
    }
    
    /* Dbg */

    /** dump some verbose dbg info to std out */
    private void debug(String msg) {
        if (DEBUG) {
            System.out.println("DBG:: " + msg);
            getLogger().severe(msg);
        }
    }
    
    public List getMessageCategories() {
        
        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageSpec msgSpec = null;
        
        String xml = Constant.TOU_SCHEDULE;
        msgSpec = addBasicMsg("Set tou schedule", xml, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
        
    }
    
    public String writeMessage(Message msg) {
        return msg.write(this);
    }
    
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
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
    
}
