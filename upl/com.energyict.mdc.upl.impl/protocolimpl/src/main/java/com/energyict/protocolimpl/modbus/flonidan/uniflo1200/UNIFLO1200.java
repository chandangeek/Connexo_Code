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
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile.UNIFLO1200Profile;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.RegisterFactory;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200Parsers;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200Registers;

/**
 * @author jme
 *
 */

public class UNIFLO1200 extends Modbus {
    
	private static final int DEBUG = 0;

	private int secLvl = 0;
	private UNIFLO1200Profile loadProfile; 
	
	public String getProtocolVersion() {
        return "$Revision: 1.2 $";
    }

	public void setTime() throws IOException {
		byte[] b = new byte[6];
		Calendar cal = ProtocolUtils.getCleanCalendar(gettimeZone());
		cal.setTime(new Date());
		b = UNIFLO1200Parsers.buildTimeDate(cal);
		
		try {
			getRegisterFactory().findRegister(RegisterFactory.REG_TIME).getWriteMultipleRegisters(b);
		} catch (IOException e) {
			throw new ProtocolException("Unable to set time. Possibly wrong password. Exception message: " + e.getMessage());
		}
				
	}

//	public ProfileData getProfileData(boolean includeEvents) throws IOException {
//		// TODO Auto-generated method stub
//		return super.getProfileData(includeEvents);
//	}
//
//	public ProfileData getProfileData(Date lastReading, boolean includeEvents)
//			throws IOException {
//		// TODO Auto-generated method stub
//		return super.getProfileData(lastReading, includeEvents);
//	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		return getLoadProfile().getProfileData(from, to, includeEvents);
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		sendDebug("getProfileInterval()", 5);
		return getLoadProfile().getProfileInterval();
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
		String password = getInfoTypePassword();
		sendDebug(password, 5);

		if (password != null) {
			byte[] b = password.getBytes();
			getRegisterFactory().findRegister(RegisterFactory.REG_LOGIN).getWriteMultipleRegisters(b);
		}
		
		setSecLvl((Integer)getRegisterFactory().findRegister(RegisterFactory.REG_ACTUAL_SECLEVEL).value());		

		if (getInfoTypeSecurityLevel() != getSecLvl())
			throw new InvalidPropertyException("SecurityLevel mismatch [" + getInfoTypeSecurityLevel() + " != " + getSecLvl() + "]: Reason may be wrong password or hardware lock.");
		
		sendDebug("Actual security lvl: " + getSecLvl(), 0);
		setLoadProfile(new UNIFLO1200Profile(this));
		
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

		if (getInfoTypePassword() != null) {
			if (getInfoTypePassword().length() > 8) throw new InvalidPropertyException("Password to long! Max length is 8 characters.");
			while (getInfoTypePassword().length() < 8) setInfoTypePassword(getInfoTypePassword() + " ");
		}
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

	public int getSecLvl() {
		return secLvl;
	}

	public void setSecLvl(int secLvl) {
		this.secLvl = secLvl;
	}
    
    public UNIFLO1200Profile getLoadProfile() {
		return loadProfile;
	}

	public void setLoadProfile(UNIFLO1200Profile loadProfile) {
		this.loadProfile = loadProfile;
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

    public void sendDebug(String message, int debuglvl) {
		if (DEBUG == 0) {
	    	message = " [" + new Date().toString() + "] > " + message;
		} else {
	    	message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
		}
		System.out.println(message);
    	if ((debuglvl <= DEBUG) && (getLogger() != null)) {
    		getLogger().info(message);
    		System.out.println(message);
    	}
    }
    
    
}
