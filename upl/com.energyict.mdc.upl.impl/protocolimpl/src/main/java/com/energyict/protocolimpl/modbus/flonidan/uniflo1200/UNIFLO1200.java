/**
 * UNIFLO1200.java
 * 
 * Created on 4-dec-2008, 15:00:50 by jme
 * 
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.util.*;

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

	public void setTime() throws IOException {
		byte[] b = new byte[6];
		Calendar cal = ProtocolUtils.getCleanCalendar(gettimeZone());
		b = UNIFLO1200Parsers.buildTimeDate(cal);
		try {
			getRegisterFactory().findRegister(RegisterFactory.REG_TIME).getWriteMultipleRegisters(b);
		} catch (IOException e) {
			throw new ProtocolException("Unable to set time. Possibly wrong password. Exception message: " + e.getMessage());
		}
	}

	public Date getTime() throws IOException {
		return getRegisterFactory().findRegister(RegisterFactory.REG_TIME).dateValue();
	}
	
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return (String) getRegisterFactory().findRegister(RegisterFactory.REG_DEVICE_TYPE).value();
	}
	
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		return ((RegisterFactory)getRegisterFactory()).readRegister(obisCode);
	}
	
	protected void doTheConnect() throws IOException {
		sendDebug("doTheConnect()", 5);
		byte[] b = getInfoTypePassword().getBytes();
		getRegisterFactory().findRegister(RegisterFactory.REG_LOGIN).getWriteMultipleRegisters(b);
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
	
    protected String getRegistersInfo(int extendedLogging) throws IOException {
    	StringBuffer strBuff = new StringBuffer();
    	if (extendedLogging==1) {
    		Iterator it = ((RegisterFactory)getRegisterFactory()).getRegisters().iterator();
    		while (it.hasNext()) {
    			AbstractRegister ar = (AbstractRegister)it.next();
    			if (ar.getObisCode()!=null)
    				strBuff.append(ar.toString() + "\n");
    		}
    	}
    	return strBuff.toString();
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
