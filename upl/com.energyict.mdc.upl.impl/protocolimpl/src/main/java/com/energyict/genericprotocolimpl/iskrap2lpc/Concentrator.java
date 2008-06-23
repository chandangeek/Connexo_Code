package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
    boolean TESTING = false;

    private Logger 					logger;
    private Properties 				properties;
    protected CommunicationProfile 	communicationProfile;	
    
    /** RtuType is used for creating new Rtu's */
    private RtuType[] rtuType = {null, null};
    
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
            
            if (useDialUp(concentrator)) {	// something for ftp?
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
            	
            	RtuType type = getRtuType(concentrator);
            	
            	/* use the meters in the dataBase */
            	if(type == null){ 	
            		meters = concentrator.getDownstreamRtus();
            		meters = collectSerialsFromRtuList(meters);
            	}
            	/* use the auto discovery */ 
            	else{
            		if(communicationProfile.getReadMeterReadings()||communicationProfile.getReadDemandValues()){
            			meterList = port(concentrator).getMetersList();
            			meters = collectSerials(meterList);
            		}
            		else{
            			meters = concentrator.getDownstreamRtus();
            			meters = collectSerialsFromRtuList(meters);
            		}
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

	private String checkConcentratorSerial(Rtu concentrator) throws IOException, ServiceException {
		String conID = null;
		getLogger().log(Level.INFO, "Checking concentrator serialnumber.");
		
        try {
			if (TESTING) {
				FileReader inFile = new FileReader(Utils.class.getResource(Constant.conSerialFile).getFile());
				conID = readWithStringBuffer(inFile);
				return conID;
			} else {
				conID = port(concentrator).getConcentratorStatus();
				return conID.substring(conID.indexOf('"') + 1, conID.indexOf('"',conID.indexOf('"') + 1));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed while reading the concentrator serialnumber.");
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new ServiceException("Failed while reading the concentrator serialnumber.");
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
    
    protected Logger getLogger( ){
        return logger;
    }
    
    /** Import a single meter */
    private void handleMeter( Rtu concentrator, String serial ) {
        
        try {
            
            MeterReadTransaction mrt = new MeterReadTransaction(this, concentrator, serial, communicationProfile);
            
            Environment.getDefault().execute(mrt);
            
        } catch (BusinessException thrown) {
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */
            String msg = Constant.METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
            
        } catch (SQLException thrown) {
            /*
             * A single MeterReadTransaction failed: log and try next meter.
             */
            String msg = Constant.METER_ERROR + serial + ". ";
            getLogger().log(Level.SEVERE, msg + thrown.getMessage(), thrown);
            thrown.printStackTrace();
        }
    }
    
    protected String readWithStringBuffer(Reader fileReader) throws IOException {
    	BufferedReader br = new BufferedReader(fileReader);
    	String line;
    	StringBuffer result = new StringBuffer();
    	while ((line = br.readLine()) != null) {
    		result.append(line);
    	}
    	return result.toString();
    }
    
    /** Generic data import procedure. All imported data is in one xml format. */
    protected void importData(String data, XmlHandler dataHandler) 
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
        	String from = Constant.getInstance().format(concentrator.getLastLogbook());
        	String to = Constant.getInstance().format(new Date());
        	String conEvents;
            if ( TESTING ){
            	FileReader inFile = new FileReader(Utils.class.getResource(Constant.conEventFile).getFile());
            	conEvents = readWithStringBuffer(inFile);
            }
            else
            	 conEvents = port(concentrator).getConcentratorEvents(from, to);
        	XmlHandler dataHandler = new XmlHandler( getLogger(), null );
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
    
    
    /** Find RtuType for creating new meters. */
    protected RtuType getRtuType(Rtu concentrator){
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

	/**
	 * @param logger the logger to set
	 */
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}
    
}
