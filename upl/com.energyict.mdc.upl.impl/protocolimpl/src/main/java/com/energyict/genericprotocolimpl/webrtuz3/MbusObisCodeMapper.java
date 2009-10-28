/**
 * 
 */
package com.energyict.genericprotocolimpl.webrtuz3;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

/**
 * @author gna
 *
 */
public class MbusObisCodeMapper {
	
	CosemObjectFactory cof = new CosemObjectFactory(null);
	private static final String[] possibleConnectStates = {"Disconnected","Connected","Ready for Reconnection"};
	
	public MbusObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		
    	//Mbus related ObisRegisters
    	if ((obisCode.getA() == 0) && ((obisCode.getB() >= 1) && (obisCode.getB() <= 4)) && (obisCode.getC() == 24) ){
    		if((obisCode.getD() == 2) && ((obisCode.getE() >= 1) && (obisCode.getE() <= 4))){
    			ExtendedRegister register = cof.getExtendedRegister(obisCode);
    			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    		} else if(obisCode.getD() == 4){
    			if(obisCode.getE() == 128){
    	        	int mode = cof.getDisconnector(adjustToDisconnectOC(obisCode)).getControlMode().getValue();
    	        	rv = new RegisterValue(obisCode,
    	        			new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()),
    	        			null, null, null, new Date(), 0,
    	        			new String("ConnectControl mode: " + mode));
    	        	return rv;
    			} else if(obisCode.getE() == 129){
    	        	int state = cof.getDisconnector(adjustToDisconnectOC(obisCode)).getControlState().getValue();
    	        	if((state < 0) || (state > 2)){
    	        		throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
    	        	}
    	        	rv = new RegisterValue(obisCode,
    	        			new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()),
    	        			null, null, null, new Date(), 0,
    	        			new String("ConnectControl state: " + possibleConnectStates[state]));
    	        	return rv;
    			}
    		}
    		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    	} else {
    		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    	}
	}
	
	private ObisCode adjustToDisconnectOC(ObisCode oc) {
		return new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), 0, oc.getF());
	}
}
