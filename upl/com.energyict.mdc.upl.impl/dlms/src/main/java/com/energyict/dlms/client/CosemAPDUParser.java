package com.energyict.dlms.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.custom.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class CosemAPDUParser {

	final int DEBUG=0;
	
	private Map<ObisCode,LoadProfile> loadProfiles=null;

	private MeterReadingData meterReadingData=null;
	private Date previousEndTime=null;
	private Date date=null;
	private AcknowledgeCustomCosem acknowledgeCustomCosem=null;
	private CommandCustomCosem commandCustomCosem=null;
	private DatabaseIDCustomCosem databaseIDCustomCosem=null;
	private DeviceIDCustomCosem deviceIDCustomCosem=null;
	private DeviceIdentification deviceIdentification=null;
	private List<ObisCode> eventLogCapturedObjects = null;
	private List<ObisCode> meterReadingsCapturedObjects = null;
	private List<DeviceMessageCustomCosem> deviceMessageCustomCosems = null;
	private DeviceCustomCosem deviceCustomCosem = null;
	private DeployDataCustomCosem deployDataCustomCosem=null;
	private List<DeviceChannelName> deviceChannelNames=null;
	private LookupResourcesCustomCosem lookupResourcesCustomCosem=null;
	private TaskStatusCustomCosem taskStatusCustomCosem=null;
	private String ipAddress="";
	
	
	public CosemAPDUParser() {
	}

	private void reset() {
	
		loadProfiles=new HashMap<ObisCode,LoadProfile>();
		
		meterReadingData=null;
		date=null;
		deviceIdentification=null;
		acknowledgeCustomCosem=null;
		commandCustomCosem=null;
		deviceMessageCustomCosems = null;
		deviceCustomCosem = null;
		deployDataCustomCosem = null;
		deviceChannelNames = null;
		lookupResourcesCustomCosem = null;
		taskStatusCustomCosem = null;
		
		// local
		previousEndTime=null;
		
		databaseIDCustomCosem=null;
		deviceIDCustomCosem=null;
		
		eventLogCapturedObjects = null;
		meterReadingsCapturedObjects = null;
		
	}
	
    public void parse(List<CosemAPDU> apdus) throws IOException {
    	
    	reset();
    	
    	Iterator<CosemAPDU> it = apdus.iterator();
    	
		if (apdus.isEmpty())
			throw new IOException("parse, nothing to parse, no CosemAPDUs in parse list...");
    	
		identifyDevice(it);
		
//		if (apdus.size()==1) {
//			// return ACK, this is just a status check if the meter is found...
//		}
//		else
			// if there are more APDUs, parse them
			parseContent(it);
    }
    
	private void identifyDevice(Iterator<CosemAPDU> it) throws IOException {
		
		// search for a starting apdu with device identification
		while(it.hasNext()) {
			CosemAPDU apdu = it.next();
			if (DEBUG>=2)
				System.out.print("identifyDevice "+apdu);
			
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

	private boolean isChannelInfoObject(ObisCode obisCode) {
		return ((obisCode.getA()==0) && (obisCode.getB()>=1) && (obisCode.getB()<=128) && (obisCode.getC()==96) && (obisCode.getD()==62) && (obisCode.getE()==0) && (obisCode.getF()==0));
			
	}
	
	private boolean isDeviceChannelNameObject(ObisCode obisCode) {
		return ((obisCode.getA()==0) && (obisCode.getB()==0) && (obisCode.getC()==96) && (obisCode.getD()==121) && (obisCode.getF()==0));
			
	}
	
	// 64 load profiles
	private boolean isLoadProfileObisCode(ObisCode obisCode) {
		return ((obisCode.getA()==0) && (obisCode.getB()==0) && (obisCode.getC()==99) && (obisCode.getD()>=1) && (obisCode.getD()<=64) && (obisCode.getE()==0) && (obisCode.getF()==255));
			
	}
	
	private void parseContent(Iterator<CosemAPDU> it) throws IOException {
		
		LoadProfile tempLoadProfile=null;
		List<MeterEvent> tempMeterEvents=null;
		List<ObisCode> loadProfileCapturedObjects = new ArrayList<ObisCode>();
		
		
		while(it.hasNext()) {
			CosemAPDU apdu = it.next();
			
			if ((apdu.getCosemAttributeDescriptor().getObis().equals(DatabaseIDCustomCosem.getObisCode())) ||
				(apdu.getCosemAttributeDescriptor().getObis().equals(DeviceIDCustomCosem.getObisCode()))) {
				// do not remove apdu and break loop!
				break;
			}
			
			if (DEBUG>=2)
				System.out.print("parseContent "+apdu);
			
			if (isDeviceChannelNameObject(apdu.getCosemAttributeDescriptor().getObis())) {
				if (deviceChannelNames==null)
					deviceChannelNames = new ArrayList();
				Structure s = apdu.getDataType().getStructure();
				
				DeviceChannelName dcn = new DeviceChannelName(apdu.getCosemAttributeDescriptor().getObis().getE(),s.getDataType(0).getOctetString().stringValue());
				deviceChannelNames.add(dcn);
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(TaskStatusCustomCosem.getObisCode())) {
				taskStatusCustomCosem = new TaskStatusCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(LookupResourcesCustomCosem.getObisCode())) {
				lookupResourcesCustomCosem = new LookupResourcesCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(DeviceMessageCustomCosem.getObisCode())) {
				if (deviceMessageCustomCosems==null)
					deviceMessageCustomCosems = new ArrayList();
				deviceMessageCustomCosems.add(new DeviceMessageCustomCosem(apdu.getDataType()));
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(DeployDataCustomCosem.getObisCode())) {
				deployDataCustomCosem = new DeployDataCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(DeviceCustomCosem.getObisCode())) {
				deviceCustomCosem = new DeviceCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ClockCustomCosem.getObisCode())) {
				date = new ClockCustomCosem(apdu.getDataType()).getDate();
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(AcknowledgeCustomCosem.getObisCode())) {
				acknowledgeCustomCosem = new AcknowledgeCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(CommandCustomCosem.getObisCode())) {
				commandCustomCosem = new CommandCustomCosem(apdu.getDataType());
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(Clock.getObisCode())) {
				date = new DateTime(apdu.getDataType().getOctetString()).getValue().getTime();
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(IPv4Setup.getObisCode())) {
				if (apdu.getCosemAttributeDescriptor().getAttributeId() == 3) { // IP_Address
					long ipAddressLong = apdu.getDataType().getUnsigned32().longValue();
					ipAddress = ""+((ipAddressLong>>24)&0xff)+"."+
										  ((ipAddressLong>>16)&0xff)+"."+  
										  ((ipAddressLong>>8)&0xff)+"."+  
										  ((ipAddressLong)&0xff);  
				}
			}
			else if (isLoadProfileObisCode(apdu.getCosemAttributeDescriptor().getObis())) {
				// We should create a hashmap that stores all load profiles...
				if (DEBUG>=1) System.out.println("Received "+apdu.getCosemAttributeDescriptor().getObis().toString());
				
				tempLoadProfile = loadProfiles.get(apdu.getCosemAttributeDescriptor().getObis());
				if (tempLoadProfile == null) {
					loadProfileCapturedObjects = new ArrayList<ObisCode>();
					tempLoadProfile = new LoadProfile(new ProfileData(),-1,apdu.getCosemAttributeDescriptor().getObis());
					loadProfiles.put(apdu.getCosemAttributeDescriptor().getObis(),tempLoadProfile);
					if (tempMeterEvents != null)
						tempLoadProfile.getProfileData().setMeterEvents(tempMeterEvents);
					tempMeterEvents=null;
				}
				buildIntervalData(apdu,tempLoadProfile,loadProfileCapturedObjects);
			} // load profile
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.99.98.0.255"))) { // meter event log
				if (DEBUG>=1) System.out.println("KV_DEBUG> Received 0.0.99.98.0.255");
				tempMeterEvents = buildEventLog(apdu);
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.99.96.0.255"))) { // meterreadings
				if (meterReadingData==null)
					meterReadingData = new MeterReadingData();
				buildMeterReadingData(apdu);
			}
			else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.99.95.0.255"))) { // messages
				if (deviceMessageCustomCosems==null)
					deviceMessageCustomCosems = new ArrayList();
				buildMessages(apdu);
			}
			else if (isChannelInfoObject(apdu.getCosemAttributeDescriptor().getObis())) {
				if ((apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_DATA_VALUE) &&
					(apdu.getCosemAttributeDescriptor().getClassId() == DLMSCOSEMGlobals.ICID_DATA)) {	
					// search for a channelInfo to adjust unit/scaler and device channel ordinal mapping...
					findAndAdjustChannelInfoFromChannelInfo(apdu,tempLoadProfile);
				}
			}
			else {
				if ((apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_REGISTER_SCALER) &&
					(apdu.getCosemAttributeDescriptor().getClassId() == DLMSCOSEMGlobals.ICID_REGISTER)) {	
					// search for a channelInfo to adjust unit/scaler...
					findAndAdjustChannelInfoFromDeviceRegister(apdu,tempLoadProfile);
				}
			}
			
			it.remove();
			
		} // while(it.hasNext())
		
	} // private void parseContent(Iterator<CosemAPDU> it) throws IOException {
	
	private void buildMessages(CosemAPDU apdu) throws IOException {
		if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
			// absorb
		}
		else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
			Array buffer = apdu.getDataType().getArray();
			for (int i=0;i<buffer.nrOfDataTypes();i++) {
				deviceMessageCustomCosems.add(new DeviceMessageCustomCosem(buffer.getDataType(i)));
			}
		}		
	} // private void buildMessages(CosemAPDU apdu) throws IOException
	
	private void buildMeterReadingData(CosemAPDU apdu) throws IOException {
		
		if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
			// absorb
			// contains an array with all register descriptions in the buffer 
			
		}
		else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
			// array only containing 1 element with  a structure of all registersz in capture objects
			Array bufferArray = apdu.getDataType().getArray();
			Structure bufferStructure = bufferArray.getDataType(0).getStructure();
			for (int i=0;i<bufferStructure.nrOfDataTypes();i++) {
				
				Structure structure = bufferStructure.getDataType(i).getStructure();
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
				meterReadingData.add(registerValue);
			}
		}
	} // private void buildMeterReadingData(CosemAPDU apdu) throws IOException
	
	private List<MeterEvent> buildEventLog(CosemAPDU apdu) throws IOException {
		if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
			// absorb
		}
		else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
			Array buffer = apdu.getDataType().getArray();
			List<MeterEvent> meterEvents = new ArrayList();
			int trickyMilliSeconds=1000;
			for (int i=0;i<buffer.nrOfDataTypes();i++) {
				Structure entry = buffer.getDataType(i).getStructure();
				Date date = buildDate(entry.getDataType(0));
				Structure eventEntry = entry.getDataType(1).getStructure();
				int eiCode = eventEntry.getDataType(0).intValue();
				int protocolCode = eventEntry.getDataType(1).intValue();
				String message = eventEntry.getDataType(2).getOctetString().stringValue();
				if ((eiCode==MeterEvent.CONFIGURATIONCHANGE) ||
					(eiCode==MeterEvent.OTHER))	{
					date = new Date(date.getTime()+trickyMilliSeconds);
					trickyMilliSeconds+=1000;
				}
				
				meterEvents.add(new MeterEvent(date,eiCode,protocolCode,message));
			}
			return meterEvents;
		}
		
		return null;
	} // private void buildEventLog(CosemAPDU apdu) throws IOException
	
	private void buildIntervalData(CosemAPDU apdu,LoadProfile tempLoadProfile,List<ObisCode> loadProfileCapturedObjects) throws IOException {
		if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS) {
			tempLoadProfile.getProfileData().setChannelInfos(buildChannelInfos(apdu,loadProfileCapturedObjects));
		}
		else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD) {
			tempLoadProfile.setIntervalInSeconds(apdu.getDataType().intValue());
		}
		else if (apdu.getCosemAttributeDescriptor().getAttributeId() == DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_BUFFER) {
			Array buffer = apdu.getDataType().getArray();
//			if (channelInfos == null) {
//				channelInfos = buildChannelInfos(buffer.nrOfDataTypes()-2);
//				profileData.setChannelInfos(channelInfos);
//			}
			
			List<IntervalData> intervalDatas = new ArrayList();
			
			for (int i=0;i<buffer.nrOfDataTypes();i++) {
				Structure entry = buffer.getDataType(i).getStructure();
				Date endTime=null;
				int eiStatus=0;
				List<IntervalValue> intervalValues = new ArrayList();
				if (loadProfileCapturedObjects.size()==0) { // in case of short format...
					List<ChannelInfo> channelInfos = tempLoadProfile.getProfileData().getChannelInfos();
					if ((channelInfos == null) || (channelInfos.size()==0)) {
						channelInfos = buildChannelInfos(entry.nrOfDataTypes()-2);
						tempLoadProfile.getProfileData().setChannelInfos(channelInfos);
					}
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
//					for (int entryIndex=0;entryIndex<entry.nrOfDataTypes();entryIndex++) {
					for (int entryIndex=0;entryIndex<loadProfileCapturedObjects.size();entryIndex++) {
						// get captured object obis code 
						
						ObisCode obisCode = loadProfileCapturedObjects.get(entryIndex);
						
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
						
					} // for (int entryIndex=0;i<loadProfileCapturedObjects.size();entryIndex++)
					
					List<IntervalValue> intervalValues2 = new ArrayList();
					for(int channelId=0;channelId<intervalValues.size();channelId++) {
						Integer channelStatus = channelsStatus.get(Integer.valueOf(channelId));
						if (channelStatus != null)
							intervalValues2.add(new IntervalValue(intervalValues.get(channelId).getNumber(),0,channelStatus.intValue()));
						else
							intervalValues2.add(new IntervalValue(intervalValues.get(channelId).getNumber(),0,0));
					}
					
					if (endTime==null) {
						endTime = new Date(previousEndTime.getTime()+tempLoadProfile.getIntervalInSeconds()*1000);
					}
					intervalDatas.add(new IntervalData(endTime,eiStatus,0,0,intervalValues2));
					previousEndTime = endTime;
				}
			}
			tempLoadProfile.getProfileData().setIntervalDatas(intervalDatas);
		}
		
	} // private void buildIntervalData(CosemAPDU apdu,List<ChannelInfo> channelInfos) throws IOException
	
	private Date buildDate(AbstractDataType dataType) throws IOException {
		Date date;
		if (dataType.isOctetString())
			date = new DateTime(dataType.getOctetString()).getValue().getTime();
		else
			date = AXDRDate.decode(dataType);
		return date;
	} // private Date buildDate(AbstractDataType dataType) throws IOException
	
	private void findAndAdjustChannelInfoFromDeviceRegister(CosemAPDU apdu,LoadProfile tempLoadProfile) {
		if ((tempLoadProfile.getProfileData().getChannelInfos() != null) && (tempLoadProfile.getProfileData().getChannelInfos().size() > 0)) {
			List<ChannelInfo> channelInfos = tempLoadProfile.getProfileData().getChannelInfos();
			
			for(int i=0;i<tempLoadProfile.getProfileData().getChannelInfos().size();i++) {
				if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString(channelInfos.get(i).getName()))) {
					ScalerUnit scalerUnit = new ScalerUnit(apdu.getDataType());
					channelInfos.get(i).setUnit(scalerUnit.getUnit());
				}
			}
		}
	} // private void findAndAdjustChannelInfoFromDeviceRegister(CosemAPDU apdu,List<ChannelInfo> channelInfos)

	private void findAndAdjustChannelInfoFromChannelInfo(CosemAPDU apdu,LoadProfile tempLoadProfile) {
		if ((tempLoadProfile.getProfileData().getChannelInfos() != null) && (tempLoadProfile.getProfileData().getChannelInfos().size() > 0)) {
			List<ChannelInfo> channelInfos = tempLoadProfile.getProfileData().getChannelInfos();
			for(int i=0;i<channelInfos.size();i++) {
				if ((apdu.getCosemAttributeDescriptor().getObis().getB()-1) == i) {


					Structure structure = apdu.getDataType().getStructure();
					channelInfos.get(i).setName(AXDRString.decode(structure.getNextDataType()));
					channelInfos.get(i).setChannelId(structure.getNextDataType().intValue());
					channelInfos.get(i).setUnit(AXDRUnit.decode(structure.getNextDataType()));
					String cummulativeWrapArountValue = AXDRString.decode(structure.getNextDataType());
					channelInfos.get(i).setCumulativeWrapValue(cummulativeWrapArountValue==null?null:new BigDecimal(cummulativeWrapArountValue));
					//System.out.println("KV_DEBUG> channelInfo "+i+", "+apdu.getCosemAttributeDescriptor().getObis().toString()+", "+channelInfos.get(i).getName()+", "+channelInfos.get(i).getChannelId()+", "+channelInfos.get(i).getUnit()+", "+channelInfos.get(i).getCumulativeWrapValue());								
				}
			}
		}
	} // private void findAndAdjustChannelInfoFromChannelInfo(CosemAPDU apdu,List<ChannelInfo> channelInfos)
	
	private List<ChannelInfo> buildChannelInfos(int nrOfChannels) {
		List<ChannelInfo> channelInfos = new ArrayList();
		for(int channelId=0;channelId<nrOfChannels;channelId++) {
			ChannelInfo chi = new ChannelInfo(channelId,"channel "+channelId,Unit.get(""));
			channelInfos.add(chi);
		}
		return channelInfos;
	} // private List<ChannelInfo> buildChannelInfos(int nrOfChannels)
	
	private List<ChannelInfo> buildChannelInfos(CosemAPDU apdu,List<ObisCode> loadProfileCapturedObjects) {
		List<ChannelInfo> channelInfos = new ArrayList();
		int channelId=0;
		Array captureObjects = apdu.getDataType().getArray();
		for(int i=0;i<captureObjects.nrOfDataTypes();i++) {
		
			AbstractDataType dataType = captureObjects.getDataType(i);
			if (dataType==null)
				break;
			int classId = dataType.getStructure().getDataType(0).getUnsigned16().intValue();
			ObisCode obisCode = ObisCode.fromByteArray(dataType.getStructure().getDataType(1).getOctetString().getOctetStr());
			
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
	} // private List<ChannelInfo> buildChannelInfos(CosemAPDU apdu)
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public LoadProfile getLoadProfile(int profileIndex) {
		return loadProfiles.get(ObisCode.fromString("0.0.99."+(profileIndex+1)+".0.255"));
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

	public DeviceCustomCosem getDeviceCustomCosem() {
		return deviceCustomCosem;
	}

	public CommandCustomCosem getCommandCustomCosem() {
		return commandCustomCosem;
	}

	public DeployDataCustomCosem getDeployDataCustomCosem() {
		return deployDataCustomCosem;
	}

	public List<DeviceChannelName> getDeviceChannelNames() {
		return deviceChannelNames;
	}

	public LookupResourcesCustomCosem getLookupResourcesCustomCosem() {
		return lookupResourcesCustomCosem;
	}

	public TaskStatusCustomCosem getTaskStatusCustomCosem() {
		return taskStatusCustomCosem;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public Map<ObisCode, LoadProfile> getLoadProfiles() {
		return loadProfiles;
	}

}
