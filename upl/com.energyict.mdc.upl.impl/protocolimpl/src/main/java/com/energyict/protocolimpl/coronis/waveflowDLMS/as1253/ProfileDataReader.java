package com.energyict.protocolimpl.coronis.waveflowDLMS.as1253;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ProfileDataReader {

	AS1253 as1253;

	public ProfileDataReader(AS1253 as1253) {
		this.as1253 = as1253;
	}
	
	public static final int CAPTURED_OBJECTS_DATE_FIELD_INDEX=0; 
	public static final int CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX=1;
	public static final int CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX=2;

	int readProfileInterval() throws IOException {
		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttribute(as1253.getLoadProfileObisCode(), 4);
		return adt.intValue();
	}
	
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		
		ProfileData profileData = new ProfileData();
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		
		int profileInterval = as1253.getProfileInterval();
		
		if (as1253.isVerifyProfileInterval()) {
			profileInterval = readProfileInterval();
		}
		
		if (profileInterval != as1253.getProfileInterval()) {
			throw new WaveFlowDLMSException("Invalid profile interval. Configured is ["+as1253.getProfileInterval()+"] s, configured in meter is ["+profileInterval+"]!");
		}
		
		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttributeRange(as1253.getLoadProfileObisCode(), TransparantObjectAccessFactory.ATTRIBUTE_VALUE,lastReading);
		
		//System.out.println("KV_DEBUG> "+adt);
		
		// parse the AXD-R returned data...
		Calendar calendar = Calendar.getInstance(as1253.getTimeZone()); 
		Array array = adt.getArray();
		for (AbstractDataType arrayElement : array.getAllDataTypes()) {
			
			Structure structure = arrayElement.getStructure();
			int nrOfchannels = structure.nrOfDataTypes()-CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX; // nr of channels = nr of elements in structure - date field - startus bits field
			if (nrOfchannels == 0) {
				throw new WaveFlowDLMSException("No channels in the load profile. Might be a configuration error!");
			}
				
			if (channelInfos.size() == 0) {
				
//				if (as1253.getNumberOfChannels() != nrOfchannels) {
//					as1253.getLogger().warning("Number of channels in the meter load profile does not match with the configured nr of channels in EIServer!");
//				}
				
				// because we have to take care not to do too many roundtrips, we leave the unit type responsability to EIServer to configurate.
				for (int i = 0; i<nrOfchannels;i++) {
					channelInfos.add(new ChannelInfo(i, "AS1253_"+(i+1), Unit.get("")));
				}
				profileData.setChannelInfos(channelInfos);
			}
			
			// Workaround from Peter Bungert (Elster R&D Lampertheim)
			// Due to a bug in the meter, we reset the protocolstatus each time and only use it when there is also a timestamp involved...
			int protocolStatus=0; 
			AbstractDataType structureElement = structure.getDataType(CAPTURED_OBJECTS_DATE_FIELD_INDEX);
			if (!structureElement.isNullData()) {
				// set the interval timestamp if it has a value
				DateTime dateTime = new DateTime(structureElement.getOctetString(), as1253.getTimeZone());
				calendar.setTime(dateTime.getValue().getTime());
				ParseUtils.roundUp2nearestInterval(calendar, as1253.getProfileInterval());
				protocolStatus = structure.getDataType(CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX).intValue();
			}
			
			
			IntervalData intervalData = new IntervalData(calendar.getTime(),protocolStatus2EICode(protocolStatus),protocolStatus);
			for (int index=0;index<nrOfchannels;index++) {
				BigDecimal bd = BigDecimal.valueOf(structure.getDataType(CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX+index).longValue());
				intervalData.addValue(bd, protocolStatus, protocolStatus2EICode(protocolStatus));
			}
			profileData.addInterval(intervalData);  
			
			// increment the interval timestamp...
			calendar.add(Calendar.SECOND, profileInterval);
			
		} // for (AbstractDataType arrayElement : array.getAllDataTypes())
		
		if (includeEvents) {
			
			profileData.setMeterEvents(readMeterLogbook(lastReading));
			
		}
		
		return profileData;
	}
	
	private List<MeterEvent> readMeterLogbook(Date lastReading) throws IOException {
		
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttributeRange(AS1253.LOG_PROFILE, TransparantObjectAccessFactory.ATTRIBUTE_VALUE,lastReading);
		//System.out.println("KV_DEBUG> "+adt);
		Array array = adt.getArray();
		for (AbstractDataType arrayElement : array.getAllDataTypes()) {
			DateTime dateTime = new DateTime(arrayElement.getStructure().getDataType(0).getOctetString(), as1253.getTimeZone());
			Date date = dateTime.getValue().getTime();
			int meterEventCode2MeterEvents = arrayElement.getStructure().getDataType(1).intValue();
			meterEvents.addAll(meterEventCode2MeterEvents(date,meterEventCode2MeterEvents));
		}
		
		int applicationstatus = as1253.getParameterFactory().readApplicationStatus();
		
		if ((applicationstatus & 0x01) == 0x01) {
			meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Link fault with energymeter"));
		}
		if ((applicationstatus & 0x02) == 0x02) {
			meterEvents.add(new MeterEvent(new Date(),MeterEvent.POWERUP,"Power Back notification"));
		}
		if ((applicationstatus & 0x04) == 0x04) {
			meterEvents.add(new MeterEvent(new Date(),MeterEvent.POWERDOWN,"Power down notification"));
		}
		
		if ((applicationstatus & 0x7) != 0) {
			as1253.getParameterFactory().writeApplicationStatus(0);
		}
		
		return meterEvents;
	}
	
	
	
	
	private List<MeterEvent> meterEventCode2MeterEvents(Date date,int meterEventCode) {
		
		/*
		  
		 I Think that the bits are reversed. 
		 FIXME: this has to be confirmed by Elster...  
		  
		b7 Power failure
		b6 Power recovery
		b5 Change of time/date
		b4 Demand reset
		b3 Seasonal switchover (summer/winter time)
		b2 Measure value disturbed
		b1 Running reserve exhausted
		b0 Fatal device error
		*/
		
		/*
Logstatus:
	0   0   0   0   0   0   0   0
	|   |   |   |   |   |   |   1		new interval because of power-down 
	|   |   |   |   |   |   |   2		new interval because of power-up and variable changed by setting 
	|   |   |   |   |   |   |   4		new time/date or daylight savings switch 
	|   |   |   |   |   |   |   8		new interval because of demand reset and 1-phase or 2-phase power outage
	|   |   |   |   |   |   1		season change, i.e. dst switch (VDEW) and system reverse energy flow 
	|   |   |   |   |   |   2		values not reliable
	|   |   |   |   |   |   4		carry over error (copy of errcovr, syserr)
	|   |   |   |   |   |   8		fatal error ('OR' of some syserr flags)
	|   |   |   |   |   1			input 2 event detected
	|   |   |   |   |   2			load profile initialised 
	|   |   |   |   |   4			logbook initialised 
	|   |   |   |   |   8			input 1 event detected 
	|   |   |   |   1			reverse power in 1 or 2 phases detected
	|   |   |   |   2			error or warning off
	|   |   |   |   4			error or warning on ('OR' of syserr and syswarn flags)
	|   |   |   |   8			variable changed by setting
	|   |   |   |
	|   |   |   1			phase L3 is missing 
	|   |   |   2			phase L2 is missing 
	|   |   |   4			phase L1 is missing 
	|   |   |   8			contactor switched off 
	|   |   1			wrong password was used
	|   |   2			main cover is or was opened
	|   |   4			terminal cover is or was opened
	|   |   8			change of Impuls constant
	|   | 
	|   1				dr1: dr4-dr1: Bit coded demand rates
	|   2				dr2
	|   4				dr3
	|   8				dr4
	1				Binary coded energy rate
					T1		T2		T3		T4
					00		10		01		11
	2			
	|
	4				Binary coded season
					S1		S2		S3		S4
					00		10		01		11
	8			

*/
		
		Map<Integer,StringBuilder> meterEventMap = new HashMap<Integer,StringBuilder>();
		
		if ((meterEventCode & 0x1) == 0x1) buildMeterEvent(meterEventMap,date,MeterEvent.POWERDOWN,meterEventCode,"new interval because of power-down");
		if ((meterEventCode & 0x2) == 0x2) buildMeterEvent(meterEventMap,date,MeterEvent.POWERUP,meterEventCode,"new interval because of power-up and variable changed by setting");
		if ((meterEventCode & 0x4) == 0x4) buildMeterEvent(meterEventMap,date,MeterEvent.SETCLOCK,meterEventCode,"new time/date or daylight savings switch");
		if ((meterEventCode & 0x8) == 0x8) buildMeterEvent(meterEventMap,date,MeterEvent.BILLING_ACTION,meterEventCode,"new interval because of demand reset and 1-phase or 2-phase power outage");
		
		if ((meterEventCode & 0x10) == 0x10) buildMeterEvent(meterEventMap,date,MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED,meterEventCode,"season change, i.e. dst switch (VDEW) and system reverse energy flow");
		if ((meterEventCode & 0x20) == 0x20) buildMeterEvent(meterEventMap,date,MeterEvent.MEASUREMENT_SYSTEM_ERROR,meterEventCode,"values not reliable");
		if ((meterEventCode & 0x40) == 0x40) buildMeterEvent(meterEventMap,date,MeterEvent.MEASUREMENT_SYSTEM_ERROR,meterEventCode,"carry over error (copy of errcovr, syserr)");
		if ((meterEventCode & 0x80) == 0x80) buildMeterEvent(meterEventMap,date,MeterEvent.FATAL_ERROR,meterEventCode,"fatal error ('OR' of some syserr flags)");
		
		if ((meterEventCode & 0x100) == 0x100) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"input 2 event detected");
		if ((meterEventCode & 0x200) == 0x200) buildMeterEvent(meterEventMap,date,MeterEvent.LOADPROFILE_CLEARED,meterEventCode,"load profile initialised");
		if ((meterEventCode & 0x400) == 0x400) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"logbook initialised");
		if ((meterEventCode & 0x800) == 0x800) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"input 1 event detected");
		
		if ((meterEventCode & 0x1000) == 0x1000) buildMeterEvent(meterEventMap,date,MeterEvent.REVERSE_RUN,meterEventCode,"reverse power in 1 or 2 phases detected");
		if ((meterEventCode & 0x2000) == 0x2000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"error or warning off");
		if ((meterEventCode & 0x4000) == 0x4000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"error or warning on ('OR' of syserr and syswarn flags)");
		if ((meterEventCode & 0x8000) == 0x8000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"variable changed by setting");
		
		if ((meterEventCode & 0x10000) == 0x10000) buildMeterEvent(meterEventMap,date,MeterEvent.PHASE_FAILURE,meterEventCode,"phase L3 is missing");
		if ((meterEventCode & 0x20000) == 0x20000) buildMeterEvent(meterEventMap,date,MeterEvent.PHASE_FAILURE,meterEventCode,"phase L2 is missing");
		if ((meterEventCode & 0x40000) == 0x40000) buildMeterEvent(meterEventMap,date,MeterEvent.PHASE_FAILURE,meterEventCode,"phase L1 is missing");
		if ((meterEventCode & 0x80000) == 0x80000) buildMeterEvent(meterEventMap,date,MeterEvent.REMOTE_DISCONNECTION,meterEventCode,"contactor switched off");
		
		if ((meterEventCode & 0x100000) == 0x100000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"wrong password was used");
		if ((meterEventCode & 0x200000) == 0x200000) buildMeterEvent(meterEventMap,date,MeterEvent.COVER_OPENED,meterEventCode,"main cover is or was opened");
		if ((meterEventCode & 0x400000) == 0x400000) buildMeterEvent(meterEventMap,date,MeterEvent.TERMINAL_OPENED,meterEventCode,"terminal cover is or was opened");
		if ((meterEventCode & 0x800000) == 0x800000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"change of Impuls constant");

		if ((meterEventCode & 0x1000000) == 0x1000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"dr1: dr4-dr1: Bit coded demand rates");
		if ((meterEventCode & 0x2000000) == 0x2000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"dr2");
		if ((meterEventCode & 0x4000000) == 0x4000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"dr3");
		if ((meterEventCode & 0x8000000) == 0x8000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"dr4");

		if ((meterEventCode & 0x30000000) == 0x00000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"energy rate T1");
		if ((meterEventCode & 0x30000000) == 0x20000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"energy rate T2");
		if ((meterEventCode & 0x30000000) == 0x10000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"energy rate T3");
		if ((meterEventCode & 0x30000000) == 0x30000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"energy rate T4");

		if ((meterEventCode & 0xC0000000) == 0x00000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"coded season S1");
		if ((meterEventCode & 0xC0000000) == 0x80000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"coded season S2");
		if ((meterEventCode & 0xC0000000) == 0x40000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"coded season S3");
		if ((meterEventCode & 0xC0000000) == 0xC0000000) buildMeterEvent(meterEventMap,date,MeterEvent.OTHER,meterEventCode,"coded season S4");
		

		
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		for (int eicCode : meterEventMap.keySet()) {
			StringBuilder strBuilder = meterEventMap.get(eicCode);
			meterEvents.add(new MeterEvent(date,eicCode,meterEventCode,strBuilder.toString()));
		}
		
		return meterEvents;
	}
	
	private void buildMeterEvent(Map<Integer,StringBuilder> meterEventMap, Date date, int eiCode, int meterEventCode, String description) {
		StringBuilder strBuilder = meterEventMap.get(eiCode);
		if (strBuilder == null) {
			strBuilder = new StringBuilder();
			meterEventMap.put(eiCode, strBuilder);
			strBuilder.append(description);
		}
		else {
			strBuilder.append(", "+description);
		}
		
	}
	
	private int protocolStatus2EICode(int protocolStatus) {
		
		/*
		b7 Power failure
		b6 Power recovery
		b5 Change of time/date
		b4 Demand reset
		b3 Seasonal switchover (summer/winter time)
		b2 Measure value disturbed
		b1 Running reserve exhausted
		b0 Fatal device error
		*/
		
		int eiCode=0;
		if ((protocolStatus & 0x01) == 0x01) eiCode |= IntervalStateBits.DEVICE_ERROR;
		if ((protocolStatus & 0x02) == 0x02) eiCode |= IntervalStateBits.BATTERY_LOW;
		if ((protocolStatus & 0x04) == 0x04) eiCode |= IntervalStateBits.CORRUPTED;
		if ((protocolStatus & 0x08) == 0x08) eiCode |= IntervalStateBits.SHORTLONG;
		if ((protocolStatus & 0x10) == 0x10) eiCode |= IntervalStateBits.OTHER;
		if ((protocolStatus & 0x20) == 0x20) eiCode |= IntervalStateBits.SHORTLONG;
		if ((protocolStatus & 0x40) == 0x40) eiCode |= IntervalStateBits.POWERUP;
		if ((protocolStatus & 0x80) == 0x80) eiCode |= IntervalStateBits.POWERDOWN;
		
		return eiCode;
	}
	
}
