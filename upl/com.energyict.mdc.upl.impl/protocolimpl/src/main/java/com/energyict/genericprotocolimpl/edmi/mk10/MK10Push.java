/**
 * MK10Push.java
 * 
 * Created on 8-jan-2009, 12:47:25 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.genericprotocolimpl.edmi.mk10.parsers.MK10InputStreamParser;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.edmi.mk10.MK10;

/**
 * @author jme
 *
 */
public class MK10Push implements GenericProtocol {

	private static final int DEBUG 				= 0;
	
	private Logger logger 						= null;
	private long connectTime					= 0;
	private long disconnectTime					= 0;
	private CommunicationScheduler scheduler 	= null;
	private Link link							= null;
	private long timeOut						= 10 * 1000;
	private Rtu meter							= null;
	private int serial							= 0;
	private TimeZone timezone					= null;
	private MK10 mk10Protocol					= new MK10();
	private Properties properties				= null;
	
	
	private InputStream inputStream				= null;
	private OutputStream outputStream			= null;
	MK10PushInputStream mk10PushInputStream 	= null;
	MK10PushOutputStream mk10PushOutputStream	= null;

	
	/*
	 * Constructors
	 */

	public MK10Push() {}

	/*
	 * Private getters, setters and methods
	 */
	
	private Logger getLogger() {
		return logger;
	}

	private long getConnectTime() {
		return connectTime;
	}
	
	private long getDisconnectTime() {
		return disconnectTime;
	}
	
	private void setDisconnectTime(long disconnectTime) {
		this.disconnectTime = disconnectTime;
	}
	
	private Link getLink() {
		return link;
	}
	
	private InputStream getInputStream() {
		return inputStream;
	}
	
	private OutputStream getOutputStream() {
		return outputStream;
	}
	
	private MK10PushInputStream getMk10PushInputStream() {
		return mk10PushInputStream;
	}
	
	private MK10PushOutputStream getMk10PushOutputStream() {
		return mk10PushOutputStream;
	}
	
	private CommunicationScheduler getScheduler() {
		return scheduler;
	}
	
	private MK10 getMk10Protocol() {
		return mk10Protocol;
	}
	
	private Rtu getMeter() {
		return meter;
	}
	
	private int getSerial() {
		return serial;
	}
	
	private TimeZone getTimezone() {
		return timezone;
	}
	
	public MeteringWarehouse mw() {
		MeteringWarehouse result = MeteringWarehouse.getCurrent();
		return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
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

	private Properties getProperties() {
		return properties;
	}
	
	private Rtu findMatchingMeter(String serial) {
		Rtu rtu = null;
		List meterList = mw().getRtuFactory().findByDialHomeId(serial);
		if (meterList.size() == 1) {
			rtu = (Rtu) meterList.get(0);
		} else return null;

		sendDebug("** " + rtu.getCommunicationSchedulers() + " **", 0);
		
		return rtu;
	}
	
	private void initMk10Protocol(Rtu rtu) throws IOException {

		this.properties = rtu.getProperties();
		
		getProperties().put(MeterProtocol.ADDRESS, getMeter().getDeviceId());
		getProperties().put(MeterProtocol.PASSWORD, getMeter().getPassword());
		getProperties().put(MeterProtocol.NODEID, getMeter().getNodeAddress());
		getProperties().put(MeterProtocol.SERIALNUMBER, getMeter().getSerialNumber());
		
//      	  setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1"));
//            setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","10000").trim()));
//            setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty("Retries","5").trim()));
//            roundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
//            securityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());

//            echoCancelling=Integer.parseInt(properties.getProperty("EchoCancelling","0").trim());
//            protocolCompatible=Integer.parseInt(properties.getProperty("ProtocolCompatible","1").trim());
//            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim());
//            channelMap = properties.getProperty("ChannelMap");
//            if (channelMap != null) 
//               protocolChannelMap = new ProtocolChannelMap(channelMap);
//            profileInterval=Integer.parseInt(properties.getProperty("ProfileInterval","900").trim());
//            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader","0").trim());
//            scaler = Integer.parseInt(properties.getProperty("Scaler","0").trim());
//            setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","300").trim()));
//            halfDuplex=Integer.parseInt(properties.getProperty("HalfDuplex","0").trim());
//            setDtrBehaviour(Integer.parseInt(properties.getProperty("DTRBehaviour","2").trim()));
//            
//            adjustChannelMultiplier = new BigDecimal(properties.getProperty("AdjustChannelMultiplier","1").trim());
//            adjustRegisterMultiplier = new BigDecimal(properties.getProperty("AdjustRegisterMultiplier","1").trim());
//            
//            sendDebug("doValidateProperties()");
//            validateLoadSurveyNumber(properties.getProperty("LoadSurveyNumber"));
//            setLoadSurveyNumber(Integer.parseInt(properties.getProperty("LoadSurveyNumber").trim())-1);
//            setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
//
//            
//            doValidateProperties(properties);
//        }
		
		properties.list(System.out);
		getMk10Protocol().setPushProtocol(true);
		getMk10Protocol().setProperties(getProperties());
		getMk10Protocol().init(getMk10PushInputStream(), getMk10PushOutputStream(), getTimezone(), getLogger());

	}

	/*
	 * Public methods
	 */
	
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.scheduler = scheduler;
		this.link = link;
		this.logger = logger;

		this.inputStream = link.getInputStream();
		this.outputStream = link.getOutputStream();
		this.mk10PushInputStream = new MK10PushInputStream(getInputStream());
		this.mk10PushOutputStream = new MK10PushOutputStream(getOutputStream());
		
		this.connectTime = System.currentTimeMillis();

		
		sendDebug("** A new UDP session is started **", 0);
		sendDebug("** ConnectionTime: [" + getConnectTime() + "] **", 0);

		
		try {

			if(scheduler == null){	// we got a message from the COMMSERVER UDP Listener

				int kar = 0;
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				while(inputStream.available() > 0){
					kar = inputStream.read();
					buffer.write(kar & 0x000000FF);
				}

				MK10InputStreamParser inputParser = new MK10InputStreamParser();
				inputParser.parse(buffer.toByteArray());

				if (inputParser.isPushPacket()) {
					this.serial = inputParser.getSerial();
					sendDebug("** Received message from device with serial: " + inputParser.getSerial() + " **", 0);
					this.meter = findMatchingMeter(String.valueOf(serial));
					if (getMeter() == null) throw new NotFoundException("RTU with callerID [" + getSerial() + "] not found.");
					this.timezone = getMeter().getTimeZone();
					initMk10Protocol(getMeter());
					getMk10Protocol().connect();
				} else {
					throw new ProtocolException("Received invalid data: " + inputParser.toString());
				}

			}

		} catch (ProtocolException e) {
			// TODO enter exceptionhandling here
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 0);
		} catch (NotFoundException e) {
			// TODO enter exceptionhandling here
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 0);
		}
		
		setDisconnectTime(System.currentTimeMillis());
		sendDebug("** DisconnectTime: [" + getDisconnectTime() + "] **", 0);
		sendDebug("** Connection ended after " + (getDisconnectTime() - getConnectTime()) + " ms **", 0);
		sendDebug("** Closing the UDP session **", 0);
		
	}

	/*
	 * Public getters and setters
	 */

	public String getVersion() {
        return "$Revision: 1.1 $";
	}

	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		list.addAll(getMk10Protocol().getOptionalKeys());
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList();
		return list;
	}

	public void addProperties(Properties properties) {
		//TODO aanpassen
	}
	
	
	
	/*
	 * Private debugging methods
	 */
	
	public void sendDebug(String message, int debuglvl) {
		String returnMessage = "";
		if (DEBUG == 0) {
			returnMessage += " [" + new Date().toString();
			returnMessage += "] > " + message;
		} else {
			returnMessage += " ##### DEBUG [";
			returnMessage += new Date().getTime();
			returnMessage += "] ######## > ";
			returnMessage += message;
		}
		returnMessage += "\n";
		if ((debuglvl <= DEBUG) && (getLogger() != null)) {
			getLogger().log(Level.INFO, returnMessage);
			System.out.print(returnMessage);
		}
	}

	public static void main(String[] args) {
		try {
			FileInputStream inFile		= new FileInputStream("C:\\debug\\raw_data.raw");
			FileOutputStream outFile	= new FileOutputStream("C:\\debug\\packet_out_1.hex");
		
			MK10PushInputStream in 		= new MK10PushInputStream(inFile);
			MK10PushOutputStream out 	= new MK10PushOutputStream(outFile);

			while (in.available() > 0) {
				System.out.println("** in.read() = " + in.read() + " **");
			}
			
//			System.out.println("** inFile.available() = " + inFile.available() + " **");
//			System.out.println("** in.available()     = " + in.available() + " **");
			
            byte[] tempBuffer = new byte[] {
            		(byte)0x8F, (byte)0x50, (byte)0xFF, (byte)0xE0, 
            		(byte)0x0C, (byte)0x4C, (byte)0x61, (byte)0xD3, 
            		(byte)0x18, (byte)0x80, (byte)0x0F, (byte)0xE7, 
            		(byte)0x18, (byte)0x7C, (byte)0x71, (byte)0x18, 
            		(byte)0x01, (byte)0x1E, (byte)0x00, (byte)0x00, 
            		(byte)0x40, (byte)0x00 
            };
            
			int crc = CRCGenerator.ccittCRC(tempBuffer, tempBuffer.length);

			System.out.println("** CRC = " + crc + " [" + ProtocolUtils.buildStringHex(crc, 4) + "]" + " **");

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	
}
