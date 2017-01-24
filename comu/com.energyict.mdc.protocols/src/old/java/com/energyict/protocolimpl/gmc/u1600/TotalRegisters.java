/*
 * TotalRegisters.java
 *
 * Created on 7 juli 2004, 11:24
 */

package com.energyict.protocolimpl.gmc.u1600;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class TotalRegisters extends AbstractLogicalAddress {

    private static final int NR_OF_REGISTERS=1;

    String meanings[]={"ActiveImport","ActiveExport","ReactiveImport","ReactiveExport","ReactiveQ1","ReactiveQ2","ReactiveQ3","ReactiveQ4","Apparent"};
    // KV TO_DO protocol doc states k 'x1000' values... analysis of protocol is not so...
    //Unit[] units={Unit.get("kWh"),Unit.get("kWh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kvarh"),Unit.get("kVAh")};
    int[] obisCMapping={1,2,3,4,5,6,7,8,9}; // apparent power is configurable. So, manufacturer specific...
    Quantity[] values= new Quantity[NR_OF_REGISTERS];

    /** Creates a new instance of TotalRegisters */
    public TotalRegisters(int channel,int size, LogicalAddressFactory laf) throws IOException {
        super(channel,size,laf);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TotalRegisters: ");
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (i>0) strBuff.append(", ");
            strBuff.append(meanings[i]+"="+values[i].toString());
        }
        return strBuff.toString();
    }

    public void parse(byte[] data, TimeZone timeZone) throws java.io.IOException {
        Double  dVal;
        double  dValue;
        String str = new String(data);




        // KV 11052006 avoid non numerical characters in str
        str = str.trim();
        if (str.indexOf(" ") >= 0)
            str = str.substring(0,str.indexOf(" "));

        dVal = Double.valueOf(str);
        dValue = dVal.doubleValue();
        BigDecimal bigValue = new  BigDecimal(dValue);
        values[0] = new Quantity(bigValue,Unit.get(""));
       }

    public Quantity getTotalValue(String meaning) throws java.io.IOException {
        for(int i=0;i<NR_OF_REGISTERS;i++) {
            if (meanings[i].compareTo(meaning)==0) {
                return values[i];
            }
        }
        throw new IOException("TotalRegisters, register "+meaning+" does not exist!");
    }

    public Quantity getTotalValue(int i) {
        return values[i];
    }

    public Quantity getTotalValueforObisB(int obisCodeB) throws java.io.IOException {
        for(int i=0;i<1;i++) {
            if (obisCMapping[i]==obisCodeB) {
                return values[i];
            }
        }
        throw new NoSuchRegisterException("TotalRegisters, register wit obis code B field "+obisCodeB+" does not exist!");
    }

     public Quantity getValueforObisB(int obisCodeB) throws java.io.IOException {


         if (obisCodeB <= 64)
           return values[0];


        throw new NoSuchRegisterException("TotalRegisters, register wit obis code B field "+obisCodeB+" does not exist!");
    }
}
