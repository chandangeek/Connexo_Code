package com.energyict.genericprotocolimpl.lgadvantis.pluggables;

import com.energyict.cbo.BusinessException;
import com.energyict.edf.messages.MessageContent;
import com.energyict.edf.messages.MessageDiscoverMeters;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.RtuEventShadow;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocol.MeterEvent;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EventFileSaxHandler extends DefaultHandler {

	private final static String DEVICEELEMENT = "ee-ia";
	private final static String ALARMELEMENT = "alarm-def";
	private final static String DEVICEIDATTRIBUTE = "ident";
	private final static String ALARMIDATTRIBUTE = "id";
	private final static String ALARMLABELATTRIBUTE = "label";

	private final static int ALARMID_NEWDEVICEDISCOVERY = 4;

	private String deviceSerial = null;
	private Integer alarmId = null;
	private String alarmDescription = null;

	public EventFileSaxHandler(){

	}

	public void endElement(String uri, String localName, String name)
	throws SAXException {
		if( name.equals(DEVICEELEMENT) ) {
			handleDeviceElementEnd();
		}

	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if( name.equals(DEVICEELEMENT) ) {
			handleDeviceElementStart(attributes);
		}

		if( name.equals(ALARMELEMENT) ) {
			handleAlarmElement(attributes);
		}
	}

	private void handleDeviceElementStart(Attributes attributes){
		deviceSerial = attributes.getValue(DEVICEIDATTRIBUTE);
	}

	private void handleAlarmElement(Attributes attributes){
		try {
			String alarmIdString = attributes.getValue(ALARMIDATTRIBUTE);
			alarmDescription = attributes.getValue(ALARMLABELATTRIBUTE);

			//Process the alarmId
			alarmId = new Integer(Integer.parseInt(alarmIdString));

			//Check is other special actions need to be taken
			performActionsBasedOnAlarmId();

			//Check whether we are in a device Element, i.e. the meter is known
			if (deviceSerial != null){
				//Create and store the device Event
				//Get the RTUs with this serialNumber, cannot differentiate should be only one
				List rtus = MeteringWarehouse.getCurrent().getRtuFactory().findBySerialNumber(deviceSerial);

				//Create the deviceEvent
				for (Iterator it=rtus.iterator(); it.hasNext();){
					Rtu rtu = (Rtu) it.next();
						
					RtuEventShadow shadow = new RtuEventShadow();
					shadow.setCode(deviceCodeTranslation(alarmId.intValue()));
					shadow.setDeviceCode(alarmId.intValue());
					shadow.setDate(Calendar.getInstance().getTime());
					shadow.setMessage(alarmDescription);
					shadow.setRtuId(rtu.getId());

					rtu.addEvent(shadow);

				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleDeviceElementEnd(){
		deviceSerial = null;
	}

	private int deviceCodeTranslation(int code){
		switch(code){
		case 1: return MeterEvent.HARDWARE_ERROR ;
		case 2: return MeterEvent.OTHER;
		case 3: return MeterEvent.OTHER;
		case 4: return MeterEvent.OTHER;
		case 5: return MeterEvent.OTHER;
		case 9: return MeterEvent.RAM_MEMORY_ERROR;
		case 10: return MeterEvent.OTHER;
		case 11: return MeterEvent.OTHER;
		case 12: return MeterEvent.OTHER;
		case 13: return MeterEvent.OTHER;

		default: return MeterEvent.OTHER;
		}
	}

	private void performActionsBasedOnAlarmId() throws BusinessException, SQLException{
		switch(alarmId.intValue()){
		case ALARMID_NEWDEVICEDISCOVERY : 
			executeDiscoverMetersOnDevice();
		default: 
			// do nothing
		}
	}

	private void executeDiscoverMetersOnDevice() throws BusinessException, SQLException{
		MessageContent content = new MessageDiscoverMeters(0);
		//Get the RTUs with this serialNumber, cannot differentiate should be only one
		List rtus = MeteringWarehouse.getCurrent().getRtuFactory().findBySerialNumber(deviceSerial);
		//Create the RtuMessage
		for (Iterator it=rtus.iterator(); it.hasNext();){
			Rtu rtu = (Rtu) it.next();
			
			RtuMessageShadow shadow = new RtuMessageShadow();
			shadow.setReleaseDate(Calendar.getInstance().getTime());
			shadow.setContents(content.xmlEncode());
			rtu.createMessage(shadow);
			
			List schedulers = rtu.getCommunicationSchedulers();
			for (Iterator jt = schedulers.iterator(); jt.hasNext();){
				CommunicationScheduler scheduler = (CommunicationScheduler) jt.next();
				if (scheduler.getCommunicationProfile().getSendRtuMessage()){
					scheduler.startReadingNow();
				}
			}
		}
	}
}
