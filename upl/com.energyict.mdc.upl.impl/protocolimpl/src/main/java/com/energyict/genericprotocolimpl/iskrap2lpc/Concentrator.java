package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedInt;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.WebServiceLocator;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.tcpip.PPPDialer;

public class Concentrator implements Messaging, GenericProtocol {
    
    private boolean DEBUG = true;
    private boolean TESTING = false;
    
    private static final int ELECTRICITY 	= 0x00;
    private static final int GAS 			= 0x01;
    
    private static File plpFile = new File("c://TEST_FILES/Sub_38517965_200802.plp");

    private Logger logger;
    private Properties properties;
    private CommunicationProfile communicationProfile;	
    
    /** RtuType is used for creating new Rtu's */
    private RtuType[] rtuType = {null, null};
    
    
    /** Error message for a meter error */
    private final static String METER_ERROR = 
        "Meter failed, serialnumber meter: ";
    
    /** Error message for a concentrator error */
    private final static String CONCENTRATOR_ERROR = 
        "Concentrator failed, serialnumber meter: ";
    
    private final static String AUTO_CREATE_ERROR_1 =
        "No automatic meter creation: no property RtuType defined.";

    private final static String AUTOCREATE_ERROR_2 =
        "No automatic meter creation: property RtuType has no prototype.";

    private final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    
    
    private final static boolean ADVANCED = true;
    
    

    public void execute( 
        CommunicationScheduler scheduler, Link link, Logger logger) 
            throws BusinessException, SQLException, IOException {
        
        this.logger = logger;
        this.communicationProfile = scheduler.getCommunicationProfile();
        
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
            
            getLogger().log(Level.INFO,"Discoverd meter count: " + discovered.size());
            
            
            Iterator im = discovered.iterator();
            while (im.hasNext()) {
                String meterSerial = (String) im.next();
                handleMeter(concentrator, meterSerial);
                progressLog.append(meterSerial + " ");
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

                    XmlHandler dataHandler = new XmlHandler( 
                            getLogger(), getChannelMap(meter) );
                   
                    importProfile(concentrator, meter, dataHandler);
                    importRegisters(concentrator, meter, dataHandler);
                    sendMeterMessages(concentrator, meter, dataHandler);
                    handleRegisters(dataHandler, meter);
                    
                    pd = dataHandler.getProfileData();
                    meter.store(pd[ELECTRICITY]);
                    
                    if ( pd[GAS].getChannelInfos().size() != 0){
                    	
                    	if (DEBUG) System.out.println("Make a new GAS meter!");
                    	
                    	if ( mbusSerial == null ){
                    		if(DEBUG)System.out.println("Read the serialnumber of the Mbus device!");
                    		
//                    		mbusSerial = readMbusSerialNumber(dataHandler, meter, serial, concentrator);
                    		
                    	}
                    	
                    	getLogger().log(Level.INFO, "New M-Bus device for meter with serialnumber " + serial + ", discovered.");
                    	Rtu gasMeter = findOrCreate(concentrator, serial, "Gas");
                    	
                    	if ( gasMeter.getRegisters().size() != 0 ){
                    		dataHandler.getMeterReadingData().getRegisterValues().clear();
                    		importRegisters(concentrator, gasMeter, dataHandler, meter.getSerialNumber());
                    		handleRegisters(dataHandler, gasMeter);
                    		sendMeterMessages(concentrator, gasMeter, dataHandler);
                    	}
                    	
                    	gasMeter.store(pd[GAS]);
                    	
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
        	if ( type == "Gas" ){
        		List gasList = mw().getRtuFactory().findByName(serial + " - " + type);
        		
        		if ( gasList.size() == 0 )
        			return createMeter(concentrator, getRtuType(GAS), serial, type, (Rtu)meterList.get(0));
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

        String mtr = meter.getSerialNumber();

        String from = Constant.getInstance().format(getLastReading( meter ) );
        String to = Constant.getInstance().format(new Date());
        
        /*
         * Read profile data 
         */
        if( communicationProfile.getReadDemandValues() ) {
        	
        	getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + mtr + ".");
            
            ProtocolChannelMap channelMap = getChannelMap(meter);
            for( int i = 0; i < channelMap.getNrOfProtocolChannels(); i ++ ) {
            
                ProtocolChannel channel = channelMap.getProtocolChannel(i);
                String profile = "99.1.0";
                String register = channel.getRegister();
                
                if ( TESTING ){
                	FileReader inFile = new FileReader(plpFile);
                	xml = readWithStringBuffer(inFile);
                }
                else
                	xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);

                dataHandler.setChannelIndex( i );
                importData(xml, dataHandler);
            
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
        	
            Iterator i = meter.getRtuType().getRtuRegisterSpecs().iterator();
            while (i.hasNext()) {
                
                RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                ObisCode oc = spec.getObisCode();
                String register = oc.toString();
                String profile = null;
                
                if (oc.getF() == 0){
                    
                    /* historical - daily*/
                    profile = "99.2.0";
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    importData(xml, dataHandler);
                   
                }
                
                else if (oc.getF() == -1){

                	 /* historical - monthly*/
                    profile = "98.1.0";
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    importData(xml, dataHandler);

                    profile = "98.2.0";
                    xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    importData(xml, dataHandler);
                    
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
    
    /** Send Pending RtuMessage to meter. */
    private void sendMeterMessages(Rtu concentrator, Rtu rtu, XmlHandler dataHandler) throws BusinessException, SQLException {
    
        /* short circuit */
        if( ! communicationProfile.getSendRtuMessage() )
            return;
        
        Iterator mi = rtu.getPendingMessages().iterator();
        while (mi.hasNext()) {
            
            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();
            
            boolean doReadRegister  = contents.indexOf(Constant.ON_DEMAND) != -1;
            boolean doConnect       = contents.indexOf(Constant.CONNECT_LOAD) != -1;
            boolean doDisconnect    = contents.indexOf(Constant.DISCONNECT_LOAD) != -1;
            
            String serial = rtu.getSerialNumber();
            
            /* A single message failure must not stop the other msgs. */
            try {
            	
                getLogger().log(Level.INFO, "Handling MESSAGES from meter with serialnumber " + serial);

                
                if (doReadRegister){
                    
                    List rl = new ArrayList( );
                    Iterator i = rtu.getRtuType().getRtuRegisterSpecs().iterator();
                    while (i.hasNext()) {
                        
                        RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                        ObisCode oc = spec.getRegisterMapping().getObisCode();
                        if (oc.getF() == 255){
	                        rl.add( new String(oc.getC()+"."+oc.getD()+"."+oc.getE()) );
	                        dataHandler.addMessageEnd(oc.getF());
                        }
                        
                    }
                    if (DEBUG) System.out.println(rl);
                    String registers [] = (String[]) rl.toArray(new String[0] ); 
                    String r = port(concentrator).getMeterOnDemandResultsList(serial, registers);
                    
//                    String register = "1.8.1";
                    
//                    String r = port(concentrator).getMeterOnDemandResults(serial, register);
                    
                    importData(r, dataHandler);
                    
                }
                
                if (doConnect) {
                    port(concentrator).setMeterDisconnectControl(serial, true);
                }
                
                if (doDisconnect) {
                    port(concentrator).setMeterDisconnectControl(serial, false);
                }
                
                /* These are synchronous calls, so no sent state is ever used */
                msg.confirm();
                getLogger().log(Level.INFO, "Current message " + contents + " has finished!");
                
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
        
        if ( energyType == "Gas" ){
        	shadow.setName(serial + " - " + energyType);
        }
        else{
        	shadow.setName(serial + " - " + energyType);
            shadow.setSerialNumber(serial);
        }
        
        //*************************************************
        // this moves the new Rtu to the Concentrator folder, else it will be placed in the prototype folder
//        shadow.setFolderId(gwRtu.getFolderId());
        //*************************************************
        
    	shadow.setGatewayId(gwRtu.getId());
    	shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);
        
    }
    
    /** Import a single concentrator. 
     * @throws ServiceException 
     * @throws RemoteException 
     * @throws ParseException */
    private void handleConcentrator(Rtu concentrator) 
        throws BusinessException, SQLException, RemoteException, 
                ServiceException, ParseException {
        
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
                
                Element e = (Element) toDom(contents).getFirstChild();
                String idString = e.getAttribute(Constant.USER_FILE_ID);
                
                int id = Integer.parseInt(idString);
                
                UserFile uf = mw().getUserFileFactory().find(id);
                if (uf != null) {
                    
                    String xml = new String(uf.loadFileInByteArray());
                    port(concentrator).setMeterTariffSettings(xml);
                    success = true;
                    
                } else {
                    severe(toErrorMsg(msg) + " User file not found (id=" + id + ")");
                }
                
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
        } catch (XmlException thrown) {
            severe(thrown, toErrorMsg(serial, msg));
            thrown.printStackTrace();
        } finally {
            if (success)
                msg.confirm();
            else
                msg.setFailed();
        }
        
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
        String sChannelMap = meter.getProperties().getProperty( Constant.CHANNEL_MAP );
        return new ProtocolChannelMap( sChannelMap );
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
        
        String xml = Constant.TOU_SCHEDULE + " " + Constant.USER_FILE_ID + "=\"\"";
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
