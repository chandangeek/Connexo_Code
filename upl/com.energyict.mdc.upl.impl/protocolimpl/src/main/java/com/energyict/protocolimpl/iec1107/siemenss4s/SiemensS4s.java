package com.energyict.protocolimpl.iec1107.siemenss4s;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;

public class SiemensS4s extends AbstractIEC1107Protocol {
	
	private S4sObjectFactory objectFactory;
	private SiemensS4sProfile profileObject;
	private String nodeAddress;
	private String deviceId;
	private String passWord;
	private String serialNumber;
	private int securityLevel;

	protected void doConnect() throws IOException {
		initLocalObjects();
	}
	
	/**
	 * Initialize local objects
	 */
	private void initLocalObjects(){
		this.objectFactory = new S4sObjectFactory(getFlagIEC1107Connection());
		this.profileObject = new SiemensS4sProfile(this.objectFactory);
	}

	protected List doGetOptionalKeys() {
		return new ArrayList();
	}
	
	protected void doValidateProperties(Properties properties)
	throws MissingPropertyException, InvalidPropertyException {
		this.deviceId = properties.getProperty(MeterProtocol.ADDRESS);
		this.passWord = properties.getProperty(MeterProtocol.PASSWORD);
		this.securityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
		this.nodeAddress=properties.getProperty(MeterProtocol.NODEID,"");
		this.serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
	}
	
	/**
	 * Check if the serialNumber matches the one you read from the device. If not fail.
	 * @throws IOException when mismatch or when received data isn't correct
	 */
	protected void validateSerialNumber() throws IOException{
		if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)){
			return;
		}
		String s4sSerialNumber = getObjectFactory().getSerialNumberObject().getSerialNumber();
		if(!s4sSerialNumber.equalsIgnoreCase(this.serialNumber)){
			throw new ConnectionException("Wrong serialNumber, EIServer: " + this.serialNumber + 
					" - Meter: " + s4sSerialNumber);
		}
	}
	
	/**
	 * Read the time from the meter.
	 * TODO : TOTest
	 */
	public Date getTime() throws IOException {
		Calendar s4sDateTime = getObjectFactory().getDateTimeObject().getMeterTime();
		return s4sDateTime.getTime();
	}
	
	/**
	 * Return the meter his current profileInterval.
	 */
	public int getProfileInterval() throws FlagIEC1107ConnectionException, ConnectionException, IOException{
		return this.profileObject.getProfileInterval();
	}
	
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return this.profileObject.getProfileData(lastReading, includeEvents);
	}

	/**
	 * @return the current objectFactory
	 */
	private S4sObjectFactory getObjectFactory(){
		return this.objectFactory;
	}
}
