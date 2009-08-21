package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;

public class SiemensS4sObisCodeMapper {

	private S4sObjectFactory s4sObjectFactory;
	
	public SiemensS4sObisCodeMapper(S4sObjectFactory objectFactory) {
		this.s4sObjectFactory = objectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) {
		RegisterValue registerValue = null;
		
		if( (obisCode.getA() == 1) && (obisCode.getC() == 1) && (obisCode.getD() == 8)){	// Active Power
			if((obisCode.getB() >= 0) && (obisCode.getB() < 4)){
				if((obisCode.getE() >= 1) && (obisCode.getE() <= 4)){
					
					//TODO Read the register and parse it back!
					
				}
			}
		}
		
		return null;
	}

}
