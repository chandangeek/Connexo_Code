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
import com.sun.xml.internal.ws.client.SenderException;


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
        	Unit returnUnit = null;
        	String returnText = null;
        	Date returnEventTime = null;
        	Date returnFromTime = null;
        	Date returnToTime = null;
        	Date returnReadTime = null;
        	Quantity returnQuantity = null;
        	int returnRtuRegisterID = 0;
        	
		try {
        	String parserName = getRegisterFactory().findRegister(obisCode).getParser();
        	
        	AbstractRegister hr = getRegisterFactory().findRegister(obisCode);

        	sendDebug("Result obiscode:         " + obisCode, 0);
        	sendDebug("Result register address: " + ProtocolUtils.buildStringHex(hr.getReg(), 4), 0);
        	sendDebug("Result register range:   " +ProtocolUtils.buildStringHex(hr.getRange(), 4), 0);

        	returnUnit = hr.getUnit();
        	returnRtuRegisterID = hr.getReg();
        	Object result = hr.value();
        	Class rc = result.getClass();

        	sendDebug("Result class type: " + result.getClass().getName() + "\n\n", 0);
        	
        	if (rc == String.class)	{
        		returnText = (String)result;
        	}
        	else if (rc == Date.class) {
        		returnEventTime = (Date)result;
        		returnQuantity = new Quantity(returnEventTime.getTime(), returnUnit);
        		returnText = returnEventTime.toString();
        	}
        	else if (rc == Quantity.class) {
        		returnQuantity = (Quantity)result;
        		returnQuantity.convertTo(returnUnit, true);
        	}
        	else if (rc == BigDecimal.class) {
        		returnQuantity = new Quantity((BigDecimal)result, returnUnit);
        	}
        	else if (rc == Integer.class) {
        		returnQuantity = new Quantity(new BigDecimal((Integer)result), returnUnit);
        	}
        	else {
        		returnText = result.toString();
        	}
        	
        	return new RegisterValue(obisCode, returnQuantity, returnEventTime, returnFromTime, returnToTime, returnReadTime, returnRtuRegisterID, returnText);
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
    
    public static void main(String[] args) {
		try {
			UNIFLO1200Registers ufl_reg = new UNIFLO1200Registers(UNIFLO1200Registers.UNIFLO1200_FW_28);
		
			for (int i = 0; i < 255; i++) {
				System.out.println(
						" DEBUG" + 
						" First: " + ProtocolUtils.buildStringHex(ufl_reg.getAbsAddr(i), 8) +
						" Changed: " + ProtocolUtils.buildStringHex(ufl_reg.getWordAddr(i), 4) +
						" Original: " + ProtocolUtils.buildStringHex(i, 4) +
						" Divided: " + ProtocolUtils.buildStringHex(i/2, 4)
					);
			}
		
		
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
