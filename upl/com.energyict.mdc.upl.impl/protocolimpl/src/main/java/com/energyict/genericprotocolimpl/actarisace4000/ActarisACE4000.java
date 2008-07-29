/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.LinkException;
import com.energyict.genericprotocolimpl.actarisace4000.objects.*;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;

/**
 * @author gna
 *
 */
public class ActarisACE4000 implements GenericProtocol{
	
	private InputStream 			inputStream;
	private OutputStream 			outputStream;
	private Dialer 					dialer;
	private DPacket 				packet;
	private PacketBuffer 			buffer;
	private UDPListener				udpListener;
	private Rtu						meter;
	private Logger 					logger;
	private CommunicationScheduler 	scheduler;
	private CommunicationProfile 	communicationProfile;
	private Link 					link;	
	private Properties				properties;
	private CreateXMLString			createXMLString;
	private ObjectFactory			objectFactory;
	
	private String					serialNumber;
	private String					phoneNumber;
	private int						tracker;
	
	public ActarisACE4000(Dialer dialer) throws SocketException, UnknownHostException{
//		this.dialer = dialer;
//		this.inputStream = dialer.getInputStream();
//		this.outputStream = dialer.getOutputStream();
		this.buffer = new PacketBuffer();
		this.udpListener = new UDPListener(this);	// start the UDPListener
	}
	
	/**
	 * This function determines the whole protocol 
	 */
	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.logger = logger;
		this.link = link;
		this.communicationProfile = scheduler.getCommunicationProfile();
		this.scheduler = scheduler;
		
		init();
		
		getObjectFactory().requestFirmwareVersion();

	}
	
	private void validateProperties() throws MissingPropertyException, InvalidPropertyException{
		
		// check the required keys - if missing, throw exception
        Iterator iterator= getRequiredKeys().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            if (properties.getProperty(key) == null)
                throw new MissingPropertyException (key + " key missing");
        }
        
        // check the optional keys - if missing, use default values
        serialNumber = meter.getSerialNumber();
        phoneNumber = meter.getPhoneNumber();
        
	}

	private void init() throws IOException {
		meter = scheduler.getRtu();
		packet = new DPacket(this);
		validateProperties();
		
		setObjectFactory(new ObjectFactory(this));
		
		tracker = 0;
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
			aace = new ActarisACE4000(dialer);
			
//			aace.sendWakeUpSMS();
//			new UDPListener();
//			aace.setSimAddressBook();
			
//			String message = "<MPull><MD><M>0505514283927180</M><T>0000</T></MD></MPull>";
			aace.packet = new DPacket(aace);
			aace.tracker = 0;
//			aace.packet.sendMessage(message);
			
//			dialer.disConnect();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(dialer != null){
					if(dialer.getStreamConnection().isOpen())
						dialer.disConnect();
				}
				
				if(aace != null){
					if(!aace.getUDPListener().getDatagramSocket().isClosed())
						aace.getUDPListener().getDatagramSocket().close();
				}
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

	public PacketBuffer getBuffer() {
		return buffer;
	}

	public UDPListener getUDPListener() {
		return udpListener;
	}

	public String getDeviceSerialnumber() {
		return serialNumber;
	}

	public int getTracker() {
		if(tracker == 4096)
			tracker = 0;
		return tracker++;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	protected void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public DPacket getPacket() {
		return packet;
	}

}
