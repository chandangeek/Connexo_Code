/**
 * 
 */
package com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.EncryptionStatus;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author gna
 *
 */
public class MbusObisCodeMapper {
	
	CosemObjectFactory cof = new CosemObjectFactory(null);
	private static final String[] possibleConnectStates = {"Disconnected","Connected","Ready for Reconnection", "UNKNOWN state"};
	
	public MbusObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		
    	//Mbus related ObisRegisters
    	if ((obisCode.getA() == 0) && (obisCode.getC() == 24) ){
    		if((obisCode.getD() == 2) && ((obisCode.getE() >= 1) && (obisCode.getE() <= 4))){
    			ExtendedRegister register = cof.getExtendedRegister(obisCode);
    			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    		} else if(obisCode.getD() == 4){
    			if(obisCode.getE() == 128){
    	        	int mode = cof.getDisconnector(adjustToDisconnectOC(obisCode)).getControlMode().getValue();
                    Quantity quantity = new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined());
                    return rv = new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "ConnectControl mode: " + mode);
                } else if (obisCode.getE() == 129) {
                    int state = cof.getDisconnector(adjustToDisconnectOC(obisCode)).getControlState().getValue();
                    Quantity quantity = new Quantity(BigDecimal.valueOf(state), Unit.getUndefined());
                    return rv = new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, getConnectControlStateDescription(state));
                } else if (obisCode.getE() == 130) {
                    boolean state = cof.getDisconnector(adjustToDisconnectOC(obisCode)).getState();
                    Quantity quantity = new Quantity(state ? "1" : "0", Unit.getUndefined());
                    return rv = new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "State: " + state);
                }
    		} else if (obisCode.getD() == 50){ // MBus encryption status
    			Data encryptionState = cof.getData(obisCode);
                long encryptionValue = encryptionState.getValue();
                Quantity quantity = new Quantity(BigDecimal.valueOf(encryptionValue), Unit.getUndefined());
                String text = EncryptionStatus.forValue((int) encryptionValue).getLabelKey();
                return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
    		}
    		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    	} else if(7 == obisCode.getA()){    // Some OMS related ObisCode
            if( 3 == obisCode.getC() ){
    			ExtendedRegister register = cof.getExtendedRegister(obisCode);
    			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
            }
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        } else {
    		throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    	}
	}

    private String getConnectControlStateDescription(int state) {
        if ((state >= 0) && (state < (possibleConnectStates.length))) {
            return "ConnectControl state: " + possibleConnectStates[state];
        } else {
            return "ConnectControl state has an UNKNOWN state";
        }
    }

    /**
	 * The given obisCode is not a valid one. We use it to make a distiction between two arguments of the same object.
	 * This function will return the original obisCode from the Disconnector object, without the E-value
	 * @param oc -  the manipulated ObisCode of the Disconnector object
	 * @return the original ObisCode of the Disconnector object
	 */
	private ObisCode adjustToDisconnectOC(ObisCode oc) {
		return new ObisCode(oc.getA(), oc.getB(), oc.getC(), oc.getD(), 0, oc.getF());
	}

    public CosemObjectFactory getCof() {
        return cof;
}
}
