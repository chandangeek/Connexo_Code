package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;

public class ObisCodeMapper {
	
	static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();
	
	static {
		// get the common obis codes from the common obis code mapper
		
		// specific Actaris mbus meters registers
		registerMaps.put(ObisCode.fromString("0.0.96.6.68.255"), "Port A encoder internal data raw string");
		registerMaps.put(ObisCode.fromString("0.0.96.6.69.255"), "Port B encoder internal data raw string");
	}
	
	private Echodis echodis;
	
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(final Echodis echodis) {
        this.echodis=echodis;
    }
    
    final String getRegisterExtendedLogging() {
    	
    	StringBuilder strBuilder=new StringBuilder();
    	
    	Iterator<Entry<ObisCode,String>> it = registerMaps.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,String> o = it.next();
    		echodis.getLogger().info(o.getKey().toString()+", "+o.getValue());
    	}
    	
    	strBuilder.append(echodis.getCommonObisCodeMapper().getRegisterExtendedLogging());
    	
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

	    	if ((obisCode.equals(ObisCode.fromString("0.0.96.6.68.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.69.255")))) {
	    		// encoder internal data
	    		int portId = obisCode.getE()-68;
	    		ActarisMBusInternalData o = (ActarisMBusInternalData)echodis.readInternalDatas()[portId];
	    		if (o==null) {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
	    		}
	    		else {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, ProtocolUtils.outputHexString(o.getEncoderInternalData()));
	    		}
	    	}
	    	else {
	    		try {
	    			return echodis.getCommonObisCodeMapper().getRegisterValue(obisCode);
	    		}
	    		catch(NoSuchRegisterException e) {
	    			return echodis.getMbusRegisterValue(obisCode);
	    		}
	    	}
	    	
		} catch (IOException e) {
			
			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");
			
		}

    }
	
}
