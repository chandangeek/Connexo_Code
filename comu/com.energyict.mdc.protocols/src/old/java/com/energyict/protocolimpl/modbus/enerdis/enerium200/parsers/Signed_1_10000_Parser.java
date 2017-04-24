/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Signed_1_10000_Parser implements Parser {

	private static final int DEBUG = 0;

	public static final String PARSER_NAME = "Signed_1_10000_Parser";

	public Object val(int[] values, AbstractRegister register) throws IOException {
		int value = 0;

		if (DEBUG >= 1) {
			System.out.print(register.getName() + " = ");
			for (int i = 0; i < values.length; i++) {
				System.out.print(" ["+i+"] " + ProtocolUtils.buildStringHex(values[i], 4));
			}
		}

		switch (values.length) {
			case 1:	value = (short) values[0]; break;
			case 2:	value = values[1] + (values[0] * (256 * 256)); break;
			default: throw new ProtocolException(PARSER_NAME + ".val(): Error while parsing register. Wrong data length: " + values.length);
		}
//		BigDecimal bd = new BigDecimal(value, new MathContext(0));
		BigDecimal bd = new BigDecimal(new BigInteger(Long.toString(value)));
		bd = bd.movePointLeft(4);
		if (DEBUG >= 1) System.out.println(" " + bd.toString());
		return bd;
	}

}
