/*
 * RegisterConfig.java
 *
 * Created on 18 oktober 2004, 16:42
 */

package com.energyict.protocolimpl.customerconfig;

import java.io.*;
import java.util.*;

import com.energyict.obis.ObisCode;


/**
 *
 * @author  Koen
 */
public abstract class RegisterConfig {
    
    abstract protected Map getRegisterMap();
    abstract protected void initRegisterMap();
    abstract public int getScaler();
    
    Map map = new HashMap();
    
    /** Creates a new instance of RegisterMapping */
    protected RegisterConfig() {
        initRegisterMap();
    }

    private boolean isManufacturerSpecific(ObisCode obisCode) {
        if ((obisCode.getA() == 0) &&
            (obisCode.getC() == 96) &&
            (obisCode.getD() == 99)) 
            return true;
        else
            return false;
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
        if (isManufacturerSpecific(oc))
            return Integer.toString(oc.getB())+(oc.getE()==0?"":"."+Integer.toString(oc.getE()))+(oc.getF()==0?"":"."+Integer.toString(oc.getF()));
        else
            return null;
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
        if (isManufacturerSpecific(oc))
            return oc.getB();
        else
            return -1;
    }
    
    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = getRegisterMap().keySet().iterator();
        while(it.hasNext()) {
            ObisCode oc = (ObisCode)it.next();
            strBuff.append(oc+" "+oc.getDescription()+"\n");
        }
        return strBuff.toString();
    }
    
    public String getRegisterInfoForId() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = getRegisterMap().keySet().iterator();
        while(it.hasNext()) {
            ObisCode oc = (ObisCode)it.next();
            if (((Register)getRegisterMap().get(oc)).getId() != -1)
                strBuff.append(oc+" "+oc.getDescription()+"\n");
        }
        return strBuff.toString();
    }
    
    static public void main(String[] args) {
        RegisterConfig reg = new EDPRegisterConfig();
        System.out.println(reg.getMeterRegisterCode(ObisCode.fromString("1.1.7.6.2.255")));
        System.out.println(reg.getMeterRegisterCode(ObisCode.fromString("1.2.7.6.2.255")));
        System.out.println(reg.getMeterRegisterId(ObisCode.fromString("7.56.96.99.67.89")));
        
        System.out.println(ObisCode.fromString("1.1.1.9.0.255").getUnitElectricity(3));
    }
}
