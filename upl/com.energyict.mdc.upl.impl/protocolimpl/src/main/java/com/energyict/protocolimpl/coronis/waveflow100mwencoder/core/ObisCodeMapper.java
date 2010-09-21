package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class ObisCodeMapper {
	
	
	private WaveFlow100mW waveFlow100mW;
	
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(final WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW=waveFlow100mW;
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	
    	if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {
    		// battery counter
    		return new RegisterInfo("Available battery power in %");
   		}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.1.255"))) {
    		// port A encoder info
    		return new RegisterInfo("Port A encoder info");
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.2.255"))) {
    		// port B encoder info
    		return new RegisterInfo("Port B encoder info");
    	}
    	else if ((obisCode.equals(ObisCode.fromString("8.1.1.0.0.255"))) || (obisCode.equals(ObisCode.fromString("8.2.1.0.0.255")))) { // Port A or B
    		return new RegisterInfo("Port "+(obisCode.getB()==1?"A":"B")+" encoder index");
    	}
    	
    	return null;
        
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {
    		// battery counter
    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife()), Unit.get(BaseUnit.PERCENT)),new Date());
   		}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.1.255"))) {
    		// port A encoder info
    		return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, waveFlow100mW.getParameterFactory().readEncoderModel(0).getEncoderModelInfo().toString());
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.2.255"))) {
    		// port B encoder info
    		return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, waveFlow100mW.getParameterFactory().readEncoderModel(1).getEncoderModelInfo().toString());
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.3.255"))) {
    		
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.4.255"))) {
    		
    	}
    	else if ((obisCode.equals(ObisCode.fromString("8.1.1.0.0.255"))) || (obisCode.equals(ObisCode.fromString("8.2.1.0.0.255")))) { // Port A or B
    		EncoderInternalData encoderInternalData = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[obisCode.getB()-1];
    		Unit unit = encoderInternalData.getEncoderUnitType().toUnit();
    		BigDecimal bd = new BigDecimal(encoderInternalData.getCurrentIndex()*100+encoderInternalData.getLastPart());
    		bd = bd.movePointLeft(10-encoderInternalData.getDecimalPosition()); 
    		return new RegisterValue(obisCode,new Quantity(bd, unit),new Date());
    	}
    	
    	return null;
    }
	
}
