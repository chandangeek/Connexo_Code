/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterConfig.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 * @author  Koen
 */
public abstract class RegisterConfig {

    abstract protected Map getRegisterMap();
    abstract protected void initRegisterMap();
    abstract public int getScaler();

    Map map = new HashMap();
    Map deviceRegisterMapping = new HashMap();


    /** Creates a new instance of RegisterMapping */
    protected RegisterConfig() {
        initRegisterMap();
    }

    public Map getDeviceRegisterMapping() {
		return deviceRegisterMapping;
	}

    public String getMeterRegisterCode(ObisCode oc) {
        Register register = (Register)getRegisterMap().get(oc);
        if (register == null) return null;
        return oc.toString();
    }

    public String getRegisterDescription(ObisCode obis) throws NoSuchRegisterException {
    	Register reg = (Register) getRegisterMap().get(obis);
    	if (reg == null) {
    		if (checkRegister(obis)) {
    			reg = new Register(obis.getDescription(), 0);
    		} else {
        		throw new NoSuchRegisterException("Register with obiscode=" + obis.toString() + " is not supported!");
        	}
    	}
    	return reg.getName();
	}

    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = getRegisterMap().keySet().iterator();
        while(it.hasNext()) {
        	ObisCode oc = (ObisCode)it.next();
        	strBuff.append(oc+" "+((Register)getRegisterMap().get(oc)).getName()+"\n");
        }
        return strBuff.toString();
    }

    public boolean checkRegister(ObisCode obis) {
    	if (obis.getA() != 1) return false;

    	switch (obis.getB()) {
    		case 1: break;
    		case 2: break;
    		default: return false;
    	}

    	if (!((obis.getC() >= 1) && (obis.getC() <= 10)))
    		if (!((obis.getC() >= 21) && (obis.getC() <= 30)))
    			if (!((obis.getC() >= 41) && (obis.getC() <= 50)))
    				if (!((obis.getC() >= 61) && (obis.getC() <= 70)))
    					return false;

    	switch (obis.getD()) {
    		case 2: break;
    		case 4: break;
    		case 5: break;
    		case 6: break;
    		case 8: break;
    		case 9: break;
    		default: return false;
    	}

    	if ((obis.getE() > 6) || (obis.getE() < 0)) return false;

//    	if (obis.getF() != 255) {
//    		if ((obis.getF() < 0) || ((obis.getF() > 14))) {
//    			return false;
//    		}//    	}

    	return true;

    }

}
