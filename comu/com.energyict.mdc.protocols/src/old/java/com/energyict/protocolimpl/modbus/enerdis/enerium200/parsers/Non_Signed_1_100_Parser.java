/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Non_Signed_1_100_Parser implements Parser {

	private static final int DEBUG = 0;

	public static final String PARSER_NAME = "Non_Signed_1_100_Parser";

	public Object val(int[] values, AbstractRegister register) throws IOException {
		long value = 0;
		switch (values.length) {
			case 1:	value = Utils.intToLongUnsigned(values[0]); break;
			case 2:	value = Utils.intToLongUnsigned(values[1]) + (Utils.intToLongUnsigned(values[0]) * (256 * 256)); break;
			default: throw new ProtocolException(PARSER_NAME + ".val(): Error while parsing register. Wrong data length: " + values.length);
		}
//		BigDecimal bd = new BigDecimal(value, new MathContext(0));
//		bd = bd.movePointLeft(2);
		BigDecimal bd = new BigDecimal(new BigInteger(Long.toString(value)), 2);
		return bd;
	}

}
