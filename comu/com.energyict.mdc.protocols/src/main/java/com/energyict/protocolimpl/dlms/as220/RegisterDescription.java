/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jme
 *
 */
public final class RegisterDescription {

	private RegisterDescription() {
		// hide util class constructor
	}

	public static final Map<String, String> INFO = new HashMap<String, String>();
	static {
		INFO.put("0.0.26.0.0.255", "PLC S-FSK frequencies channel 1-6");
		INFO.put("0.0.26.1.0.255", "PLC S-FSK active initiator");
		INFO.put("0.0.26.2.0.255", "PLC S-FSK MAC sync timeouts");
		INFO.put("0.0.26.3.0.255", "PLC S-FSK MAC counters");
		INFO.put("0.0.0.0.0.0", "Debugging register");
		INFO.put("0.0.1.0.0.255", "Clock");

		INFO.put("0.0.42.0.0.255", "Logical device name");
		INFO.put("1.0.0.2.0.255", "Active firmware version AM500");
		INFO.put("1.1.0.2.0.255", "Passive firmware version AM500");
		INFO.put("1.0.96.63.11.255", "Producer consumer code");
		INFO.put("1.0.96.63.12.255", "Test mode");
		INFO.put("1.0.0.0.0.255", "Serial number");
		INFO.put("0.0.96.2.0.255", "Number of program configuration changes");
		INFO.put("0.0.96.15.0.255", "Cover open counter");
		INFO.put("0.0.96.15.1.255", "Breaker open counter");
		INFO.put("0.5.96.11.0.255", "Voltage cut event code");
		INFO.put("1.0.96.5.1.255", "Status");
		INFO.put("0.0.97.97.0.255", "Error");
		INFO.put("0.0.97.98.0.255", "Alarm");
		INFO.put("0.0.97.98.10.255", "Alarm filter");
        INFO.put("0.0.96.50.0.255", "Tarif switch");
        INFO.put("0.0.96.14.0.255", "Tarif switch");

	}

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
		RegisterInfo regInfo = null;
		if (INFO.containsKey(obisCode.toString())) {
			regInfo = new RegisterInfo(INFO.get(obisCode.toString()));
		}
		return regInfo;
	}

}
