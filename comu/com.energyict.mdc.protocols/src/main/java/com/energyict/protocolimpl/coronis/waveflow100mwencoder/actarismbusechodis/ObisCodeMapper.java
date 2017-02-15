/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.ActarisMBusInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.MBusInternalLogs.HistoricalValue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

	static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();

	static {
		// get the common obis codes from the common obis code mapper

		// specific Actaris mbus meters registers
		//registerMaps.put(ObisCode.fromString("0.0.96.6.68.255"), "Port A encoder internal data raw string");
		//registerMaps.put(ObisCode.fromString("0.0.96.6.69.255"), "Port B encoder internal data raw string");


		// MBus data parsed registers
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.0"), "Fabrication Number");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.1"), "Volume");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.2"), "Volume Flow");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.3"), "Date and Time (actual or associated with a storage number/function)");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.4"), "Duration of meter accumulation");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.5"), "Duration of meter accumulation");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.6"), "Metrology Firmware Version");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.7"), "Other software version");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.8"), "Volume Flow, End date/time of last upper linit exceed");
		registerMaps.put(ObisCode.fromString("0.0.96.99.0.9"), "Volume Flow");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.248"), "MBUS header meter ID ACW (477)");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.249"), "MBUS header manufacturer identification (477)");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.250"), "MBUS header version (c)");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.251"), "MBUS header devicetype DeviceType: Water (7)");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.252"), "MBUS header access number");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.253"), "MBUS header identification number");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.254"), "MBUS header status byte");
		registerMaps.put(ObisCode.fromString("0.0.96.99.255.255"), "MBUS header signature field");

		registerMaps.put(ObisCode.fromString("0.1.96.99.0.0"), "Fabrication Number");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.1"), "Volume");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.2"), "Volume Flow");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.3"), "Date and Time (actual or associated with a storage number/function)");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.4"), "Duration of meter accumulation");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.5"), "Duration of meter accumulation");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.6"), "Metrology Firmware Version");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.7"), "Other software version");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.8"), "Volume Flow, End date/time of last upper linit exceed");
		registerMaps.put(ObisCode.fromString("0.1.96.99.0.9"), "Volume Flow");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.248"), "MBUS header meter ID ACW (477)");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.249"), "MBUS header manufacturer identification (477)");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.250"), "MBUS header version (c)");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.251"), "MBUS header devicetype DeviceType: Water (7)");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.252"), "MBUS header access number");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.253"), "MBUS header identification number");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.254"), "MBUS header status byte");
		registerMaps.put(ObisCode.fromString("0.1.96.99.255.255"), "MBUS header signature field");

        registerMaps.put(ObisCode.fromString("1.1.96.1.0.255"), "Meter serial number on port A");
        registerMaps.put(ObisCode.fromString("1.2.96.1.0.255"), "Meter serial number on port B");

        registerMaps.put(ObisCode.fromString("0.1.96.5.2.255"), "Alarm code for meter on port A");
        registerMaps.put(ObisCode.fromString("0.2.96.5.2.255"), "Alarm code for meter on port B");

		registerMaps.put(ObisCode.fromString("8.1.1.0.0.255"), "Port A current index");
		registerMaps.put(ObisCode.fromString("8.2.1.0.0.255"), "Port B current index");


//		for (int historicalIndex=0;historicalIndex<13;historicalIndex++) {
//			registerMaps.put(ObisCode.fromString("8.1.1.0.0."+historicalIndex), "Port A encoder current index");
//			registerMaps.put(ObisCode.fromString("8.2.1.0.0."+historicalIndex), "Port B encoder current index");
//		}

	}

	private Echodis echodis;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(final Echodis echodis) {
        this.echodis=echodis;
    }

    final String getRegisterExtendedLogging() {

    	StringBuilder strBuilder=new StringBuilder();

    	Iterator<Entry<ObisCode,String>> it = registerMaps.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,String> o = it.next();
    		echodis.getLogger().info(o.getKey().toString()+", "+o.getValue());
    	}

    	strBuilder.append(echodis.getCommonObisCodeMapper().getRegisterExtendedLogging());

    	return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	String info = registerMaps.get(obisCode);
    	if (info !=null) {
    		return new RegisterInfo(info);
    	}
    	else {
    		return CommonObisCodeMapper.getRegisterInfo(obisCode);
    	}
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		try {
            if (obisCode.equals(ObisCode.fromString("1.1.96.1.0.255")) || obisCode.equals(ObisCode.fromString("1.2.96.1.0.255"))) {
                int portId = obisCode.getB() - 1;
                ActarisMBusInternalData internalData = (ActarisMBusInternalData) echodis.readInternalDatas()[portId];
                if (internalData == null) {
                    throw new NoSuchRegisterException("No encoder connected to port " + (portId == 1 ? "A" : "B"));
                }
                return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, internalData.getSerialNumber());
            }
            if (obisCode.equals(ObisCode.fromString("0.1.96.5.2.255")) || obisCode.equals(ObisCode.fromString("0.2.96.5.2.255"))) {
                int portId = obisCode.getB() - 1;
                ActarisMBusInternalData internalData = (ActarisMBusInternalData) echodis.readInternalDatas()[portId];
                if (internalData == null) {
                    throw new NoSuchRegisterException("No encoder connected to port " + (portId == 1 ? "A" : "B"));
                }
                return new RegisterValue(obisCode, new Quantity(internalData.getAlarmCode(), Unit.get("")));
            }
	    	if ((obisCode.equals(ObisCode.fromString("0.0.96.6.68.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.69.255")))) {
	    		// encoder internal data
	    		int portId = obisCode.getE()-68;
	    		ActarisMBusInternalData o = (ActarisMBusInternalData)echodis.readInternalDatas()[portId];
	    		if (o==null) {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
	    		}
	    		else {
	    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, ProtocolUtils.outputHexString(o.getEncoderInternalData()));
	    		}
	    	}
	    	else if ((obisCode.equals(ObisCode.fromString("8.1.1.0.0.255")))) { // port A
	    		return echodis.getMbusRegisterValue(ObisCode.fromString("0.0.96.99.0.1"));
	    	}
	    	else if (obisCode.equals(ObisCode.fromString("8.2.1.0.0.255"))) { // Port B
	    		return echodis.getMbusRegisterValue(ObisCode.fromString("0.1.96.99.0.1"));
	    	}
	    	else if ((obisCode.getA()==8) && (obisCode.getC()==1) && (obisCode.getD()==0) && (obisCode.getE()==0)) { // port A

	    		int portId = obisCode.getB()-1;
	    		if ((portId==0) || (portId==1)) {
	    			int historicalId=obisCode.getF();

	    			if (historicalId<0) {
	    				historicalId = Math.abs(obisCode.getF());
	    			}
	    			if ((historicalId>=0) && (historicalId<=12)) {
	    	    		if (historicalId < echodis.getRadioCommandFactory().readMBusInternalLogs(portId).getHistoricalValues().size()) {
	    	    			HistoricalValue o = echodis.getRadioCommandFactory().readMBusInternalLogs(portId).getHistoricalValues().get(historicalId);
	    	    			return new RegisterValue(obisCode, o.getValue(), o.getCal().getTime());
	    	    		}
	    			}
	    		}
	    	}

    		try {
    			return echodis.getCommonObisCodeMapper().getRegisterValue(obisCode);
    		}
    		catch(NoSuchRegisterException e) {
    			return echodis.getMbusRegisterValue(obisCode);
    		}

		} catch (IOException e) {

			throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");

		}

    }

}
