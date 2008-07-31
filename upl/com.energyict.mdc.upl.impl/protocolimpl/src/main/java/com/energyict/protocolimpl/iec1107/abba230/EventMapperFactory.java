package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;
import java.util.*;



import com.energyict.protocol.MeterEvent;

public class EventMapperFactory {

	
	
	static List<MeterEventMapEntry> entries = new ArrayList();
	static {
		// TSystemStatus4 not needed!
		
		// TSystemStatus6
		entries.add(new MeterEventMapEntry(6,0,MeterEvent.CONFIGURATIONCHANGE,"Programming event occured"));
		entries.add(new MeterEventMapEntry(6,1,MeterEvent.CONFIGURATIONCHANGE,"Password changed"));
		entries.add(new MeterEventMapEntry(6,2,MeterEvent.POWERDOWN,"Powerfail longer than 3 minutes"));
		entries.add(new MeterEventMapEntry(6,3,MeterEvent.POWERDOWN,"Powerfail"));
		entries.add(new MeterEventMapEntry(6,4,MeterEvent.OTHER,"Contactor OPEN"));
		entries.add(new MeterEventMapEntry(6,5,MeterEvent.OTHER,"Contactor CLOSE"));
		entries.add(new MeterEventMapEntry(6,6,MeterEvent.OTHER,"Contactor ARM"));
		entries.add(new MeterEventMapEntry(6,7,MeterEvent.METER_ALARM,"Terminal cover tamper event"));
		
		// TSystemStatus7
		entries.add(new MeterEventMapEntry(7,0,MeterEvent.METER_ALARM,"Main cover tamper event"));
		entries.add(new MeterEventMapEntry(7,1,MeterEvent.METER_ALARM,"Magnetic tamper event"));
		entries.add(new MeterEventMapEntry(7,2,MeterEvent.OTHER,"Reverse run occured"));
		entries.add(new MeterEventMapEntry(7,3,MeterEvent.PROGRAM_FLOW_ERROR,"Transient reset occured"));
		entries.add(new MeterEventMapEntry(7,5,MeterEvent.OTHER,"Firmware download"));
		entries.add(new MeterEventMapEntry(7,6,MeterEvent.METER_ALARM,"Meter error"));
		entries.add(new MeterEventMapEntry(7,7,MeterEvent.OTHER,"Battery voltage low"));
		
		// TSystemError0
		entries.add(new MeterEventMapEntry(16,5,MeterEvent.HARDWARE_ERROR,"Contactor drive failure"));
		entries.add(new MeterEventMapEntry(16,6,MeterEvent.HARDWARE_ERROR,"RTC failed to initialise"));
	}
	
	public List<MeterEvent> getMeterEvents(String eventString) throws IOException{
		
		long ms = new Date().getTime();
		if (eventString.length() != 42)
			throw new IOException("EventMapperFactory, getMeterEvents, event string has wrong length! ("+eventString+")");
		
		List<MeterEvent> meterEvents = new ArrayList();
		
		for (int statusId=0;statusId<21;statusId++) {
			String s = eventString.substring(statusId*2, (statusId*2+2));
			int val = Integer.parseInt(s, 16);
           
			for(int bitId=0;bitId<8;bitId++) {
				if ((val&(0x01<<bitId)) != 0) {
					Date date = new Date(ms);
					ms+=1000; // very tricky because same events with different description are not persisted if date is the same. Only add a millisecond doesn't help either...
					meterEvents.add(getMeterEvent(statusId,bitId,date));
				}
			}
		}
		return meterEvents;
	}

	private MeterEvent getMeterEvent(int statusId, int bitId, Date date) {
		
		Iterator<MeterEventMapEntry> it = entries.iterator();
		
		while(it.hasNext()) {
			MeterEventMapEntry m = it.next();
			if ((m.getStatusId()==statusId) && (m.getBitId() == bitId)) {
				return new MeterEvent(date,m.getMeterEventCode(),m.getDescription());
			}
		}
		return new MeterEvent(date,MeterEvent.OTHER,"Unknown event occured on offset "+statusId+", bit "+bitId);
		
	}
	
	public static void main(String[] args) {
		EventMapperFactory o = new EventMapperFactory();
		try {
			Iterator<MeterEvent> it = o.getMeterEvents("000000000055550100000000000000ff0000000000").iterator();
			while(it.hasNext()) {
				
				System.out.println(it.next());
				
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
