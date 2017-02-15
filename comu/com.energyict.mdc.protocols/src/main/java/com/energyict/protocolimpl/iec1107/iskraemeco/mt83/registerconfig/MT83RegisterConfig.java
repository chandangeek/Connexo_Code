/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MT83RegisterConfig.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.util.Map;

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

    	// Serial, Programming ID and error register
    	addToMap("0.0.96.1.0.255", "Device serial number", null);
    	addToMap("0.0.96.1.1.255", "[Iskra specific] Programming ID", null);
    	addToMap("0.0.97.97.0.255", "Errorcode", null);

    	// Voltage phase x Instantaneous value
    	addToMap("1.1.32.7.0.255", "Voltage phase 1 Instantaneous value", null);
    	addToMap("1.1.52.7.0.255", "Voltage phase 2 Instantaneous value", null);
    	addToMap("1.1.72.7.0.255", "Voltage phase 3 Instantaneous value", null);

    	// Current phase x Instantaneous value
    	addToMap("1.1.31.7.0.255", "Current phase 1 Instantaneous value", null);
    	addToMap("1.1.51.7.0.255", "Current phase 2 Instantaneous value", null);
    	addToMap("1.1.71.7.0.255", "Current phase 3 Instantaneous value", null);

    	// Phi phase x Instantaneous value
    	addToMap("1.1.81.7.40.255", "Phi (°) phase 1 Instantaneous value", null);
    	addToMap("1.1.81.7.51.255", "Phi (°) phase 2 Instantaneous value", null);
    	addToMap("1.1.81.7.62.255", "Phi (°) phase 3 Instantaneous value", null);

    	// Powerfactor phase x Instantaneous value
    	addToMap("1.1.33.7.0.255", "Powerfactor phase 1 Instantaneous value", null);
    	addToMap("1.1.53.7.0.255", "Powerfactor phase 2 Instantaneous value", null);
    	addToMap("1.1.73.7.0.255", "Powerfactor phase 3 Instantaneous value", null);

    	// Frequency all phases Instantaneous value (Hz)
    	addToMap("1.1.14.7.0.255", "Frequency all phases Instantaneous value", null);

    	// CT and VT numerator
    	addToMap("1.1.0.4.2.255", "Transformer ratio - Current (numerator)", "1.0.0.4.2.255");
    	addToMap("1.1.0.4.3.255", "Transformer ratio - Voltage (numerator)", "1.0.0.4.2.255");

    	// Billing points timestamps
    	addToMap("1.1.0.1.2.255", 6, 0, 14, 1, "1.0.0.1.2.255");
        addToMap("1.1.0.1.2.255", "LastBillingTimeStamp", "1.1.0.1.2.255");
        addToMap("1.1.0.1.0.255", "Billing counter", "1.1.0.1.0.255");

        // FirmwareVersion
        addToMap("0.0.96.1.5.255", "FirmwareVersion", "0.0.96.1.5.255");

        // add Programming counter
        addToMap("0.0.96.1.4.255", "Date of last program change", "0.0.96.1.4.255");

        //Battery usage counter
        addToMap("0.0.96.6.0.255", "Battery Usage counter", "0.0.96.6.0.255");

    	return;
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
    private void addToMap(String obisString, int field, int startvalue, int endvalue, int deviceCodeOffset, String deviceObisMapping) {
    	if (field < 1 || field > 6) return;
    	if (startvalue < 0x00 || startvalue > 0xFF) return;
    	if (endvalue < 0x00 || endvalue > 0xFF) return;
    	if (startvalue > endvalue) return;

    	if (deviceObisMapping == null) deviceObisMapping = obisString;

    	ObisCode oc = ObisCode.fromString(obisString);
    	ObisCode doc = ObisCode.fromString(deviceObisMapping);

    	for (int i = startvalue; i <= endvalue; i++) {
    		oc = new ObisCode(
    				(field == 1) ? i : oc.getA(),
    	    		(field == 2) ? i : oc.getB(),
    	    	    (field == 3) ? i : oc.getC(),
    	    	    (field == 4) ? i : oc.getD(),
    	    	    (field == 5) ? i : oc.getE(),
    	    	    (field == 6) ? i : oc.getF());

    		doc = new ObisCode(
    				(field == 1) ? i+deviceCodeOffset : doc.getA(),
    	    		(field == 2) ? i+deviceCodeOffset : doc.getB(),
    	    	    (field == 3) ? i+deviceCodeOffset : doc.getC(),
    	    	    (field == 4) ? i+deviceCodeOffset : doc.getD(),
    	    	    (field == 5) ? i+deviceCodeOffset : doc.getE(),
    	    	    (field == 6) ? i+deviceCodeOffset : doc.getF());

    		addToMap(oc.toString(), oc.getDescription(), doc.toString());
    	}
	}

    private void addToMapInclBillingPoints(String obisString) {
    	ObisCode doc = null;
    	ObisCode oc = ObisCode.fromString(obisString);
    	map.put(oc,new Register(oc.getDescription(), 0));
    	getDeviceRegisterMapping().put(oc.toString(), oc.getDescription());

    	String stringValue = oc.getA() + "." + oc.getB() + "." + oc.getC() + "." + oc.getD() + "." + oc.getE() + ".";
    	for (int i = 0; i <= 14; i++) {
    		oc = ObisCode.fromString(stringValue + i);
    		doc = ObisCode.fromString(stringValue + (i+1));
        	map.put(oc,new Register(oc.getDescription(), 0));
        	getDeviceRegisterMapping().put(oc.toString(), doc.getDescription());
    	}
	}

     /**
     *
     * Add a single obiscode to the map, using the name generated from ObisCode.fromString(String string)<br>
     *
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     */
    private void addToMap(String obisString, String deviceObisMapping) {
    	this.addToMap(obisString, ObisCode.fromString(obisString).getDescription(), deviceObisMapping);
	}

    /**
     *
     * Add a single obiscode to the map, using the given description as name.
     *
     * @param obisString	The obis code as string eg: "1.0.3.6.0.255"
     * @param description	Description of the obiscode eg: "Billing reset counter"
     */
    private void addToMap(String obisString, String description, String deviceObisMapping) {
    	map.put(ObisCode.fromString(obisString),new Register(description, 0));
    	if (deviceObisMapping == null) deviceObisMapping = obisString;
    	getDeviceRegisterMapping().put(obisString, deviceObisMapping);
    }

    public int getScaler() {
        return SCALER;
    }

    public ObisCode obisToDeviceCode(ObisCode obis) throws NoSuchRegisterException {
    	String deviceCode = (String) getDeviceRegisterMapping().get(obis.toString());
    	if (deviceCode == null) {
    		deviceCode = genDeviceCode(obis);
    	}
    	return ObisCode.fromString(deviceCode);
	}

    public ObisCode deviceCodeToObis(ObisCode deviceCode) throws NoSuchRegisterException {
       	if (deviceCode == null) throw new NoSuchRegisterException("Register " + deviceCode + " not found!");
    	if (!getDeviceRegisterMapping().containsValue(deviceCode.toString())) return null;
    	String obis = null;
    	String[] mapArrayValues = (String[]) getDeviceRegisterMapping().values().toArray();
    	String[] mapArrayKeys = (String[]) getDeviceRegisterMapping().keySet().toArray();
    	for (int i = 0; i < mapArrayKeys.length; i++) {
			if (mapArrayKeys[i].equalsIgnoreCase(deviceCode.toString())) {
				obis = mapArrayKeys[i];
				break;
			}
		}

    	if (obis == null) {
    		if (deviceCode.getF() != 255) {
    			obis = (new ObisCode(deviceCode.getA(), deviceCode.getB(), deviceCode.getC(), deviceCode.getD(), deviceCode.getE(), deviceCode.getF() - 1)).toString();
    		} else {
    			obis = deviceCode.toString();
    		}
    	}

		return ObisCode.fromString(obis );
	}

    private String genDeviceCode(ObisCode obis) throws NoSuchRegisterException {
        int billing;
        if (!checkRegister(obis)) {
            throw new NoSuchRegisterException("Register not found!");
	}
        if (obis.getF() != 255) {
            billing = 1 - obis.getF();
        } else {
            billing = 255;
        }
        return (new ObisCode(obis.getA(), obis.getB(), obis.getC(), obis.getD(), obis.getE(), billing)).toString();
    }


}
