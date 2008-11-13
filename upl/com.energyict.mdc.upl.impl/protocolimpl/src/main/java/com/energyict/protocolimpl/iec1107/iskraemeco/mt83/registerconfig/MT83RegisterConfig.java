/*
 * EDPRegisters.java
 *
 * Created on 18 oktober 2004, 16:58
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83;

/**
 *
 * @author  Koen
 */
public class MT83RegisterConfig extends RegisterConfig {
    
    final public int SCALER=3;
    private static final int DEBUG = 0;  
    
    /** Creates a new instance of EDPRegisters */
    public MT83RegisterConfig() {
        super();
    }
    
    protected void initRegisterMap() {
    	
    	addToMap("0.0.96.1.0.255", "Serial", 1, 0, 1);
    	addToMap("0.0.97.97.0.255", "Errorcode", 2, 0, 1);
    	
    	// Voltage phase x Instantaneous value 
    	addToMap("1.0.32.7.0.255");
    	addToMap("1.0.52.7.0.255");
    	addToMap("1.0.72.7.0.255");

    	// Current phase x Instantaneous value 
    	addToMap("1.0.31.7.0.255");
    	addToMap("1.0.51.7.0.255");
    	addToMap("1.0.71.7.0.255");

    	// Phi phase x Instantaneous value 
    	addToMap("1.0.81.7.40.255", "Phi (°) phase 1 Instantaneous value");
    	addToMap("1.0.81.7.51.255", "Phi (°) phase 2 Instantaneous value");
    	addToMap("1.0.81.7.62.255", "Phi (°) phase 3 Instantaneous value");

    	// Powerfactor phase x Instantaneous value 
    	addToMap("1.0.33.7.0.255");
    	addToMap("1.0.53.7.0.255");
    	addToMap("1.0.73.7.0.255");

    	// Frequency all phases Instantaneous value (Hz)
    	addToMap("1.0.14.7.0.255");

    	// Active power+ all phases rate 1 Maximum using measurement period 1 in billing period xx (0 to 15)
    	addToMap("1.1.1.6.1.255");
    	addToMap("1.1.1.6.1.0", 6, 0, 15);

    	// Active power+ all phases Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.1.1.8.0.255");
    	addToMap("1.1.1.8.0.0", 6, 0, 15);

    	// Active power+ all phases rate 1 Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.1.1.8.1.255");
    	addToMap("1.1.1.8.1.0", 6, 0, 15);
    	
    	// Active power+ all phases rate 2 Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.1.1.8.2.255");
    	addToMap("1.1.1.8.2.0", 6, 0, 15);
    	
    	// Active power+ all phases rate 3 Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.1.1.8.3.255");
    	addToMap("1.1.1.8.3.0", 6, 0, 15);

    	// Reactive power QI all phases rate 1 Maximum using measurement period 1 in billing period xx (0 to 15)
    	addToMap("1.2.5.6.1.255");
    	addToMap("1.2.5.6.1.0", 6, 0, 15);

    	// Reactive power QI all phases rate 2 Maximum using measurement period 1 in billing period xx (0 to 15)
    	addToMap("1.2.5.6.2.255");
    	addToMap("1.2.5.6.2.0", 6, 0, 15);
    	
    	// Reactive power QIV all phases rate 1 Maximum using measurement period 1 in billing period xx (0 to 15)
    	addToMap("1.2.8.6.1.255");
    	addToMap("1.2.8.6.1.0", 6, 0, 15);
    	
    	// Reactive power QIV all phases rate 2 Maximum using measurement period 1 in billing period xx (0 to 15)
    	addToMap("1.2.8.6.2.255");
    	addToMap("1.2.8.6.2.0", 6, 0, 15);
    	
    	// Reactive power QI all phases Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.2.5.8.0.255");
    	addToMap("1.2.5.8.0.0", 6, 0, 15);

    	// Reactive power QI all phases rate 1 Time integral from start of measurement to billing point xx in billing period xx	(0 to 15)
    	addToMap("1.2.5.8.1.255");
    	addToMap("1.2.5.8.1.0", 6, 0, 15);
    	
    	// Reactive power QI all phases rate 2 Time integral from start of measurement to billing point xx in billing period xx	(0 to 15)
    	addToMap("1.2.5.8.2.255");
    	addToMap("1.2.5.8.2.0", 6, 0, 15);
    	
    	// Reactive power QIV all phases Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.2.8.8.0.255");
    	addToMap("1.2.8.8.0.0", 6, 0, 15);
    	
    	// Reactive power QIV all phases rate 1 Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.2.8.8.1.255");
    	addToMap("1.2.8.8.1.0", 6, 0, 15);
    	
    	// Reactive power QIV all phases rate 2 Time integral from start of measurement to billing point xx in billing period xx (0 to 15)
    	addToMap("1.2.8.8.2.255");
    	addToMap("1.2.8.8.2.0", 6, 0, 15);
    	    	
    	// CT numerator
    	addToMap("1.0.0.4.2.255");
    	
    	// VT numerator
    	addToMap("1.0.0.4.3.255");
    	
    	// Programming ID
    	addToMap("1.2.0.0.1.255", "[Iskra specific] Programming ID");
    	    	
//        // cumulative maximum demand registers
//        map.put(ObisCode.fromString("1.1.1.2.0.255"),new Register("2",-1));
//        map.put(ObisCode.fromString("1.1.5.2.0.255"),new Register("32",-1));
//        map.put(ObisCode.fromString("1.1.8.2.0.255"),new Register("33",-1));
//        map.put(ObisCode.fromString("1.1.2.2.0.255"),new Register("3",-1));
//        map.put(ObisCode.fromString("1.1.7.2.0.255"),new Register("42",-1));
//        map.put(ObisCode.fromString("1.1.6.2.0.255"),new Register("43",-1));
//        
//        // current average registers
//        map.put(ObisCode.fromString("1.1.1.4.0.255"),new Register("4",18)); // ??? Enermet???
//        map.put(ObisCode.fromString("1.1.5.4.0.255"),new Register("34",-1));
//        map.put(ObisCode.fromString("1.1.8.4.0.255"),new Register("35",-1));
//        map.put(ObisCode.fromString("1.1.2.4.0.255"),new Register("5",-1));
//        map.put(ObisCode.fromString("1.1.7.4.0.255"),new Register("44",-1));
//        map.put(ObisCode.fromString("1.1.6.4.0.255"),new Register("45",-1));
//        
//        // maximum demand registers
//        map.put(ObisCode.fromString("1.1.1.6.0.255"),new Register("6",-1));
//        map.put(ObisCode.fromString("1.1.1.6.1.255"),new Register("6.1",1));
//        map.put(ObisCode.fromString("1.1.1.6.2.255"),new Register("6.2",2));
//        map.put(ObisCode.fromString("1.1.1.6.3.255"),new Register("6.3",-1));
//        map.put(ObisCode.fromString("1.1.5.6.0.255"),new Register("36",-1));
//        map.put(ObisCode.fromString("1.1.5.6.1.255"),new Register("36.1",8));
//        map.put(ObisCode.fromString("1.1.5.6.2.255"),new Register("36.2",9));
//        map.put(ObisCode.fromString("1.1.8.6.0.255"),new Register("37",-1));
//        map.put(ObisCode.fromString("1.1.8.6.1.255"),new Register("37.1",13));
//        map.put(ObisCode.fromString("1.1.8.6.1.255"),new Register("37.2",14));
//        map.put(ObisCode.fromString("1.1.2.6.0.255"),new Register("7",-1));
//        map.put(ObisCode.fromString("1.1.2.6.1.255"),new Register("7.1",-1));
//        map.put(ObisCode.fromString("1.1.2.6.2.255"),new Register("7.2",-1));
//        map.put(ObisCode.fromString("1.1.2.6.3.255"),new Register("7.3",-1));
//        map.put(ObisCode.fromString("1.1.7.6.0.255"),new Register("46",-1));
//        map.put(ObisCode.fromString("1.1.7.6.1.255"),new Register("46.1",-1));
//        map.put(ObisCode.fromString("1.1.7.6.2.255"),new Register("46.2",-1));
//        map.put(ObisCode.fromString("1.1.6.6.0.255"),new Register("47",-1));
//        map.put(ObisCode.fromString("1.1.6.6.1.255"),new Register("47.1",-1));
//        map.put(ObisCode.fromString("1.1.6.6.1.255"),new Register("47.2",-1));
//        
//        // time integral registers
//        map.put(ObisCode.fromString("1.1.1.8.0.255"),new Register("20",7));
//        map.put(ObisCode.fromString("1.1.1.8.1.255"),new Register("8.1",3));
//        map.put(ObisCode.fromString("1.1.1.8.2.255"),new Register("8.2",4));
//        map.put(ObisCode.fromString("1.1.1.8.3.255"),new Register("8.3",5));
//        map.put(ObisCode.fromString("1.1.1.8.4.255"),new Register("8.4",6));
//        map.put(ObisCode.fromString("1.1.2.8.0.255"),new Register("21",-1));
//        map.put(ObisCode.fromString("1.1.2.8.1.255"),new Register("9.1",-1));
//        map.put(ObisCode.fromString("1.1.2.8.2.255"),new Register("9.2",-1));
//        map.put(ObisCode.fromString("1.1.2.8.3.255"),new Register("9.3",-1));
//        map.put(ObisCode.fromString("1.1.2.8.4.255"),new Register("9.4",-1));
//        map.put(ObisCode.fromString("1.1.5.8.0.255"),new Register("22",12));
//        map.put(ObisCode.fromString("1.1.5.8.1.255"),new Register("38.1",10));
//        map.put(ObisCode.fromString("1.1.5.8.2.255"),new Register("38.2",11));
//        map.put(ObisCode.fromString("1.1.8.8.0.255"),new Register("23",17));
//        map.put(ObisCode.fromString("1.1.8.8.1.255"),new Register("39.1",15));
//        map.put(ObisCode.fromString("1.1.8.8.2.255"),new Register("39.2",16));
//        map.put(ObisCode.fromString("1.1.7.8.0.255"),new Register("24",-1));
//        map.put(ObisCode.fromString("1.1.7.8.1.255"),new Register("48.1",-1));
//        map.put(ObisCode.fromString("1.1.7.8.2.255"),new Register("48.2",-1));
//        map.put(ObisCode.fromString("1.1.6.8.0.255"),new Register("25",-1));
//        map.put(ObisCode.fromString("1.1.6.8.1.255"),new Register("49.1",-1));
//        map.put(ObisCode.fromString("1.1.6.8.2.255"),new Register("49.2",-1));
//        // ????? KV 13122004 map.put(ObisCode.fromString("1.1.1.6.0.255"),new Register("4",18));
//        
//        
//        
//        // special purpose registers
//        map.put(ObisCode.fromString("0.0.96.1.0.255"),new Register("0",-1)); // Serial number
//        map.put(ObisCode.fromString("0.1.96.1.0.255"),new Register("0",-1)); // Serial number
//        map.put(ObisCode.fromString("0.0.96.6.0.255"),new Register("14",-1)); // Battery operated hours counter
//        map.put(ObisCode.fromString("0.1.96.6.0.255"),new Register("14",-1)); // Battery operated hours counter
//        
//        map.put(ObisCode.fromString("1.0.0.1.2.255"),new Register("0.1.2",-1)); // billng point timestamp
//        map.put(ObisCode.fromString("1.1.0.1.2.255"),new Register("0.1.2",-1)); // billng point timestamp
//        map.put(ObisCode.fromString("1.0.0.1.0.255"),new Register("1",-1)); // Billing reset counter
//        map.put(ObisCode.fromString("1.1.0.1.0.255"),new Register("1",-1)); // Billing reset counter
//        
//        map.put(ObisCode.fromString("0.0.96.2.11.255"),new Register("95",-1)); // Date of last configuration program change (only a date yy-mm-dd)
//        map.put(ObisCode.fromString("0.1.96.2.11.255"),new Register("95",-1)); // Date of last configuration program change (only a date yy-mm-dd)
//        map.put(ObisCode.fromString("1.0.0.4.2.255"),new Register("97.1",-1)); // CT ("___" if not used!)
//        map.put(ObisCode.fromString("1.1.0.4.2.255"),new Register("97.1",-1)); // CT ("___" if not used!)
//        map.put(ObisCode.fromString("1.0.0.4.3.255"),new Register("97.2",-1)); // VT ("___" if not used!)
//        map.put(ObisCode.fromString("1.1.0.4.3.255"),new Register("97.2",-1)); // VT ("___" if not used!)
//
//        // Iskra specific (B=2)
//        // Done automatically when b==2 in the obiscodemapper!
//        //map.put(ObisCode.fromString("1.2.0.4.2.255"),new Register("0.4.2",-1)); // CT 
//        //map.put(ObisCode.fromString("1.2.0.4.3.255"),new Register("0.4.3",-1)); // VT 
//        //map.put(ObisCode.fromString("1.2.0.0.1.255"),new Register("0.0.1",-1)); // Programming ID 
//        
        
    }
    
    
    
    protected Map getRegisterMap() {
        return map;
    }
    

    private void addToMap(String obisString, int field, int startvalue, int endvalue) {
    	if (field < 1 || field > 6) return;
    	if (startvalue < 0x00 || startvalue > 0xFF) return;
    	if (endvalue < 0x00 || endvalue > 0xFF) return;
    	if (startvalue > endvalue) return;

    	ObisCode oc = ObisCode.fromString(obisString);

    	for (int i = startvalue; i <= endvalue; i++) {
    		oc = new ObisCode(
    				(field == 1) ? i : oc.getA(), 
    	    		(field == 2) ? i : oc.getB(), 
    	    	    (field == 3) ? i : oc.getC(), 
    	    	    (field == 4) ? i : oc.getD(), 
    	    	    (field == 5) ? i : oc.getE(), 
    	    	    (field == 6) ? i : oc.getF());	
    		
    		addToMap(oc.toString(), oc.getDescription());
    	}
	}
    
    private void addToMap(String obisString, String description, int field, int startvalue, int endvalue) {
    	if (field < 1 || field > 6) return;
    	if (startvalue < 0x00 || startvalue > 0xFF) return;
    	if (endvalue < 0x00 || endvalue > 0xFF) return;
    	if (startvalue > endvalue) return;

    	ObisCode oc = ObisCode.fromString(obisString);

    	for (int i = startvalue; i <= endvalue; i++) {
    		oc = new ObisCode(
    				(field == 1) ? i : oc.getA(), 
    	    		(field == 2) ? i : oc.getB(), 
    	    	    (field == 3) ? i : oc.getC(), 
    	    	    (field == 4) ? i : oc.getD(), 
    	    	    (field == 5) ? i : oc.getE(), 
    	    	    (field == 6) ? i : oc.getF());	
    		
    		addToMap(oc.toString(), description);
    	}
    }

    private void addToMap(String obisString) {
    	this.addToMap(obisString, ObisCode.fromString(obisString).getDescription());
	}
    
    private void addToMap(String obisString, String description) {
    	map.put(ObisCode.fromString(obisString),new Register(description, 0)); 
    }

    public int getScaler() {
        return SCALER;
    }
    
    
    
}
