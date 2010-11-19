package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;

public class ObisCodeMapper {
	
	static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();
	
	static {

		// get the common obis codes from the common obis code mapper
		
		// specific waveflow registers
		registerMaps.put(ObisCode.fromString("1.1.82.8.0.255"), "Input A index");
		registerMaps.put(ObisCode.fromString("1.2.82.8.0.255"), "Input B index");
		registerMaps.put(ObisCode.fromString("1.3.82.8.0.255"), "Input C index");
		registerMaps.put(ObisCode.fromString("1.4.82.8.0.255"), "Input D index");
		


	}
	
	private WaveFlowV2 waveLogV2;
	
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(final WaveFlowV2 waveLogV2) {
        this.waveLogV2=waveLogV2;
    }
    
    final String getRegisterExtendedLogging() {
    	
    	StringBuilder strBuilder=new StringBuilder();
    	
    	Iterator<Entry<ObisCode,String>> it = registerMaps.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,String> o = it.next();
    		waveLogV2.getLogger().info(o.getKey().toString()+", "+o.getValue());
    	}
    	
    	strBuilder.append(waveLogV2.getCommonObisCodeMapper().getRegisterExtendedLogging());
    	
    	return strBuilder.toString();
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	String info = registerMaps.get(obisCode);
    	if (info !=null) {
    		return new RegisterInfo(info);
    	}
    	else {
    		return CommonObisCodeMapper.getRegisterInfo(obisCode); 
    	}
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	try {
	    	if ((obisCode.equals(ObisCode.fromString("1.1.82.8.0.255"))) || 
	    		(obisCode.equals(ObisCode.fromString("1.2.82.8.0.255"))) ||
	    		(obisCode.equals(ObisCode.fromString("1.3.82.8.0.255"))) ||
	    		(obisCode.equals(ObisCode.fromString("1.4.82.8.0.255")))) { // Input A..D
	    		
	    		
	    		int inputId = obisCode.getB()-1;
	    		BigDecimal bd = new BigDecimal(waveLogV2.getRadioCommandFactory().readCurrentReading().getReadings()[inputId]);
	    		return new RegisterValue(obisCode,new Quantity(bd, Unit.get("")),new Date());
	    	}	    	
	    	
	    	else {
	    		return waveLogV2.getCommonObisCodeMapper().getRegisterValue(obisCode);
	    	}
	    	
		} catch (IOException e) {
			
			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");
			
		}

    }
	
}
