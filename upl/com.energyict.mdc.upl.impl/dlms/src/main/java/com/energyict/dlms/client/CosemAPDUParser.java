package com.energyict.dlms.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class CosemAPDUParser {

	final int DEBUG=1;
	final boolean DBASE_ACCESS=false;
	
	ProfileData profileData=null;
	MeterReadingData meterReadingData=null;
	int profileInterval;
	Date date=null;
	
	public CosemAPDUParser() {
	}

    public MeteringWarehouse getMeteringWarehouse() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
    }
	
	
    public void parse(List<CosemAPDU> apdus) throws IOException {
    	int index=0;

		if (apdus.isEmpty())
			throw new IOException("parse, nothing to parse, no CosemAPDUs in parse list...");
    	
		Rtu device=null;
		if (DBASE_ACCESS) {
			device = identifyDevice(apdus.get(index++));
			if (DEBUG>=1)
				System.out.println(device);
		}
		else index++; // skip identification
		
		if (apdus.size()==1) {
			// return ACK, this is just a status check if the meter is found...
		}
		else
			// if there are more APDUs, parse them
			parseContent(device,apdus,index);
		
		
		if (profileData != null) {
			if (DBASE_ACCESS) {
				try {
					device.store(profileData);
				}
				catch(BusinessException e) {
					e.printStackTrace();
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
	private Rtu identifyDevice(CosemAPDU apdu) throws IOException {
		if (DEBUG>=1)
			System.out.println(apdu);
		
		if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.96.50.0.0"))) {
			// device referenced by database id
			int dbaseId = apdu.getDataType().getInteger32().intValue();
			Rtu device = getMeteringWarehouse().getRtuFactory().find(dbaseId);
			if (device==null)
				throw new IOException("No Rtu device found with database ID "+dbaseId+"! Cannot continue!");
			else
				return device;
		}
		else if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.96.1.0.255"))) {
			// device referenced by serialnumber
			String serialNumber = apdu.getDataType().getOctetString().stringValue();
			List<Rtu> devices = getMeteringWarehouse().getRtuFactory().findBySerialNumber(serialNumber);
			if (devices.size()==0)
				throw new IOException("No rtu device with the same serialnumber "+serialNumber+" found! Cannot continue!");
			else if (devices.size()>1)
				throw new IOException("Multiple rtu devices with the same serialnumber "+serialNumber+" found! Cannot continue!");
			else
				return devices.get(0);
		}
		else throw new IOException("First CosemAPDU in a POST must have the 0.0.96.50.0.0 database ID or 0.0.96.1.0.200 serial number of the device!");
	}
	
	private void parseContent(Rtu device, List<CosemAPDU> apdus, int index) throws IOException {
		List<ChannelInfo> channelInfos=null;
		do {
			CosemAPDU apdu = apdus.get(index++);
			if (DEBUG>=1)
				System.out.println(apdu);
			if (apdu.getCosemAttributeDescriptor().getObis().equals(ObisCode.fromString("0.0.1.0.0.255"))) {
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
						Date endTime = new DateTime(entry.getDataType(0).getOctetString()).getValue().getTime();
						int eiStatus = entry.getDataType(1).intValue();
						List<IntervalValue> intervalValues = new ArrayList();
						for (int channelId=0;channelId<(entry.nrOfDataTypes()-2);channelId++) {
							NumberFormat o = new NumberFormat(entry.getDataType(channelId+2));
							intervalValues.add(new IntervalValue(o.toBigDecimal(),0,0));
						}
						intervalDatas.add(new IntervalData(endTime,eiStatus,0,0,intervalValues));
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
						Date date = new DateTime(entry.getDataType(0).getOctetString()).getValue().getTime();
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
						Date readTime = new DateTime(entry.getDataType(3).getOctetString()).getValue().getTime();
						Date fromTime = new DateTime(entry.getDataType(4).getOctetString()).getValue().getTime();
						Date toTime = new DateTime(entry.getDataType(5).getOctetString()).getValue().getTime();
						Date eventTime = new DateTime(entry.getDataType(6).getOctetString()).getValue().getTime();
						String text = entry.getDataType(7).getOctetString().stringValue();
						
						RegisterValue registerValue = new RegisterValue(obisCode,new Quantity(number,unit),eventTime,fromTime,toTime,readTime,rtuRegisterId,text);
						if (meterReadingData==null)
							meterReadingData = new MeterReadingData();
						meterReadingData.add(registerValue);
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
			
		} while(index < apdus.size());
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

}
