/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer8;

public final class AXDRBoolean {

	private static final int	TRUE_VALUE	= 1;

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRBoolean() {
	}

	/**
	 * @param val
	 * @return
	 */
	public static Integer8 encode(boolean val) {
		return new Integer8(val ? TRUE_VALUE : 0);
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static boolean decode(AbstractDataType dataType) {
		return (dataType != null) && (dataType.intValue() == TRUE_VALUE ? true : false);
	}

}
