package com.energyict.protocolimpl.coronis.core;

import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

public interface RegisterCache {

	/**
	 * Cache a list of registers using a specific bulk command.
	 * E.g. Wavenis supports a command that allows us to read a list of registers sin order to reduce the roundtrips over the RF
	 * @param obisCodes, list of obiscodes
	 * @throws java.io.IOException
	 */
	void cacheRegisters(List<ObisCode> obisCodes) throws IOException;
}
