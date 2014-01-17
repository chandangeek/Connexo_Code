package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class SignedParser implements Parser {

	private static final int DEBUG = 0;

	public static final String PARSER_NAME = "SignedParser";

	public Object val(int[] values, AbstractRegister register) throws IOException {
		int value = 0;
		switch (values.length) {
			case 1:	value = values[0]; break;
			case 2:	value = values[1] + (values[0] * (256 * 256)); break;
			default: throw new ProtocolException(PARSER_NAME + ".val(): Error while parsing register. Wrong data length: " + values.length);
		}
//		BigDecimal bd = new BigDecimal(value, new MathContext(0));
		BigDecimal bd = new BigDecimal(new BigInteger(Long.toString(value)));
		return bd;
	}

}
