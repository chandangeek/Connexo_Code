package com.energyict.protocolimpl.coronis.wavetalk;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.wavetalk.core.AbstractCommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavetalk.core.AbstractWaveTalk;
import com.energyict.protocolimpl.coronis.wavetalk.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.wavetalk.core.ParameterFactory;
import com.energyict.protocolimpl.coronis.wavetalk.core.ParameterFactoryImpl;

import java.io.IOException;

public class WaveTalk extends AbstractWaveTalk {

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
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }

	@Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

	@Override
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
           return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
   	}

}