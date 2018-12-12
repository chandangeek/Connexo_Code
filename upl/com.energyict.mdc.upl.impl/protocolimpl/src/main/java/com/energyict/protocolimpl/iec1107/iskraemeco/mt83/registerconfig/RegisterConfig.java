/*
 * RegisterConfig.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author  Koen
 */
public abstract class RegisterConfig {

    protected abstract Map<ObisCode, Register> getRegisterMap();
    protected abstract void initRegisterMap();
    public abstract int getScaler();

    Map<ObisCode, Register> map = new HashMap<>();
    private Map<String, String> deviceRegisterMapping = new HashMap<>();


    /** Creates a new instance of RegisterMapping */
    protected RegisterConfig() {
        initRegisterMap();
    }

    public Map<String, String> getDeviceRegisterMapping() {
		return deviceRegisterMapping;
	}

    public String getMeterRegisterCode(ObisCode oc) {
        Register register = getRegisterMap().get(oc);
        if (register == null) {
	        return null;
        }
        return oc.toString();
    }

    public String getRegisterDescription(ObisCode obis) throws NoSuchRegisterException {
    	Register reg = getRegisterMap().get(obis);
    	if (reg == null) {
    		if (checkRegister(obis)) {
    			reg = new Register(obis.toString(), 0);
    		} else {
        		throw new NoSuchRegisterException("Register with obiscode=" + obis.toString() + " is not supported!");
        	}
    	}
    	return reg.getName();
	}

    public String getRegisterInfo() {
        StringBuilder builder = new StringBuilder();
	    for (ObisCode oc : getRegisterMap().keySet()) {
		    builder.append(oc).append(" ").append(getRegisterMap().get(oc).getName()).append("\n");
	    }
        return builder.toString();
    }

    boolean checkRegister(ObisCode obis) {
    	if (obis.getA() != 1) {
		    return false;
	    }

    	switch (obis.getB()) {
    		case 1: break;
    		case 2: break;
    		default: return false;
    	}

    	if (!((obis.getC() >= 1) && (obis.getC() <= 10))) {
		    if (!((obis.getC() >= 21) && (obis.getC() <= 30))) {
			    if (!((obis.getC() >= 41) && (obis.getC() <= 50))) {
				    if (!((obis.getC() >= 61) && (obis.getC() <= 70))) {
					    return false;
				    }
			    }
		    }
	    }

    	switch (obis.getD()) {
    		case 2: break;
    		case 4: break;
    		case 5: break;
    		case 6: break;
    		case 8: break;
    		case 9: break;
    		default: return false;
    	}

    	if ((obis.getE() > 6) || (obis.getE() < 0)) {
		    return false;
	    }

    	return true;
    }
}