package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.CosemObject;
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
	
	private boolean debug = false;

	CosemObjectFactory cof = new CosemObjectFactory(null);
	
	public ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		int billingPoint = -1;
		CosemObject co = null;
		
		if(debug){
			System.out.println(obisCode);
		}
		
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
    	
    	//Instantaneous, average values (voltage/current/power)
    	if((obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getE() == 0) && (obisCode.getF() == 255)){
    		if(obisCode.getD() == 7) { // instantaneous values
    			if((obisCode.getC() == 32) ||		// inst. voltage L1
    					(obisCode.getC() == 31) ||	// inst. current L1
    					(obisCode.getC() == 52) ||	// inst. voltage L2
    					(obisCode.getC() == 51) ||	// inst. current L2
    					(obisCode.getC() == 72) ||	// inst. voltage L3
    					(obisCode.getC() == 71) ||	// inst. current L3
    					(obisCode.getC() == 15) ||	// inst. active power (P+)
    					(obisCode.getC() == 90) ){	// inst. current
    				co = cof.getCosemObject(obisCode);
    			}
    		} else if(obisCode.getD() == 24) {
    			if((obisCode.getC() == 32) ||		// average voltage L1
    					(obisCode.getC() == 52) ||	// average voltage L2
    					(obisCode.getC() == 72)){	// average voltage L3
    				co = cof.getCosemObject(obisCode);
    			}
    		}
    		
    		if(co != null){
    			return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(co));
    		}
    	}
    	
    	// Power Qualities (voltage sags and swells)
    	if((obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getE() == 0) && (obisCode.getF() == 255)){
       		if(((obisCode.getC() == 12)	||			
       				(obisCode.getC() == 32) ||
       				(obisCode.getC() == 52) ||
       				(obisCode.getC() == 72)) &&
       				((obisCode.getD() == 31) ||
       						(obisCode.getD() == 43) ||
       						(obisCode.getD() == 32) ||
       						(obisCode.getD() == 35) ||
       						(obisCode.getD() == 44) ||
       						(obisCode.getD() == 36))){		
       			co = cof.getCosemObject(obisCode);
       			return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(co));
       		}
    	}
    	
    	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

}
