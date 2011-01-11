package com.energyict.protocolimpl.coronis.waveflowDLMS.as1253;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflowDLMS.*;

public class ProfileDataReader {

	AS1253 as1253;

	public ProfileDataReader(AS1253 as1253) {
		this.as1253 = as1253;
	}
	
	public static final int CAPTURED_OBJECTS_DATE_FIELD_INDEX=0; 
	public static final int CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX=1;
	public static final int CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX=2;

	int readProfileInterval() throws IOException {
		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttribute(AS1253.LOAD_PROFILE_PULSE_VALUES, 4);
		return adt.intValue();
	}
	
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		
		ProfileData profileData = new ProfileData();
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		
		int profileInterval = readProfileInterval();
		
		if (profileInterval != as1253.getProfileInterval()) {
			throw new WaveFlowDLMSException("Invalid profile interval. Configured is ["+as1253.getProfileInterval()+"] s, configured in meter is ["+profileInterval+"]!");
		}
		
		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttribute(AS1253.LOAD_PROFILE_PULSE_VALUES, TransparantObjectAccessFactory.ATTRIBUTE_VALUE,lastReading);
		
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
			
			AbstractDataType structureElement = structure.getDataType(CAPTURED_OBJECTS_DATE_FIELD_INDEX);
			if (!structureElement.isNullData()) {
				// set the interval timestamp if it has a value
				DateTime dateTime = new DateTime(structureElement.getOctetString(), as1253.getTimeZone());
				calendar.setTime(dateTime.getValue().getTime());
				//ParseUtils.roundDown2nearestInterval(calendar, as1253.getProfileInterval());
			}
			int protocolStatus = structure.getDataType(CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX).intValue();
			
			IntervalData intervalData = new IntervalData(calendar.getTime(),protocolStatus,protocolStatus2EICode(protocolStatus));
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

		AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttribute(AS1253.LOG_PROFILE, TransparantObjectAccessFactory.ATTRIBUTE_VALUE,lastReading);
		
		Array array = adt.getArray();
		for (AbstractDataType arrayElement : array.getAllDataTypes()) {
			DateTime dateTime = new DateTime(arrayElement.getStructure().getDataType(0).getOctetString(), as1253.getTimeZone());
			Date date = dateTime.getValue().getTime();
			int meterEventCode2MeterEvents = arrayElement.getStructure().getDataType(1).intValue();
			meterEvents.addAll(meterEventCode2MeterEvents(date,meterEventCode2MeterEvents));
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
		
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		if ((meterEventCode & 0x80) == 0x80) meterEvents.add(new MeterEvent(date,MeterEvent.HARDWARE_ERROR,meterEventCode,"Fatal device error"));
		if ((meterEventCode & 0x40) == 0x40) meterEvents.add(new MeterEvent(date,MeterEvent.BATTERY_VOLTAGE_LOW,meterEventCode,"Running reserve exhausted"));
		if ((meterEventCode & 0x20) == 0x20) meterEvents.add(new MeterEvent(date,MeterEvent.MEASUREMENT_SYSTEM_ERROR,meterEventCode,"Measure value disturbed"));
		if ((meterEventCode & 0x10) == 0x10) meterEvents.add(new MeterEvent(date,MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED,meterEventCode,"Seasonal switchover"));
		if ((meterEventCode & 0x08) == 0x08) meterEvents.add(new MeterEvent(date,MeterEvent.BILLING_ACTION,meterEventCode,"Demand reset"));
		if ((meterEventCode & 0x04) == 0x04) meterEvents.add(new MeterEvent(date,MeterEvent.SETCLOCK,meterEventCode,"Change of date/time"));
		if ((meterEventCode & 0x02) == 0x02) meterEvents.add(new MeterEvent(date,MeterEvent.POWERUP,meterEventCode,"Power recovery"));
		if ((meterEventCode & 0x01) == 0x01) meterEvents.add(new MeterEvent(date,MeterEvent.POWERDOWN,meterEventCode,"Power failure"));
		
		return meterEvents;
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
