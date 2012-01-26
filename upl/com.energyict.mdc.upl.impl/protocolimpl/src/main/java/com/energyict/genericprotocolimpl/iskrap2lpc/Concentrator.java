package com.energyict.genericprotocolimpl.iskrap2lpc;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.WebServiceLocator;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import org.apache.axis.types.UnsignedInt;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 
 * @author gna
 * BeginChanges:
 * GNA |27032009| Added forceClock to the concentrator, only this schedule is handle, all the meters are ignored
 */
public class Concentrator implements Messaging, GenericProtocol {
    
    private boolean DEBUG = false;
    boolean TESTING = false;
    private int TESTLOGGING = 0; 
    private int delayAfterFail;
    private int readingsFileType;
    private int retry;
    private String tempSerialnumber = "99999999";
	private String lpDaily;
    private String lpMonthly;
    private String lpElectricity;
    private String lpMbus;
    private long timeDifference;

    private Logger 					logger;
    private Properties 				properties;
    private Connection				connection;
    protected CommunicationProfile 	communicationProfile;	
    private CommunicationScheduler 	communicationScheduler;
    private Rtu						concentrator;
    
    private static final boolean ADVANCED = true;
    

    public void execute( CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        
    	if(TESTLOGGING >= 1) {
			logger.log(Level.INFO, "TESTLOGGING - 1/ Started the execute method.");
		}
    	
        this.logger = logger;
        this.communicationScheduler = scheduler;
        this.communicationProfile = communicationScheduler.getCommunicationProfile();
        this.connection = new RealTimeConnection(this, retry, delayAfterFail);
        
        int meterCount = -1;
        
        concentrator = scheduler.getRtu();
        if(TESTLOGGING >= 1) {
			getLogger().log(Level.INFO, "TESTLOGGING - 2/ Got the rtu from database");
		}
        String serial = concentrator.getSerialNumber();
        if(TESTLOGGING >= 1) {
			getLogger().log(Level.INFO, "TESTLOGGING - 3/ Got serialNumber form rtu from database");
		}
        StringBuffer progressLog = new StringBuffer(); progressLog.append("");	// just for safety
        PPPDialer dialer = null;

        
        try {
            
            if (useDialUp(concentrator)) {	// something for ftp?
                dialer = new PPPDialer(serial, logger);
                String user = getUser(concentrator);
                if( user!=null ) {
					dialer.setUserName(user);
				}
                String pwd = getPassword(concentrator);
                if( pwd!=null ) {
					dialer.setPassword(pwd);
				}
                dialer.connect();
            }
            if(TESTLOGGING >= 1) {
				getLogger().log(Level.INFO, "TESTLOGGING - 4/ Will start the request for the serialNumber over GPRS");
			}
            String conSerial = checkConcentratorSerial(concentrator);
            String meterList = null;
            List meters = null;
            
            if(serial.equalsIgnoreCase(conSerial)){
            	
            	if(!communicationProfile.getForceClock()){
            		
            		// get the prototype 
            		RtuType type = getRtuType(concentrator);
            		
            		/** use the meters in the dataBase */
            		if(type == null){ 	
            			meters = concentrator.getDownstreamRtus();
            			meters = collectSerialsFromRtuList(meters);
            		}
            		/** use the auto discovery - which means create all the meters if they don't exist */ 
            		else{
            			/** Only when you want to read a profile or meter registers */
            			if(communicationProfile.getReadMeterReadings()||communicationProfile.getReadDemandValues()){
            				meterList = getConnection().getMetersList();
            				meters = collectSerials(meterList);
            			}
            			else{	/** But if you only want to write the clock or send messages, use the downStreamRtu's */
            				meters = concentrator.getDownstreamRtus();		// returns a list with RTU's
            				meters = collectSerialsFromRtuList(meters);		// returns a list with RTU serialnumbers
            			}
            		}
            		
            		meterCount = meters.size();
            		
            		getLogger().log(Level.INFO, meterCount + " meter(s) will be handled");
            		
            		Iterator im = meters.iterator();
            		while (im.hasNext()) {
            			String meterSerial = (String) im.next();
            			if(!meterSerial.equalsIgnoreCase(tempSerialnumber)){
            				handleMeter(concentrator, meterSerial);
            				progressLog.append(meterSerial + " ");
            				getLogger().log(Level.INFO, "" + --meterCount + " meter(s) to go.");
            			} else {
            				getLogger().log(Level.INFO, "Temporary serialnumber 99999999 is ignored.");
            				--meterCount;
            			}
            		}
            	}
                
                handleConcentrator(concentrator);
            }
            
            else{
            	String serialError = "ConcentratorID EIServer(" + serial + ") didn't match concentratorID(" + conSerial + ").";
            	getLogger().log(Level.CONFIG, serialError);
            	throw new IOException(serialError);
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

        } catch (SQLException thrown) {
        	
        	/** Close the connection after an SQL exception, connection will startup again if requested */
        	Environment.getDefault().closeConnection();
        	
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
            
            throw new BusinessException( msg, thrown );
            
        } catch (ParseException thrown) {
        	
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
            
            throw new BusinessException( msg, thrown );
		} catch (BusinessException thrown) {
            
            /* Single concentrator failed, log and on to next concentrator */
            String msg = toConcetratorErrorMsg(serial, progressLog);
            severe(thrown, msg);
            thrown.printStackTrace();
        
            throw new BusinessException( msg, thrown );
            
        } finally {
            /** clean up, must simply ALWAYS happen */
            if (useDialUp(concentrator) && dialer != null) {
				dialer.disconnect();
			}
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

	private String checkConcentratorSerial(Rtu concentrator) throws ServiceException, IOException, BusinessException{
		String conID = null;
		getLogger().log(Level.INFO, "Checking concentrator serialnumber.");
		
        try {
			if (TESTING) {
				FileReader inFile = new FileReader(Utils.class.getResource(Constant.conSerialFile).getFile());
				conID = readWithStringBuffer(inFile);
				return conID;
			} else {
				conID = getConnection().getConcentratorStatus();
				if(TESTLOGGING >= 1) {
					getLogger().log(Level.INFO, "TESTLOGGING - SerialNumber = " + conID);
				}
				return conID.substring(conID.indexOf('"') + 1, conID.indexOf('"',conID.indexOf('"') + 1));
			}
			
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new ServiceException("Failed while reading the concentrator serialnumber." + e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new FileNotFoundException("Failed while reading the concentrator serialnumber." + e.getMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RemoteException("Failed while reading the concentrator serialnumber" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed while reading the concentrator serialnumber" + e.getMessage());
		}
	}
	

	public void addProperties(Properties properties) {
        this.properties = properties;
        try {
			TESTLOGGING = Integer.parseInt(properties.getProperty("TestLogging", "0"));
			this.retry = Integer.parseInt(properties.getProperty("Retries", "3"));
			this.delayAfterFail = Integer.parseInt(properties.getProperty(Constant.DELAY_AFTER_FAIL, "5000"));
			this.readingsFileType = Integer.parseInt(properties.getProperty(Constant.READING_FILE, "0"));
			this.lpDaily = properties.getProperty(Constant.LP_DAILY, "");
			this.lpElectricity = properties.getProperty(Constant.LP_ELECTRICITY, "");
			this.lpMbus = properties.getProperty(Constant.LP_MBUS, "");
			this.lpMonthly = properties.getProperty(Constant.LP_MONTHLY, "");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException("Could not convert one or several custom properties to an integer.");
		}
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
        result.add( Constant.TESTLOGGING );
        result.add( Constant.DELAY_AFTER_FAIL );
        result.add( Constant.READING_FILE );
        result.add( Constant.LP_DAILY);
        result.add( Constant.LP_ELECTRICITY);
        result.add( Constant.LP_MBUS);
        result.add( Constant.LP_MONTHLY);
        return result;
    }

    public List getRequiredKeys() {
        ArrayList result = new ArrayList();
        result.add( Constant.CHANNEL_MAP );
        return result;
    }
    
    protected Logger getLogger( ){
        return logger;
    }
    
    /** Import a single meter */
    private void handleMeter( Rtu concentrator, String serial ) {
        
        try {
            
            MeterReadTransaction mrt = new MeterReadTransaction(this, concentrator, serial, communicationProfile);
            mrt.doExecute();
            
        } catch (BusinessException thrown) {
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */
            String msg = Constant.METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
            
        } catch (SQLException thrown) {
        	
        	/** Close the connection after an SQL exception, connection will startup again if requested */
        	Environment.getDefault().closeConnection();
        	
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */
            String msg = Constant.METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
        }
    }
    
    protected String readWithStringBuffer(Reader fileReader) throws IOException{
    	try {
    		BufferedReader br = new BufferedReader(fileReader);
    		String line;
    		StringBuffer result = new StringBuffer();
			while ((line = br.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to readin the file." + "("+e.getMessage()+")");
		}
    }
    
    /** Generic data import procedure. All imported data is in one xml format. */
    protected void importData(String data, DefaultHandler dataHandler) 
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
    
	protected String getMessageValue(String msgStr, String str) {
		try {
			return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
					+ 1, msgStr.indexOf("</" + str));
		} catch (Exception e) {
			return "";
		}
	}
    
	public Connection getConnection(){
		return this.connection;
	}
	
	protected void setConnection(Connection connection){
		this.connection = connection;
	}
    /** 
     * Import a single concentrator. 
     * @throws ServiceException 
     * @throws ParseException 
     * @throws IOException 
     * */
    protected void handleConcentrator(Rtu concentrator) throws BusinessException, SQLException, ServiceException, ParseException, IOException {
    	
    	getLogger().log(Level.INFO, "Handling the concentrator with serialnumber: " + concentrator.getSerialNumber());
        
//        if( communicationProfile.getWriteClock() ) {
        setTime();
//        }
        
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
        	getLogger().log(Level.INFO, "Getting events from concentrator with serialnumber: " + concentrator.getSerialNumber());
        	Date lastLogBookConcentrator = concentrator.getLastLogbook();
        	if(lastLogBookConcentrator == null){
        		lastLogBookConcentrator = getClearMidnightDate(concentrator);
        	}
        	String from = Constant.getInstance().format(lastLogBookConcentrator);
        	String to = Constant.getInstance().format(new Date());
        	String conEvents = null;
            if ( TESTING ){
            	FileReader inFile = new FileReader(Utils.class.getResource(Constant.conEventFile).getFile());
            	conEvents = readWithStringBuffer(inFile);
            } else{
            	conEvents = getConnection().getConcentratorEvents(from, to);
            }

        	XmlHandler dataHandler = new XmlHandler( getLogger(), null );
        	ProfileData pd = new ProfileData();
        	importData(conEvents, dataHandler);
        	pd = dataHandler.addEvents();
        	concentrator.store(pd);
        	
        }
        
        getLogger().log(Level.INFO, "Concentrator " + concentrator.getSerialNumber() + " has completely finished.");
    }

    private Date getClearMidnightDate(Rtu rtu){
   		Calendar tempCalendar = Calendar.getInstance(rtu.getDeviceTimeZone());
		tempCalendar.set(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.set(Calendar.MINUTE, 0 );
		tempCalendar.set(Calendar.SECOND, 0 );
		tempCalendar.set(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
    }
    
    private void setTime() 
        throws ServiceException, ParseException, IOException, BusinessException {
        
        /* Don't worry about clock sets over interval boundaries, Iskra
         * will (probably) handle this. 
         */
        
    	String systime = getConnection().getConcentratorSystemTime();
        
        systime = 
            Pattern.compile(":\\d{2}$").matcher(systime).replaceFirst("00");
        Date cTime = Constant.getInstance().getDateFormat().parse(systime);
        
        Date now = new Date();
        
        this.timeDifference = now.getTime() - cTime.getTime() ;
        long sAbsDiff = Math.abs( this.timeDifference ) / 1000;
        
        getLogger().info( 
                "Difference between metertime and systemtime is " + this.timeDifference + " ms");
        if( communicationProfile.getWriteClock() ) {
        	long max = communicationProfile.getMaximumClockDifference();
        	long min = communicationProfile.getMinimumClockDifference();
        	
        	if( ( sAbsDiff < max ) && ( sAbsDiff > min ) ) { 
        		
        		getLogger().severe("Adjust concentrator time to system time. Concentrator time: " + cTime + ", System time: " + now);
        		
        		String d = Constant.getInstance().getDateFormatFixed().format(now);
        		
        		getConnection().setConcentratorSystemTime(d);
//            getConnection().timeSync();	Don't do this, the concentrator will handle the timeSet with all his meters
        		
        	}
        } else if(communicationProfile.getForceClock()){
    		getLogger().severe("Forcing concentrator time to system time. Concentrator time: " + cTime + ", System time: " + now);
    		String d = Constant.getInstance().getDateFormatFixed().format(now);
    		getConnection().setConcentratorSystemTime(d);
        }
        
    }
    
    protected void handleConcentratorRtuMessage(
        Rtu concentrator, String serial, RtuMessage msg)
            throws BusinessException, SQLException {
    	String contents = msg.getContents();
        boolean success = false;
        boolean tou = contents.toLowerCase().indexOf(RtuMessageConstant.TOU_SCHEDULE.toLowerCase()) != -1;
        boolean applyThreshold = (contents.toLowerCase().indexOf(RtuMessageConstant.APPLY_THRESHOLD.toLowerCase()) != -1) ||
        					(contents.toLowerCase().indexOf(RtuMessageConstant.THRESHOLD_STARTDT.toLowerCase()) != -1) ||
        					(contents.toLowerCase().indexOf(RtuMessageConstant.THRESHOLD_STOPDT.toLowerCase()) != -1) || 
        					(contents.toLowerCase().indexOf(RtuMessageConstant.THRESHOLD_GROUPID.toLowerCase()) != -1);
        boolean clearThreshold = contents.toLowerCase().indexOf(RtuMessageConstant.CLEAR_THRESHOLD.toLowerCase()) != -1;
        boolean changePLCFrequency = (contents.toLowerCase().indexOf(RtuMessageConstant.FREQUENCY_MARK.toLowerCase()) != -1) ||
        						(contents.toLowerCase().indexOf(RtuMessageConstant.FREQUENCY_SPACE.toLowerCase()) != -1);
        boolean upgradeFirmware = contents.toLowerCase().indexOf(RtuMessageConstant.FIRMWARE.toLowerCase()) != -1;
        
        try {
            
            if (tou) {
                
                int id = getTouFileId(contents);
                
                UserFile uf = mw().getUserFileFactory().find(id);
                if (uf != null) {
                	getLogger().severe("Sending new tariff program to concentrator.");
                    String xml = new String(uf.loadFileInByteArray());
                    if(xml.startsWith("<P2LPCTariff>")){
                    	getConnection().setMeterTariffSettings(xml);
                    	success = true;
                    } else {
                    	severe(toErrorMsg(msg) + "UserFile is NOT a tariff file.");
                    }
                    
                } else {
                    severe(toErrorMsg(msg) + "User file not found (id=" + id + ")");
                }
                
            }
            
            else if (changePLCFrequency){
            	String mark = getMessageValue(contents, RtuMessageConstant.FREQUENCY_MARK);
            	String space = getMessageValue(contents, RtuMessageConstant.FREQUENCY_SPACE);
            	
            	if(!(ParseUtils.isInteger(mark) && ParseUtils.isInteger(space))){
            		msg.setFailed();
            		getLogger().log(Level.INFO, "Not a valid entry for the current Concentrator message (" + contents + ").");
            	} else {
            		int fileSize = getConnection().getFileSize(Constant.p2lpcCorrectFileName);
            		byte[] fileChunk = getConnection().downloadFileChunk(Constant.p2lpcCorrectFileName, 0, fileSize);
            		System.out.println(new String(fileChunk));
            		
            		try {
            			Document doc = toDom(new String(fileChunk));
						NodeList nl = doc.getElementsByTagName(Constant.DLC);
						if(nl.getLength() == 1){
							Element e = (Element)nl.item(0);
							
							
							/**
							 * There are different parameters to be set in the concentrator and the meter to change both there plc frequencies.
							 * ______________________________________________________________________________________
							 * |Mark(concentrator)	|Space(concentrator)	|Freq channel - 0.0.128.0.2.255 (meter)	|
							 * |	66				|	75					|	4									|
							 * |	72				|	64					|	3									|
							 * --------------------------------------------------------------------------------------
							 * 
							 * There are other possibilities but these result in a good quality signal.
							 */
				            e.setAttribute(Constant.mark, mark);	// default value
				            e.setAttribute(Constant.space, space);	// default value

				            
				            String xmlString = FileUtils.convertFromDocToString(doc);
				            
				            /**
				             * First we upload the P2LPC.tmp file to the concentrator. Due to the low connection it is possible that the concentrator
				             * will pick up the file before it is completely uploaded, so that is why the temp name is used for.
				             * Afterwards we just change the name to P2LPC.xml and the concentrator can do his job with it.
				             */
				            getConnection().uploadFileChunk(Constant.p2lpcTempFileName, 0, true, xmlString.getBytes());
				            getConnection().copyFile(Constant.p2lpcTempFileName, Constant.p2lpcCorrectFileName, true);
				            
				            byte[] restartBytes = FileUtils.convertStringToZippedBytes(Constant.restart, Constant.restartFileName);
				            getConnection().uploadFileChunk(Constant.upgradeZipName, 0, true, restartBytes);
				            
				            getLogger().log(Level.INFO, "Concentrator will RESTART!");
				            
				            success = true;
				            
						} else {
							throw new IOException("P2LPC.xml does not contain a DLC tag.");
						}
						
						
					} catch (XmlException e) {
						e.printStackTrace();
						throw new IOException("Failed to parse the P2LPC.xml file.");
					}
            		
            	}
            } else if (upgradeFirmware){
            	
            	String userFileID = getMessageValue(contents, RtuMessageConstant.FIRMWARE);
            	String groupID = getMessageValue(contents, RtuMessageConstant.FIRMWARE_METERS);
            	
            	if(!ParseUtils.isInteger(userFileID) || !ParseUtils.isInteger(groupID)){
            		msg.setFailed();
            		getLogger().log(Level.INFO, "Not a valid entry for the current Concentrator message (" + contents + ").");
            	} else {
            		
            		Group gr = mw().getGroupFactory().find(Integer.parseInt(groupID));
            		if(gr != null){
            			if(gr.getObjectType() == mw().getRtuFactory().getId()){
            				UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
            				if(!(uf instanceof UserFile )){
            					msg.setFailed();
            					getLogger().log(Level.INFO, "Not a valid entry for the userfileID " + userFileID);
            				} else {
	            				byte[] b = uf.loadFileInByteArray();
	            				if(b.length == 0){
	            					msg.setFailed();
	            					getLogger().log(Level.INFO, "The binary file is empty.");
	            				} else {
	            					List<Rtu> meters = gr.getMembers();
	            					if(meters.size() > 0){
	            						String[] meterSerials = new String[gr.getMembers().size()];
	            						Iterator<Rtu> it = meters.iterator();
	            						for(int i = 0; i < meterSerials.length; i++){
	            							meterSerials[i] = it.next().getSerialNumber();
	            						}
	            						byte[] upload = new byte[Constant.MAX_UPLOAD];
	            						int length = Constant.MAX_UPLOAD;
	            						boolean last = false;
	            						for(int i = 0; i <= b.length/Constant.MAX_UPLOAD; i++){
	            							if(i == b.length/Constant.MAX_UPLOAD){
	            								last = true;
	            								length = b.length - i*Constant.MAX_UPLOAD;
	            								upload = new byte[length];
	            							}
	            							System.arraycopy(b, i*Constant.MAX_UPLOAD, upload, 0, length);
	            							getConnection().uploadFileChunk(Constant.firmwareBinFile, i*Constant.MAX_UPLOAD, last, upload);
	            							
	            						}
	            						getConnection().upgradeMeters(Constant.firmwareBinFile, meterSerials);
	            						
	            						success = true;
	            					} else {
	            						msg.setFailed();
	            						getLogger().log(Level.INFO, "There are no meters in the group " + gr.getFullName());
	            					}
	            				}
            				}
            			} else {
            				msg.setFailed();
                			getLogger().log(Level.INFO, "Objects in group '"+ gr.getFullName() +"'are not of the type Rtu.");
            			}
            		} else {
            			msg.setFailed();
            			getLogger().log(Level.INFO, "No valid group with id " + groupID);
            		}
            	}
            	
            } else if (applyThreshold ){
            	String groupID = getMessageValue(contents, RtuMessageConstant.THRESHOLD_GROUPID);
            	if (groupID.equalsIgnoreCase("")) {
					throw new BusinessException("No groupID was entered.");
				}
            	
            	UnsignedInt uiDuration = new UnsignedInt();
            	UnsignedInt uiGrId = new UnsignedInt();
            	String startDate = getMessageValue(contents, RtuMessageConstant.THRESHOLD_STARTDT);
            	String stopDate = getMessageValue(contents, RtuMessageConstant.THRESHOLD_STOPDT);
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
            	getConnection().setCodeRed(startDate, uiDuration, uiGrId);
            	success = true;
            }
            
            else if (clearThreshold){
        		Calendar startCal = Calendar.getInstance();
        		UnsignedInt uiDuration = new UnsignedInt(0);
        		UnsignedInt uiGrId = new UnsignedInt();
            	try{
            		getLogger().severe("Clearing the threshold value, max. consumption will be the contractual level again.");
                	uiGrId.setValue((long)Integer.parseInt(getMessageValue(contents, RtuMessageConstant.CLEAR_THRESHOLD)));
            	}
            	catch(NumberFormatException e){
            		throw new BusinessException("Invalid groupID for the stop threshold message.");
            	}
            	
            	String startDate = Constant.getInstance().getDateFormatFixed().format(startCal.getTime());
            	getConnection().setCodeRed(startDate, uiDuration, uiGrId);
            	
            	success = true;
            }
            else {
            	success = false;
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
            if (success){
                msg.confirm();
                getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
            }
            else{
            	msg.setFailed();
            	getLogger().log(Level.INFO, "Current message " + contents + " has failed.");
            }
        }
        
    }
    
	protected int getTouFileId(String contents) throws BusinessException {
		int startIndex = 2 + RtuMessageConstant.TOU_SCHEDULE.length();  // <TOU>
		int endIndex = contents.indexOf("</" + RtuMessageConstant.TOU_SCHEDULE );
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
            if( "DLCMeters".equals( e.getAttribute("GroupID") ) ) {
				result.add( e.getAttribute( "DeviceID" ) );
			}
        }
        
        return result;
        
    }
    
    /** Instantiate webservice stub */
    protected P2LPCSoapPort_PortType port(Rtu concentrator) throws ServiceException {
        
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
    
    
    /** Find RtuType for creating new meters. 
     * @throws IOException */
    protected RtuType getRtuType(Rtu concentrator) throws IOException{
    	String type = concentrator.getProperties().getProperty(Constant.RTU_TYPE);
    	if(type != null){
    		RtuType rtuType = mw().getRtuTypeFactory().find(type);
            if (rtuType == null) {
				throw new IOException("Iskra Mx37x, No rtutype defined with name '" + type + "'");
			}
            if (rtuType.getPrototypeRtu() == null) {
				throw new IOException("Iskra Mx37x, rtutype '" + type + "' has no prototype rtu");
			}
            return rtuType;
    	}
    	else{
    		getLogger().warning("No automatic meter creation: no property RtuType defined.");
    		return null;
    	}
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
                .append(Constant.CONCENTRATOR_ERROR)
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
    
    /** log to severe */
    protected void severe(Throwable thrown, String eMsg) {
        String msg = eMsg + " (" + thrown.toString() + ")";
        getLogger().log(Level.SEVERE, msg, thrown);
    }
    
    /** log to severe */
    private void severe(String eMsg) {
        getLogger().severe(eMsg);
    }
    
    /** DOM wrapping */
    protected Document toDom(String data) throws XmlException  {
        
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
    protected class XmlException extends Exception {
        
        private static final long serialVersionUID = 1L;

        public XmlException(String message, Throwable cause) {
            super(message, cause);
        }

        public XmlException(Throwable cause) {
            super(cause);
        }
    }

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
        
        msgSpec = addBasicMsg("Set new tariff program", RtuMessageConstant.TOU_SCHEDULE, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addThresholdMsg("Apply LoadLimiting", RtuMessageConstant.APPLY_THRESHOLD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addClearThresholdMsg(RtuMessageKeyIdConstants.LOADLIMITCLEAR, RtuMessageConstant.CLEAR_THRESHOLD, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addPLCFreqChange("Change PLC Frequency", RtuMessageConstant.CHANGE_PLC_FREQUENCY, !ADVANCED);
        cat.addMessageSpec(msgSpec);
        
        msgSpec = addFirmWare("Upgrade the meters firmware", RtuMessageConstant.FIRMWARE_UPGRADE, !ADVANCED);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
        
    }
    
    private MessageSpec addFirmWare(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
    	MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.FIRMWARE);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	tagSpec = new MessageTagSpec(RtuMessageConstant.FIRMWARE_METERS);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	return msgSpec;
    }
    
    private MessageSpec addPLCFreqChange(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
    	MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.FREQUENCY_MARK);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	tagSpec = new MessageTagSpec(RtuMessageConstant.FREQUENCY_SPACE);
    	tagSpec.add(new MessageValueSpec());
    	msgSpec.add(tagSpec);
    	return msgSpec;
    }
    
    private MessageSpec addThresholdMsg(String keyId, String tagName, boolean advanced){
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
    
    private MessageSpec addClearThresholdMsg(String keyId, String tagName, boolean advanced){
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.CLEAR_THRESHOLD);
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
            if (att.getValue() == null || att.getValue().length() == 0) {
				continue;
			}
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
            if (elt.isTag()) {
				buf.append(writeTag((MessageTag) elt));
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
					return "";
				}
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

	/**
	 * @param logger the logger to set
	 */
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

	public int getTESTLOGGING() {
		return TESTLOGGING;
	}
	
	public Rtu getConcentrator(){
		return this.concentrator;
	}
    
	public int getReadingsFileType(){
		return this.readingsFileType;
	}
	
	public CommunicationScheduler getCommunicationScheduler(){
		return this.communicationScheduler;
	}
	
	protected void setCommunicationProfile(CommunicationProfile communicationProfile){
		this.communicationProfile = communicationProfile;
	}
	
    protected String getLpDaily() {
		return lpDaily;
	}

	protected String getLpMonthly() {
		return lpMonthly;
	}

	protected String getLpElectricity() {
		return lpElectricity;
	}

	protected String getLpMbus() {
		return lpMbus;
	}

	public long getTimeDifference() {
		return this.timeDifference;
	}
}
