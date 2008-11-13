/*
 * MT83RegisterConfig.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.MT83;

/**
 *
 * @author  jme
 */
public class MT83RegisterConfig extends RegisterConfig {
    
    final public int SCALER=3;
    private static final int DEBUG = 0;  
    
    /** Creates a new instance of MT83RegisterConfig */
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
    	    	
    }
    
    protected Map getRegisterMap() {
        return map;
    }

    /**
     * 
     * Add list of obiscodes to the map, using the name generated from ObisCode.fromString(String string)<br>
     * The field is the changing obis field (1-6 <=> A-F)
     * 
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     * @param field			Obisfield (1-6 <=> A-F)
     * @param startvalue	Startvalue for obisfield
     * @param endvalue		Endvalue for obisfield
     */
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
    
    /**
     * 
     * Add list of obiscodes to the map, all using the same description <br>
     * The field is the changing obis field (1-6 <=> A-F)
     * 
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     * @param description	Description of the obiscode eg: "Billing reset counter"
     * @param field			Obisfield (1-6 <=> A-F)
     * @param startvalue	Startvalue for obisfield
     * @param endvalue		Endvalue for obisfield
     */
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

    /**
     * 
     * Add a single obiscode to the map, using the name generated from ObisCode.fromString(String string)<br>
     * 
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     */
    private void addToMap(String obisString) {
    	this.addToMap(obisString, ObisCode.fromString(obisString).getDescription());
	}
    
    /**
     * 
     * Add a single obiscode to the map, using the given description as name.
     * 
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     * @param description	Description of the obiscode eg: "Billing reset counter"
     */
    private void addToMap(String obisString, String description) {
    	map.put(ObisCode.fromString(obisString),new Register(description, 0)); 
    }

    public int getScaler() {
        return SCALER;
    }
    
}
