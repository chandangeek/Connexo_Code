package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

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
}
