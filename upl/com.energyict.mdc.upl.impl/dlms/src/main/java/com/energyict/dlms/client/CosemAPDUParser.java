package com.energyict.dlms.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.custom.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class CosemAPDUParser {

	final int DEBUG=1;
	
	private ProfileData profileData=null;
	private MeterReadingData meterReadingData=null;
	private int profileInterval=-1;
	private Date previousEndTime=null;
	private Date date=null;
	private AcknowledgeCustomCosem acknowledgeCustomCosem=null;
	private DatabaseIDCustomCosem databaseIDCustomCosem=null;
	private DeviceIDCustomCosem deviceIDCustomCosem=null;
	private DeviceIdentification deviceIdentification=null;
	private List<ObisCode> loadProfileCapturedObjects = null;
	private List<ObisCode> eventLogCapturedObjects = null;
	private List<ObisCode> meterReadingsCapturedObjects = null;
	private List<DeviceMessageCustomCosem> deviceMessageCustomCosems = null;
	
	public CosemAPDUParser() {
	}

	private void reset() {
	
		profileData=null;
		meterReadingData=null;
		profileInterval=-1;
		date=null;
		deviceIdentification=null;
		acknowledgeCustomCosem=null;
		deviceMessageCustomCosems = null;
		
		// local
		previousEndTime=null;
		
		databaseIDCustomCosem=null;
		deviceIDCustomCosem=null;
		
		loadProfileCapturedObjects = null;
		eventLogCapturedObjects = null;
		meterReadingsCapturedObjects = null;
		
	}
	
    public void parse(List<CosemAPDU> apdus) throws IOException {
    	
    	reset();
    	
    	Iterator<CosemAPDU> it = apdus.iterator();
    	
		if (apdus.isEmpty())
			throw new IOException("parse, nothing to parse, no CosemAPDUs in parse list...");
    	
		identifyDevice(it);
		
		if (apdus.size()==1) {
			// return ACK, this is just a status check if the meter is found...
		}
		else
			// if there are more APDUs, parse them
			parseContent(it);
    }
    
	private void identifyDevice(Iterator<CosemAPDU> it) throws IOException {
		
		// search for a starting apdu with device identification
		while(it.hasNext()) {
			CosemAPDU apdu = it.next();
			if (DEBUG>=1)
				System.out.println(apdu);
			
			if (apdu.getCosemAttributeDescriptor().getObis().equals(DatabaseIDCustomCosem.getObisCode())) {
				// device referenced by database id
				databaseIDCustomCosem = new DatabaseIDCustomCosem(apdu.getDataType());
				it.remove();
				break;
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(DeviceIDCustomCosem.getObisCode())) {
				// device referenced by serialnumber
				deviceIDCustomCosem = new DeviceIDCustomCosem(apdu.getDataType());
				it.remove();
				break;
			}
//			else throw new IOException("First CosemAPDU in a POST must have the 0.0.96.50.0.0 database ID or 0.0.96.1.0.255 serial number of the device!");
			
		} // while(it.hasNext())
		
		if (deviceIdentification==null) {
			if ((databaseIDCustomCosem != null) || (deviceIDCustomCosem != null))
				deviceIdentification = new DeviceIdentification(deviceIDCustomCosem==null?null:deviceIDCustomCosem.getSerialID(),databaseIDCustomCosem==null?0:databaseIDCustomCosem.getDatabaseID());
			else {
				throw new IOException("No CosemAPDU with the 0.0.96.50.0.0 database ID or 0.0.96.1.0.255 serial number found in the received APDU list!");
			}
		}
	}
	
	private void parseContent(Iterator<CosemAPDU> it) throws IOException {
		List<ChannelInfo> channelInfos=null;
		while(it.hasNext()) {
			CosemAPDU apdu = it.next();
			if (DEBUG>=1)
				System.out.println(apdu);
			
			
			if ((apdu.getCosemAttributeDescriptor().getObis().equals(DatabaseIDCustomCosem.getObisCode())) ||
				(apdu.getCosemAttributeDescriptor().getObis().equals(DeviceIDCustomCosem.getObisCode()))) {
				// do not remove apdu and break loop!
				break;
			}
			
			if (apdu.getCosemAttributeDescriptor().getObis().equals(DeviceMessageCustomCosem.getObisCode())) {
				if (deviceMessageCustomCosems==null)
					deviceMessageCustomCosems = new ArrayList();
				deviceMessageCustomCosems.add(new DeviceMessageCustomCosem(apdu.getDataType()));
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ClockCustomCosem.getObisCode())) {
				date = new ClockCustomCosem(apdu.getDataType()).getDate();
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(AcknowledgeCustomCosem.getObisCode())) {
				acknowledgeCustomCosem = new AcknowledgeCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(Clock.getObisCode())) {
				date = new DateTime(apdu.getDataType().getOctetString()).getValue().getTime();
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("1.0.99.1.0.255"))) {
				if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
					channelInfos = buildChannelInfos(apdu);
					if (profileData==null)
						profileData = new ProfileData();
					profileData.setChannelInfos(channelInfos);
				}
				else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD) {
					profileInterval = apdu.getDataType().intValue();
				}
				else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
					Array buffer = apdu.getDataType().getArray();
					if (channelInfos == null) {
						channelInfos = buildChannelInfos(buffer.nrOfDataTypes()-2);
						if (profileData==null)
							profileData = new ProfileData();
						profileData.setChannelInfos(channelInfos);
					}
					
					List<IntervalData> intervalDatas = new ArrayList();
					
					for (int i=0;i<buffer.nrOfDataTypes();i++) {
						Structure entry = buffer.getDataType(i).getStructure();
						Date endTime=null;
						int eiStatus=0;
						List<IntervalValue> intervalValues = new ArrayList();
						if (loadProfileCapturedObjects==null) { // in case of short format...
							endTime = buildDate(entry.getDataType(0));
							eiStatus = entry.getDataType(1).intValue();
							for (int channelId=0;channelId<(entry.nrOfDataTypes()-2);channelId++) {
								NumberFormat o = new NumberFormat(entry.getDataType(channelId+2));
								intervalValues.add(new IntervalValue(o.toBigDecimal(),0,0));
							}
							intervalDatas.add(new IntervalData(endTime,eiStatus,0,0,intervalValues));
						}
						else {
							Map<Integer,Integer> channelsStatus=null;
							for (int entryIndex=0;entryIndex<entry.nrOfDataTypes();entryIndex++) {
								// get captured object obis code 
								ObisCode obisCode = loadProfileCapturedObjects.get(Integer.valueOf(entryIndex));
								if (channelsStatus == null) 
									channelsStatus = new HashMap();
								
								// is channel value?
								if (!ParseUtils.isObisCodeAbstract(obisCode)) {
									// channel value
									NumberFormat o = new NumberFormat(entry.getDataType(entryIndex));
									intervalValues.add(new IntervalValue(o.toBigDecimal(),0,0));
								}
								// is custom cosem clock object? 0.0.96.101.0.0 OR cosem clock object? 0.0.1.0.0.255
								else if ((obisCode.equals(ClockCustomCosem.getObisCode())) || (obisCode.equals(Clock.getObisCode()))) { 
									endTime = buildDate(entry.getDataType(entryIndex));
								}
								else if (obisCode.equals(IntervalStatusCustomCosem.getObisCode())) { // global interval status flags
									eiStatus = entry.getDataType(entryIndex).intValue();
								}
								else if (ParseUtils.isObisCodeChannelIntervalStatus(obisCode)) { // channel specific interval status flags
									channelsStatus.put(Integer.valueOf(obisCode.getE()-1),Integer.valueOf(entry.getDataType(entryIndex).intValue()));
								}
								
							} // for (int entryIndex=0;i<entry.nrOfDataTypes();entryIndex++)
							
							List<IntervalValue> intervalValues2 = new ArrayList();
							for(int channelId=0;channelId<intervalValues.size();channelId++) {
								Integer channelStatus = channelsStatus.get(Integer.valueOf(channelId));
								if (channelStatus != null)
									intervalValues2.add(new IntervalValue(intervalValues.get(channelId).getNumber(),0,channelStatus.intValue()));
								else
									intervalValues2.add(new IntervalValue(intervalValues.get(channelId).getNumber(),0,0));
							}
							
							if (endTime==null) {
								endTime = new Date(previousEndTime.getTime()+profileInterval*1000);
							}
							intervalDatas.add(new IntervalData(endTime,eiStatus,0,0,intervalValues2));
							previousEndTime = endTime;
						}
					}
					profileData.setIntervalDatas(intervalDatas);
				}
			} // load profile
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("1.0.99.98.0.255"))) {
				if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
					// absorb
				}
				else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
					Array buffer = apdu.getDataType().getArray();
					List<MeterEvent> meterEvents = new ArrayList();
					for (int i=0;i<buffer.nrOfDataTypes();i++) {
						Structure entry = buffer.getDataType(i).getStructure();
						Date date = buildDate(entry.getDataType(0));
						Structure eventEntry = entry.getDataType(1).getStructure();
						int eiCode = eventEntry.getDataType(0).intValue();
						int protocolCode = eventEntry.getDataType(1).intValue();
						String message = eventEntry.getDataType(2).getOctetString().stringValue();
						meterEvents.add(new MeterEvent(date,eiCode,protocolCode,message));
					}
					profileData.setMeterEvents(meterEvents);
				}
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("1.0.99.96.0.255"))) { // meterreadings
				if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
					// absorb
					
				}
				else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
					Array buffer = apdu.getDataType().getArray();
					for (int i=0;i<buffer.nrOfDataTypes();i++) {
						
						Structure structure = buffer.getDataType(i).getStructure();
						// logical name
						ObisCode obisCode = ObisCode.fromByteArray(structure.getDataType(0).getOctetString().getOctetStr());
						
						// value
						Structure entry = structure.getDataType(1).getStructure();
						int rtuRegisterId = entry.getDataType(0).getUnsigned32().intValue();
						Number number = new NumberFormat(entry.getDataType(1)).toBigDecimal();
						Unit unit = new ScalerUnit(entry.getDataType(2).getStructure()).getUnit();
						
						Date readTime = buildDate(entry.getDataType(3));
						Date fromTime = buildDate(entry.getDataType(4));
						Date toTime = buildDate(entry.getDataType(5));
						Date eventTime = buildDate(entry.getDataType(6));
						
						String text = entry.getDataType(7).getOctetString().stringValue();
						
						RegisterValue registerValue = new RegisterValue(obisCode,new Quantity(number,unit),eventTime,fromTime,toTime,readTime,rtuRegisterId,text);
						if (meterReadingData==null)
							meterReadingData = new MeterReadingData();
						meterReadingData.add(registerValue);
					}
				}
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("1.0.99.95.0.255"))) { // messages
				if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
					// absorb
				}
				else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
					Array buffer = apdu.getDataType().getArray();
					for (int i=0;i<buffer.nrOfDataTypes();i++) {
						if (deviceMessageCustomCosems==null)
							deviceMessageCustomCosems = new ArrayList();
						deviceMessageCustomCosems.add(new DeviceMessageCustomCosem(buffer.getDataType(i)));
					}
				}
			}
			else {
				if ((apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_REGISTER_SCALER) &&
					(apdu.getCosemAttributeDescriptor().getClassId() == DLMSCOSEMGlobals.ICID_REGISTER)) {	
					// search for a channelInfo to adjust unit/scaler...
					findAndAdjustChannelInfoUnit(apdu,channelInfos);
				}
			}
			
			it.remove();
			
		} // while(it.hasNext())
		
	} // private void parseContent(Iterator<CosemAPDU> it) throws IOException {
	
	private Date buildDate(AbstractDataType dataType) throws IOException {
		Date date;
		if (dataType.isOctetString())
			date = new DateTime(dataType.getOctetString()).getValue().getTime();
		else
			date = new Date(dataType.longValue()*1000);
		return date;
	}
	
	private void findAndAdjustChannelInfoUnit(CosemAPDU apdu,List<ChannelInfo> channelInfos) {
		if (channelInfos != null) {
			for(int i=0;i<channelInfos.size();i++) {
				if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString(channelInfos.get(i).getName()))) {
					ScalerUnit scalerUnit = new ScalerUnit(apdu.getDataType());
					channelInfos.get(i).setUnit(scalerUnit.getUnit());
				}
			}
		}
	}

	private List<ChannelInfo> buildChannelInfos(int nrOfChannels) {
		List<ChannelInfo> channelInfos = new ArrayList();
		for(int channelId=0;channelId<nrOfChannels;channelId++) {
			ChannelInfo chi = new ChannelInfo(channelId,"channel "+channelId,Unit.get(""));
			channelInfos.add(chi);
		}
		return channelInfos;
	}
	
	private List<ChannelInfo> buildChannelInfos(CosemAPDU apdu) {
		List<ChannelInfo> channelInfos = new ArrayList();
		int channelId=0;
		Array captureObjects = apdu.getDataType().getArray();
		for(int i=0;i<captureObjects.nrOfDataTypes();i++) {
			
			AbstractDataType dataType = captureObjects.getDataType(i);
			if (dataType==null)
				break;
			int classId = dataType.getStructure().getDataType(0).getUnsigned16().intValue();
			ObisCode obisCode = ObisCode.fromByteArray(dataType.getStructure().getDataType(1).getOctetString().getOctetStr());
			
			if (loadProfileCapturedObjects==null)
				loadProfileCapturedObjects = new ArrayList();
			loadProfileCapturedObjects.add(obisCode);
			
			int attr = dataType.getStructure().getDataType(2).getInteger8().intValue();
			if (!ParseUtils.isObisCodeAbstract(obisCode)) {
				ChannelInfo chi = new ChannelInfo(channelId++,obisCode.toString(),Unit.get(""));
				if (ParseUtils.isObisCodeCumulative(obisCode)) {
					// This is a cumulative value... We should have a possibility to set the wraparound in EIServer
					// Setting the CumulativeWrapValue in ChannelInfo only indicates that the value is a cumulative value
					chi.setCumulativeWrapValue(new BigDecimal(2^32)); 
				}
				channelInfos.add(chi);
			}
		}
		return channelInfos;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ProfileData getProfileData() {
		return profileData;
	}

	public int getProfileInterval() {
		return profileInterval;
	}

	public Date getDate() {
		return date;
	}

	public MeterReadingData getMeterReadingData() {
		return meterReadingData;
	}

	public AcknowledgeCustomCosem getAcknowledgeCustomCosem() {
		return acknowledgeCustomCosem;
	}

	public List<DeviceMessageCustomCosem> getDeviceMessageCustomCosems() {
		return deviceMessageCustomCosems;
	}
	public DeviceIdentification getDeviceIdentification() {
		return deviceIdentification;
	}

}
