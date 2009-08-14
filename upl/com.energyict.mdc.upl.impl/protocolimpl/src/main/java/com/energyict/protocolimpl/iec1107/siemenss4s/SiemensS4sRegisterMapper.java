package com.energyict.protocolimpl.iec1107.siemenss4s;

import java.util.HashMap;

import com.energyict.protocol.NoSuchRegisterException;

public class SiemensS4sRegisterMapper{
	
	private HashMap registers = new HashMap();
	public static String METER_SERIAL_NUMBER 			= "MeterSerialNumber";
	
//	public static String PROFILE_MEMORY_START			= "ProfileMemoryStart";
//	public static String PROFILE_MEMORY_STOP			= "ProfileMemoryStop";
	public static String PROFILE_POINTER				= "ProfilePointer";
	public static String PROFILE_INTERVAL				= "ProfileInterval";
	public static String TOTAL_CHANNEL_CONFIGURATION 	= "ChannelConfiguration";
	public static String CHANNEL_CONFIG1				= "ChannelConfig1";
	public static String CHANNEL_CONFIG2				= "ChannelConfig2";
	public static String CHANNEL_CONFIG3				= "ChannelConfig3";
	public static String CHANNEL_CONFIG4				= "ChannelConfig4";
	
	public static String TOTAL_REGISTER_UNITS			= "TotalRegisterUnits";
	public static String TOTAL_REGISTER_UNIT1			= "TotalRegisterUnit1";
	public static String TOTAL_REGISTER_UNIT2			= "TotalRegisterUnit2";
	public static String TOTAL_REGISTER_UNIT3			= "TotalRegisterUnit3";
	public static String TOTAL_REGISTER_UNIT4			= "TotalRegisterUnit4";
	
	public static String DATE 							= "Date";
	public static String TIME 							= "Time";
	
	public SiemensS4sRegisterMapper(){
		initRegisters();
	}
	
	//TODO check what register 13A4 and 13AC mean, they are read in the MV90 file, but not documented!
	protected void initRegisters() {
		registers.put(METER_SERIAL_NUMBER, new SiemensS4sRegister("13E0", "1C"));			// Meter serialNumber
//		registers.put(PROFILE_MEMORY_START, new SiemensS4sRegister("2000","3FFF"));				// The complete ProfileBuffer
		registers.put(PROFILE_POINTER, new SiemensS4sRegister("0800", "03"));				// Pointer where the last profileEntry was stored
		registers.put(PROFILE_INTERVAL, new SiemensS4sRegister("12C6", "01"));				// Profile interval
		registers.put(TOTAL_CHANNEL_CONFIGURATION, new SiemensS4sRegister("12EC", "08"));	// A channel config register contains two byte, but this way you get all 4 of them
		registers.put(CHANNEL_CONFIG1, new SiemensS4sRegister("12EC", "02"));				// Channel config 1
		registers.put(CHANNEL_CONFIG2, new SiemensS4sRegister("12EE", "02"));				// Channel config 2
		registers.put(CHANNEL_CONFIG3, new SiemensS4sRegister("12F0", "02"));				// Channel config 3
		registers.put(CHANNEL_CONFIG4, new SiemensS4sRegister("12F2", "02"));				// Channel config 4
		registers.put(TOTAL_REGISTER_UNITS, new SiemensS4sRegister("1068", "08"));			// All register Units
		registers.put(TOTAL_REGISTER_UNIT1, new SiemensS4sRegister("1068", "02"));			// Total register Unit 1
		registers.put(TOTAL_REGISTER_UNIT2, new SiemensS4sRegister("106A", "02"));			// Total register Unit 2
		registers.put(TOTAL_REGISTER_UNIT3, new SiemensS4sRegister("106C", "02"));			// Total register Unit 3
		registers.put(TOTAL_REGISTER_UNIT4, new SiemensS4sRegister("106E", "02"));			// Total register Unit 4
		registers.put(TIME, new SiemensS4sRegister("0900","06"));							// Time register
		registers.put(DATE, new SiemensS4sRegister("0906","06"));							// Date register
	}

	public SiemensS4sRegister find(String register) throws NoSuchRegisterException {
		if(this.registers.containsKey(register)){
			return (SiemensS4sRegister)this.registers.get(register);
		} else {
			throw new NoSuchRegisterException("A register with the name \"" + register + "\" is not know in the protocol.");
		}
	}

}
