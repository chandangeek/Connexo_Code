package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;

import java.io.IOException;
import java.util.Date;

public class SM150E extends WaveFlow100mW {

	/**
	 * specific severntrent obis code mapper
	 */
	private ObisCodeMapper obisCodeMapper;

	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

	public SM150E(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
	}

	@Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }

	@Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }


	@Override
	protected ProfileData getTheProfileData(Date lastReading, int portId,boolean includeEvents) throws IOException {
		return profileDataReader.getProfileData(lastReading, portId, includeEvents);
	}

	@Override
	protected MeterProtocolType getMeterProtocolType() {
		return MeterProtocolType.SM150E;
	}

	@Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
		return obisCodeMapper.getRegisterExtendedLogging();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}