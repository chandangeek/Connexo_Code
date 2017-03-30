/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.TreeMap;

class TypeHistoryMessageRcd {

    private static TreeMap<Integer, TypeHistoryMessageRcd> codes = new TreeMap<>();

    private int id;
    private String description;
    private int eiCode;

    static TypeHistoryMessageRcd get( int id ){
        return codes.get( new Integer( id) );
    }

    private static void put( int id, String description, int eiCode ){
        TypeHistoryMessageRcd hm = new TypeHistoryMessageRcd( );
        hm.id = id;
        hm.description = description;
        hm.eiCode = eiCode;
        codes.put(id, hm);
    }

    private static void put( int id, String description ){
        TypeHistoryMessageRcd hm = new TypeHistoryMessageRcd( );
        hm.id = id;
        hm.description = description;
        codes.put( id, hm );
    }

    /** Default EiCode is OTHER */
    static {
        //put(0x00, "No Event Recorded" );
        put(0x01, "AC Power Down", MeterEvent.POWERDOWN );
        put(0x02, "AC Power Up", MeterEvent.POWERUP );
        put(0x03, "Recorder Clock Set - Previous Time", MeterEvent.SETCLOCK_BEFORE );
        put(0x04, "Recorder Clock Set - New time", MeterEvent.SETCLOCK_AFTER );
        put(0x05, "Recorder Clock Malfunction Detected" );
        put(0x06, "PROM Error Detected", MeterEvent.HARDWARE_ERROR );
        put(0x07, "RAM Error Detected", MeterEvent.HARDWARE_ERROR  );
        put(0x08, "Program Malfunction Detected", MeterEvent.FATAL_ERROR );
        put(0x09, "Mtr. Input #1 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x0A, "Mtr. Input #2 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x0B, "Mtr. Input #3 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x0C, "Mtr. Input #4 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x0D, "Status Input 1 Closed" );
        put(0x0E, "Status Input 1 Opened" );
        put(0x0F, "Status Input 2 Closed" );
        put(0x10, "Status Input 2 Opened" );
        put(0x11, "Status Input 3 Closed" );
        put(0x12, "Status Input 3 Opened" );
        put(0x13, "Status Input 4 Closed" );
        put(0x14, "Status Input 4 Opened" );
        put(0x15, "Status Input 5 Closed" );
        put(0x16, "Status Input 5 Opened" );
        put(0x17, "Unit accessed" );
        put(0x18, "Password changed" );
        put(0x19, "Operating parameter changed" );
        put(0x1A, "Mtr. Input #5 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x1B, "Mtr. Input #6 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x1C, "Mtr. Input #7 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x1D, "Mtr. Input #8 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x1E, "Horn on" );
        put(0x1F, "Horn off" );
        put(0x20, "Alter Option command executed" );
        put(0x21, "Begin Record Command Executed" );
        put(0x22, "Attempted Access With Invalid Password" );
        put(0x23, "Mtr. Input Static" );
        put(0x24, "Channel 1 exceeded setpoint" );
        put(0x25, "Channel 2 exceeded setpoint" );
        put(0x26, "Channel 3 exceeded setpoint" );
        put(0x27, "Channel 4 exceeded setpoint" );
        put(0x28, "Channel 5 exceeded setpoint" );
        put(0x29, "Channel 6 exceeded setpoint" );
        put(0x2A, "Line of message printed" );
        put(0x2B, "Encoder simulation error" );
        put(0x30, "Status input 6 closed" );
        put(0x31, "Status input 6 opened" );
        put(0x32, "Status input 7 closed" );
        put(0x33, "Status input 7 opened" );
        put(0x34, "Status input 8 closed" );
        put(0x35, "Status input 8 opened" );
        put(0x36, "Status input 9 closed" );
        put(0x37, "Status input 9 opened" );
        put(0x38, "Status input 10 closed" );
        put(0x39, "Status input 10 opened" );
        put(0x3A, "Status input 11 closed" );
        put(0x3B, "Status input 11 opened" );
        put(0x3C, "Status input 12 closed" );
        put(0x3D, "Status input 12 opened" );
        put(0x3E, "Status input 13 closed" );
        put(0x3F, "Status input 13 opened" );
        put(0x40, "Status input 14 closed" );
        put(0x41, "Status input 14 opened" );
        put(0x42, "Status input 15 closed" );
        put(0x43, "Status input 15 opened" );
        put(0x44, "Status input 16 closed" );
        put(0x45, "Status input 16 opened" );
        put(0x4B, "Auto horn off" );
        put(0x4C, "Abort command" );
        put(0x4D, "Schedule loaded" );
        put(0x4E, "Load relay closed" );
        put(0x4F, "Load relay opened" );
        put(0x50, "Message print not available" );
        put(0x51, "Battery Low" );
        put(0x52, "EEPROM Read" );
        put(0x53, "EEPROM Written" );
        put(0x54, "RAM chip 0 error", MeterEvent.HARDWARE_ERROR );
        put(0x55, "RAM chip 1 error", MeterEvent.HARDWARE_ERROR );
        put(0x56, "RAM chip 2 error", MeterEvent.HARDWARE_ERROR );
        put(0x57, "RAM chip 3 error", MeterEvent.HARDWARE_ERROR );
        put(0x58, "Relay 1 Closed" );
        put(0x59, "Relay 2 Closed" );
        put(0x5A, "Relay 3 Closed" );
        put(0x5B, "Relay 4 Closed" );
        put(0x5C, "Relay 1 Opened" );
        put(0x5D, "Relay 2 Opened" );
        put(0x5E, "Relay 3 Opened" );
        put(0x5F, "Relay 4 Opened" );
        put(0x60, "SMD cold started", MeterEvent.POWERUP);
        put(0x61, "No Modem" );
        put(0x62, "Test mode started" );
        put(0x63, "Test mode ended" );
        put(0x64, "Low Speed Bus Error" );
        put(0x65, "Modem Failure" );
        put(0x66, "Primary CL" );
        put(0x67, "Secondary CL" );
        put(0x68, "Begin Idle Mode" );
        put(0x69, "End of Date Event Table reached" );
        put(0x6A, "Display Failure" );
        put(0x6B, "Rate (Demand) Reset", MeterEvent.CONFIGURATIONCHANGE );
        put(0x6C, "Meter readings entered" );
        put(0x6D, "Non-volatile memory failure", MeterEvent.ROM_MEMORY_ERROR );
        put(0x6E, "Output overflow", MeterEvent.REGISTER_OVERFLOW );
        put(0x6F, "Printer time out" );
        put(0x70, "Relay 5 Closed" );
        put(0x71, "Relay 6 Closed" );
        put(0x72, "Relay 7 Closed" );
        put(0x73, "Relay 8 Closed" );
        put(0x74, "Relay 9 Closed" );
        put(0x75, "Relay 10 Closed" );
        put(0x76, "Relay 11 Closed" );
        put(0x77, "Relay 12 Closed" );
        put(0x78, "Relay 5 Opened" );
        put(0x79, "Relay 6 Opened" );
        put(0x7A, "Relay 7 Opened" );
        put(0x7B, "Relay 8 Opened" );
        put(0x7C, "Relay 9 Opened" );
        put(0x7D, "Relay 10 Opened" );
        put(0x7E, "Relay 11 Opened" );
        put(0x7F, "Relay 12 Opened" );
        put(0x80, "Mtr. Input #9 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x81, "Mtr. Input #10 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x82, "Mtr. Input #11 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x83, "Mtr. Input #12 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x84, "Mtr. Input #13 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x85, "Mtr. Input #14 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x86, "Mtr. Input #15 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x87, "Mtr. Input #16 Register Overflow Detected", MeterEvent.REGISTER_OVERFLOW );
        put(0x88, "Relay 13 Closed" );
        put(0x89, "Relay 14 Closed" );
        put(0x8A, "Relay 15 Closed" );
        put(0x8B, "Relay 16 Closed" );
        put(0x8C, "Relay 13 Opened" );
        put(0x8D, "Relay 14 Opened" );
        put(0x8E, "Relay 15 Opened" );
        put(0x8F, "Relay 16 Opened" );
        put(0xBC, "Socket Sleuth Phase A B" );
        put(0xBD, "Socket Sleuth Phase B C" );
        put(0xBE, "Socket Sleuth Phase C A" );
        put(0xBF, "Battery malfunction" );
        put(0xC0, "Phase A Loss Short", MeterEvent.PHASE_FAILURE );
        put(0xC1, "Phase B Loss Short", MeterEvent.PHASE_FAILURE );
        put(0xC2, "Phase C Loss Short", MeterEvent.PHASE_FAILURE );
        put(0xC3, "Phase A Loss Long", MeterEvent.PHASE_FAILURE );
        put(0xC4, "Phase B Loss Long", MeterEvent.PHASE_FAILURE );
        put(0xC5, "Phase C Loss Long", MeterEvent.PHASE_FAILURE );
        put(0xC6, "Phase A Low" );
        put(0xC7, "Phase B Low" );
        put(0xC8, "Phase C Low" );
        put(0xC9, "Phase A High" );
        put(0xCA, "Phase B High" );
        put(0xCB, "Phase C High" );
        put(0xCC, "Phase A Delta" );
        put(0xCD, "Phase B Delta" );
        put(0xCE, "Phase C Delta" );
        put(0xCF, "Power Quality Event" );
        put(0xD0, "RAM Chip 4 Error", MeterEvent.HARDWARE_ERROR );
        put(0xD1, "RAM Chip 5 Error", MeterEvent.HARDWARE_ERROR );
        put(0xD2, "RAM Chip 6 Error", MeterEvent.HARDWARE_ERROR );
        put(0xD3, "RAM Chip 7 Error", MeterEvent.HARDWARE_ERROR );
        put(0xD4, "Battery Installed" );
        put(0xD5, "Cartridge inserted" );
        put(0xD6, "Cartridge changed" );
        put(0xD7, "Cartridge removed" );
        put(0xD8, "Operating system failure", MeterEvent.FATAL_ERROR );
        put(0xD9, "Diagnostic event" );
    }

    String getDescription() {
        return description;
    }

    int getEiCode() {
        return eiCode;
    }

    public String toString(){
        return "HistoryMessage [" + Integer.toHexString(id) + ", " + description + " " + this.eiCode + "]";
    }

}
