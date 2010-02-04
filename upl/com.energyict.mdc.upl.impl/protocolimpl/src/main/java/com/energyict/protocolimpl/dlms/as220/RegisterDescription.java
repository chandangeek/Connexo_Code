/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.util.HashMap;
import java.util.Map;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;

/**
 * @author jme
 *
 */
public class RegisterDescription {

	public static final Map<String, String> INFO = new HashMap<String, String>();
	static {
		INFO.put("0.1.26.0.0.255", "PLC S-FSK mark frequency channel 1");
		INFO.put("0.2.26.0.0.255", "PLC S-FSK mark frequency channel 2");
		INFO.put("0.3.26.0.0.255", "PLC S-FSK mark frequency channel 3");
		INFO.put("0.4.26.0.0.255", "PLC S-FSK mark frequency channel 4");
		INFO.put("0.5.26.0.0.255", "PLC S-FSK mark frequency channel 5");
		INFO.put("0.6.26.0.0.255", "PLC S-FSK mark frequency channel 6");

		INFO.put("0.1.26.0.1.255", "PLC S-FSK space frequency channel 1");
		INFO.put("0.2.26.0.1.255", "PLC S-FSK space frequency channel 2");
		INFO.put("0.3.26.0.1.255", "PLC S-FSK space frequency channel 3");
		INFO.put("0.4.26.0.1.255", "PLC S-FSK space frequency channel 4");
		INFO.put("0.5.26.0.1.255", "PLC S-FSK space frequency channel 5");
		INFO.put("0.6.26.0.1.255", "PLC S-FSK space frequency channel 6");

	}

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
		RegisterInfo regInfo = null;
		if (INFO.containsKey(obisCode.toString())) {
			regInfo = new RegisterInfo(INFO.get(obisCode.toString()));
		}
		return regInfo;
	}

}
