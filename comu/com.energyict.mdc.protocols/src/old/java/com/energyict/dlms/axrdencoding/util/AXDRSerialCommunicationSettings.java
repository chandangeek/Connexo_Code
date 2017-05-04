/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding.util;

import com.energyict.mdc.protocol.api.SerialCommunicationSettings;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;

public final class AXDRSerialCommunicationSettings {

	private static final int	DATABITS_INDEX	= 0;
	private static final int	STOPBITS_INDEX	= 1;
	private static final int	SPEED_INDEX		= 2;
	private static final int	PARITY_INDEX	= 3;

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRSerialCommunicationSettings() {
	}

	/**
	 * @param serialCommnicationSettings
	 * @return
	 */
	public static Structure encode(SerialCommunicationSettings serialCommnicationSettings) {
		Structure structure = null;
		if (serialCommnicationSettings != null) {
			structure = new Structure();
			structure.addDataType(new Integer8(serialCommnicationSettings.getDataBits()));
			structure.addDataType(new Integer8(serialCommnicationSettings.getStopBits()));
			structure.addDataType(new Integer32(serialCommnicationSettings.getSpeed()));
			structure.addDataType(new Integer8((int) serialCommnicationSettings.getParity()));
		}
		return structure;
	}

	/**
	 * @param dataType
	 * @return
	 */
	public static SerialCommunicationSettings decode(AbstractDataType dataType) {
		if ((dataType != null) && (dataType.isStructure())) {
			Structure structure = dataType.getStructure();
			int dataBits = structure.getDataType(DATABITS_INDEX).intValue();
			int stopBits = structure.getDataType(STOPBITS_INDEX).intValue();
			int speed = structure.getDataType(SPEED_INDEX).intValue();
			char parity = (char) structure.getDataType(PARITY_INDEX).intValue();
			return new SerialCommunicationSettings(speed, dataBits, parity, stopBits);
		} else {
			return null;
		}
	}

}
