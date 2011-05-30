package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

public abstract class AbstractCommonObisCodeMapper {
	abstract public String getRegisterExtendedLogging();
	abstract public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException,IOException; 
}
