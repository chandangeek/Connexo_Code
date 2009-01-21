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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.genericprotocolimpl.edmi.mk10.executer.MK10ProtocolExecuter;
import com.energyict.genericprotocolimpl.edmi.mk10.parsers.MK10InputStreamParser;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;

/**
 * @author jme
 *
 */
public class MK10Push implements GenericProtocol {

	private static final int DEBUG 				= 0;
	
	private static final int BYTE_MASK			= 0x000000FF;
	
	private Logger logger 						= null;
	private long connectTime					= 0;
	private long disconnectTime					= 0;
	private Link link							= null;
	private MK10ProtocolExecuter MK10Executor	= new MK10ProtocolExecuter(this);
	private StringBuilder errorString			= new StringBuilder();
	
	private InputStream inputStream				= null;
	private OutputStream outputStream			= null;
	MK10PushInputStream mk10PushInputStream 	= null;
	MK10PushOutputStream mk10PushOutputStream	= null;
	
	/*
	 * Constructors
	 */

	public MK10Push() {
		errorString.append("");
	}

	/*
	 * Private getters, setters and methods
	 */
		
	private long getConnectTime() {
		return connectTime;
	}
	
	private long getDisconnectTime() {
		return disconnectTime;
	}
	
	private void setDisconnectTime(long disconnectTime) {
		this.disconnectTime = disconnectTime;
	}
		
	private InputStream getInputStream() {
		return inputStream;
	}
	
	private OutputStream getOutputStream() {
		return outputStream;
	}
			
	private Rtu getMeter() {
		return getMK10Executor().getMeter();
	}
	
	public MK10ProtocolExecuter getMK10Executor() {
		return MK10Executor;
	}
	
	public MeteringWarehouse mw() {
		MeteringWarehouse result = MeteringWarehouse.getCurrent();
		return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
	}

	public String getErrorString() {
		if (errorString == null) return "";
		String returnValue = errorString.toString();
		if (returnValue == null) return "";
		return returnValue;
	}
	
	private void addLogging(int completionCode, String completionMessage, List journal, boolean success, Exception exception) throws SQLException, BusinessException {
		sendDebug("** addLogging **", 2);
		
		// check if there was an protocol or timeout error
		if (!success && (completionCode == AmrJournalEntry.CC_OK)) {
			if (exception != null) {
				if (exception.getMessage().contains("timeout")) {
					completionCode = AmrJournalEntry.CC_IOERROR;
				} else {
					completionCode = AmrJournalEntry.CC_PROTOCOLERROR;
				}
			}
		}
		
		if(getMeter() != null){
			Iterator it = getMeter().getCommunicationSchedulers().iterator();
			while(it.hasNext()){
				CommunicationScheduler cs = (CommunicationScheduler)it.next();
				if( !cs.getActive() ){
					cs.startCommunication();
					AMRJournalManager amrjm = new AMRJournalManager(getMeter(), cs);
					amrjm.journal(new AmrJournalEntry(completionCode));
					amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - getConnectTime())/1000));

					for (int i = 0; i < journal.size(); i++) {
						AmrJournalEntry amrJournalEntry = (AmrJournalEntry) journal.get(i);
						amrjm.journal(amrJournalEntry);
					}
					
					if (getErrorString().length() > 0) amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, getErrorString()));
					if (completionMessage.length() > 0) amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, completionMessage));
					if (exception != null) amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, "Exception: " + exception.toString()));
					
					if (completionCode == AmrJournalEntry.CC_OK) {
						sendDebug("** updateLastCommunication **", 3);
						amrjm.updateLastCommunication();
					} else {
						sendDebug("** updateRetrials **", 3);
						amrjm.updateRetrials();
					}
					break;
				}
			}
		}else{
			getLogger().log(Level.INFO, "Failed to enter an AMR journal entry.");
		}
	}
	
	private Rtu findMatchingMeter(String serial) {
		Rtu rtu = null;
		List meterList = mw().getRtuFactory().findByDialHomeId(serial);
		if (meterList.size() == 1) {
			rtu = (Rtu) meterList.get(0);
		} else return null;

		return rtu;
	}
	
	private void initMk10Protocol(Rtu rtu) throws IOException {

//		this.properties = rtu.getProperties();
//		
//		getProperties().put(MeterProtocol.ADDRESS, getMeter().getDeviceId());
//		getProperties().put(MeterProtocol.PASSWORD, getMeter().getPassword());
//		getProperties().put(MeterProtocol.NODEID, getMeter().getNodeAddress());
//		getProperties().put(MeterProtocol.SERIALNUMBER, getMeter().getSerialNumber());
//		
//		getMk10Protocol().setPushProtocol(true);
//		getMk10Protocol().setProperties(getProperties());
//		getMk10Protocol().init(getMk10PushInputStream(), getMk10PushOutputStream(), getTimezone(), getLogger());

	}

	private Rtu waitForPushMeter() throws IOException, BusinessException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		MK10InputStreamParser inputParser = new MK10InputStreamParser();

		while(inputStream.available() > 0) {
			buffer.write(inputStream.read() & BYTE_MASK);
		}

		inputParser.parse(buffer.toByteArray(), true);
		if(!inputParser.isPushPacket()) throw new ProtocolException("Received invalid data: " + inputParser.toString());
		sendDebug("** Received message from device with serial: " + inputParser.getSerial() + " **", 0);
		
		Rtu pushDevice = findMatchingMeter(inputParser.getSerialAsString());
		
		if (pushDevice == null) throw new BusinessException("RTU with callerID [" + inputParser.getSerial() + "] not found.");

		return pushDevice; 
	}
	
	private void storeMeterData(MeterReadingData meterReadingData, ProfileData meterProfileData) throws SQLException, BusinessException {
		
		if (DEBUG >= 2) System.out.println("storeMeterData()");
		if (DEBUG >= 2) System.out.println(" meterReadingData = " + meterReadingData);
		if (DEBUG >= 2) System.out.println(" meterProfileData = " + meterProfileData);
		
		if (meterReadingData != null) getMeter().store(meterReadingData);
		if (meterProfileData != null) getMeter().store(meterProfileData);
	}
	
	/*
	 * Public methods
	 */
	
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		boolean success = true;
		Exception exception = null;

		this.link = link;
		this.logger = logger;
		this.inputStream = getLink().getInputStream();
		this.outputStream = getLink().getOutputStream();
		this.mk10PushInputStream = new MK10PushInputStream(getInputStream());
		this.mk10PushOutputStream = new MK10PushOutputStream(getOutputStream());
		this.connectTime = System.currentTimeMillis();
				
		try {

			// Check if we got a message from the COMMSERVER UDP Listener
			if(scheduler != null) throw new ProtocolException("scheduler != null. Execute must be triggered by UDP listener.");	
			
			sendDebug("** A new UDP session is started **", 0);
			sendDebug("** ConnectionTime: [" + getConnectTime() + "] **", 0);

			Rtu pushDevice = waitForPushMeter();
			getMK10Executor().setMeter(pushDevice);
			getMK10Executor().doMeterProtocol();
			storeMeterData(getMK10Executor().getMeterReadingData(), getMK10Executor().getMeterProfileData());
			
		} catch (ProtocolException e) {
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (IOException e) {
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (SQLException e) {
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} catch (Exception e) {
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		} finally {
			
			setDisconnectTime(System.currentTimeMillis());
			
			sendDebug("** DisconnectTime: [" + getDisconnectTime() + "] **", 0);
			sendDebug("** Connection ended after " + (getDisconnectTime() - getConnectTime()) + " ms **", 0);
			sendDebug("** Closing the UDP session **", 0);

			try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();};
						
			try {
				if (DEBUG >= 1)	System.out.println("addLogging()");
				addLogging(
						getMK10Executor().getCompletionCode(), 
						getMK10Executor().getCompletionErrorString(), 
						getMK10Executor().getJournal(),
						success,
						exception
				);
				if (DEBUG >= 1)	System.out.println("addLogging() ended");
			} catch (SQLException e) {
				sendDebug("** SQLException **", 1);
				e.printStackTrace();
				// Close the connection after an SQL exception, connection will startup again if requested
				Environment.getDefault().closeConnection();
				throw e;
			}			
		}
				
	}

	/*
	 * Public getters and setters
	 */

	public Logger getLogger() {
		return logger;
	}

	public Link getLink() {
		return link;
	}

	public String getVersion() {
        return "$Revision: 1.1 $";
	}

	public void addProperties(Properties properties) {
		sendDebug("** addProperties **", 2);
		getMK10Executor().addProperties(properties);
	}
	
	/*
	 * Private debugging methods
	 */
	
	public void sendDebug(String message, int debuglvl) {
		String returnMessage = "";
		if (DEBUG == 0) {
			returnMessage += " [MK10Push] > " + message;
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
			FileInputStream inFile		= new FileInputStream("C:\\debug\\packet.raw");
			FileOutputStream outFile	= new FileOutputStream("C:\\debug\\packet_out_1.hex");
		
			MK10PushInputStream in 		= new MK10PushInputStream(inFile);
			MK10PushOutputStream out 	= new MK10PushOutputStream(outFile);

			ByteArrayOutputStream fileBuffer = new ByteArrayOutputStream();

			int crc = 0;
			
			while (inFile.available() > 0) {
				fileBuffer.write(inFile.read());
			}
			
			
			
			
//            byte[] tempBuffer = new byte[] {
//            		(byte)0x8F, (byte)0x50, (byte)0xFF, (byte)0xE0, 
//            		(byte)0x0C, (byte)0x4C, (byte)0x61, (byte)0xD3, 
//            		(byte)0x18, (byte)0x80, (byte)0x0F, (byte)0xE7, 
//            		(byte)0x18, (byte)0x7C, (byte)0x71, (byte)0x18, 
//            		(byte)0x01, (byte)0x1E, (byte)0x00, (byte)0x00, 
//            		(byte)0x40, (byte)0x00 
//            };
            
            byte [] tempBuffer = fileBuffer.toByteArray();
            
			crc = CRCGenerator.ccittCRC(tempBuffer, tempBuffer.length - 3);
			System.out.println("** CRC = " + crc + " [" + ProtocolUtils.buildStringHex(crc, 4) + "]" + " **");

			crc = CRCGenerator.ccittCRC(tempBuffer, tempBuffer.length - 2);
			System.out.println("** CRC = " + crc + " [" + ProtocolUtils.buildStringHex(crc, 4) + "]" + " **");
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

	public List getOptionalKeys() {
		ArrayList list = new ArrayList(0);
		list.addAll(getMK10Executor().getOptionalKeys());
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList(0);
		list.addAll(getMK10Executor().getRequiredKeys());
		return list;
	}
	
}
