package com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.InvalidBooleanStateException;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.Register;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.device.data.RegisterValue;
import com.energyict.protocol.NoSuchRegisterException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author gna
 * Changes:
 * GNA |03062009| Added abstract registers (activity Calendar, Active Firmware)
 */
public class ObisCodeMapper {

	private boolean debug = false;
	private static final String[] possibleConnectStates = {"Disconnected","Connected","Ready for Reconnection"};

	CosemObjectFactory cof = new CosemObjectFactory(null);
    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");

	public ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
		cof = cosemObjectFactory;
	}

    public CosemObjectFactory getCosemObjectFactory() {
        return cof;
    }

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue rv = null;
		int billingPoint = -1;
		CosemObject co = null;

		if(debug){
			System.out.println(obisCode);
		}

		// Abstract Registers
        if(ACTIVITY_CALENDAR.equals(obisCode)){	// Activity Calendar
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        	return rv;
        } else if (obisCode.toString().indexOf("1.0.0.2.0.255") != -1){	// Core firmware (not upgradeable)
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			new String(cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1).getString()));
        	return rv;
        } else if (obisCode.toString().indexOf("1.1.0.2.0.255") != -1){	// Module firmware (upgradeable)
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			new String(cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1).getString()));
        	return rv;
        } else if (obisCode.toString().indexOf("1.0.0.2.8.255") != -1){	// Core firmware signature (not upgradeable)
            OctetString os = new OctetString(cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1).getResponseData(), 0);
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			ParseUtils.decimalByteToString(os.getOctetStr()));
        	return rv;
        } else if (obisCode.toString().indexOf("1.1.0.2.8.255") != -1){	// Module firmware signature (upgradeable)
            OctetString os = new OctetString(cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1).getResponseData(), 0);
        	rv = new RegisterValue(obisCode,
        			null,
        			null, null, null, new Date(), 0,
        			ParseUtils.decimalByteToString(os.getOctetStr()));
        	return rv;
        } else if (obisCode.toString().indexOf("0.0.96.3.128.255") != -1){	// E-meter connect control mode	- Use the E field as '128' to indicate the controlMode
        	int mode = cof.getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).getControlMode().getValue();
        	rv = new RegisterValue(obisCode,
        			new Quantity(BigDecimal.valueOf(mode), Unit.getUndefined()),
        			null, null, null, new Date(), 0,
        			new String("ConnectControl mode: " + mode));
        	return rv;
        } else if (obisCode.toString().indexOf("0.0.96.3.129.255") != -1){	// Current control status of the breaker - Use the E field as '129' to indicate the controlState
        	int state = cof.getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).getControlState().getValue();
        	if((state < 0) || (state > 2)){
        		throw new IllegalArgumentException("The connectControlState has an invalid value: " + state);
        	}
        	rv = new RegisterValue(obisCode,
        			new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()),
        			null, null, null, new Date(), 0,
        			new String("ConnectControl state: " + possibleConnectStates[state]));
        	return rv;
        } else if (obisCode.toString().indexOf("0.0.96.3.130.255") != -1) {    // Current status of the breaker as boolean - Use the E field as '130' to indicate the controlState
            boolean state = false;
            try {
                state = cof.getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).getState();
                Quantity quantity = new Quantity(state ? "1" : "0", Unit.getUndefined());
                return rv = new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, "State: " + state);
            } catch (InvalidBooleanStateException e) {
                Quantity quantity = new Quantity("-1", Unit.getUndefined());
                return rv = new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, e.getMessage());
            }
        } else if (obisCode.toString().indexOf("0.0.97.98.1.255") != -1) {
        	GenericRead gr = cof.getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1);
        	String text = getEncryptionText(gr.getValue());
        	rv = new RegisterValue(obisCode,
        			new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined()),
        			null, null, null, new Date(), 0, text);
        	return rv;
        } else if (obisCode.toString().indexOf("0.0.96.12.5.255") != -1) { // GSM Signal strength KP meter
            Register register = cof.getRegister(obisCode);
            Quantity quantity = new Quantity(register.getValue(), Unit.getUndefined());
			return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, register.getValue() + " dBm");
        }

    	//Electricity related ObisRegisters
    	if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() == 128)) && (obisCode.getC() >=1) && (obisCode.getC() <= 2)
			&& (obisCode.getD() == 8) && (obisCode.getE() >=0) && (obisCode.getE() <=4) && (obisCode.getF() == 255)){
			Register register = cof.getRegister(obisCode);
			return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
    	}

    	//Instantaneous, average values (voltage/current/power)
    	if((obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getE() == 0) && (obisCode.getF() == 255)){
    		if(obisCode.getD() == 7) { // instantaneous values
    			if((obisCode.getC() == 32) ||		// inst. voltage L1
    					(obisCode.getC() == 31) ||	// inst. current L1
    					(obisCode.getC() == 52) ||	// inst. voltage L2
    					(obisCode.getC() == 51) ||	// inst. current L2
    					(obisCode.getC() == 72) ||	// inst. voltage L3
    					(obisCode.getC() == 71) ||	// inst. current L3
    					(obisCode.getC() == 15) ||	// inst. active power (P+)
    					(obisCode.getC() == 90) ||  // inst. current
                        (obisCode.getC() == 4)  ||  // inst. ReAct. Power export Total
                        (obisCode.getC() == 3)  ||  // inst. ReAct. Power import Total
                        (obisCode.getC() == 2)  ||  // inst. Act. Power export Total
                        (obisCode.getC() == 1)){    // inst. Act. Power import Total
    				co = cof.getCosemObject(obisCode);
    			}
    		} else if(obisCode.getD() == 24) {
    			if((obisCode.getC() == 32) ||		// average voltage L1
    					(obisCode.getC() == 52) ||	// average voltage L2
    					(obisCode.getC() == 72)){	// average voltage L3
    				co = cof.getCosemObject(obisCode);
    			}
    		}

    		if(co != null){
    			return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(co));
    		}
    	}

    	// Power Qualities (voltage sags and swells)
    	if((obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getE() == 0) && (obisCode.getF() == 255)){
       		if(((obisCode.getC() == 12)	||
       				(obisCode.getC() == 32) ||
       				(obisCode.getC() == 52) ||
       				(obisCode.getC() == 72)) &&
       				((obisCode.getD() == 31) ||
       						(obisCode.getD() == 43) ||
       						(obisCode.getD() == 32) ||
       						(obisCode.getD() == 35) ||
       						(obisCode.getD() == 44) ||
       						(obisCode.getD() == 36))){
       			co = cof.getCosemObject(obisCode);
       			return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(co));
       		}
    	}

    	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

	/**
	 * Convert the received value to a readeable text
	 *
	 * @param value
	 * 		- the value from the alarm register
	 * @return UserFriendly text saying which Mbus decryption failed
	 */
	protected String getEncryptionText(long value) {
	    StringBuilder strBuilder = new StringBuilder();
	    long mask = 134217728;
	    for (int i = 0; i < 4; i++) {
		if((value&mask) == mask){
		    strBuilder.append("Decryption error on Mbus " + (i+1) + "\r\n");
		}
		mask = mask<<1;
	    }
	    if("".equalsIgnoreCase(strBuilder.toString())){
		return "No encryption errors on Mbus channels";
	    } else {
		return strBuilder.toString();
	    }
	}

}
