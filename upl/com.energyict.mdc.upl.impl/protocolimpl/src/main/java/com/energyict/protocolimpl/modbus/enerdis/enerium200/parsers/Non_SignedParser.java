package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

public class Non_SignedParser implements Parser {

	private static final int DEBUG = 0;
	
	public static final String PARSER_NAME = "Non_SignedParser";
	
	public Object val(int[] values, AbstractRegister register) throws IOException {
		long value = 0;
		switch (values.length) {
			case 1:	value = values[0]; break;
			case 2:	value = values[1] + (values[0] * (256 * 256)); break;
			case 4:	value = (values[1] + (values[0] * (256 * 256))) + ((values[3] + (values[2] * (256 * 256)))  * 1000000); break;
			default: throw new ProtocolException(PARSER_NAME + ".val(): Error while parsing register. Wrong data length: " + values.length);
		}
		BigDecimal bd = new BigDecimal(value, new MathContext(0));
		return bd;
	}

}
