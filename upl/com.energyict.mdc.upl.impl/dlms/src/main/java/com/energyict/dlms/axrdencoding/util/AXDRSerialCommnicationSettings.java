package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.SerialCommunicationSettings;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;

public class AXDRSerialCommnicationSettings {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRSerialCommnicationSettings() {
	}

	/**
	 * @param serialCommnicationSettings
	 * @return
	 */
	static public Structure encode(SerialCommunicationSettings serialCommnicationSettings) {
		Structure structure = new Structure();
		structure.addDataType(new Integer8(serialCommnicationSettings.getDataBits()));
		structure.addDataType(new Integer8(serialCommnicationSettings.getStopBits()));
		structure.addDataType(new Integer32(serialCommnicationSettings.getSpeed()));
		structure.addDataType(new Integer8((int) serialCommnicationSettings.getParity()));
		return structure;
	}

	/**
	 * @param dataType
	 * @return
	 */
	static public SerialCommunicationSettings decode(AbstractDataType dataType) {
		Structure structure = dataType.getStructure();
		int dataBits = structure.getDataType(0).intValue();
		int stopBits = structure.getDataType(1).intValue();
		int speed = structure.getDataType(2).intValue();
		char parity = (char) structure.getDataType(3).intValue();
		SerialCommunicationSettings serialCommnicationSettings = new SerialCommunicationSettings(speed, dataBits, parity, stopBits);
		return serialCommnicationSettings;
	}

}
