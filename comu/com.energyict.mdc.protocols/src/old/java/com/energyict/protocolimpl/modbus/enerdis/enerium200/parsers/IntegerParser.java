/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

import java.io.IOException;

public class IntegerParser  implements Parser {

	private static final int DEBUG 	= 0;

	public static final String PARSER_NAME = "IntegerParser";

	public Object val(int[] values, AbstractRegister register) throws IOException {
		return new Integer(0);
	}

}
