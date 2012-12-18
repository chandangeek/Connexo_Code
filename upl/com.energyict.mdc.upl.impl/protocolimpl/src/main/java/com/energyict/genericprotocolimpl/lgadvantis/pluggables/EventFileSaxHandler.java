package com.energyict.genericprotocolimpl.lgadvantis.pluggables;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.DeviceEventShadow;
import com.energyict.mdw.shadow.DeviceMessageShadow;
import com.energyict.protocol.MeterData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageDiscoverMeters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;
import java.util.*;

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

			//Check whether we are in a device NumberOfElements, i.e. the meter is known
			if (deviceSerial != null){
				//Create and store the device Event
				//Get the RTUs with this serialNumber, cannot differentiate should be only one
				List rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);

				//Create the deviceEvent
				for (Iterator it=rtus.iterator(); it.hasNext();){
					Device rtu = (Device) it.next();
                    List<LogBook> logBooks = rtu.getLogBooks();
                    if(logBooks.size() == 1){
                        MeterData md = new MeterData();
                        // will not be used anymore, generics will be deleted in 9.1
//                        md.addMeterEvent(new MeterProtocolEvent(new Date(), alarmId.intValue(), alarmId.intValue(),
//                                alarmDescription,
//                                0, logBooks.get(0).getId(), 0));
                        rtu.store(md);
                    }
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
		List rtus = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(deviceSerial);
		//Create the DeviceMessage
		for (Iterator it=rtus.iterator(); it.hasNext();){
			Device rtu = (Device) it.next();
			
			DeviceMessageShadow shadow = new DeviceMessageShadow();
			shadow.setReleaseDate(Calendar.getInstance().getTime());
			shadow.setContents(content.xmlEncode());
			rtu.createMessage(shadow);

            // Note: protocol will not be used anymore
//			List schedulers = rtu.getCommunicationSchedulers();
//			for (Iterator jt = schedulers.iterator(); jt.hasNext();){
//				CommunicationScheduler scheduler = (CommunicationScheduler) jt.next();
//				if (scheduler.getCommunicationProfile().getSendRtuMessage()){
//					scheduler.startReadingNow();
//				}
//			}
		}
	}
}
