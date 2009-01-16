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
import com.energyict.cbo.NotFoundException;
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
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;
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

	public MK10Push() {}

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

	private void addFailureLogging(Exception exception, StringBuilder eString) throws SQLException, BusinessException {
		sendDebug("** addFailureLogging **", 0);
		if (1 == 1) return; // FIXME: remove debugging code
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
					sendDebug("** updateRetrials **", 0);
					break;
				}
			}
		}else{
			getLogger().log(Level.INFO, "Failed to enter an AMR journal entry.");
		}
	}
	
	private void addSuccessLogging() throws SQLException, BusinessException {
		sendDebug("** addSuccessLogging **", 0);
		if (1 == 1) return; // FIXME: remove debugging code
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
					sendDebug("** updateLastCommunication **", 0);
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

		//sendDebug("** " + rtu.getCommunicationSchedulers() + " **", 0);
		
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

		inputParser.parse(buffer.toByteArray());
		if(!inputParser.isPushPacket()) throw new ProtocolException("Received invalid data: " + inputParser.toString());
		sendDebug("** Received message from device with serial: " + inputParser.getSerial() + " **", 0);
		
		Rtu pushDevice = findMatchingMeter(inputParser.getSerialAsString());
		
		if (pushDevice == null) throw new BusinessException("RTU with callerID [" + inputParser.getSerial() + "] not found.");

		return pushDevice; 
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
			
			throw new ProtocolException("Generated dummy exception: 123 test");
			
		} catch (ProtocolException e) {
			// TODO enter exceptionhandling here
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 0);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			//throw new BusinessException(e.getMessage());
		} catch (IOException e) {
			// TODO enter exceptionhandling here
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 0);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			//throw new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			// TODO enter exceptionhandling here
			sendDebug("** EXCEPTION: " + e.getMessage() + " **", 0);
			errorString.append(e.getMessage());
			success = false;
			exception = e;
			e.printStackTrace();
			//throw new BusinessException(e.getMessage());
		} finally {
			
			setDisconnectTime(System.currentTimeMillis());
			
			sendDebug("** DisconnectTime: [" + getDisconnectTime() + "] **", 0);
			sendDebug("** Connection ended after " + (getDisconnectTime() - getConnectTime()) + " ms **", 0);
			sendDebug("** Closing the UDP session **", 0);
			try {Thread.sleep(2000);} catch (InterruptedException e) {};

			try {
				if(success)	addSuccessLogging();
					else addFailureLogging(exception, errorString);
			} catch (SQLException e) {
				sendDebug("** SQLException **", 0);
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
		sendDebug("** addProperties **", 0);
		getMK10Executor().addProperties(properties);
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
