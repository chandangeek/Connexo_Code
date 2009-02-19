package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

public class SignedParser implements Parser {

	private static final int DEBUG = 0;
	
	public static final String PARSER_NAME = "SignedParser";
	
	public Object val(int[] values, AbstractRegister register) throws IOException {
		int value = values[1] + (values[0] * (256 * 256));
		BigDecimal bd = new BigDecimal(value, new MathContext(0));
		return bd;
	}

}
