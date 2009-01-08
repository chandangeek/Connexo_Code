/**
 * 
 */
package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author gna
 *
 */
public class MbusObisCodeMapper {
	
	CosemObjectFactory cof = new CosemObjectFactory(null);
	
	public MbusObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		
    	//Mbus related ObisRegisters
    	if ((obisCode.getA() == 0) && ((obisCode.getB() >= 1) && (obisCode.getB() <= 4)) && (obisCode.getC() == 24) && (obisCode.getD() == 2) 
    			&& ((obisCode.getE() >= 1) && (obisCode.getE() <= 4))){
			ExtendedRegister register = cof.getExtendedRegister(obisCode);
			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    	}
		return rv;
	}
}
