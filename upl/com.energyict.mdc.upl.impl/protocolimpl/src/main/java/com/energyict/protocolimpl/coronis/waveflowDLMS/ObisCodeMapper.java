package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;

public class ObisCodeMapper {
	

	private final AbstractDLMS abstractDLMS;
	
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
    	
    	try {
	    	if ((obisCode.equals(ObisCode.fromString("0.0.96.6.50.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.51.255")))) {
/*	    		// read status
	    		int portId = obisCode.getE()-50;
	    		EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
	    		if (o==null) {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
	    		}
	    		else {
	    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getStatus()), Unit.get("")),new Date());
	    		}*/
	    	}
	    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.52.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.53.255")))) {
	    		// dry count
	    		/*
	    		int portId = obisCode.getE()-52;
	    		EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
	    		if (o==null) {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
	    		}
	    		else {
	    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getDryCount()), Unit.get("")),new Date());
	    		}
	    		*/
	    	}
	    	throw new IOException();
		} catch (IOException e) {
			
			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");
			
		}

    }
	
}
