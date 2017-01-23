package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;

import java.io.IOException;

public abstract class AbstractCommonObisCodeMapper {
	abstract public String getRegisterExtendedLogging();
	abstract public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException,IOException;
}
