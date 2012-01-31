package com.energyict.genericprotocolimpl.webrtuz3.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

public class MbusControlLog {
	
	private DataContainer dcEvents;
	
	// Mbus control log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS1 = 160;
	private static final int EVENT_MANUAL_CONNECTION_MBUS1 = 161;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS1 = 162;
	private static final int EVENT_REMOTE_CONNECTION_MBUS1 = 163;
	private static final int EVENT_VALVE_ALARM_MBUS1 = 164;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS2 = 170;
	private static final int EVENT_MANUAL_CONNECTION_MBUS2 = 171;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS2 = 172;
	private static final int EVENT_REMOTE_CONNECTION_MBUS2 = 173;
	private static final int EVENT_VALVE_ALARM_MBUS2 = 174;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS3 = 180;
	private static final int EVENT_MANUAL_CONNECTION_MBUS3 = 181;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS3 = 182;
	private static final int EVENT_REMOTE_CONNECTION_MBUS3 = 183;
	private static final int EVENT_VALVE_ALARM_MBUS3 = 184;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS4 = 190;
	private static final int EVENT_MANUAL_CONNECTION_MBUS4 = 191;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS4 = 192;
	private static final int EVENT_REMOTE_CONNECTION_MBUS4 = 193;
	private static final int EVENT_VALVE_ALARM_MBUS4 = 194;
	
	public MbusControlLog(DataContainer dc){
		this.dcEvents = dc;
	}
	
	public List<MeterEvent> getMeterEvents() throws IOException{
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i < size; i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1)&0xFF; // To prevent negative values
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
			}
			if(eventTimeStamp != null){
                MeterEvent event;
                if (ExtraEvents.contains(eventId)) {
                    event = ExtraEvents.getExtraEvent(eventTimeStamp, eventId);
                } else {
                    event = new MeterEvent(eventTimeStamp, eventId, eventId);
                }
                // Create a new meter Event using the toString method of the previous event
                // to work around the missing resources problem in EiServer ([JIRA] EISERVER-583)
                meterEvents.add(new MeterEvent(eventTimeStamp, eventId, eventId, event.toString()));
			}
		}
		return meterEvents;
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
}
