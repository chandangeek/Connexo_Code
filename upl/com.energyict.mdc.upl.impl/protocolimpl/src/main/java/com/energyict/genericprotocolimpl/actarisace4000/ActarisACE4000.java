/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.LinkException;
import com.energyict.genericprotocolimpl.actarisace4000.objects.ObjectFactory;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.CommunicationProtocolShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.mdw.shadow.RtuTypeShadow;

/**
 * @author gna
 *
 */
public class ActarisACE4000 implements GenericProtocol{
	
	private int DEBUG = 0;
	
	private InputStream 			inputStream;
	private OutputStream 			outputStream;
	private Dialer 					dialer;
	private Rtu						meter;
	private HashMap<String, Rtu>	mbusMeters;
	private Logger 					logger;
	private CommunicationScheduler 	scheduler;
	private CommunicationProfile 	communicationProfile;
	private Link 					link;	
	private Properties				properties;
	private ObjectFactory			objectFactory;
	
	private List<String>			mbSerialNumber;
	private String					pushedSerialNumber;
	private String					masterSerialNumber;
	private String					necessarySerialNumber;
	private String					phoneNumber;
	private StringBuilder			errorString;
	private int						tracker;
	
	private int oneTimer = 0;
	private long connectTime = 0;
	
	// TODO change the timeOut
	// TODO change the timeOut
	// TODO change the timeOut
	// TODO change the timeOut
	// TODO change the timeOut
	// TODO change the timeOut
	// TODO change the timeOut
	private int 					timeOut = 30000;	// default timeout of 1min
	
	public ActarisACE4000(){
		
	}
	
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.logger = logger;
		this.link = link;
		this.scheduler = scheduler;
		
		boolean success = true;
		Exception exception = null;
		
		try {
			//lazy init
			meter = null;
			
			if(scheduler == null){	// we got a message from the COMMSERVER UDP Listener
				
				logger.log(Level.INFO, "** A new UDP session is started **");
				setConnectTime(System.currentTimeMillis());
				
				this.inputStream = this.link.getInputStream();
				this.outputStream = this.link.getOutputStream();
				
				setObjectFactory(new ObjectFactory(this));
				
				// keep reading until you get no data for one minute
				long interMessageTimeout = System.currentTimeMillis() + timeOut;
				
				while(true){	// this loop controls the responses that we get from our own requests
					
					while(true){	// this loop controls the UDP packets pushed from the meter
						int kar = 0;
						String msg = "";
						
						// TODO infinit loop takes a lot of CPU usage,  search for better way
						// the thread.sleep is a way to do it ...
						if(inputStream.available() > 0){
							// DOM parser did not handle the inputStream correctly so we read the complete string and parse it ourselves
							while(inputStream.available() > 0){
								kar = inputStream.read();
								msg = msg.concat(Character.toString((char)kar));
							}
							interMessageTimeout = System.currentTimeMillis() + timeOut;
						}
						else{
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
								throw new InterruptedException(e+"(Interrupted while waiting for next message.)");
							}
						}
						
						if(msg != ""){
							getObjectFactory().parseXML(msg);
						}
						if (((long) (System.currentTimeMillis() - interMessageTimeout)) > 0) {
							break; // we can leave the loop cause we did not receive a message within the passed minute
						}
					}	// end of UPD listen loop - check if we want to do something
					
//					if(oneTimer == 0){
//						oneTimer++;
//						Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("GMT"));
//						cal.add(Calendar.DAY_OF_MONTH, -1);
//						interMessageTimeout = System.currentTimeMillis() + timeOut;	// keep the session alive
//						getObjectFactory().sendMBLoadProfileRequest(cal.getTime());
//						getObjectFactory().setAutoPushConfig(1, 5, 60, false);
//						getObjectFactory().sendBDConfig(1, 1, 15);
//						getObjectFactory().sendForceTime();
//						getObjectFactory().sendFullMeterConfigRequest();
//						getObjectFactory().sendLoadProfileRequest(cal.getTime());
//						getObjectFactory().sendBDRequest(cal.getTime());
//						getObjectFactory().sendTimeConfig(4800, 120, 3);
//						getObjectFactory().sendForceTime();		// TODO this way we can check the time of the meter, but other ways would be better.
//						getObjectFactory().sendBDRequest();
//					}
					if (((long) (System.currentTimeMillis() - interMessageTimeout)) > 0) {
						break; // we can leave the loop cause we did not receive a message within the passed minute
					}
				}
				
				/**
				 * If there is valid data in the pro file, store it in the database
				 */
				if(getObjectFactory().getLoadProfile().getProfileData().getIntervalDatas().size() > 0){
					getObjectFactory().getLoadProfile().getProfileData().sort();
					getMeter().store(getObjectFactory().getLoadProfile().getProfileData());
				}
				if(getObjectFactory().getMBLoadProfile().getProfileData().getIntervalDatas().size() > 0){
					getObjectFactory().getMBLoadProfile().getProfileData().sort();
					// TODO make MBLoadProfile a dynamic object so multiple MBus meters can have one ...
					for( int i = 0; i < getMBSerialNumber().size(); i++){
						getMbusMetersMap().get(getMBSerialNumber().get(i)).store(getObjectFactory().getMBLoadProfile().getProfileData());
					}
				}
				if(getObjectFactory().getBillingData().getProfileData().getIntervalDatas().size() > 0){
					getObjectFactory().getBillingData().getProfileData().sort();
					getMeter().store(getObjectFactory().getBillingData().getProfileData(), false);
				}
				
				getLogger().log(Level.INFO, "** Closing the UDP session **");
//				success = true;
			}
			
		} catch (InterruptedException e1) {
			if(errorString == null){
				errorString = new StringBuilder();
			}
			errorString.append(e1.getMessage());
			success = false;
			exception = e1;
			e1.printStackTrace();
			throw new BusinessException(e1.getMessage());
		} catch (ParserConfigurationException e) {
			if(errorString == null){
				errorString = new StringBuilder();
			}
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (SAXException e) {
			if(errorString == null){
				errorString = new StringBuilder();
			}
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (SQLException e){
			if(errorString == null){
				errorString = new StringBuilder();
			}
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
        	// Close the connection after an SQL exception, connection will startup again if requested
        	Environment.getDefault().closeConnection();
			throw new BusinessException(e.getMessage());
		} catch (IOException e){
			if(errorString == null){
				errorString = new StringBuilder();
			}
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			throw new BusinessException(e.getMessage());
		} finally {
			try {
				if(success){
					addSuccessLogging();
				}
				else{
					addFailureLogging(exception, errorString);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
	        	// Close the connection after an SQL exception, connection will startup again if requested
	        	Environment.getDefault().closeConnection();
	        	throw e;
			}
		}
	}
	
	private void setConnectTime(long currentTimeMillis) {
		this.connectTime = currentTimeMillis;
	}
	
	public long getConnectTime(){
		return connectTime;
	}

	private void addFailureLogging(Exception exception, StringBuilder eString) throws SQLException, BusinessException {
		if(getMeter() != null){
			Iterator it = getMeter().getCommunicationSchedulers().iterator();
			while(it.hasNext()){
				CommunicationScheduler cs = (CommunicationScheduler)it.next();
				if( !cs.getActive() ){
					cs.startCommunication();
					AMRJournalManager amrjm = new AMRJournalManager(getMeter(), cs);
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, eString + ": " + exception.toString()));
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - getConnectTime())/1000));
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
					amrjm.updateRetrials();
					break;
				}
			}
		}else{
			getLogger().log(Level.INFO, "Failed to enter an AMR journal entry.");
		}
	}
	
	private void addSuccessLogging() throws SQLException, BusinessException {
		if(getMeter() != null){
			Iterator it = getMeter().getCommunicationSchedulers().iterator();
			while(it.hasNext()){
				CommunicationScheduler cs = (CommunicationScheduler)it.next();
				if( !cs.getActive() ){
					cs.startCommunication();
					AMRJournalManager amrjm = new AMRJournalManager(getMeter(), cs);
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - getConnectTime())/1000));
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CC_OK));
					amrjm.updateLastCommunication();
					break;
				}
			}
		}else{
			getLogger().log(Level.INFO, "Failed to enter an AMR journal entry.");
		}
	}

	public void addProperties(Properties properties) {
		this.properties = properties;
	}

	public String getVersion() {
		return "$Date$";
	}

	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList();
		return list;
	}
	
	public static void main(String[] args){
		
		Dialer dialer = null;
		ActarisACE4000 aace = null;
		
		try {
//			dialer = DialerFactory.getDirectDialer().newDialer();
//			dialer.init("COM1");
//			dialer.connect("", 60000);
//			aace = new ActarisACE4000(dialer);
			
//			aace.sendWakeUpSMS();
//			new UDPListener();
//			aace.setSimAddressBook();
			
//			String message = "<MPull><MD><M>0505514283927180</M><T>0000</T></MD></MPull>";
//			aace.packet = new DPacket(aace);
			aace.tracker = 0;
//			aace.packet.sendMessage(message);
			
//			dialer.disConnect();
			
			
		} finally {
			try {
				if(dialer != null){
					if(dialer.getStreamConnection().isOpen()) {
						dialer.disConnect();
					}
				}
				
//				if(aace != null){
//					if(!aace.getUDPListener().getDatagramSocket().isClosed()) {
//						aace.getUDPListener().getDatagramSocket().close();
//					}
//				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LinkException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Only for TESTING the sendSms with a GSM/GPRS modem
	 */
	public void sendWakeUpSMS(){
		String resetModem = "+++\r\n";
		String initModem = "ATZ\r\n";
		String ath = "ATH\r\n";
		String e0 = "ATE0\r\n";
		String setTextMode = "AT+CMGF=1\r\n";						// set to TEXT mode
		String charEncoding = "AT+CSCS=GSM\r\n";					// set to 7bit-coding scheme
		String receiver = "AT+CMGS=\"0031657596241\"\r\n";	// KPN SIM
//		String receiver = "AT+CMGS=\"0032473222607\"\r\n";	// My SIM
//		String receiver = "AT+CMGS=\"0032473824379\"\r\n";	// TestSim19
//		String message = "AAAA<Mpush><MD><M>E2G8NRB1D2110D07</M><T>0033</T></MD></Mpush>";
		String message = "<MPush><MD><M>0505514283927180</M><T>0000</T></MD></MPush>";
//		String message = "AAAA<Mpull><MD><qCR/></MD></Mpull>";
		byte[] ctrlZ = {(byte)0x1A}; 
		sendATCommand(resetModem.getBytes());
		sendATCommand(initModem.getBytes());
		sendATCommand(ath.getBytes());
		sendATCommand(e0.getBytes());
		sendATCommand(setTextMode.getBytes());
		sendATCommand(charEncoding.getBytes());
		sendATCommand(receiver.getBytes());
		sendATCommand(message.getBytes());
		sendATCommand(ctrlZ);
	}
	
	/**
	 * Only for setting the SIM cards AddressBook items
	 */
	public void setSimAddressBook(){
		String msg;
		String ath = "ATH\r\n";
		String e0 = "ATE0\r\n";
		String addressEntry = "AT+CPBW=";
		String setTextMode = "AT+CMGF=1\r\n";
		String entry1 = "1,13,209,\"194.151.228.18\""+"\r\n";		// IP address of DNS Server KPN
//		String entry1 = "1,13,129,\"195.238.2.21\""+"\r\n";			// IP address of DNS Server Proximus
		String entry2 = "2,23,209,\"KPN\""+"\r\n";					// GPRS specific username
		String entry3 = "3,33,209,\"gprs\""+"\r\n";					// GPRS specific password
		String entry4 = "4,43,209,\"internet\""+"\r\n";				// Fully qualified APN name
		String entry5 = "5,53,209,\"195.207.140.162\""+"\r\n";		// IP address of the AMR system UDP socket
		String entry6 = "6,63,209,\"4096\""+"\r\n";					// Port number of the AMR System UDP Socket
		String entry7 = "7,73,209,\"+31653131313\""+"\r\n";			// Telephone number of Message center KPN
//		String entry7 = "7,73,129,\"+32475161616\""+"\r\n";			// Telephone number of Message center Proximus
		String entry8 = "8,83,209,\"0046709032259\""+"\r\n";		// Telephone number of SMS gateway connected to the AMR System
		
		sendATCommand(ath.getBytes());
		sendATCommand(e0.getBytes());
		sendATCommand(setTextMode.getBytes());
		sendATCommand((msg=addressEntry.concat(entry1)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry2)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry3)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry4)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry5)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry6)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry7)).getBytes());
		sendATCommand((msg=addressEntry.concat(entry8)).getBytes());
		
		System.out.println("Done writing to SIM address book.");
	}
	
	/**
	 * Only for sending direct ATCommands to the dialer that's configured in the MAIN
	 * @param atCommand
	 */
	public void sendATCommand(byte[] atCommand){
		long stop = System.currentTimeMillis()+500;
		int inewKar;
		String strToParse = "";
		try {
			outputStream.write(atCommand);
			while(stop >= System.currentTimeMillis()){
				if (dialer.getStreamConnection().getInputStream().available() != 0) {
					inewKar = dialer.getStreamConnection().getInputStream().read();
					strToParse+=(char)inewKar;  
				}
			}
			System.out.println(strToParse);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getTracker() {
		// TODO use the scheduler ID to track messages
		if(tracker == 4096) {
			tracker = 0;
		}
		return tracker++;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	protected void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}
	
	public OutputStream getOutputStream(){
		return this.outputStream;
	}

	/**
	 * Important method to determine the meters topology
	 * If we get a serialNumber from the UDPListener, fill in all the serialNumber we can find in the database and
	 * fill in the necessary Rtu's as well
	 * @param pushedSerialNumber
	 * @throws BusinessException 
	 */
	public void setSerialnumbers(String pushedSerialNumber) throws BusinessException{
		this.pushedSerialNumber = pushedSerialNumber;
		
		try {
			if(meter == null){	// First message cause nothing is filled in yet, Master must be filled in
				
				if(isMasterMeter(getPushedSerialNumber())){
					findAllSlaveMeters();
				}
				else if(isSlaveMeter(getPushedSerialNumber())){
					findMasterMeter();
					findAllSlaveMeters();
				} else {
					throw new ApplicationException("Meter "+ pushedSerialNumber +" is not found in database, no autodiscovery YET implemented.");
				}
				
				getLogger().log(Level.INFO, "Received data from meter with serialnumber " + getMasterSerialNumber());
				
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
	}
	
	public boolean isMasterMeter(String serialNumber) throws ConfigurationException{
		
		// find by CallHomeID, unique in database
		List meterList = mw().getRtuFactory().findByDialHomeId("ACE4000"+serialNumber);
		
		if(meterList.size() == 1){	// we found him, take him down boys ....
			setMasterMeter((Rtu)meterList.get(0));
			setMasterSerialNumber(serialNumber);
			return true;
		}
        else if( meterList.size() > 1 ) {
            getLogger().severe( "Multiple meters where found with serial: " + serialNumber );
            throw new ConfigurationException("Multiple meters where found with serial: " + serialNumber);
        }
		return false;
	}
	
	public boolean isSlaveMeter(String serialNumber) throws ConfigurationException{
		
		// find by CallHomeID, unique in database
		List meterList = mw().getRtuFactory().findByDialHomeId("ACE4000MB"+serialNumber);
		
		if(meterList.size() == 1){	// we found him, take him down boys ....
			if(getMbusMetersMap() == null) {
				mbusMeters = new HashMap<String, Rtu>();
			}
			getMbusMetersMap().put(serialNumber, (Rtu)meterList.get(0));
			getMBSerialNumber().add(serialNumber);
			return true;
		}
        else if( meterList.size() > 1 ) {
            getLogger().severe( "Multiple meters where found with serial: " + serialNumber );
            throw new ConfigurationException("Multiple meters where found with serial: " + serialNumber);
        }
		return false;
	}
	
	public void setNecessarySerialNumber(String necessarySerialNumber){
		this.necessarySerialNumber = necessarySerialNumber;
	}
	
	/**
	 * Returns the serialNumber of the E-meter or the MBus-meters
	 * Needs to be set before the message is sent!
	 * @return a serialNumber
	 */
	public String getNecessarySerialnumber(){
		return necessarySerialNumber;
	}
	
	public String getPushedSerialNumber(){
		return pushedSerialNumber;
	}
	
	/** @return the masters serialNumber */
	public String getMasterSerialNumber(){
		return masterSerialNumber;
	}
	/** Sets the masters serialNumber 
	 * @param masterSerialNumber */
	public void setMasterSerialNumber(String masterSerialNumber){
		this.masterSerialNumber = masterSerialNumber;
	}
	
	/**
	 * Returns a list of all serialNumber of the slave devices
	 * @return MBus serialnumbers
	 */
	public List<String> getMBSerialNumber() {
		if(mbSerialNumber == null) {
			mbSerialNumber = new ArrayList();
		}
		return mbSerialNumber;
	}
	
	/**
	 * Contains a map of the serialNumbers with there Rtu's.
	 * No database overkill if we keep on asking the Rtu
	 * @return the mbusMeters hashMap
	 */
	public HashMap<String, Rtu> getMbusMetersMap(){
		return mbusMeters;
	}
	
	public int getMeterProfileInterval(){
		if( meter != null) {
			return meter.getIntervalInSeconds();
		} else {
			return -1;
		}
	}
	
	/** Short notation for MeteringWarehouse.getCurrent() */
	public MeteringWarehouse mw() {
		MeteringWarehouse result = MeteringWarehouse.getCurrent();
		return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Rtu getMeter() {
		return meter;
	}
	
	public CommunicationScheduler getCommScheduler(){
		return scheduler;
	}
	
	public void setMasterMeter(Rtu meter) {
		this.meter = meter;
		setCommunicationScheduler();
	}
	
	private void setCommunicationScheduler(){
		Iterator it = getMeter().getCommunicationSchedulers().iterator();
		while(it.hasNext()){
			CommunicationScheduler cs = (CommunicationScheduler)it.next();
			if( !cs.getActive() && cs.getDialerFactory().getDialerClassName().equalsIgnoreCase("")){
				this.scheduler = cs;
				break;
			}
		}
	}
	
	private Rtu findMeter(String serial){
		// find by CallHomeID, unique in database
		// TODO both meter types have same dialhome prefix(ACE4000), maybe make a difference between them
		List meterList = mw().getRtuFactory().findByDialHomeId("ACE4000"+serial);
		
		if(meterList.size() == 1){
			return (Rtu) meterList.get(0);
		}
        else if( meterList.size() > 1 ) {
            getLogger().severe( "Multiple meters where found with serial: " + serial );
            return null;
        }
        else{	// try to check for MBus meters
        	getLogger().severe("Meter serialnumber is not found in database.");
        }
        	
		return null;
	}
	
	private void findMasterMeter(){
		if(!getMbusMetersMap().isEmpty()){
			Rtu mbusSlave = getMbusMetersMap().get(getPushedSerialNumber());
			
			if(mbusSlave.getGateway() != null){
				setMasterMeter(mbusSlave.getGateway());
				setMasterSerialNumber(getMeter().getSerialNumber());
			} else {
				getLogger().severe("MBus slave meter has no gateway configured so no master meter was found!");
			}
		} else {
			getLogger().severe("MasterMeter can NOT be found because no slaves are detected.");
		}
	}
	
	private void findAllSlaveMeters(){
		if(meter != null){
			List<Rtu> slaves = meter.getDownstreamRtus();
			if(slaves.size() > 0){
				Iterator it = slaves.iterator();
				while(it.hasNext()){
					if(getMbusMetersMap() == null) {
						mbusMeters = new HashMap<String, Rtu>();
					}
					
					Rtu mbus = (Rtu)it.next();
					if(!getMbusMetersMap().containsKey(mbus.getSerialNumber())){
						getMbusMetersMap().put(mbus.getSerialNumber(), mbus);
						// add this one so we can loop through the list for all the serialnumbers
						getMBSerialNumber().add(mbus.getSerialNumber());
					}
				}
			}
			else{
				getLogger().log(Level.INFO, "No slave meters were found on meter " + getMasterSerialNumber());
			}
		} else {
			getLogger().severe("No slaves can be found because the MasterMeter is NULL.");
		}
	}
	
	/**
	 * TODO need to complete this method in order to work with the announcement
	 */
	public void findOrCreateMeter() {
		if(meter == null){
			// find by CallHomeID, unique in database
			List meterList = mw().getRtuFactory().findByDialHomeId("ACE4000"+getNecessarySerialnumber());
			
			if(meterList.size() == 1){
				meter = (Rtu) meterList.get(0);
			}
			else if(meterList.size() == 0){
				createMeter();
			}
		}
	}
	
	//**********************************************************************************************************************
	// TODO
	// Should not go this deep, using a prototype should be better, but where do I get this?
	// Momentarely leave it like this
	//**********************************************************************************************************************
	
	private void createMeter(){
		try {
			CommunicationProtocol commProt = getCommunicationProtocol(this.getClass().getName());
			RtuType rtuType = createRtuType(commProt, getNecessarySerialnumber(), 4);
			meter = createRtu(rtuType, getNecessarySerialnumber(), "ACE4000"+getNecessarySerialnumber(), 1800);
		} catch (BusinessException e) {
			e.printStackTrace();
			getLogger().log(Level.INFO, "Could not create meter with serialnumber " + getNecessarySerialnumber());
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().log(Level.INFO, "Could not create meter with serialnumber " + getNecessarySerialnumber());
		}
	}
	
	private CommunicationProtocol getCommunicationProtocol(String javaClassName) throws BusinessException, SQLException{
		CommunicationProtocol commProtMeter = null;
		List result = new ArrayList();
		// find out if the communication profile exists, if not, create it
		result = mw().getCommunicationProtocolFactory().findAll();
		for(int i = 0; i < result.size(); i++){
			if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(javaClassName)){
				commProtMeter = (CommunicationProtocol)result.get(i);
				break;
			}
		}
		if(commProtMeter == null){
			CommunicationProtocolShadow commProtShadow = new CommunicationProtocolShadow();
			commProtShadow.setJavaClassName(javaClassName);
			commProtShadow.setName(javaClassName);
			commProtMeter = mw().getCommunicationProtocolFactory().create(commProtShadow); 
		}
		return commProtMeter;
	}
	
	private RtuType createRtuType(CommunicationProtocol commProtocol, String name, int channelCount) throws SQLException, BusinessException{
		List result = new ArrayList();
		RtuType rtuTypeMeter = null;
		// find out if there is an rtuType defined with this testName, it not, create it
		result = mw().getRtuTypeFactory().findByName(name);
		if(result.size() == 0){
			RtuTypeShadow rtuTypeShadow = new RtuTypeShadow();
			rtuTypeShadow.setChannelCount(channelCount);
			rtuTypeShadow.setName(name);
//			rtuTypeShadow.setProtocolShadow(commProtocol.getShadow());
			rtuTypeShadow.setProtocolId(commProtocol.getId());
			rtuTypeMeter = mw().getRtuTypeFactory().create(rtuTypeShadow);
		} else {
			rtuTypeMeter = (RtuType)result.get(0);
		}
		return rtuTypeMeter;
	}
	
	private Rtu createRtu(RtuType rtuType, String serial, String callHomeID, int interval) throws SQLException, BusinessException{
		RtuShadow rtuShadow = rtuType.newRtuShadow();
		rtuShadow.setRtuTypeId(rtuType.getId());
		rtuShadow.setName(rtuType.getName());
		rtuShadow.setExternalName(rtuType.getName());
		rtuShadow.setIntervalInSeconds(interval);
		rtuShadow.setSerialNumber(serial);
		rtuShadow.setDialHomeId(callHomeID);
		Rtu rtu = mw().getRtuFactory().create(rtuShadow);
		return rtu;
	}

	public long getTimeDifference() {
		// TODO Auto-generated method stub
		return 0;
	}

	//**********************************************************************************************************************

}
