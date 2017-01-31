/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class SM150E extends WaveFlow100mW {

	@Override
	public String getProtocolDescription() {
		return "Severntrent SM150E WaveFlow";
	}

	/**
	 * specific severntrent obis code mapper
	 */
	private ObisCodeMapper obisCodeMapper;

	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;

	@Inject
	public SM150E(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
	}

	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {

	}

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }

    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     * @param obisCode obiscode of the register to lookup
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterInfo object
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }


	@Override
	protected ProfileData getTheProfileData(Date lastReading, int portId,boolean includeEvents) throws UnsupportedException, IOException {
		return profileDataReader.getProfileData(lastReading, portId, includeEvents);
	}


	@Override
	protected MeterProtocolType getMeterProtocolType() {
		return MeterProtocolType.SM150E;
	}

	public void startMeterDetection() throws IOException {
		getRadioCommandFactory().startMeterDetection();
	}

    /**
     * Override if you want to provide info of the meter setup and registers when the "ExtendedLogging" custom property > 0
     * @param extendedLogging int
     * @throws java.io.IOException thrown when something goes wrong
     * @return String with info
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
		return obisCodeMapper.getRegisterExtendedLogging();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}
