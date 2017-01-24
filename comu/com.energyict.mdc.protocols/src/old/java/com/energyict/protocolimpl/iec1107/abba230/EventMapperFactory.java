package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EventMapperFactory {



	static List entries = new ArrayList();

	static {
		// TSystemStatus4 not needed!

		// TSystemStatus6
		entries.add(new MeterEventMapEntry(6,0,MeterEvent.CONFIGURATIONCHANGE,"Programming event occurred"));
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
		entries.add(new MeterEventMapEntry(7,2,MeterEvent.OTHER,"Reverse run occurred"));
		entries.add(new MeterEventMapEntry(7,3,MeterEvent.PROGRAM_FLOW_ERROR,"Transient reset occurred"));

		//added cc
		entries.add(new MeterEventMapEntry(7,4,MeterEvent.BILLING_ACTION ,"STATUS_EVENT_BILLING Billing event occurred"));

		entries.add(new MeterEventMapEntry(7,5,MeterEvent.OTHER,"Firmware download"));
		entries.add(new MeterEventMapEntry(7,6,MeterEvent.METER_ALARM,"Meter error"));
		entries.add(new MeterEventMapEntry(7,7,MeterEvent.OTHER,"Battery voltage low"));

		// TSystemStatus8
		entries.add(new MeterEventMapEntry(8,7,MeterEvent.METER_ALARM,"Magnetic tamper in progress"));
		entries.add(new MeterEventMapEntry(8,6,MeterEvent.METER_ALARM,"Terminal cover tamper in progress"));
		entries.add(new MeterEventMapEntry(8,5,MeterEvent.METER_ALARM,"Main cover tamper in progress"));
		entries.add(new MeterEventMapEntry(8,3,MeterEvent.METER_ALARM,"Undervoltage event"));
		entries.add(new MeterEventMapEntry(8,2,MeterEvent.METER_ALARM,"Overvoltage event"));

		//added cc
		entries.add(new MeterEventMapEntry(8,1,MeterEvent.OTHER ,"STATUS_EVENT_MODULE_COMMS_SESSION Module Comms event"));  //this creates an infinite loop, so we won't be using it!
		//added cc
		entries.add(new MeterEventMapEntry(8,0,MeterEvent.OTHER ,"STATUS_EVENT_OPTICAL_COMMS_SESSION Optical FLAG Comms event"));

		// TSystemStatus9
                //cc modified description
		entries.add(new MeterEventMapEntry(9,7,MeterEvent.METER_ALARM,"STATUS_EVENT_LOADMON_HIGH_TRIP Load monitor high trip event"));
                //cc modified description
		entries.add(new MeterEventMapEntry(9,6,MeterEvent.METER_ALARM,"STATUS_EVENT_LOADMON_LOW_TRIP Load monitor low trip event"));
		entries.add(new MeterEventMapEntry(9,3,MeterEvent.METER_ALARM,"Undervoltage confirmed"));
		entries.add(new MeterEventMapEntry(9,2,MeterEvent.METER_ALARM,"Undervoltage detected"));
		entries.add(new MeterEventMapEntry(9,1,MeterEvent.METER_ALARM,"Overvoltage confirmed"));
		entries.add(new MeterEventMapEntry(9,0,MeterEvent.METER_ALARM,"Overvoltage detected"));

                // TSystemStatus10 - all added by cc
                entries.add(new MeterEventMapEntry(10,7,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_ARM_LOADMON Contacted Armed via Load Monitor"));
                entries.add(new MeterEventMapEntry(10,6,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_ARM_MOD Contactor Armed via Remote Module Comms"));
                entries.add(new MeterEventMapEntry(10,5,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACTOR_ARM_OPT Contactor Armed via Optical Comms"));
                entries.add(new MeterEventMapEntry(10,4,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_OPEN_DISCONNECT Contactor Opened by Disconnector"));
                entries.add(new MeterEventMapEntry(10,3,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_OPEN_LOADMON_HIGH Contactor Opened by Load Monitor High"));
                entries.add(new MeterEventMapEntry(10,2,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_OPEN_LOADMON_LOW Contactor Opened by Load Monitor Low"));
                entries.add(new MeterEventMapEntry(10,1,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACTOR_OPEN_MOD Contactor Opened via Remote Module Comms"));
		entries.add(new MeterEventMapEntry(10,0,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACTOR_OPEN_OPT Contactor Opened via Optical Comms"));

                // TSystemStatus11 - all added by cc
                entries.add(new MeterEventMapEntry(11,3,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_CLOSE_BUT Contactor Closed by Pushbutton"));
                entries.add(new MeterEventMapEntry(11,2,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_CLOSE_MOD Contactor Closed via Remote Module Comms"));
                entries.add(new MeterEventMapEntry(11,1,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACTOR_CLOSE_OPT Contactor Closed via Optical Comms"));
                entries.add(new MeterEventMapEntry(11,0,MeterEvent.METER_ALARM,"STATUS_EVENT_CONTACT_ARM_DISCONNECT Contactor Armed by Disconnector"));


		// TSystemError0
		//added cc
		entries.add(new MeterEventMapEntry(16,7,MeterEvent.HARDWARE_ERROR,"Error Power Fail Backup"));

		entries.add(new MeterEventMapEntry(16,5,MeterEvent.HARDWARE_ERROR,"Contactor drive failure"));
		entries.add(new MeterEventMapEntry(16,6,MeterEvent.HARDWARE_ERROR,"RTC failed to initialise"));

		//added cc
		entries.add(new MeterEventMapEntry(16,4,MeterEvent.HARDWARE_ERROR,"I2C failure"));
		entries.add(new MeterEventMapEntry(16,3,MeterEvent.HARDWARE_ERROR,"I2C unknown device failure"));
		entries.add(new MeterEventMapEntry(16,2,MeterEvent.HARDWARE_ERROR,"I2C device 2 failure"));
		entries.add(new MeterEventMapEntry(16,1,MeterEvent.HARDWARE_ERROR,"I2C device 1 failure"));
		entries.add(new MeterEventMapEntry(16,0,MeterEvent.HARDWARE_ERROR,"I2C device 0 failure"));


		// TSystemError1 all added by cc
		entries.add(new MeterEventMapEntry(17,6,MeterEvent.HARDWARE_ERROR,"Firmware Checksum error"));
		entries.add(new MeterEventMapEntry(17,5,MeterEvent.HARDWARE_ERROR,"Error Invalid Instrumentation Period Configuration"));
		entries.add(new MeterEventMapEntry(17,4,MeterEvent.HARDWARE_ERROR,"Error Invalid Instrumentation profile"));
		entries.add(new MeterEventMapEntry(17,3,MeterEvent.HARDWARE_ERROR,"Error Estimated Battery Life Exceeded"));
		entries.add(new MeterEventMapEntry(17,2,MeterEvent.HARDWARE_ERROR,"Error Invalid Demand Period Configuration"));
		entries.add(new MeterEventMapEntry(17,1,MeterEvent.HARDWARE_ERROR,"Error Load Profile"));
		entries.add(new MeterEventMapEntry(17,0,MeterEvent.HARDWARE_ERROR,"Error Backup"));


		// TSystemError2 all added by cc
		entries.add(new MeterEventMapEntry(18,3,MeterEvent.HARDWARE_ERROR,"Manufacturing Configuration PROFILES"));
		entries.add(new MeterEventMapEntry(18,2,MeterEvent.HARDWARE_ERROR,"Manufacturing Configuration REGISTRATION"));
		entries.add(new MeterEventMapEntry(18,1,MeterEvent.HARDWARE_ERROR,"Manufacturing Configuration REG_SP"));
		entries.add(new MeterEventMapEntry(18,0,MeterEvent.HARDWARE_ERROR,"Manufacturing Configuration CE"));


	}

	public List getMeterEvents(String eventString) throws IOException{

		long ms = new Date().getTime();
		if (eventString.length() != 42){
			throw new IOException("EventMapperFactory, getMeterEvents, event string has wrong length! ("+eventString+")");
		}

		List meterEvents = new ArrayList();

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

		Iterator it = entries.iterator();

		while(it.hasNext()) {
			MeterEventMapEntry m = (MeterEventMapEntry) it.next();
			if ((m.getStatusId()==statusId) && (m.getBitId() == bitId)) {
				return new MeterEvent(date,m.getMeterEventCode(),m.getDescription());
			}
		}
		return new MeterEvent(date,MeterEvent.OTHER,"Unknown event occured on offset "+statusId+", bit "+bitId);

	}

}
