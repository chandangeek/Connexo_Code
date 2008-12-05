/**
 * UNIFLO1200.java
 * 
 * Created on 4-dec-2008, 15:00:50 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocol.discover.*;
import com.energyict.protocolimpl.modbus.core.*;


/**
 * @author jme
 *
 */
public class UNIFLO1200 extends Modbus {

    //ModbusConnection modbusConnection;
    //private MultiplierFactory multiplierFactory=null;
    
    private static final int DEBUG = 1;

	public String getProtocolVersion() {
		sendDebug("getProtocolVersion()", 5);
        return "$Revision: 1.2 $";
    }

	public Date getTime() throws IOException {
		return getRegisterFactory().findRegister(RegisterFactory.TIME).dateValue();
		//return new Date(5345678910112L);
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
    	if ((debuglvl >= DEBUG) && (getLogger() != null)) {
    		getLogger().info(message);
    		System.out.println(message);
    	}
    }

}
