package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Register;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

public class ObisCodeMapper {

	CosemObjectFactory cof = new CosemObjectFactory(null);
	
	public ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		int billingPoint = -1;
		
    	if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF()+101;
        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
            billingPoint = (obisCode.getF()*-1)+101;
        else if ((obisCode.getF()  <=101) && (obisCode.getF() < 255))
            billingPoint = obisCode.getF();
        else if (obisCode.getF() == 255)
            billingPoint = -1;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		
    	//Electricity related ObisRegisters
    	if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() <= 4))){
    		// cumulative values, indexes
    		if ((obisCode.getD() == 8) && ((obisCode.getC() == 1) || (obisCode.getC() == 2))) {
    			Register register = cof.getRegister(obisCode);
    			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    		}
    	}
		return rv;
	}

}
