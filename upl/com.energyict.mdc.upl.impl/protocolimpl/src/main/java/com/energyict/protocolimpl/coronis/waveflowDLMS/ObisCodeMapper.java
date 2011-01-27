package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class ObisCodeMapper {
	

	private final AbstractDLMS abstractDLMS;
	
	private final int CLASS_DATA=1;
	private final int CLASS_REGISTER=3;
	private final int CLASS_EXTENDED_REGISTER=4;
	
	private final int ATTRIBUTE_VALUE=2;
	private final int ATTRIBUTE_SCALER=3;	
	private final int ATTRIBUTE_CAPTURETIME=5;	
	
	
	/**
	 * cached registervalues read by the Transparant object list reader using the property ObisCodeList
	 */
	private Map<ObisCode,RegisterValue>[] cachedRegisterValues=null; 
	
	private final int findRegisterInList(ObisCode obisCode) {
		for (int index=0;index<abstractDLMS.getObjectInfosLists().length;index++) {
			
			List<ObjectInfo> objectInfos = abstractDLMS.getObjectInfosLists()[index];
			for (ObjectInfo o : objectInfos) {
				
				if (o.getObisCode().equals(obisCode)) {
					return index;
				}
			}
		}
		return -1;
	}
	
	private final RegisterValue getCachedRegisterValues(ObisCode obisCode) throws IOException {
		
		int index = findRegisterInList(obisCode); // does the obis code exist in a list
		if (index == -1) return null; // if the obiscode does not belongs to a list, leave
		
		// initialize the cacedregistervalues array if not already done
		if (cachedRegisterValues == null) { 
			cachedRegisterValues = new Map[abstractDLMS.getObjectInfosLists().length];
		}
		
		// read cachedRegisterValues if not already done
		if (cachedRegisterValues[index] == null) {
			cachedRegisterValues[index] = new HashMap<ObisCode,RegisterValue>(); 
			if (abstractDLMS.getObjectInfosLists()[index].size() > 0) {
				abstractDLMS.getLogger().info("Invoke a block register read for ["+abstractDLMS.getObjectInfosLists()[index].size()+"] values...");
		    	TransparentObjectListRead t = new TransparentObjectListRead(abstractDLMS,abstractDLMS.getObjectInfosLists()[index]);
		    	t.read();
		    	cachedRegisterValues[index] = t.getRegisterValues();
			}
		}
		
		return cachedRegisterValues[index].get(obisCode);
	}
	
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(final AbstractDLMS abstractDLMS) {
        this.abstractDLMS=abstractDLMS;
    }
    
    final String getRegisterExtendedLogging() {
    	
    	StringBuilder strBuilder=new StringBuilder();
    	
    	Iterator<Entry<ObisCode,ObjectEntry>> it = abstractDLMS.getObjectEntries().entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,ObjectEntry> o = it.next();
    		strBuilder.append(o.getKey().toString()+", "+o.getValue().getDescription()+"\n");
    	}
    	strBuilder.append("0.0.96.1.0.255"+", firmware version\n");
    	strBuilder.append("0.0.96.2.0.255"+", operation mode\n");
    	strBuilder.append("0.0.96.3.0.255"+", application status\n");
    	strBuilder.append("0.0.96.4.0.255"+", alarm configuration\n");
    	strBuilder.append("0.0.96.5.0.255"+", RSSI level\n");
    	
    	return strBuilder.toString();
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	return new RegisterInfo(AbstractDLMS.findObjectByObiscode(obisCode).getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	

    	// common non-register obis codes
    	if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) { //firmware version
    		return new RegisterValue(obisCode,"V"+WaveflowProtocolUtils.toHexString(abstractDLMS.getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion())+", Mode of transmission "+abstractDLMS.getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission());
    	}
    	if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) { //Operation mode
    		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getParameterFactory().readOperatingMode(),Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//   			return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getOperatingMode(),Unit.get("")));
    	}
    	if (obisCode.equals(ObisCode.fromString("0.0.96.3.0.255"))) { //Application status
    		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getParameterFactory().readApplicationStatus(),Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//       		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getApplicationStatus(),Unit.get("")));
    	}
    	if (obisCode.equals(ObisCode.fromString("0.0.96.4.0.255"))) { //Alarm Configuration
    		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getParameterFactory().readAlarmConfiguration(),Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//       		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getAlarmConfiguration(),Unit.get("")));
    	}
    	if (obisCode.equals(ObisCode.fromString("0.0.96.5.0.255"))) { //RSSI level
    		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getRadioCommandFactory().readRSSILevel(),Unit.get("")));
    	}
    	
    	
    	ObjectEntry objectEntry = AbstractDLMS.findObjectByObiscode(obisCode);
    	
    	RegisterValue registerValue = getCachedRegisterValues(obisCode);
    	if (registerValue != null) {
    		abstractDLMS.getLogger().info("Read "+obisCode+" from cached values");
    		return registerValue;
    	}
    	
    	try {
		if (objectEntry.getClassId() == CLASS_DATA) {
			AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(obisCode);
			
			if (adt.isOctetString()) {
				return new RegisterValue(obisCode, ProtocolUtils.outputHexString(adt.getOctetString().getOctetStr()));
			}
			else if (adt.isVisibleString()) {
				return new RegisterValue(obisCode, adt.getVisibleString().getStr());
			}
			else {
				return new RegisterValue(obisCode, new Quantity(adt.toBigDecimal(),Unit.get("")));
			}
		}
		else if (objectEntry.getClassId() == CLASS_REGISTER) {
			
			AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_SCALER);
			int scale = adt.getStructure().getDataType(0).intValue();
			int code = adt.getStructure().getDataType(1).intValue();
			Unit unit = Unit.get(code, scale);
			
			adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_VALUE);
			BigDecimal value = adt.toBigDecimal();
			
			return new RegisterValue(obisCode,new Quantity(value,unit));
		}    	
		else if (objectEntry.getClassId() == CLASS_EXTENDED_REGISTER) {
			
			AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_SCALER);
			int scale = adt.getStructure().getDataType(0).intValue();
			int code = adt.getStructure().getDataType(1).intValue();
			Unit unit = Unit.get(code, scale);
			
			adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_VALUE);
			BigDecimal value = adt.toBigDecimal();
			
			adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_CAPTURETIME);
			
			DateTime eventTime = new DateTime(adt.getOctetString(), abstractDLMS.getTimeZone());

			return new RegisterValue(obisCode,new Quantity(value,unit),eventTime.getValue().getTime());
		}    	
    	}
    	catch(DataAccessResultException e) {
    		if ((e.getCode() == e.getCode().OBJECT_UNAVAILABLE) ||
   				(e.getCode() == e.getCode().OBJECT_UNDEFINED)) {
    			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist ["+e.getMessage()+"]!");
    		}
    		else {
    			throw e;
    		}
    	}
		
		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!"); // has an error ["+e.getMessage()+"]!");
    }
	
}
