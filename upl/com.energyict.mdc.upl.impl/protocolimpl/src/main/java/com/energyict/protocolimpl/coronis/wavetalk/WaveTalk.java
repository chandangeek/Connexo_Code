package com.energyict.protocolimpl.coronis.wavetalk;

import java.io.IOException;
import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.wavetalk.core.*;


public class WaveTalk extends WaveFlow {

	
	/**
	 * specific obis code mapper
	 */
	private ObisCodeMapper obisCodeMapper;	

	
	/**
	 * the common ohis code mapper for the WaveTalk
	 */
	private CommonObisCodeMapper commonObisCodeMapper=null;
	
	/**
	 * The parameter factory interface
	 */
	private ParameterFactory parameterFactory=null;
	
	
	@Override
	protected void doTheConnect() throws IOException {

		getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(1100);
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		commonObisCodeMapper = new CommonObisCodeMapper(this);
		parameterFactory = new ParameterFactoryImpl(this);
	}	
	
	@Override
	public Date getTime() throws IOException {
		//FIXME: use the correct way to readout time from a wavetalk
		return new Date();
	}
	
	
	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(110);
		
	}

	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
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
	public AbstractCommonObisCodeMapper getCommonObisCodeMapper() {
		return commonObisCodeMapper;
	}

	@Override
	public ParameterFactory getParameterFactory() {
		return parameterFactory;
	}		
}
