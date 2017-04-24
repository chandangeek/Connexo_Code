/*
 * SCTMRegister.java
 *
 * Created on 21 februari 2003, 11:30
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author  Koen
 */
public class SCTMRegister {

    String register;

    /** Creates a new instance of SCTMRegister */
    public SCTMRegister(byte[] data) throws IOException {
        if (data==null) throw new IOException("SCTMRegister, data == null!");
        if ((data.length != 16) && (data.length != 12)) throw new IOException("SCTMRegister, datalength != 16 or 12 ("+data.length+")");
        register = new String(data);
    }

    public String toString() {
        return register;
    }

    public long getLongValue() {
        try {
           return Long.parseLong(register);
        }
        catch(NumberFormatException e) {
           return Long.parseLong(register,16);
        }
    }

    public int getIntValue() {
        try {
           return Integer.parseInt(register);
        }
        catch(NumberFormatException e) {
           return Integer.parseInt(register,16);
        }
    }

    public boolean isQuantity() {
        String numStrValue=null;
        String unitStrValue=null;
        byte[] data = register.trim().getBytes();
        int i;
        int countDots=0; // KV 06092005 WVEM some SCTM meters return registervalues like
        for(i = 0; i<data.length; i++) {

            if (data[i] == 0x2E) countDots++; // KV 06092005 WVEM some SCTM meters return registervalues like

            if (!(((data[i] >= 0x30) && (data[i] <= 0x39)) || (data[i] == 0x2E))) {
                numStrValue = new String(ProtocolUtils.getSubArray(data,0,i-1));
                unitStrValue = new String(ProtocolUtils.getSubArray(data,i,data.length-1));
                break;
            }
        }
        // KV 06092005 WVEM some SCTM meters return registervalues like
        if (countDots>1) {
            register = register.replace('.',' ');
            numStrValue = register.trim();
            unitStrValue="";
            try {
               new BigDecimal(numStrValue);
            }
            catch(NumberFormatException e) {
                return false;
            }
            return true;

        }

        if (i==data.length)
            return true;
        if ((numStrValue == null) || (unitStrValue == null))
            return false;
        try {
           new BigDecimal(numStrValue);
        }
        catch(NumberFormatException e) {
            return false;
        }
        if (Unit.get(unitStrValue) == null)
            return false;
        return true;
    }

    public Quantity getQuantityValue() throws IOException {
        String numStrValue;
        String unitStrValue;
        int countDots=0; // KV 06092005 WVEM some SCTM meters return registervalues like
        byte[] data = register.trim().getBytes();
        for(int i = 0; i<data.length; i++) {
            if (data[i] == 0x2E) countDots++; // KV 06092005 WVEM some SCTM meters return registervalues like
            if (!(((data[i] >= 0x30) && (data[i] <= 0x39)) || (data[i] == 0x2E))) {
                numStrValue = new String(ProtocolUtils.getSubArray(data,0,i-1));
                unitStrValue = new String(ProtocolUtils.getSubArray(data,i,data.length-1));
                return new Quantity(new BigDecimal(numStrValue),Unit.get(unitStrValue));
            }

        }

        // KV 06092005 WVEM some SCTM meters return registervalues like
        if (countDots>1) {
            register = register.replace('.',' ');
        }

        // only numerical, no unit
        numStrValue = register.trim();
        unitStrValue = "";
        return new Quantity(new BigDecimal(numStrValue),Unit.get(unitStrValue));
    }

}
