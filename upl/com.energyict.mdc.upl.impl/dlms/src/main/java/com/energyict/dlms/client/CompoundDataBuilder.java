package com.energyict.dlms.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.*;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.custom.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class CompoundDataBuilder {

	final int DEBUG=0;
	
	public CompoundDataBuilder() {
	}

	public byte[] buildCompoundData(MeterReadingData meterReadingData, ProfileData profileData, int profileInterval, int databaseID, String serialID) throws IOException {
		CompoundDataBuilderConnection cosemAPDUBuilder = new CompoundDataBuilderConnection();
		
		// set custom object database ID
		if (databaseID > 0) {
			DatabaseIDCustomCosem ack = new DatabaseIDCustomCosem(cosemAPDUBuilder);
			ack.setFields(databaseID);
		}
		else if (serialID != null) {
			DeviceIDCustomCosem ack = new DeviceIDCustomCosem(cosemAPDUBuilder);
			ack.setFields(serialID);
		}
		
		//SelectDevice
		
		// set (report) current clock
		Clock clock = new Clock(cosemAPDUBuilder);
		clock.setTimeAttr(new DateTime(TimeZone.getTimeZone("GMT")));
		
//		ClockCustomCosem o2 = new ClockCustomCosem(cosemAPDUBuilder);
//		o2.setFields(new Date());
		
		
		if (profileData != null) {
			if (profileData.getIntervalDatas().size()>0) {
				
				// scan intervals if there are channel specific interval status flags...
				Map<Integer,ObisCode> channelStatusFlags=new HashMap();
				for (int i=0;i<profileData.getIntervalDatas().size();i++) {
					IntervalData ivd = (IntervalData)profileData.getIntervalDatas().get(i);
					for (int t=0;t<ivd.getValueCount();t++) {
						if (((IntervalValue)ivd.getIntervalValues().get(t)).getEiStatus() != 0)
							channelStatusFlags.put(Integer.valueOf(t),ObisCode.fromString("0.0.96.60."+(t+1)+".0"));
					}
				}
				
				
				ProfileGeneric profileGeneric = new ProfileGeneric(cosemAPDUBuilder,new ObjectReference(ObisCode.fromString("0.0.99.1.0.255").getLN()));
				
				// set load profile object capture objects attribute
				ProfileGenericCaptureObjectsBuilder pgcob = new ProfileGenericCaptureObjectsBuilder();
				//pgcob.add(DLMSCOSEMGlobals.ICID_CLOCK, ObisCode.fromString("0.0.1.0.0.255").getLN(),DLMSCOSEMGlobals.ATTR_CLOCK_TIME);
				pgcob.add(DLMSCOSEMGlobals.ICID_DATA, ObisCode.fromString("0.0.96.101.0.0").getLN(),DLMSCOSEMGlobals.ATTR_DATA_VALUE);
				pgcob.add(DLMSCOSEMGlobals.ICID_DATA, ObisCode.fromString("0.0.96.60.0.0").getLN(),DLMSCOSEMGlobals.ATTR_DATA_VALUE);
				for (int i=0;i<profileData.getChannelInfos().size();i++) {
					
					ObisCode obisCode = channelStatusFlags.get(Integer.valueOf(i));
					if (obisCode != null)
						pgcob.add(DLMSCOSEMGlobals.ICID_DATA, obisCode.getLN(),DLMSCOSEMGlobals.ATTR_DATA_VALUE);
					
					
					// if time integrals from start of measurements (origin)
					if (ParseUtils.isObisCodeCumulative(ObisCode.fromString(profileData.getChannel(i).getName())))
						pgcob.add(DLMSCOSEMGlobals.ICID_REGISTER, ObisCode.fromString(profileData.getChannel(i).getName()).getLN(),DLMSCOSEMGlobals.ATTR_REGISTER_VALUE);
					else
						pgcob.add(DLMSCOSEMGlobals.ICID_DEMAND_REGISTER, ObisCode.fromString(profileData.getChannel(i).getName()).getLN(),DLMSCOSEMGlobals.ATTR_DEMAND_REGISTER_LAST_AVERAGE);
					
				}
				profileGeneric.setCaptureObjectsAttr(pgcob.getCaptureObjectsArray());
		
				// ... set register scaler/units only for the interval data channels...
				Register register = null;
				for (int i=0;i<profileData.getChannelInfos().size();i++) {
					register = new Register(cosemAPDUBuilder,new ObjectReference(ObisCode.fromString(profileData.getChannel(i).getName()).getLN()));
					register.setScalerUnitAttr(profileData.getChannel(i).getUnit());
				}
				
				// set load profile object capture period attribute
				profileGeneric.setCapturePeriodAttr(new Unsigned32(profileInterval));
				
				// set load profile object buffer
				ProfileGenericBufferBuilder pgbb = new ProfileGenericBufferBuilder();
				for (int i=0;i<profileData.getIntervalDatas().size();i++) {
					pgbb.addInterval(profileData.getIntervalData(i),channelStatusFlags);
				}
				profileGeneric.setBufferAttr(pgbb.getBufferArray());
				
			} //if (profileData.getIntervalDatas().size()>0)
	
			if (profileData.getMeterEvents().size()>0) {
				// set event log capture objects
				ProfileGeneric profileGenericEventLog = new ProfileGeneric(cosemAPDUBuilder,new ObjectReference(ObisCode.fromString("0.0.99.98.0.255").getLN()));
				
				ProfileGenericCaptureObjectsBuilder pgcob = new ProfileGenericCaptureObjectsBuilder();
				pgcob.add(DLMSCOSEMGlobals.ICID_CLOCK, ObisCode.fromString("0.0.1.0.0.255").getLN(),DLMSCOSEMGlobals.ATTR_CLOCK_TIME);
				pgcob.add(DLMSCOSEMGlobals.ICID_DATA, ObisCode.fromString("0.0.96.70.0.0").getLN(),DLMSCOSEMGlobals.ATTR_DATA_VALUE); // manufacturer specific meter event object
				profileGenericEventLog.setCaptureObjectsAttr(pgcob.captureObjectsArray);
				
				// set event log buffer entries
				ProfileGenericBufferBuilder pgbb = new ProfileGenericBufferBuilder();
				for (int i=0;i<profileData.getMeterEvents().size();i++) {
					pgbb.addMeterEvent((MeterEvent)profileData.getMeterEvents().get(i));
				}
				profileGenericEventLog.setBufferAttr(pgbb.getBufferArray());
				
			} // if (profileData.getMeterEvents().size()>0)
			
		} // if (profileData != null)
		
		if (meterReadingData != null) {
			if (meterReadingData.getRegisterValues().size() > 0) {
				ProfileGeneric profileGenericRegisterValues = new ProfileGeneric(cosemAPDUBuilder,new ObjectReference(ObisCode.fromString("0.0.99.96.0.255").getLN()));
				
				ProfileGenericCaptureObjectsBuilder pgcob = new ProfileGenericCaptureObjectsBuilder();
				for (int i=0;i<meterReadingData.getRegisterValues().size();i++) {
					pgcob.add(DLMSCOSEMGlobals.ICID_DATA, ((RegisterValue)meterReadingData.getRegisterValues().get(i)).getObisCode().getLN(),0); // manufacturer specific registerValue object attribute 0
				}
				profileGenericRegisterValues.setCaptureObjectsAttr(pgcob.captureObjectsArray);
				
				ProfileGenericBufferBuilder pgbb = new ProfileGenericBufferBuilder();
			    for (int i=0;i<meterReadingData.getRegisterValues().size();i++) {
			    	pgbb.addRegisterValue((RegisterValue)meterReadingData.getRegisterValues().get(i));
			    }
			    profileGenericRegisterValues.setBufferAttr(pgbb.getBufferArray());
			}
			
		} // if (meterReadingData != null)
		
		byte[] compoundData = cosemAPDUBuilder.getAdaptorConnection().getCompoundData();
		

		if (DEBUG>=1) {
			System.out.println(ProtocolUtils.outputHexString(compoundData));
			CompoundDataParser o = new CompoundDataParser();
			o.parse(compoundData);
			List<CosemAPDU> apdus = o.getApdus();
			Iterator<CosemAPDU> it = apdus.iterator();
			while(it.hasNext()) {
				CosemAPDU apdu = it.next();
				System.out.println(apdu);
			}
		}
			
		return compoundData;
	}
	
	
	
	
//	private void testEnd2End() {
//		
//		ProfileData profileData = new ProfileData();
//		profileData.addChannel(new ChannelInfo(0,"1.1.1.27.0.255",Unit.get("kW")));
//		profileData.addChannel(new ChannelInfo(1,"1.1.2.27.0.255",Unit.get("kW")));
//		profileData.addChannel(new ChannelInfo(2,"1.1.3.27.0.255",Unit.get("kvar")));
//		profileData.addChannel(new ChannelInfo(3,"1.1.4.27.0.255",Unit.get("kvar")));
//		
//		List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
//		intervalValues.add(new IntervalValue(new BigDecimal("0.123456"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("456000"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("103.123456"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("4056.9987"),0,0));
//		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MINUTE, 15);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
//		profileData.addInterval(new IntervalData(cal.getTime(),2,0,0,intervalValues));
//		
//		intervalValues = new ArrayList<IntervalValue>();
//		intervalValues.add(new IntervalValue(new BigDecimal("56.78"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("6.987"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("0"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("60.987"),0,0));
//		cal.set(Calendar.MINUTE, 30);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
//		profileData.addInterval(new IntervalData(cal.getTime(),0,0,0,intervalValues));		
//		
//		intervalValues = new ArrayList<IntervalValue>();
//		intervalValues.add(new IntervalValue(new BigDecimal("456"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("9994.97"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("4056"),0,0));
//		intervalValues.add(new IntervalValue(new BigDecimal("90994.97"),0,0));
//		cal.set(Calendar.MINUTE, 45);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
//		profileData.addInterval(new IntervalData(cal.getTime(),1,0,0,intervalValues));
//		
//		profileData.addEvent(new MeterEvent(new Date(),MeterEvent.METER_ALARM,"test meter alarm"));
//		profileData.addEvent(new MeterEvent(new Date(new Date().getTime()+10000),MeterEvent.METER_ALARM,"test meter alarm2"));
//		
//		MeterReadingData meterReadingData = new MeterReadingData();
//		meterReadingData.add(new RegisterValue(ObisCode.fromString("1.1.1.8.0.255"),new Quantity(BigDecimal.valueOf(1234000),Unit.get("kWh")),new Date(new Date().getTime()-10000),new Date(new Date().getTime()-20000),new Date(new Date().getTime()-30000),new Date(new Date().getTime()-40000),100,"test register with id 100"));
//		meterReadingData.add(new RegisterValue(ObisCode.fromString("1.1.2.8.0.255"),new Quantity(BigDecimal.valueOf(12345.456),Unit.get("kWh")),new Date(new Date().getTime()-10000),new Date(new Date().getTime()-20000),new Date(new Date().getTime()-30000),new Date(new Date().getTime()-40000),101,"test register with id 101"));
//		meterReadingData.add(new RegisterValue(ObisCode.fromString("1.1.3.8.0.255"),new Quantity(BigDecimal.valueOf(0),Unit.get("kvarh")),new Date(new Date().getTime()-10000),new Date(new Date().getTime()-20000),new Date(new Date().getTime()-30000),new Date(new Date().getTime()-40000),102,"test register with id 102"));
//		meterReadingData.add(new RegisterValue(ObisCode.fromString("1.1.4.8.0.255"),new Quantity(BigDecimal.valueOf(0.1235467000),Unit.get("kvarh")),new Date(new Date().getTime()-10000),new Date(new Date().getTime()-20000),new Date(new Date().getTime()-30000),new Date(new Date().getTime()-40000),103,"test register with id 103"));
//		
//		try {
//			byte[] compoundData = buildCompoundData(meterReadingData,profileData,900, 1234, null);
//			
//			/// send over https ///
//			
//			CompoundDataParser o1 = new CompoundDataParser();
//			o1.parse(compoundData);
//			CosemAPDUParser o2 = new CosemAPDUParser();
//			o2.parse(o1.getApdus());
//			System.out.println(o2.getDate()); 
//			System.out.println(o2.getProfileInterval()); 
//			System.out.println(o2.getProfileData());
//			System.out.println(o2.getMeterReadingData());
//			
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	} // private void testEnd2End()
//	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		CompoundDataBuilder o = new CompoundDataBuilder();
//		o.testEnd2End();
//	}

}
