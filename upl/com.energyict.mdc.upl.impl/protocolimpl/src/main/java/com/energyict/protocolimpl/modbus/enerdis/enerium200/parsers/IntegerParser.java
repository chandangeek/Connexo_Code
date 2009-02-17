package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

public class IntegerParser  implements Parser {

	private static final int DEBUG 	= 0;
	
	public Integer val(int[] values, AbstractRegister register) throws IOException {
		return new Integer(0);
	}
	
}
