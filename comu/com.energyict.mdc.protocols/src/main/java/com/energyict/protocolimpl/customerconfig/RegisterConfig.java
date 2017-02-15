/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterConfig.java
 *
 * Created on 18 oktober 2004, 16:42
 */

package com.energyict.protocolimpl.customerconfig;

import com.energyict.mdc.common.ObisCode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 * @author  Koen
 */
public abstract class RegisterConfig {

    protected abstract Map getRegisterMap();
    protected abstract void initRegisterMap();
    public abstract int getScaler();

    Map map = new HashMap();

    /** Creates a new instance of RegisterMapping */
    protected RegisterConfig() {
        initRegisterMap();
    }

    private boolean isManufacturerSpecific(ObisCode obisCode) {
        return (obisCode.getA() == 0) &&
                (obisCode.getC() == 96) &&
                (obisCode.getD() == 99);
    }

    // changes for manufacturer obis codes KV 01092005!
    public String getMeterRegisterCode(ObisCode oc) {

        // KV 020606 special cases where we build the ediscode using C..E field of the OBIS code
        if (oc.getA() == 255) {
            return (oc.getC()==255?"":""+oc.getC()+".")+(oc.getD()==255?"":""+oc.getD()+".")+(oc.getE()==255?"":""+oc.getE());
        }

        Register register = (Register)getRegisterMap().get(oc);
        if (register != null) {
            return register.getName();
        }
        if (isManufacturerSpecific(oc)) {
            return Integer.toString(oc.getB()) + (oc.getE() == 0 ? "" : "." + Integer.toString(oc.getE())) + (oc.getF() == 0 ? "" : "." + Integer.toString(oc.getF()));
        }
        else {
            return null;
        }
    }

    // changes for manufacturer obis codes KV 01092005!
    public int getMeterRegisterId(ObisCode oc) {

        // KV 020606 special cases where we build the registerId using C field of the OBIS code
        if (oc.getA() == 255) {
            return oc.getC();
        }

        Register register = (Register)getRegisterMap().get(oc);
        if (register != null) {
            return register.getId();
        }
        if (isManufacturerSpecific(oc)) {
            return oc.getB();
        }
        else {
            return -1;
        }
    }

    public String getRegisterInfo() {
        StringBuilder strBuff = new StringBuilder();
        Iterator it = getRegisterMap().keySet().iterator();
        while(it.hasNext()) {
            ObisCode oc = (ObisCode)it.next();
            strBuff.append(oc).append(" ").append(oc.getDescription()).append("\n");
        }
        return strBuff.toString();
    }

    public String getRegisterInfoForId() {
        StringBuilder strBuff = new StringBuilder();
        Iterator it = getRegisterMap().keySet().iterator();
        while(it.hasNext()) {
            ObisCode oc = (ObisCode)it.next();
            if (((Register)getRegisterMap().get(oc)).getId() != -1) {
                strBuff.append(oc + " " + oc.getDescription() + "\n");
            }
        }
        return strBuff.toString();
    }

}