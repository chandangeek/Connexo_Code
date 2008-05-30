package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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

import org.apache.axis.types.UnsignedByte;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedShort;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.WebServiceLocator;
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
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
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
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.tcpip.PPPDialer;

public class Concentrator implements Messaging, GenericProtocol {
    
    private boolean DEBUG = false;
    private boolean TESTING = false;
    
    private static final int ELECTRICITY 	= 0x00;
    private static final int MBUS 			= 0x01;
    
    private static String conSerialFile 	= "/offlineFiles/ConcentratorSerial.xml";
    private static String profileConfig1 	= "/offlineFiles/ObjectDefFile1.xml";
    private static String profileConfig2 	= "/offlineFiles/ObjectDefFile2.xml";
    private static String[] profileFiles 	= {"/offlineFiles/profile0.xml", "/offlinefiles/profile1.xml"};
    private static String mbusProfile 		= "/offlineFiles/mbus.xml";
    private static String eventsFile 		= "/offlineFiles/events.xml";
    private static String powerDownFile 	= "/offlineFiles/powerFailures.xml";
    private static String dateTimeFile 		= "/offlineFiles/cosemDateTime.xml";
    private static String conEventFile 		= "/offlineFiles/conEvent.xml";
    private static String mbusSerialFile 	= "/offlineFiles/mbusSerial.xml";
    private static String testFile 			= "/offlineFiles/test.xml";
    
    private Logger 					logger;
    private Properties 				properties;
    protected CommunicationProfile 	communicationProfile;	
    private MbusDevice[]			mbusDevices = {null, null, null, null};				// max. 4 MBus meters
    
    /** Cached Objects */
	public int confProgChange;
	public int loadProfilePeriod1;
	public int loadProfilePeriod2;
	public boolean changed;
	public ObjectDef[] loadProfileConfig1;
	public ObjectDef[] loadProfileConfig2;
	public CosemDateTime billingReadTime;
	public CosemDateTime captureObjReadTime;
    
    /** RtuType is used for creating new Rtu's */
    private RtuType[] rtuType = {null, null};
	private ProtocolChannelMap protocolChannelMap = null;
    
    /** Error message for a meter error */
    private final static String METER_ERROR = 
        "Meter failed, serialnumber meter: ";
    
    /** Error message for a concentrator error */
    private final static String CONCENTRATOR_ERROR = 
        "Concentrator failed, serialnumber concentrator: ";
    
//    private final static String AUTO_CREATE_ERROR_1 =
//        "No automatic meter creation: no property RtuType defined.";
//
//    private final static String AUTOCREATE_ERROR_2 =
//        "No automatic meter creation: property RtuType has no prototype.";

    private final static String DUPLICATE_SERIALS =
        "Multiple meters where found with serial: {0}.  Data will not be read.";
    
    private final static String NO_AUTODISCOVERY =
        "Meter doesn't exist in database and no rtuType is configured so automatic meter creation.";
    
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
            
            String conSerial = checkConcentratorSerial(concentrator);
            String meterList = null;
            List meters = null;
            
            if(serial.equalsIgnoreCase(conSerial)){
            	
//            	RtuType type = getRtuType(ELECTRICITY);
            	RtuType type = getRtuType(concentrator);
            	
            	/* use the meters in the dataBase */
            	if(type == null){ 	
            		meters = concentrator.getDownstreamRtus();
            		meters = collectSerialsFromRtuList(meters);
            	}
            	/* use the auto discovery */ 
            	else{					
            		meterList = port(concentrator).getMetersList();
            		meters = collectSerials(meterList);
            	}
            	
                meterCount = meters.size();
                
                getLogger().log(Level.INFO, meterCount + " meter(s) will be handled");
                
                Iterator im = meters.iterator();
                while (im.hasNext()) {
                    String meterSerial = (String) im.next();
                    handleMeter(concentrator, meterSerial);
                    progressLog.append(meterSerial + " ");
                    getLogger().log(Level.INFO, "" + --meterCount + " meter(s) to go.");
                }
                
                handleConcentrator(concentrator);
            }
            
            else{
            	getLogger().log(Level.CONFIG, "ConcentratorID EIServer(" + serial + ") didn't match concentratorID(" + conSerial + ").");
            }

            
            
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
    
	private List collectSerialsFromRtuList(List meters) {
		ArrayList serials = new ArrayList();
		Iterator it = meters.iterator();
		while(it.hasNext()){
			serials.add((String)((RtuImpl)it.next()).getSerialNumber());
		}
		return serials;
	}

	private String checkConcentratorSerial(Rtu concentrator) throws BusinessException {
		String conID = null;
		getLogger().log(Level.INFO, "Checking concentrator serialnumber.");
		
        try {
			if (TESTING) {
				FileReader inFile = new FileReader(Utils.class.getResource(conSerialFile).getFile());
				conID = readWithStringBuffer(inFile);
				return conID;
			} else {
				conID = port(concentrator).getConcentratorStatus();
				return conID.substring(conID.indexOf('"') + 1, conID.indexOf('"',conID.indexOf('"') + 1));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException("Failed while reading the concentrator serialnumber.");
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new BusinessException("Failed while reading the concentrator serialnumber.");
		}
	}
	

	public void addProperties(Properties properties) {
        this.properties = properties;
    }

    public String getProtocolVersion() {
//        return "$Revision: 1.9$";
    	return "$Date$";
    }
    
    public String getVersion() {
//        return "$Revision: 1.9$";
    	return "$Date$";
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
            
            MeterReadTransaction mrt = new MeterReadTransaction(concentrator, serial);
            
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
    class MeterReadTransaction implements Transaction, CacheMechanism {
    	
        private Object source;
        
        /** Concentrator "containing" the meter */
        private Rtu concentrator;
        
        /** Serial of the meter */
        private String serial;
        
        /** Serial of the mbusMeter */
        private String mbusSerial[] = {null, null, null, null};
        private String mSerial;

		private Cache dlmsCache;
        
        public MeterReadTransaction(Rtu concentrator, String serial) {
            
            this.concentrator = concentrator;
            this.serial = serial;
            this.dlmsCache = new Cache();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see com.energyict.cpo.Transaction#doExecute()
         */
        public Object doExecute() throws BusinessException, SQLException {
            
        	ProfileData[] pd = {new ProfileData(), new ProfileData()};
            Rtu meter = findOrCreate(concentrator, serial, ELECTRICITY);
            
            try {
				startCacheMechanism(this);

			} catch (FileNotFoundException e) {
				// absorb
				e.printStackTrace();
			} catch (IOException e) {
				// absorb
				e.printStackTrace();
			}
            
            try {
            	
				collectCache();
				saveConfiguration();
            	checkMbusDevices(meter);
                
                if (meter != null) {

                    XmlHandler dataHandler = new XmlHandler( getLogger(), getChannelMap(meter) );
                   
                    importProfile(concentrator, meter, dataHandler);
                    
                    pd = dataHandler.getProfileData();
                    meter.store(pd[ELECTRICITY]);
                    
                    importRegisters(concentrator, meter, dataHandler);
                    sendMeterMessages(concentrator, meter, dataHandler);
                    handleRegisters(dataHandler, meter);
                    
                    if ( mbusDevices[0] != null ){
                    	
                    	if(mbusSerial[0].equalsIgnoreCase(mSerial)){
                        	
                        	if(mbusDevices[0].getRtu() != null){
                        		
                        		mbusDevices[0].getRtu().store(pd[MBUS]);
                        		
                            	if ( mbusDevices[0].getRtu().getRegisters().size() != 0 ){
                            		dataHandler.getMeterReadingData().getRegisterValues().clear();
                            		importRegisters(concentrator, mbusDevices[0].getRtu(), dataHandler, meter.getSerialNumber());
                            	}
                            	
                            	if ( mbusDevices[0].getRtu().getMessages().size() != 0 ){
                            		sendMeterMessages(concentrator, meter, mbusDevices[0].getRtu(), dataHandler);
                            	}
                            	
                            	handleRegisters(dataHandler, mbusDevices[0].getRtu());
                        	}
                    	}
                    	else
                    	getLogger().log(Level.CONFIG, "MBus serialnumber in EIServer(" + mbusSerial[0] + ") didn't match serialnumber in meter(" + mSerial+ ")");
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

		private void checkMbusDevices(Rtu meter) throws IOException, NumberFormatException, ServiceException, SQLException, BusinessException {
			
			if ((meter.getDownstreamRtus().size() > 0) || (getRtuType(meter) != null)){
				if ( TESTING ){
//					FileReader inFile = new FileReader(mbusSerialFile);
					FileReader inFile = new FileReader(Utils.class.getResource(mbusSerialFile).getFile());
					mSerial = readWithStringBuffer(inFile);
				}
				else{
					getLogger().log(Level.INFO, "Checking mbus configuration.");
					mSerial = getMbusSerial(concentrator, serial);
				}
				
				if (( getRtuType(meter) != null ) && (meter.getDownstreamRtus().size() == 0)){
					mbusDevices[0] = new MbusDevice(1, mSerial, findOrCreate(meter, mSerial, MBUS), getLogger());	
				}
				fillInMbusSerials(meter.getDownstreamRtus());
				
				if(!(( getRtuType(meter) != null ) && (meter.getDownstreamRtus().size() == 0)))
					mbusDevices[0] = new MbusDevice(1, mbusSerial[0], findOrCreate(meter, mbusSerial[0], MBUS), getLogger());
				
				
			}
			
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
						iConf = requestConfigurationChanges(concentrator, serial);
						
					}catch (NumberFormatException e) {
						iConf = -1;
						getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
						getLogger().log(Level.INFO, "(This will take several minutes.)");
						requestConfigurationParameters(concentrator, serial);
						dlmsCache.setConfProgChange(iConf);
						e.printStackTrace();
					} catch (ServiceException e) {
						iConf = -1;
						getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
						getLogger().log(Level.INFO, "(This will take several minutes.)");
						requestConfigurationParameters(concentrator, serial);
						dlmsCache.setConfProgChange(iConf);
						e.printStackTrace();
					} catch (RemoteException e) {
						iConf = -1;
						getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
						getLogger().log(Level.INFO, "(This will take several minutes.)");
						requestConfigurationParameters(concentrator, serial);
						dlmsCache.setConfProgChange(iConf);
						e.printStackTrace();
					}
					
					if (iConf != dlmsCache.getConfProgChange()){
						getLogger().log(Level.INFO, "Iskra Mx37x: Configuration changed, requesting configuration parameters...");
						getLogger().log(Level.INFO, "(This will take several minutes.)");
						requestConfigurationParameters(concentrator, serial);
						dlmsCache.setConfProgChange(iConf);
					}
				}
				
				else{ 	//if cache doesn't exist
					getLogger().log(Level.INFO, "Iskra Mx37x: Cache does not exist, requesting configuration parameters...");
					getLogger().log(Level.INFO, "(This will take several minutes.)");
					requestConfigurationParameters(concentrator, serial);
					
					try{
						iConf = requestConfigurationChanges(concentrator, serial);
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
    }
    
	private String getMbusSerial(Rtu concentrator, String meterID) throws NumberFormatException, RemoteException, ServiceException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		cal.add(Calendar.MINUTE, 10);
		String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		String str = new String(port(concentrator).cosemGetRequest(meterID, startBefore, endBefore, Constant.mbusSerialObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2)));
		return str.substring(2);
	}
    
    public void requestConfigurationParameters(Rtu concentrator, String serial) throws BusinessException {
        String loadProfile1 = "LoadProfile1";
        String loadProfile2 = "LoadProfile2";
        
		try {
			
			this.loadProfilePeriod1 = port(concentrator).getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile1)).intValue();
			this.loadProfilePeriod2 = port(concentrator).getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile2)).intValue();
			this.loadProfileConfig1 = port(concentrator).getMeterProfileConfig(serial, new ProfileType(loadProfile1));
			this.loadProfileConfig2 = port(concentrator).getMeterProfileConfig(serial, new ProfileType(loadProfile2));
			this.billingReadTime = port(concentrator).getMeterBillingReadTime(serial);
			
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

	public int requestConfigurationChanges(Rtu concentrator, String serial) throws NumberFormatException, RemoteException, ServiceException {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		String startBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		cal.add(Calendar.MINUTE, 10);
		String endBefore = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
		byte[] byteStrs = port(concentrator).cosemGetRequest(serial, startBefore, endBefore, Constant.confChangeObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2));
		int changes = byteStrs[2];
		changes = changes + (byteStrs[1]<<8);
		return changes;
	}

	public void setCachedObjects(CosemDateTime billingReadTime,
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

	private Rtu findOrCreate(Rtu concentrator, String serial, int type) throws SQLException, BusinessException { 
        
        List meterList = mw().getRtuFactory().findBySerialNumber(serial);

        if( meterList.size() == 1 ) {
    		
        	((Rtu)meterList.get(0)).updateGateway(concentrator);
        	
			//******************************************************************
			// this moves the rtu to his concentrator folder
        	// ((Rtu)meterList.get(0)).moveToFolder(concentrator.getFolder());
        	//******************************************************************
        	
            return (Rtu) meterList.get(0);
        }
        
        if( meterList.size() > 1 ) {
            getLogger().severe( toDuplicateSerialsErrorMsg(serial) );
            return null;
        }
            
//        if( getRtuType(ELECTRICITY) == null ) {
//            getLogger().severe( AUTO_CREATE_ERROR_1 );
//            return null;
//        }
//        
//        if( getRtuType(ELECTRICITY).getPrototypeRtu() == null ) {
//            getLogger().severe( AUTOCREATE_ERROR_2 );    
//            return null;
//        }
        
//        if(getRtuType(type) != null)
//        	return createMeter(concentrator, getRtuType(type), serial);
        if(getRtuType(concentrator) != null)
        	return createMeter(concentrator, getRtuType(concentrator), serial);
        else{
        	getLogger().severe( NO_AUTODISCOVERY ); 
        	return null;
        }
    }

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
    protected void importProfile(Rtu ctr, Rtu meter, XmlHandler dataHandler) throws ServiceException, IOException, BusinessException {
    
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
        
//        String[] loadProfile1 = {"LoadProfile1", "99.1.0"};
//        String[] loadProfile2 = {"LoadProfile2", "99.2.0"};
        
        String lpString1 = "99.1.0";
        String lpString2 = "99.2.0";
        
        /*
         * Read profile data 
         */
        if( communicationProfile.getReadDemandValues() ) {
        	
        	getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + mtr + ".");
            
            ProtocolChannelMap channelMap = getChannelMap(meter);
            
            ObjectDef[] lp1;
            ObjectDef[] lp2 = null;
            int lpPeriod1;
            int lpPeriod2 = -1;
            if ( TESTING ){
//            	FileReader inFile = new FileReader(profileConfig1);
            	FileReader inFile = new FileReader(Utils.class.getResource(profileConfig1).getFile());
            	xml = readWithStringBuffer(inFile);
            	lp1 = getlpConfigObjectDefFromString(xml);
            	lpPeriod1 = 900;
            	inFile = new FileReader(Utils.class.getResource(profileConfig2).getFile());
            	xml = readWithStringBuffer(inFile);
            	lp2 = getlpConfigObjectDefFromString(xml);
            	lpPeriod2 = 3600;
            }
            else{
//            	lp1 = port(ctr).getMeterProfileConfig(mtr, new ProfileType(loadProfile1[0]));
//            	lpPeriod1 = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType(loadProfile1[0])).intValue();
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
//                		FileReader inFile = new FileReader(profileFiles[i]);
                		FileReader inFile = new FileReader(Utils.class.getResource(profileFiles[i]).getFile());
                		xml = readWithStringBuffer(inFile);
                	}
                	else xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                	
                    dataHandler.setChannelIndex( i );
                    importData(xml, dataHandler);
                }
                
                else{
                	
//                	if(TESTING){
//                    	FileReader inFile = new FileReader(profileConfig2);
//                    	xml = readWithStringBuffer(inFile);
//                    	lp2 = getlpConfigObjectDefFromString(xml);
//                    	lpPeriod2 = 3600;
//                	}
//                	else{
//                    	if (lp2 == null){
//                    		lp2 = port(ctr).getMeterProfileConfig(mtr, new ProfileType(loadProfile2[0]));
//                    	}
//                    	if (lpPeriod2 == -1){
//                    		lpPeriod2 = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType(loadProfile2[0])).intValue();
//                    	}	
//                	}
                	
                    if (lpContainsRegister(lp2, register)){
                    	profile = lpString2;
                    	
                    	if(TESTING){
//                    		FileReader inFile = new FileReader(mbusProfile);
                    		FileReader inFile = new FileReader(Utils.class.getResource(mbusProfile).getFile());
                    		xml = readWithStringBuffer(inFile);
                    	}
                    	else xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                    	
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
            String events, powerFailures;
            if(TESTING){
//        		FileReader inFile = new FileReader(eventsFile);
            	FileReader inFile = new FileReader(Utils.class.getResource(eventsFile).getFile());
        		events = readWithStringBuffer(inFile);
//        		inFile = new FileReader(powerDownFile);
        		inFile = new FileReader(Utils.class.getResource(powerDownFile).getFile());
        		powerFailures = readWithStringBuffer(inFile);
            }
            else{
            	events = port(ctr).getMeterEvents(mtr, from, to);
            	powerFailures = port(ctr).getMeterPowerFailures(mtr, from, to);
            }
            importData(events, dataHandler);
            importData(powerFailures, dataHandler);
            
            getLogger().log(Level.INFO, "Done reading EVENTS.");
        }
    }
    
    private ObjectDef[] getlpConfigObjectDefFromString(String xml) {
    	ObjectDef[] od = {null, null, null, null, null, null, null, null, null, null};
    	try {
			Element topElement = toDom(xml).getDocumentElement();
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

	private void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler) throws ServiceException, IOException, BusinessException{
    	importRegisters(ctr, meter, dataHandler, meter.getSerialNumber());
    }
    
    private void importRegisters(Rtu ctr, Rtu meter, XmlHandler dataHandler, String serialNumb)throws ServiceException, IOException, BusinessException {
    	
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
//        	int count = 0;
        	
        	int period;
        	CosemDateTime cdt;
            if ( TESTING ){
//            	FileReader inFile = new FileReader(dateTimeFile);
            	FileReader inFile = new FileReader(Utils.class.getResource(dateTimeFile).getFile());
            	xml = readWithStringBuffer(inFile);
            	period = 3600;
            	cdt = getCosemDateTimeFromXmlString(xml);
            }
            else{
//            	period = port(ctr).getMeterLoadProfilePeriod(mtr, new PeriodicProfileType("LoadProfile2")).intValue();
//            	cdt = port(ctr).getMeterBillingReadTime(mtr);
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
    		
//            while( ((daily == null ) || (monthly == null)) || count != 2 ) {
//                switch(count){
//            	case 0:{
//
//            		count++;
//            	}break;
//            	case 1:{
//
//            		count++;
//            	}break;
//            	default:break;
//            	
//            	}
//            }
            
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
                    List registerValues = mw().getRtuRegisterReadingFactory().findByRegister(meter.getRegister(oc).getId());
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
                        xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                        importData(xml, dataHandler);
                       
                    }
                    
                    else if (oc.getF() == -1){

                    	 /* historical - monthly*/
                    	profile = monthly;
                        xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
                        importData(xml, dataHandler);
                        
//                        profile = "98.2.0";
//                        xml = port(ctr).getMeterProfile(mtr, profile, register, from, to);
//                        importData(xml, dataHandler);
                        
                    }
                }
            }
            getLogger().log(Level.INFO, "Done reading REGISTERS.");
            
        }
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
    
    private CosemDateTime getCosemDateTimeFromXmlString(String xml) {
    	CosemDateTime cdt = null;
    	try {
			Element topElement = toDom(xml).getDocumentElement();
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
            
//            boolean loadControlOn     = contents.indexOf(Constant.LOAD_CONTROL_ON) != -1;
//            boolean loadControlOff    = contents.indexOf(Constant.LOAD_CONTROL_OFF) != -1;
            
            boolean thresholdParameters	= (contents.indexOf(Constant.THRESHOLD_GROUPID) != -1) ||
            									(contents.indexOf(Constant.THRESHOLD_POWERLIMIT) != -1) ||
            									(contents.indexOf(Constant.CONTRACT_POWERLIMIT) != -1);
//            boolean applythreshold			= (contents.indexOf(Constant.THRESHOLD_STARTDT) != -1) ||
//            									(contents.indexOf(Constant.THRESHOLD_STOPDT) != -1);            
            
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
                    String r = port(concentrator).getMeterOnDemandResultsList(serial, registers);
                    
                    importData(r, dataHandler);
                    dataHandler.checkOnDemands(false);
                    
                }
                
                if (doConnect) {
                    port(concentrator).setMeterDisconnectControl(serial, true);
                }
                
                if (doDisconnect) {
                    port(concentrator).setMeterDisconnectControl(serial, false);
                }
                
                if (thresholdParameters){
                	
                	String groupID = getMessageValue(contents, Constant.THRESHOLD_GROUPID);
                	if (groupID.equalsIgnoreCase(""))
                		throw new BusinessException("No groupID was entered.");
                	
                	String thresholdPL = getMessageValue(contents, Constant.THRESHOLD_POWERLIMIT);
                	String contractPL = getMessageValue(contents, Constant.CONTRACT_POWERLIMIT);
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
                		port(concentrator).cosemSetRequest(serial, startBefore, endBefore, Constant.powerLimitObisCode.toString(), new UnsignedInt(3), new UnsignedInt(2), contractPowerLimit);
                	}
                	
                	port(concentrator).setMeterCodeRedGroupId(serial, uiGrId);
                	if (!thresholdPL.equalsIgnoreCase(""))
                		port(concentrator).setMeterCodeRedPowerLimit(serial, crPl);
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
    
	private Calendar getCalendarFromString(String strDate) {
		Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.DATE, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))));
    	cal.set(Calendar.MONTH, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
    	cal.set(Calendar.YEAR, Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));
    	
    	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
    	cal.set(Calendar.MINUTE, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
    	cal.set(Calendar.SECOND, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
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
        return mw().getRtuFactory().create(shadow);
        
    }
    
    /** Import a single concentrator. 
     * @throws ServiceException 
     * @throws ParseException 
     * @throws IOException */
    private void handleConcentrator(Rtu concentrator) throws BusinessException, SQLException, ServiceException, ParseException, IOException {
    	
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
        
        if ( communicationProfile.getReadMeterEvents() ){
        	String from = Constant.getInstance().format(getLastLogboog(concentrator));
        	String to = Constant.getInstance().format(new Date());
        	String conEvents;
            if ( TESTING ){
//            	FileReader inFile = new FileReader(conEventFile);
            	FileReader inFile = new FileReader(Utils.class.getResource(conEventFile).getFile());
            	conEvents = readWithStringBuffer(inFile);
            }
            else
            	 conEvents = port(concentrator).getConcentratorEvents(from, to);
        	XmlHandler dataHandler = new XmlHandler( getLogger(), getChannelMap(concentrator) );
        	ProfileData pd = new ProfileData();
        	importData(conEvents, dataHandler);
        	pd = dataHandler.addEvents();
        	concentrator.store(pd);
        	
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
    	String contents = msg.getContents();
        boolean success = false;
        boolean tou = contents.indexOf(Constant.TOU_SCHEDULE) != -1;
        boolean applyThreshold = (contents.indexOf(Constant.APPLY_THRESHOLD) != -1) ||
        					(contents.indexOf(Constant.THRESHOLD_STARTDT) != -1) ||
        					(contents.indexOf(Constant.THRESHOLD_STOPDT) != -1) || 
        					(contents.indexOf(Constant.THRESHOLD_GROUPID) != -1);
        boolean clearThreshold = contents.indexOf(Constant.CLEAR_THRESHOLD) != -1;
        
        try {
            
            if (tou) {
                
                int id = getTouFileId(contents);
                
                UserFile uf = mw().getUserFileFactory().find(id);
                if (uf != null) {
                	getLogger().severe("Sending new tariff program to concentrator.");
                    String xml = new String(uf.loadFileInByteArray());
                    if(xml.startsWith("<P2LPCTariff>")){
                    	port(concentrator).setMeterTariffSettings(xml);
                    	success = true;
                    } else {
                    	severe(toErrorMsg(msg) + "UserFile is NOT a tariff file.");
                    }
                    
                } else {
                    severe(toErrorMsg(msg) + "User file not found (id=" + id + ")");
                }
                
            }
            
            if (applyThreshold ){
            	String groupID = getMessageValue(contents, Constant.THRESHOLD_GROUPID);
            	if (groupID.equalsIgnoreCase(""))
            		throw new BusinessException("No groupID was entered.");
            	
            	UnsignedInt uiDuration = new UnsignedInt();
            	UnsignedInt uiGrId = new UnsignedInt();
            	String startDate = getMessageValue(contents, Constant.THRESHOLD_STARTDT);
            	String stopDate = getMessageValue(contents, Constant.THRESHOLD_STOPDT);
            	Calendar stopCal;
            	Calendar startCal = (startDate.equalsIgnoreCase(""))?Calendar.getInstance():getCalendarFromString(startDate);
            	if (stopDate.equalsIgnoreCase("")){
            		stopCal = Calendar.getInstance();
            		stopCal.setTime(startCal.getTime());
            		stopCal.add(Calendar.YEAR, 1);
            	}else{
            		stopCal = getCalendarFromString(stopDate);
            	}
            	try{
                	uiDuration.setValue((Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis()))/1000);
                	uiGrId.setValue((long)Integer.parseInt(groupID));
            	}
            	catch(NumberFormatException e){
            		throw new BusinessException("Invalid parameters for the threshold message.");
            	}
            	startDate = Constant.getInstance().getDateFormatFixed().format(startCal.getTime());
            	getLogger().severe("Setting the threshold value for metergroup " + uiGrId + ".");
            	port(concentrator).setCodeRed(startDate, uiDuration, uiGrId);
            	success = true;
            }
            
            if (clearThreshold){
        		Calendar startCal = Calendar.getInstance();
        		UnsignedInt uiDuration = new UnsignedInt(0);
        		UnsignedInt uiGrId = new UnsignedInt();
            	try{
            		getLogger().severe("Clearing the threshold value, max. consumption will be the contractual level again.");
                	uiGrId.setValue((long)Integer.parseInt(getMessageValue(contents, Constant.CLEAR_THRESHOLD)));
            	}
            	catch(NumberFormatException e){
            		throw new BusinessException("Invalid groupID for the stop threshold message.");
            	}
            	
            	String startDate = Constant.getInstance().getDateFormatFixed().format(startCal.getTime());
            	port(concentrator).setCodeRed(startDate, uiDuration, uiGrId);
            	
            	success = true;
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
    
	protected int getTouFileId(String contents) throws BusinessException {
		int startIndex = 2 + Constant.TOU_SCHEDULE.length();  // <TOU>
		int endIndex = contents.indexOf("</" + Constant.TOU_SCHEDULE );
		String value = contents.substring(startIndex, endIndex);
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			throw new BusinessException("Invalid userfile id: " + value);
		}
	}

	/** Short notation for MeteringWarehouse.getCurrent() */
    public MeteringWarehouse mw() {
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
    
//    private RtuType getRtuType(int energyType) {
//    	
//        if (rtuType[energyType] == null) {
//            String type = getProperty(Constant.RTU_TYPE);
//            if (type != null){
//	            type = type.split(":")[energyType];
//	            rtuType[energyType] = mw().getRtuTypeFactory().find(type);
//            }
//            else return null;
//        }
//        
//        return rtuType[energyType];
//    }
    
    private RtuType getRtuType(Rtu concentrator){
    	String type = concentrator.getProperties().getProperty(Constant.RTU_TYPE);
    	if(type != null){
    		return mw().getRtuTypeFactory().find(type);
    	}
    	else 
    		return null;
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
    
    public ProtocolChannelMap getChannelMap(Rtu meter) throws InvalidPropertyException {
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
        
        msgSpec = addBasicMsg("Set new tariff program", Constant.TOU_SCHEDULE, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addThresholdMsg("Apply threshold", Constant.APPLY_THRESHOLD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addClearThresholdMsg("Clear threshold", Constant.CLEAR_THRESHOLD, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
        
    }
    
    private MessageSpec addThresholdMsg(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(Constant.THRESHOLD_GROUPID);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
//        tagSpec = new MessageTagSpec(Constant.THRESHOLD_POWERLIMIT);
//        tagSpec.add(new MessageValueSpec());
//        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(Constant.THRESHOLD_STARTDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(Constant.THRESHOLD_STOPDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    private MessageSpec addClearThresholdMsg(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(Constant.CLEAR_THRESHOLD);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }
    
    public String writeMessage(Message msg) {
        return msg.write(this);
    }
    
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
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

	protected boolean isTESTING() {
		return TESTING;
	}

	protected void setTESTING(boolean testing) {
		TESTING = testing;
	}
    
}
