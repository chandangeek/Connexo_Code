package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.device.data.RegisterValue;
import com.energyict.protocol.NoSuchRegisterException;

import java.io.IOException;

public abstract class AbstractCommonObisCodeMapper {
	abstract public String getRegisterExtendedLogging();
	abstract public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException,IOException;
}
