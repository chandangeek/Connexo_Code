package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;

public class SM150E extends WaveFlow100mW {

	
	ObisCodeMapper obisCodeMapper;
	
	@Override
	protected void doTheConnect() throws IOException {
		if (getExtendedLogging() >= 1) {
			obisCodeMapper.getRegisterExtendedLogging();
		}
	}


	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		
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

	
}
