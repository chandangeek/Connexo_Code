package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;

public class IntegerParser  implements Parser {

	private static final int DEBUG 	= 0;
	
	public Integer val(int[] values, AbstractRegister register) throws IOException {
		return new Integer(0);
	}
	
}
