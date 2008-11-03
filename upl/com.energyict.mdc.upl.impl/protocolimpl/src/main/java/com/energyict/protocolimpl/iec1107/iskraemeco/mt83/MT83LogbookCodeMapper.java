package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.util.*;

public final class MT83LogbookCodeMapper {
	public static Map LogBookEvent = new HashMap();

	static {
		LogBookEvent.put("0080","Power down");
		LogBookEvent.put("0040","Power up");

		LogBookEvent.put("8102","Voltage down phase L1");
		LogBookEvent.put("8103","Voltage down phase L2");
		LogBookEvent.put("8104","Voltage down phase L3");

		LogBookEvent.put("8105","Under-voltage phase L1");
		LogBookEvent.put("8106","Under-voltage phase L2");
		LogBookEvent.put("8107","Under-voltage phase L3");

		LogBookEvent.put("8108","Voltage normal phase L1");
		LogBookEvent.put("8109","Voltage normal phase L2");
		LogBookEvent.put("810A","Voltage normal phase L3");

		LogBookEvent.put("810B","Over-voltage phase L1");
		LogBookEvent.put("810C","Over-voltage phase L2");
		LogBookEvent.put("810D","Over-voltage phase L3");
		
		LogBookEvent.put("810E","Billing reset");

		LogBookEvent.put("810F","RTC sync start");
		LogBookEvent.put("8110","RTC sync end");
		LogBookEvent.put("0020","RTC Set");
		LogBookEvent.put("0008","DST");
		
		LogBookEvent.put("2000", "Log-Book erased");
		LogBookEvent.put("4000", "Load-Profile erased");

		LogBookEvent.put("0001", "Device disturbance");
		LogBookEvent.put("8117", "Parameters changed");
		LogBookEvent.put("8118", "Watch dog");

		LogBookEvent.put("8119", "Fraud start");
		LogBookEvent.put("811A", "Fraud end");

		LogBookEvent.put("811B", "Terminal cover opened");
		LogBookEvent.put("811C", "Terminal cover closed");
		LogBookEvent.put("811D", "Main cover opened");
		LogBookEvent.put("811E", "Main cover closed");

		LogBookEvent.put("811F", "Master reset");

		LogBookEvent.put("8120", "Parameter changed via remote comm.");
		LogBookEvent.put("8121", "Scheduled parameter change");
		LogBookEvent.put("8122", "Private key changed");

		LogBookEvent.put("8123", "Local communication started");
		LogBookEvent.put("8124", "Local communication ended");
		LogBookEvent.put("8125", "Remote communication started");
		LogBookEvent.put("8126", "Remote communication ended");
		LogBookEvent.put("8127", "GPS communication established");
		LogBookEvent.put("8128", "GPS communication lost");
		
		LogBookEvent.put("8129", "Contract1 communication started");
		LogBookEvent.put("812A", "Contract1 parameter changed");
		LogBookEvent.put("812B", "Contract1 parameter changed");
		LogBookEvent.put("812C", "Contract1 parameter changed");
		LogBookEvent.put("812D", "Contract1 parameter changed");
		LogBookEvent.put("812E", "Contract1 billing reset");

		LogBookEvent.put("812F", "Contract2 communication started");
		LogBookEvent.put("8130", "Contract2 parameter changed");
		LogBookEvent.put("8131", "Contract2 parameter changed");
		LogBookEvent.put("8132", "Contract2 parameter changed");
		LogBookEvent.put("8133", "Contract2 parameter changed");
		LogBookEvent.put("8134", "Contract2 billing reset");

		LogBookEvent.put("8135", "Contract3 communication started");
		LogBookEvent.put("8136", "Contract3 parameter changed");
		LogBookEvent.put("8137", "Contract3 parameter changed");
		LogBookEvent.put("8138", "Contract3 parameter changed");
		LogBookEvent.put("8139", "Contract3 parameter changed");
		LogBookEvent.put("813A", "Contract3 billing reset");


		LogBookEvent.put("813B", "Contract4 communication started");
		LogBookEvent.put("813C", "Contract4 parameter changed");
		LogBookEvent.put("813D", "Contract4 parameter changed");
		LogBookEvent.put("813E", "Contract4 parameter changed");
		LogBookEvent.put("813F", "Contract4 parameter changed");
		LogBookEvent.put("8140", "Contract4 billing reset");

		LogBookEvent.put("8141", "Reverse power flow");
		LogBookEvent.put("8142", "Breaker failure");
		LogBookEvent.put("8143", "Invalid password");
		LogBookEvent.put("8144", "Corrupted SMS");
		LogBookEvent.put("8145", "Incorrect credit code");
		LogBookEvent.put("8146", "Keypad locked");
		LogBookEvent.put("8147", "GSM network failure");
		LogBookEvent.put("8148", "Programming failed");
		LogBookEvent.put("8149", "Invalid SMS source");
		LogBookEvent.put("814A", "All code entered");
		LogBookEvent.put("814B", "Valid code time");
		LogBookEvent.put("814C", "Customer purchase request");
		LogBookEvent.put("814D", "Meter removal");
		LogBookEvent.put("814E", "Full Technical Log Book");
		LogBookEvent.put("814F", "Unable to send SMS alarm");
		LogBookEvent.put("8150", "Intrusion reset");
		LogBookEvent.put("8151", "Previous values reset");

		LogBookEvent.put("8152", "Current without Voltage L1 - start");
		LogBookEvent.put("8153", "Current without Voltage L2 - start");
		LogBookEvent.put("8154", "Current without Voltage L3 - start");
		
		LogBookEvent.put("8155", "Current without Voltage L1 - end");
		LogBookEvent.put("8156", "Current without Voltage L2 - end");
		LogBookEvent.put("8157", "Current without Voltage L3 - end");
		
	
	}
    

	

}
