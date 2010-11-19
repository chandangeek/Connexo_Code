package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class CommonObisCodeMapper {
	
	static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();
	
	static {
		registerMaps.put(ObisCode.fromString("0.0.96.6.0.255"), "Available battery power in %");
		registerMaps.put(ObisCode.fromString("0.0.96.6.3.255"), "Application status");
		// specific waveflow registers start with E-field 50
		

	}
	
	private WaveFlow waveFlow;
	
    /** Creates a new instance of ObisCodeMapper */
    public CommonObisCodeMapper(final WaveFlow waveFlow) {
        this.waveFlow=waveFlow;
    }
    
    final public String getRegisterExtendedLogging() {
    	
    	StringBuilder strBuilder=new StringBuilder();
    	
    	Iterator<Entry<ObisCode,String>> it = registerMaps.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,String> o = it.next();
    		waveFlow.getLogger().info(o.getKey().toString()+", "+o.getValue());
    	}
    	
    	return strBuilder.toString();
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	String info = registerMaps.get(obisCode);
    	if (info !=null) {
    		return new RegisterInfo(info);
    	}
    	else {
    		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
    	}
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
		try {
	    	if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {
	    		// battery counter
				return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife()), Unit.get(BaseUnit.PERCENT)),new Date());
	   		}
	    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.3.255"))) {
	    		// application status
	    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow.getParameterFactory().readApplicationStatus()), Unit.get("")),new Date());
	    	}
	    	
			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
			
		} catch (IOException e) {
			
			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");
			
		}

    }
	
}
