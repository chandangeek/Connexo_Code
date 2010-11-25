package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class ObisCodeMapper {
	

	private final AbstractDLMS abstractDLMS;
	
	private final int CLASS_DATA=1;
	private final int CLASS_REGISTER=3;
	private final int CLASS_EXTENDED_REGISTER=4;
	
	private final int ATTRIBUTE_VALUE=2;
	private final int ATTRIBUTE_SCALER=3;	
	private final int ATTRIBUTE_CAPTURETIME=5;	
	
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
    	
    	return strBuilder.toString();
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	return new RegisterInfo(AbstractDLMS.findObjectByObiscode(obisCode).getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	
    	ObjectEntry objectEntry = AbstractDLMS.findObjectByObiscode(obisCode);
    	
		if (objectEntry.getClassId() == CLASS_DATA) {
			AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(obisCode);
			if (adt.isOctetString()) {
				return new RegisterValue(obisCode, adt.getOctetString().stringValue());
			}
			else if (adt.isVisibleString()) {
				return new RegisterValue(obisCode, adt.getOctetString().stringValue());
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
		
		
		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!"); // has an error ["+e.getMessage()+"]!");
    }
	
}
