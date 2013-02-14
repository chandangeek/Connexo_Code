package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.protocolimpl.edf.messages.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ReadConcentratorTransaction implements Transaction {

	private Concentrator concentrator;
	private CommunicationProfile communicationProfile;
	private Device device;

	public ReadConcentratorTransaction(Concentrator protocol, Device concentrator)  throws BusinessException {
		this.concentrator = protocol;
		this.communicationProfile = protocol.getCommunicationProfile();
		this.device = concentrator;
	}

	public Object doExecute() throws BusinessException, SQLException {
		debug( "ReadMeter: " + device.getFullName() );

		if (communicationProfile.getSendRtuMessage())
			executeMessages();

		debug( "ReadMeter: " + device.getFullName() + " DONE \n\n" );
		return null;        
	}

	private void executeMessages() throws BusinessException, SQLException {
		List messages =  device.getOldPendingMessages();
		for (Iterator it = messages.iterator(); it.hasNext();){
			OldDeviceMessage message = (OldDeviceMessage) it.next();
			String content = message.getContents();
			MessageContent mc = MessageContentFactory.createMessageContent(content);
			addMessage(mc, message);
		}
	}

	private void addMessage( MessageContent mc, OldDeviceMessage rtuMessage) throws BusinessException, SQLException {

		CosemFactory rf = concentrator.getCosemFactory();

		if (mc instanceof MessageDiscoverMeters) {
			MessageDiscoverMeters message = (MessageDiscoverMeters) mc;
			switch(message.getScriptId()){
			case MessageDiscoverMeters.READMETERLIST :
				updateAllMeters();
				break;
			case MessageDiscoverMeters.RESETANDREDISCOVER : 
				resetAndDiscoverMeters();
				break;
			case MessageDiscoverMeters.RESET : 
				resetMeters();
				break;
			case MessageDiscoverMeters.DISCOVER :
				discoverMeters();
			default :
				// do nothing
			}
			rtuMessage.confirm();
			return;
		}
		if (mc instanceof MessagePerformanceTest){
			MessagePerformanceTest message = (MessagePerformanceTest) mc;
			initiatePerformanceTest();
			rtuMessage.confirm();
			return;
		} 
		if (mc instanceof MessagePostXmlFile){
			MessagePostXmlFile message = (MessagePostXmlFile) mc;
			postXmlFile(message.getFilename());
			rtuMessage.confirm();
			return;
		} 
			rtuMessage.cancel();
	}

	private void updateAllMeters() throws BusinessException, SQLException{
		try {
			List meterSerials = (List) concentrator.getAllMeters();
			for (Iterator it = meterSerials.iterator(); it.hasNext();){
				EeItem meterSerial = (EeItem) it.next();
				String serial = meterSerial.getIdent();
				List rtus = (List) MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(serial);
				for (Iterator jt=rtus.iterator(); jt.hasNext();){
					Device rtu = (Device) jt.next();
					DeviceShadow shadow = rtu.getShadow();
                    // LGAdvantis is deprectaed, just fixing syntax error
//					if (shadow.getGatewayId() != device.getId() ){
//						shadow.setGatewayId(device.getId());
//						rtu.update(shadow);
//					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}

	}

	private void resetAndDiscoverMeters() throws BusinessException{
		try {
			concentrator.post(ConstructXmlFiles.getResetAndDiscoverXml());
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}
	
	private void resetMeters() throws BusinessException{
		try {
			concentrator.post(ConstructXmlFiles.getResetXml());
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}

	private void discoverMeters() throws BusinessException{
		try {
			concentrator.post(ConstructXmlFiles.getDiscoverXml());
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}

	private void initiatePerformanceTest() throws BusinessException{
		try {
			concentrator.post(ConstructXmlFiles.getInitiatePerformanceTestXml());
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}		
	}
	
	private void postXmlFile(String filename) throws BusinessException{
		try {
			concentrator.post(ConstructXmlFiles.getXmlFile(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}		
		
	}
	
	private void debug(String msg) {
		concentrator.getLogger().info(msg);
		System.out.println(msg);
	}

}
