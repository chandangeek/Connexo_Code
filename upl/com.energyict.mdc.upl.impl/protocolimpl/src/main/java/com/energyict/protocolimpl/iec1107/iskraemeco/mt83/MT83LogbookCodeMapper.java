package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.util.*;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;

public final class MT83LogbookCodeMapper {
	public static Map LogBookEvent = new HashMap();

	static {
		LogBookEvent.put(0x0080, new MT83EventType("Power down", MeterEvent.POWERDOWN));
		LogBookEvent.put(0x0040, new MT83EventType("Power up", MeterEvent.POWERUP));

		LogBookEvent.put(0x8102, new MT83EventType("Voltage down phase L1", MeterEvent.PHASE_FAILURE ));
		LogBookEvent.put(0x8103, new MT83EventType("Voltage down phase L2", MeterEvent.PHASE_FAILURE));
		LogBookEvent.put(0x8104, new MT83EventType("Voltage down phase L3", MeterEvent.PHASE_FAILURE));

		LogBookEvent.put(0x8105, new MT83EventType("Under-voltage phase L1", MeterEvent.VOLTAGE_SAG));
		LogBookEvent.put(0x8106, new MT83EventType("Under-voltage phase L2", MeterEvent.VOLTAGE_SAG));
		LogBookEvent.put(0x8107, new MT83EventType("Under-voltage phase L3", MeterEvent.VOLTAGE_SAG));

		LogBookEvent.put(0x8108, new MT83EventType("Voltage normal phase L1", MeterEvent.POWERUP));
		LogBookEvent.put(0x8109, new MT83EventType("Voltage normal phase L2", MeterEvent.POWERUP));
		LogBookEvent.put(0x810A, new MT83EventType("Voltage normal phase L3", MeterEvent.POWERUP));

		LogBookEvent.put(0x810B, new MT83EventType("Over-voltage phase L1", MeterEvent.VOLTAGE_SWELL));
		LogBookEvent.put(0x810C, new MT83EventType("Over-voltage phase L2", MeterEvent.VOLTAGE_SWELL));
		LogBookEvent.put(0x810D, new MT83EventType("Over-voltage phase L3", MeterEvent.VOLTAGE_SWELL));
		
		LogBookEvent.put(0x810E, new MT83EventType("Billing reset", MeterEvent.BILLING_ACTION));

		LogBookEvent.put(0x810F, new MT83EventType("RTC sync start", MeterEvent.SETCLOCK_BEFORE));
		LogBookEvent.put(0x8110, new MT83EventType("RTC sync end", MeterEvent.SETCLOCK_AFTER));
		LogBookEvent.put(0x0020, new MT83EventType("RTC Set", MeterEvent.SETCLOCK));
		LogBookEvent.put(0x0008, new MT83EventType("DST", MeterEvent.CONFIGURATIONCHANGE));
		
		LogBookEvent.put(0x2000, new MT83EventType("Log-Book erased", MeterEvent.CLEAR_DATA));
		LogBookEvent.put(0x4000, new MT83EventType("Load-Profile erased", MeterEvent.CLEAR_DATA));

		LogBookEvent.put(0x0001, new MT83EventType("Device disturbance", MeterEvent.HARDWARE_ERROR));
		LogBookEvent.put(0x8117, new MT83EventType("Parameters changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8118, new MT83EventType("Watch dog", MeterEvent.WATCHDOGRESET));

		LogBookEvent.put(0x8119, new MT83EventType("Fraud start", MeterEvent.TAMPER));
		LogBookEvent.put(0x811A, new MT83EventType("Fraud end", MeterEvent.TAMPER));

		LogBookEvent.put(0x811B, new MT83EventType("Terminal cover opened", MeterEvent.TERMINAL_OPENED));
		LogBookEvent.put(0x811C, new MT83EventType("Terminal cover closed", MeterEvent.TERMINAL_OPENED));
		LogBookEvent.put(0x811D, new MT83EventType("Main cover opened", MeterEvent.COVER_OPENED));
		LogBookEvent.put(0x811E, new MT83EventType("Main cover closed", MeterEvent.COVER_OPENED));

		LogBookEvent.put(0x811F, new MT83EventType("Master reset", MeterEvent.OTHER));

		LogBookEvent.put(0x8120, new MT83EventType("Parameter changed via remote comm.", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8121, new MT83EventType("Scheduled parameter change", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8122, new MT83EventType("Private key changed", MeterEvent.CONFIGURATIONCHANGE));

		LogBookEvent.put(0x8123, new MT83EventType("Local communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x8124, new MT83EventType("Local communication ended", MeterEvent.OTHER));
		LogBookEvent.put(0x8125, new MT83EventType("Remote communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x8126, new MT83EventType("Remote communication ended", MeterEvent.OTHER));
		LogBookEvent.put(0x8127, new MT83EventType("GPS communication established", MeterEvent.OTHER));
		LogBookEvent.put(0x8128, new MT83EventType("GPS communication lost", MeterEvent.PROGRAM_FLOW_ERROR));
		
		LogBookEvent.put(0x8129, new MT83EventType("Contract1 communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x812A, new MT83EventType("Contract1 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x812B, new MT83EventType("Contract1 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x812C, new MT83EventType("Contract1 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x812D, new MT83EventType("Contract1 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x812E, new MT83EventType("Contract1 billing reset", MeterEvent.BILLING_ACTION));

		LogBookEvent.put(0x812F, new MT83EventType("Contract2 communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x8130, new MT83EventType("Contract2 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8131, new MT83EventType("Contract2 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8132, new MT83EventType("Contract2 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8133, new MT83EventType("Contract2 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8134, new MT83EventType("Contract2 billing reset", MeterEvent.BILLING_ACTION));

		LogBookEvent.put(0x8135, new MT83EventType("Contract3 communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x8136, new MT83EventType("Contract3 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8137, new MT83EventType("Contract3 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8138, new MT83EventType("Contract3 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8139, new MT83EventType("Contract3 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x813A, new MT83EventType("Contract3 billing reset", MeterEvent.BILLING_ACTION));


		LogBookEvent.put(0x813B, new MT83EventType("Contract4 communication started", MeterEvent.OTHER));
		LogBookEvent.put(0x813C, new MT83EventType("Contract4 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x813D, new MT83EventType("Contract4 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x813E, new MT83EventType("Contract4 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x813F, new MT83EventType("Contract4 parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		LogBookEvent.put(0x8140, new MT83EventType("Contract4 billing reset", MeterEvent.BILLING_ACTION));

		LogBookEvent.put(0x8141, new MT83EventType("Reverse power flow", MeterEvent.REVERSE_RUN));
		LogBookEvent.put(0x8142, new MT83EventType("Breaker failure", MeterEvent.OTHER));
		LogBookEvent.put(0x8143, new MT83EventType("Invalid password", MeterEvent.OTHER));
		LogBookEvent.put(0x8144, new MT83EventType("Corrupted SMS", MeterEvent.OTHER));
		LogBookEvent.put(0x8145, new MT83EventType("Incorrect credit code", MeterEvent.OTHER));
		LogBookEvent.put(0x8146, new MT83EventType("Keypad locked", MeterEvent.OTHER));
		LogBookEvent.put(0x8147, new MT83EventType("GSM network failure", MeterEvent.OTHER));
		LogBookEvent.put(0x8148, new MT83EventType("Programming failed", MeterEvent.PROGRAM_FLOW_ERROR));
		LogBookEvent.put(0x8149, new MT83EventType("Invalid SMS source", MeterEvent.OTHER));
		LogBookEvent.put(0x814A, new MT83EventType("All code entered", MeterEvent.OTHER));
		LogBookEvent.put(0x814B, new MT83EventType("Valid code time", MeterEvent.OTHER));
		LogBookEvent.put(0x814C, new MT83EventType("Customer purchase request", MeterEvent.OTHER));
		LogBookEvent.put(0x814D, new MT83EventType("Meter removal", MeterEvent.HARDWARE_ERROR));
		LogBookEvent.put(0x814E, new MT83EventType("Full Technical Log Book", MeterEvent.OTHER));
		LogBookEvent.put(0x814F, new MT83EventType("Unable to send SMS alarm", MeterEvent.PROGRAM_FLOW_ERROR));
		LogBookEvent.put(0x8150, new MT83EventType("Intrusion reset", MeterEvent.TAMPER));
		LogBookEvent.put(0x8151, new MT83EventType("Previous values reset", MeterEvent.CLEAR_DATA));

		LogBookEvent.put(0x8152, new MT83EventType("Current without Voltage L1 - start", MeterEvent.APPLICATION_ALERT_START));
		LogBookEvent.put(0x8153, new MT83EventType("Current without Voltage L2 - start", MeterEvent.APPLICATION_ALERT_START));
		LogBookEvent.put(0x8154, new MT83EventType("Current without Voltage L3 - start", MeterEvent.APPLICATION_ALERT_START));
		
		LogBookEvent.put(0x8155, new MT83EventType("Current without Voltage L1 - end", MeterEvent.APPLICATION_ALERT_STOP));
		LogBookEvent.put(0x8156, new MT83EventType("Current without Voltage L2 - end", MeterEvent.APPLICATION_ALERT_STOP));
		LogBookEvent.put(0x8157, new MT83EventType("Current without Voltage L3 - end", MeterEvent.APPLICATION_ALERT_STOP));
		
		// The following logbook events are NOT documented in the MT83 specifications, 
		// but the meter uses them in the logfiles !!! Mapped to the MT173 logbook events.
		LogBookEvent.put(0x0010, new MT83EventType("Billing reset", 0)); 
	
	}
	
}
