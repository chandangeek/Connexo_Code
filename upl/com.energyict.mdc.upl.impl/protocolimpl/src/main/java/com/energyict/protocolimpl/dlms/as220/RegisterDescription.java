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
		INFO.put("0.0.26.0.0.255", "PLC S-FSK frequencies channel 1-6");
		INFO.put("0.0.26.2.0.255", "PLC S-FSK MAC sync timeouts");
	}

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
		RegisterInfo regInfo = null;
		if (INFO.containsKey(obisCode.toString())) {
			regInfo = new RegisterInfo(INFO.get(obisCode.toString()));
		}
		return regInfo;
	}

}
