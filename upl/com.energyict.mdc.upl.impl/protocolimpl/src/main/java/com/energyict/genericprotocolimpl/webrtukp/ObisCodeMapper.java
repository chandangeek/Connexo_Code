package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Register;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

/**
 * 
 * @author gna
 * Changes:
 * GNA |03062009| Added abstract registers (activity Calendar, Acive Firmware)
 */
public class ObisCodeMapper {

	CosemObjectFactory cof = new CosemObjectFactory(null);
	
	public ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		int billingPoint = -1;
		
		// Abstract Registers
        if(obisCode.toString().indexOf("0.0.13.0.0.255") != -1){	// Activity Calendar
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        	return rv;
        } else if (obisCode.toString().indexOf("1.0.0.2.0.255") != -1){	// Active firmware identifier
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			new String(cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1).getString()));
        	return rv;
        }
		
    	//Electricity related ObisRegisters
    	if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() == 128)) && (obisCode.getC() >=1) && (obisCode.getC() <= 2) 
			&& (obisCode.getD() == 8) && (obisCode.getE() >=0) && (obisCode.getE() <=4) && (obisCode.getF() == 255)){
			Register register = cof.getRegister(obisCode);
			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    	}
    	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

}
