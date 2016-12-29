/*
 * RegisterConfig.java
 *
 * Created on 18 oktober 2004, 16:42
 */

package com.energyict.protocolimpl.customerconfig;

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

        Register register = getRegisterMap().get(oc);
        if (register != null) {
            return register.getName();
        }
        if (isManufacturerSpecific(oc)) {
            return Integer.toString(oc.getB()) + (oc.getE() == 0 ? "" : "." + Integer.toString(oc.getE())) + (oc.getF() == 0 ? "" : "." + Integer.toString(oc.getF()));
        } else {
            return null;
        }
    }

    // changes for manufacturer obis codes KV 01092005!
    public int getMeterRegisterId(ObisCode oc) {

        // KV 020606 special cases where we build the registerId using C field of the OBIS code
        if (oc.getA() == 255) {
            return oc.getC();
        }

        Register register = getRegisterMap().get(oc);
        if (register != null) {
            return register.getId();
        }
        if (isManufacturerSpecific(oc)) {
            return oc.getB();
        } else {
            return -1;
        }
    }

    public String getRegisterInfo() {
        StringBuilder builder = new StringBuilder();
        for (ObisCode oc : getRegisterMap().keySet()) {
            builder
                .append(oc)
                .append(" ")
                .append(oc.toString())
                .append("\n");
        }
        return builder.toString();
    }

    public String getRegisterInfoForId() {
        StringBuilder builder = new StringBuilder();
        for (ObisCode oc : getRegisterMap().keySet()) {
            if (getRegisterMap().get(oc).getId() != -1) {
                builder
                    .append(oc)
                    .append(" ")
                    .append(oc.toString())
                    .append("\n");
            }
        }
        return builder.toString();
    }

}
