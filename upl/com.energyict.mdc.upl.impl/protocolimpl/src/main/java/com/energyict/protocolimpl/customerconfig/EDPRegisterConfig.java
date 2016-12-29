/*
 * EDPRegisters.java
 *
 * Created on 18 oktober 2004, 16:58
 */

package com.energyict.protocolimpl.customerconfig;

import com.energyict.obis.ObisCode;

import java.util.Map;

/**
 *
 * @author  Koen
 */
public class EDPRegisterConfig extends RegisterConfig {

    public final int SCALER=3;

    public EDPRegisterConfig() {
        super();
    }

    protected void initRegisterMap() {
        map.put(ObisCode.fromString("1.1.9.8.0.255"),new Register("50",-1));
        map.put(ObisCode.fromString("1.1.9.6.0.255"),new Register("52",0));

        // cumulative maximum demand registers
        map.put(ObisCode.fromString("1.1.1.2.0.255"),new Register("2",-1));
        map.put(ObisCode.fromString("1.1.5.2.0.255"),new Register("32",-1));
        map.put(ObisCode.fromString("1.1.8.2.0.255"),new Register("33",-1));
        map.put(ObisCode.fromString("1.1.2.2.0.255"),new Register("3",-1));
        map.put(ObisCode.fromString("1.1.7.2.0.255"),new Register("42",-1));
        map.put(ObisCode.fromString("1.1.6.2.0.255"),new Register("43",-1));

        // current average registers
        map.put(ObisCode.fromString("1.1.1.4.0.255"),new Register("4",18)); // ??? Enermet???
        map.put(ObisCode.fromString("1.1.5.4.0.255"),new Register("34",-1));
        map.put(ObisCode.fromString("1.1.8.4.0.255"),new Register("35",-1));
        map.put(ObisCode.fromString("1.1.2.4.0.255"),new Register("5",-1));
        map.put(ObisCode.fromString("1.1.7.4.0.255"),new Register("44",-1));
        map.put(ObisCode.fromString("1.1.6.4.0.255"),new Register("45",-1));

        // maximum demand registers
        map.put(ObisCode.fromString("1.1.1.6.0.255"),new Register("6",-1));
        map.put(ObisCode.fromString("1.1.1.6.1.255"),new Register("6.1",1));
        map.put(ObisCode.fromString("1.1.1.6.2.255"),new Register("6.2",2));
        map.put(ObisCode.fromString("1.1.1.6.3.255"),new Register("6.3",-1));
        map.put(ObisCode.fromString("1.1.5.6.0.255"),new Register("36",-1));
        map.put(ObisCode.fromString("1.1.5.6.1.255"),new Register("36.1",8));
        map.put(ObisCode.fromString("1.1.5.6.2.255"),new Register("36.2",9));
        map.put(ObisCode.fromString("1.1.8.6.0.255"),new Register("37",-1));
        map.put(ObisCode.fromString("1.1.8.6.1.255"),new Register("37.1",13));
        map.put(ObisCode.fromString("1.1.8.6.1.255"),new Register("37.2",14));
        map.put(ObisCode.fromString("1.1.2.6.0.255"),new Register("7",-1));
        map.put(ObisCode.fromString("1.1.2.6.1.255"),new Register("7.1",-1));
        map.put(ObisCode.fromString("1.1.2.6.2.255"),new Register("7.2",-1));
        map.put(ObisCode.fromString("1.1.2.6.3.255"),new Register("7.3",-1));
        map.put(ObisCode.fromString("1.1.7.6.0.255"),new Register("46",-1));
        map.put(ObisCode.fromString("1.1.7.6.1.255"),new Register("46.1",-1));
        map.put(ObisCode.fromString("1.1.7.6.2.255"),new Register("46.2",-1));
        map.put(ObisCode.fromString("1.1.6.6.0.255"),new Register("47",-1));
        map.put(ObisCode.fromString("1.1.6.6.1.255"),new Register("47.1",-1));
        map.put(ObisCode.fromString("1.1.6.6.1.255"),new Register("47.2",-1));

        // time integral registers
        map.put(ObisCode.fromString("1.1.1.8.0.255"),new Register("20",7));
        map.put(ObisCode.fromString("1.1.1.8.1.255"),new Register("8.1",3));
        map.put(ObisCode.fromString("1.1.1.8.2.255"),new Register("8.2",4));
        map.put(ObisCode.fromString("1.1.1.8.3.255"),new Register("8.3",5));
        map.put(ObisCode.fromString("1.1.1.8.4.255"),new Register("8.4",6));
        map.put(ObisCode.fromString("1.1.2.8.0.255"),new Register("21",-1));
        map.put(ObisCode.fromString("1.1.2.8.1.255"),new Register("9.1",-1));
        map.put(ObisCode.fromString("1.1.2.8.2.255"),new Register("9.2",-1));
        map.put(ObisCode.fromString("1.1.2.8.3.255"),new Register("9.3",-1));
        map.put(ObisCode.fromString("1.1.2.8.4.255"),new Register("9.4",-1));
        map.put(ObisCode.fromString("1.1.5.8.0.255"),new Register("22",12));
        map.put(ObisCode.fromString("1.1.5.8.1.255"),new Register("38.1",10));
        map.put(ObisCode.fromString("1.1.5.8.2.255"),new Register("38.2",11));
        map.put(ObisCode.fromString("1.1.8.8.0.255"),new Register("23",17));
        map.put(ObisCode.fromString("1.1.8.8.1.255"),new Register("39.1",15));
        map.put(ObisCode.fromString("1.1.8.8.2.255"),new Register("39.2",16));
        map.put(ObisCode.fromString("1.1.7.8.0.255"),new Register("24",-1));
        map.put(ObisCode.fromString("1.1.7.8.1.255"),new Register("48.1",-1));
        map.put(ObisCode.fromString("1.1.7.8.2.255"),new Register("48.2",-1));
        map.put(ObisCode.fromString("1.1.6.8.0.255"),new Register("25",-1));
        map.put(ObisCode.fromString("1.1.6.8.1.255"),new Register("49.1",-1));
        map.put(ObisCode.fromString("1.1.6.8.2.255"),new Register("49.2",-1));
        // ????? KV 13122004 map.put(ObisCode.fromString("1.1.1.6.0.255"),new Register("4",18));

        // special purpose registers
        map.put(ObisCode.fromString("0.0.96.1.0.255"),new Register("0",-1)); // Serial number
        map.put(ObisCode.fromString("0.1.96.1.0.255"),new Register("0",-1)); // Serial number
        map.put(ObisCode.fromString("0.0.96.6.0.255"),new Register("14",-1)); // Battery operated hours counter
        map.put(ObisCode.fromString("0.1.96.6.0.255"),new Register("14",-1)); // Battery operated hours counter

        map.put(ObisCode.fromString("1.0.0.1.2.255"),new Register("0.1.2",-1)); // billng point timestamp
        map.put(ObisCode.fromString("1.1.0.1.2.255"),new Register("0.1.2",-1)); // billng point timestamp
        map.put(ObisCode.fromString("1.0.0.1.0.255"),new Register("1",-1)); // Billing reset counter
        map.put(ObisCode.fromString("1.1.0.1.0.255"),new Register("1",-1)); // Billing reset counter

        map.put(ObisCode.fromString("0.0.96.2.11.255"),new Register("95",-1)); // Date of last configuration program change (only a date yy-mm-dd)
        map.put(ObisCode.fromString("0.1.96.2.11.255"),new Register("95",-1)); // Date of last configuration program change (only a date yy-mm-dd)
        map.put(ObisCode.fromString("1.0.0.4.2.255"),new Register("97.1",-1)); // CT ("___" if not used!)
        map.put(ObisCode.fromString("1.1.0.4.2.255"),new Register("97.1",-1)); // CT ("___" if not used!)
        map.put(ObisCode.fromString("1.0.0.4.3.255"),new Register("97.2",-1)); // VT ("___" if not used!)
        map.put(ObisCode.fromString("1.1.0.4.3.255"),new Register("97.2",-1)); // VT ("___" if not used!)

        // Iskra specific (B=2)
        // Done automatically when b==2 in the obiscodemapper!
        //map.put(ObisCode.fromString("1.2.0.4.2.255"),new Register("0.4.2",-1)); // CT
        //map.put(ObisCode.fromString("1.2.0.4.3.255"),new Register("0.4.3",-1)); // VT
        //map.put(ObisCode.fromString("1.2.0.0.1.255"),new Register("0.0.1",-1)); // Programming ID

        map.put(ObisCode.fromString("0.0.97.97.0.255"),new Register("F",-1)); // error code of the meter
        map.put(ObisCode.fromString("0.1.97.97.0.255"),new Register("F",-1)); // error code of the meter

    }

    protected Map<ObisCode, Register> getRegisterMap() {
        return map;
    }

    public int getScaler() {
        return SCALER;
    }

}
