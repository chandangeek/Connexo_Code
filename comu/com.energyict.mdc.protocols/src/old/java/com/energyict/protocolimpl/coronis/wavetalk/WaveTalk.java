package com.energyict.protocolimpl.coronis.wavetalk;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.wavetalk.core.AbstractCommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavetalk.core.AbstractWaveTalk;
import com.energyict.protocolimpl.coronis.wavetalk.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavetalk.core.ParameterFactory;
import com.energyict.protocolimpl.coronis.wavetalk.core.ParameterFactoryImpl;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;


public class WaveTalk extends AbstractWaveTalk {

	@Override
	public String getProtocolDescription() {
		return "Coronis WaveTalk";
	}

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

	@Inject
	public WaveTalk(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		commonObisCodeMapper = new CommonObisCodeMapper(this);
		parameterFactory = new ParameterFactoryImpl(this);
	}

	@Override
	protected void doTheDisConnect() throws IOException {

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

    public int getNumberOfChannels() {
        return 0;   //The repeater has no channels
    }

	@Override
	public AbstractCommonObisCodeMapper getCommonObisCodeMapper() {
		return commonObisCodeMapper;
	}

	@Override
	public ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

    @Override
   	public String getProtocolVersion() {
           return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
   	}
}
