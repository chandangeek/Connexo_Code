/**
 * UNIFLO1200.java
 * 
 * Created on 4-dec-2008, 15:00:50 by jme
 * 
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.discover.*;
import com.energyict.protocolimpl.modbus.core.*;


/**
 * @author jme
 *
 */
public class UNIFLO1200 extends Modbus {
    private static final int DEBUG = 1;

	public String getProtocolVersion() {
        return "$Revision: 1.2 $";
    }

	public Date getTime() throws IOException {
		return getRegisterFactory().findRegister(RegisterFactory.REG_TIME).dateValue();
	}
	
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return (String) getRegisterFactory().findRegister(RegisterFactory.REG_DEVICE_TYPE).value();
	}
	
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
        	String parserName = getRegisterFactory().findRegister(obisCode).getParser();
        	
        	AbstractRegister hr = getRegisterFactory().findRegister(obisCode);
        	Object result = hr.value();

        	sendDebug(hr.getUnit().toString(), 0);
        	
        	Class rc = result.getClass();

        	sendDebug("Result class type: " + result.getClass().getName(), 0);
        	
        	if (rc == String.class)	return new RegisterValue(obisCode, (String)result);
        	else if (rc == Date.class) return new RegisterValue(obisCode, (Date)result);
        	else if (rc == Quantity.class) return new RegisterValue(obisCode, (Quantity)result);
        	else if (rc == Integer.class) return new RegisterValue(obisCode, new Quantity(new BigDecimal((Integer)result), Unit.get("")));
        	else return new RegisterValue(obisCode, result.toString());
        	
        }
        catch(ModbusException e) {
            if ((e.getExceptionCode()==0x02) && (e.getFunctionErrorCode()==0x83))
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            else
                throw e;
        }
	}
	
	protected void doTheConnect() throws IOException {
		sendDebug("doTheConnect()", 5);
		// TODO Auto-generated method stub
		
	}

	protected void doTheDisConnect() throws IOException {
		sendDebug("doTheDisConnect()", 5);
		// TODO Auto-generated method stub
		
	}

	protected List doTheGetOptionalKeys() {
		sendDebug("doTheGetOptionalKeys()", 5);
		// TODO Auto-generated method stub
		return null;
	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		sendDebug("doTheValidateProperties()", 5);
		// TODO Auto-generated method stub
		
	}

	protected void initRegisterFactory() {
		sendDebug("initRegisterFactory()", 5);
        setRegisterFactory(new RegisterFactory(this));
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		sendDebug("discover()", 5);
		// TODO Auto-generated method stub
		return null;
	}
	
    public void sendDebug(String message, int debuglvl) {
		message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
		System.out.println(message);
    	if ((debuglvl <= DEBUG) && (getLogger() != null)) {
    		getLogger().info(message);
    		System.out.println(message);
    	}
    }

}
