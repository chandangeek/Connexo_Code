package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import java.io.IOException;
import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

public class WaveFlowV2 extends WaveFlow {

	/**
	 * specific obis code mapper
	 */
	private ObisCodeMapper obisCodeMapper;	
	
	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;

	
	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
	}	
	
	@Override
	protected void doTheConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MeterProtocolType getMeterProtocolType() {
		// TODO Auto-generated method stub
		return null;
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
	
}
