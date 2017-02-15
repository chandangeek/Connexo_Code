/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * UNIFLO1200.java
 *
 * Created on 4-dec-2008, 15:00:50 by jme
 *
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.connection.UNIFLO1200Connection;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.UNIFLO1200Profile;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author jme
 *
 */

public class UNIFLO1200 extends Modbus {

	@Override
	public String getProtocolDescription() {
		return "Flonidan UNIFLO Modbus";
	}

	private static final int DEBUG = 0;

	private static final int MIN_LOADPROFILE_NUMBER = 1;
	private static final int MAX_LOADPROFILE_NUMBER = 3;

	private int secLvl = 0;
	private UNIFLO1200Profile loadProfile;
	private int loadProfileNumber;

	@Inject
	public UNIFLO1200(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeout, int retries, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        modbusConnection = new UNIFLO1200Connection(inputStream, outputStream, timeout, getInterframeTimeout(), retries, forcedDelay, echoCancelling, halfDuplexController, getLogger());
        return getModbusConnection();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

	public int getLoadProfileNumber() {
		return loadProfileNumber;
	}

	public void setLoadProfileNumber(int loadProfileNumber) {
		this.loadProfileNumber = loadProfileNumber;
	}

	public void setTime() throws IOException {
		byte[] b;
		Calendar cal = ProtocolUtils.getCleanCalendar(gettimeZone());
		cal.setTime(new Date());
		b = UNIFLO1200Parsers.buildTimeDate(cal);

		try {
			getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_TIME).getWriteMultipleRegisters(b);
		} catch (IOException e) {
			throw new ProtocolException("Unable to set time. Possibly wrong password. Exception message: " + e.getMessage());
		}

	}

	protected void validateSerialNumber() throws IOException {
		if (getInfoTypeSerialNumber() == null) {
			return;
		}
		String serialNumber = (String) getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_SERIAL_NUMBER).value();
		serialNumber = serialNumber.trim();
		if (!getInfoTypeSerialNumber().equalsIgnoreCase(serialNumber)) {
			throw new InvalidPropertyException("SerialNumber [" + getInfoTypeSerialNumber() + "] doesn't match the serialnumber of the device [" + serialNumber + "] !!!");
		}
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		return getLoadProfile().getProfileData(from, to, includeEvents);
	}

	public int getProfileInterval() throws IOException {
		sendDebug("getProfileInterval()", 5);
		return getLoadProfile().getProfileInterval();
	}

	public int getNumberOfChannels() throws IOException {
		return getLoadProfile().getNumberOfChannels();
	}

	public Date getTime() throws IOException {
		return getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_TIME).dateValue();
	}

	public String getFirmwareVersion() throws IOException {
		return (String) getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_DEVICE_TYPE).value();
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		return ((UNIFLO1200RegisterFactory)getRegisterFactory()).readRegister(obisCode);
	}

	protected void doTheConnect() throws IOException {
		sendDebug("doTheConnect()", 5);
		String password = getInfoTypePassword();
		sendDebug(password, 5);

		if (password != null) {
			byte[] b = password.getBytes();
			getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_LOGIN).getWriteMultipleRegisters(b);
		}

		setSecLvl(((Integer)getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_ACTUAL_SECLEVEL).value()).intValue());

		if (getInfoTypeSecurityLevel() != getSecLvl()) {
			throw new InvalidPropertyException("SecurityLevel mismatch [" + getInfoTypeSecurityLevel() + " != " + getSecLvl() + "]: Reason may be wrong password or hardware lock.");
		}

		sendDebug("Actual security lvl: " + getSecLvl(), 2);
		setLoadProfile(new UNIFLO1200Profile(this));

	}

	protected void doTheDisConnect() throws IOException {
		sendDebug("doTheDisConnect()", 5);
	}

	protected List<String> doTheGetOptionalKeys() {
		sendDebug("doTheGetOptionalKeys()", 5);
        return Collections.singletonList("LoadProfileNumber");
	}

	protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		sendDebug("doTheValidateProperties()", 5);

        setLoadProfileNumber(Integer.parseInt(properties.getProperty("LoadProfileNumber","1").trim()));

        if ((getLoadProfileNumber() > MAX_LOADPROFILE_NUMBER) || (getLoadProfileNumber() < MIN_LOADPROFILE_NUMBER)) {
	        throw new InvalidPropertyException(
			        "Invalid loadProfileNumber (" + getLoadProfileNumber() + ")! " +
					        "Valid values are: '1' for INTERVAL_LOG, '2' for 24_HOUR_LOG or '3' for MONTH_LOG."
	        );
        }

		if (getInfoTypePassword() != null) {
			if (getInfoTypePassword().length() > 8) {
				throw new InvalidPropertyException("Password to long! Max length is 8 characters.");
			}
			while (getInfoTypePassword().length() < 8) setInfoTypePassword(getInfoTypePassword() + " ");
		}
	}

	protected void initRegisterFactory() {
		sendDebug("initRegisterFactory()", 5);
        setRegisterFactory(new UNIFLO1200RegisterFactory(this));
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		sendDebug("discover()", 5);
		return null;
	}

    protected String getRegistersInfo(int extendedLogging) throws IOException {
    	StringBuilder strBuff = new StringBuilder();
    	if (extendedLogging==1) {
    		Iterator it = ((UNIFLO1200RegisterFactory)getRegisterFactory()).getRegisters().iterator();
    		while (it.hasNext()) {
    			AbstractRegister ar = (AbstractRegister)it.next();
    			if (ar.getObisCode()!=null) {
    				strBuff.append(ar.getObisCode().toString()).append(", ");
    				strBuff.append("unit = ").append(ar.getUnit()).append(", ");
    				strBuff.append("name = ").append(ar.getName());
    				strBuff.append("\n");
    			}
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

    public void sendDebug(String message, int debuglvl) {
		if (DEBUG == 0) {
	    	message = " [" + new Date().toString() + "] > " + message;
		} else {
	    	message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
		}
    	if ((debuglvl <= DEBUG) && (getLogger() != null)) {
    		getLogger().info(message);
    		System.out.println(message);
    	}
    }

}