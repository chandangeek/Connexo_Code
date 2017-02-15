/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;
import com.energyict.protocolimpl.iec1107.siemenss4s.security.SiemensS4sEncryptor;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SiemensS4s extends AbstractIEC1107Protocol {

	@Override
	public String getProtocolDescription() {
		return "Landis&Gyr S4S IEC1107";
	}

	private S4sObjectFactory objectFactory;
	private SiemensS4sProfile profileObject;
	private SiemensS4sObisCodeMapper obisCodeMapper;

	private String nodeAddress;
	private String deviceId;
	private String passWord;
	private String serialNumber;

	private int securityLevel;
	private int channelMap;

	@Inject
	public SiemensS4s(PropertySpecService propertySpecService){
		super(propertySpecService, new SiemensS4sEncryptor());
	}

    public void connect() throws IOException {
        try {
            if (requestDataReadout) {
               dataReadout = getFlagIEC1107Connection().dataReadout(deviceId,nodeAddress);
               getFlagIEC1107Connection().disconnectMAC();
            }
            getFlagIEC1107Connection().connectMAC(deviceId,passWord,securityLevel,nodeAddress);
            doConnect();
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        try {
            validateSerialNumber();
        }
        catch(FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }
    }

	protected void doConnect() throws IOException {
		initLocalObjects();
	}

    public int getNumberOfChannels(){
        return this.channelMap;
     }

	/**
	 * Initialize local objects
	 */
	private void initLocalObjects(){
		this.objectFactory = new S4sObjectFactory(getFlagIEC1107Connection());
		this.profileObject = new SiemensS4sProfile(this.objectFactory);
	}

	protected List<String> doGetOptionalKeys() {
		return Collections.emptyList();
	}

	/**
	 * Set certain properties before doing anything
	 */
	protected void doValidateProperties(Properties properties)
	throws MissingPropertyException, InvalidPropertyException {
		this.deviceId = properties.getProperty(MeterProtocol.ADDRESS);
		this.passWord = properties.getProperty(MeterProtocol.PASSWORD,"4281602592");
		if (this.passWord.isEmpty()) {
			this.passWord = "4281602592";
		}
		//TODO set the level in the encryptor
		this.securityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","2").trim());
		this.nodeAddress=properties.getProperty(MeterProtocol.NODEID,"");
		this.serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
		this.channelMap = Integer.parseInt(properties.getProperty("ChannelMap","1"));
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
	 */
	public Date getTime() throws IOException {
		Calendar s4sDateTime = getObjectFactory().getDateTimeObject().getMeterTime();
		return s4sDateTime.getTime();
	}

	/**
	 * @return the meter his current profileInterval.
	 */
	public int getProfileInterval() throws IOException {
		return this.profileObject.getProfileInterval();
	}

	/**
	 * Create the profileObject
	 * @param lastReading - the from date from where to start reading
	 * @param includeEvents - indicates whether we need to read the events
	 * @return the requested loadProfile
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return this.profileObject.getProfileData(lastReading, includeEvents);
	}

	/**
	 * Read a register given the obisCode
	 * @param obisCode - the ObisCode of the register
	 * @return a registerValue containing necessary register information
	 */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    /**
     * Getter for the obisCodeMapper. If he doesn't exist, then create ONE.
     * @return the registerObisCodeMapper
     */
    private SiemensS4sObisCodeMapper getObisCodeMapper(){
    	if(this.obisCodeMapper == null){
    		this.obisCodeMapper = new SiemensS4sObisCodeMapper(this.getObjectFactory());
    	}
    	return this.obisCodeMapper;
    }

	/**
	 * @return the current objectFactory
	 */
	private S4sObjectFactory getObjectFactory(){
		return this.objectFactory;
	}

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}
