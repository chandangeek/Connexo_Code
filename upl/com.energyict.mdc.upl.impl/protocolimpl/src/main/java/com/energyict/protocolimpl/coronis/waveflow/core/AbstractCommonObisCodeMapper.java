package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public abstract class AbstractCommonObisCodeMapper {
	abstract public String getRegisterExtendedLogging();
	abstract public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException,IOException; 
}
