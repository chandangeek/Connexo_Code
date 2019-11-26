package com.energyict.protocolimplv2.edmi.mk10.events;

import com.energyict.protocolimpl.ansi.c12.tables.Log;
import org.junit.Assert;
import org.junit.Test;

import java.util.TimeZone;

import static com.energyict.protocolimplv2.edmi.mk10.events.LogBookDescription.*;
import static org.junit.Assert.assertEquals;

public class EventTest {

    private static TimeZone tz = TimeZone.getTimeZone("Europe/Amsterdam");

    @Test
    public void testPowerOffEvents() {
        assertEventCode(SYSTEM, 0x1000, 1);
        assertEventCode(SYSTEM, 0x1001, 1);
        assertEventDescription(SYSTEM, 0x1000, "The meter was switched off");
        assertEventDescription(SYSTEM, 0x1001, "The meter was switched off");

    }

    @Test
    public void testPowerOnEvents() {
        assertEventCode(SYSTEM, 0x1010, 2);
        assertEventCode(SYSTEM, 0x1011, 2);
        assertEventCode(SYSTEM, 0x1012, 2);
        assertEventCode(SYSTEM, 0x1013, 2);
        assertEventCode(SYSTEM, 0x1014, 2);
        assertEventCode(SYSTEM, 0x1015, 2);
        assertEventCode(SYSTEM, 0x1016, 2);
        assertEventCode(SYSTEM, 0x1017, 2);
        assertEventCode(SYSTEM, 0x1018, 2);
        assertEventCode(SYSTEM, 0x1019, 2);
        assertEventCode(SYSTEM, 0x101A, 2);
        assertEventCode(SYSTEM, 0x101B, 2);
        assertEventCode(SYSTEM, 0x101C, 2);
        assertEventCode(SYSTEM, 0x101D, 2);
        assertEventCode(SYSTEM, 0x101E, 2);
        assertEventCode(SYSTEM, 0x101F, 2);
        assertEventDescription(SYSTEM, 0x1010, "The meter powered up. Reason: 0x00");
        assertEventDescription(SYSTEM, 0x1011, "The meter powered up. Reason: 0x01");
        assertEventDescription(SYSTEM, 0x1012, "The meter powered up. Reason: 0x02");
        assertEventDescription(SYSTEM, 0x1013, "The meter powered up. Reason: 0x03");
        assertEventDescription(SYSTEM, 0x1014, "The meter powered up. Reason: 0x04");
        assertEventDescription(SYSTEM, 0x1015, "The meter powered up. Reason: 0x05");
        assertEventDescription(SYSTEM, 0x1016, "The meter powered up. Reason: 0x06");
        assertEventDescription(SYSTEM, 0x1017, "The meter powered up. Reason: 0x07");
        assertEventDescription(SYSTEM, 0x1018, "The meter powered up. Reason: 0x08");
        assertEventDescription(SYSTEM, 0x1019, "The meter powered up. Reason: 0x09");
        assertEventDescription(SYSTEM, 0x101A, "The meter powered up. Reason: 0x0a");
        assertEventDescription(SYSTEM, 0x101B, "The meter powered up. Reason: 0x0b");
        assertEventDescription(SYSTEM, 0x101C, "The meter powered up. Reason: 0x0c");
        assertEventDescription(SYSTEM, 0x101D, "The meter powered up. Reason: 0x0d");
        assertEventDescription(SYSTEM, 0x101E, "The meter powered up. Reason: 0x0e");
        assertEventDescription(SYSTEM, 0x101F, "The meter powered up. Reason: 0x0f");
    }

    @Test
    public void testRecoveredEvents() {
        assertEventCode(SYSTEM, 0x1020, 204);
        assertEventCode(SYSTEM, 0x1021, 204);
        assertEventCode(SYSTEM, 0x1022, 204);
        assertEventCode(SYSTEM, 0x1023, 204);
        assertEventCode(SYSTEM, 0x1024, 204);
        assertEventCode(SYSTEM, 0x1025, 204);
        assertEventCode(SYSTEM, 0x1026, 204);
        assertEventCode(SYSTEM, 0x1027, 204);
        assertEventCode(SYSTEM, 0x1028, 204);
        assertEventCode(SYSTEM, 0x1029, 204);
        assertEventCode(SYSTEM, 0x102A, 204);
        assertEventCode(SYSTEM, 0x102B, 204);
        assertEventCode(SYSTEM, 0x102C, 204);
        assertEventCode(SYSTEM, 0x102D, 204);
        assertEventCode(SYSTEM, 0x102E, 204);
        assertEventCode(SYSTEM, 0x102F, 204);
        assertEventDescription(SYSTEM, 0x1020, "Recovered some parameters. Parameters: 0x00");
        assertEventDescription(SYSTEM, 0x1021, "Recovered some parameters. Parameters: 0x01 - Flash backup bad");
        assertEventDescription(SYSTEM, 0x1022, "Recovered some parameters. Parameters: 0x02");
        assertEventDescription(SYSTEM, 0x1023, "Recovered some parameters. Parameters: 0x03 - Flash backup bad");
        assertEventDescription(SYSTEM, 0x1024, "Recovered some parameters. Parameters: 0x04 - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1025, "Recovered some parameters. Parameters: 0x05 - Flash backup bad - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1026, "Recovered some parameters. Parameters: 0x06 - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1027, "Recovered some parameters. Parameters: 0x07 - Flash backup bad - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1028, "Recovered some parameters. Parameters: 0x08 - Control data restored");
        assertEventDescription(SYSTEM, 0x1029, "Recovered some parameters. Parameters: 0x09 - Flash backup bad - Control data restored");
        assertEventDescription(SYSTEM, 0x102A, "Recovered some parameters. Parameters: 0x0a - Control data restored");
        assertEventDescription(SYSTEM, 0x102B, "Recovered some parameters. Parameters: 0x0b - Flash backup bad - Control data restored");
        assertEventDescription(SYSTEM, 0x102C, "Recovered some parameters. Parameters: 0x0c - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x102D, "Recovered some parameters. Parameters: 0x0d - Flash backup bad - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x102E, "Recovered some parameters. Parameters: 0x0e - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x102F, "Recovered some parameters. Parameters: 0x0f - Flash backup bad - Energy accumulation restored - Control data restored");
    }

    @Test
    public void testInitalizedEvents() {
        assertEventCode(SYSTEM, 0x1030, 205);
        assertEventCode(SYSTEM, 0x1031, 205);
        assertEventCode(SYSTEM, 0x1032, 205);
        assertEventCode(SYSTEM, 0x1033, 205);
        assertEventCode(SYSTEM, 0x1034, 205);
        assertEventCode(SYSTEM, 0x1035, 205);
        assertEventCode(SYSTEM, 0x1036, 205);
        assertEventCode(SYSTEM, 0x1037, 205);
        assertEventCode(SYSTEM, 0x1038, 205);
        assertEventCode(SYSTEM, 0x1039, 205);
        assertEventCode(SYSTEM, 0x103A, 205);
        assertEventCode(SYSTEM, 0x103B, 205);
        assertEventCode(SYSTEM, 0x103C, 205);
        assertEventCode(SYSTEM, 0x103D, 205);
        assertEventCode(SYSTEM, 0x103E, 205);
        assertEventCode(SYSTEM, 0x103F, 205);
        assertEventDescription(SYSTEM, 0x1030, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x00");
        assertEventDescription(SYSTEM, 0x1031, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x01 - Flash backup bad");
        assertEventDescription(SYSTEM, 0x1032, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x02");
        assertEventDescription(SYSTEM, 0x1033, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x03 - Flash backup bad");
        assertEventDescription(SYSTEM, 0x1034, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x04 - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1035, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x05 - Flash backup bad - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1036, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x06 - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1037, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x07 - Flash backup bad - Energy accumulation restored");
        assertEventDescription(SYSTEM, 0x1038, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x08 - Control data restored");
        assertEventDescription(SYSTEM, 0x1039, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x09 - Flash backup bad - Control data restored");
        assertEventDescription(SYSTEM, 0x103A, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0a - Control data restored");
        assertEventDescription(SYSTEM, 0x103B, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0b - Flash backup bad - Control data restored");
        assertEventDescription(SYSTEM, 0x103C, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0c - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x103D, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0d - Flash backup bad - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x103E, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0e - Energy accumulation restored - Control data restored");
        assertEventDescription(SYSTEM, 0x103F, "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x0f - Flash backup bad - Energy accumulation restored - Control data restored");
    }


    @Test
    public void testRunTimeEvents() {
        assertEventCode(SYSTEM, 0x1040, 206);
        assertEventCode(SYSTEM, 0x1041, 206);
        assertEventCode(SYSTEM, 0x1042, 206);
        assertEventCode(SYSTEM, 0x1043, 206);
        assertEventCode(SYSTEM, 0x1044, 206);
        assertEventCode(SYSTEM, 0x1045, 206);
        assertEventCode(SYSTEM, 0x1046, 206);
        assertEventCode(SYSTEM, 0x1047, 206);
        assertEventCode(SYSTEM, 0x1048, 206);
        assertEventCode(SYSTEM, 0x1049, 206);
        assertEventCode(SYSTEM, 0x104A, 206);
        assertEventCode(SYSTEM, 0x104B, 206);
        assertEventCode(SYSTEM, 0x104C, 206);
        assertEventCode(SYSTEM, 0x104D, 206);
        assertEventCode(SYSTEM, 0x104E, 206);
        assertEventCode(SYSTEM, 0x104F, 206);
        assertEventDescription(SYSTEM, 0x1040, "Meter runtime statistics changed. 0x00 - ON time changed.");
        assertEventDescription(SYSTEM, 0x1041, "Meter runtime statistics changed. 0x01 - OFF time changed.");
        assertEventDescription(SYSTEM, 0x1042, "Meter runtime statistics changed. 0x02 - Number of power ups changed.");
        assertEventDescription(SYSTEM, 0x1043, "Meter runtime statistics changed. 0x03 - Reserved.");
        assertEventDescription(SYSTEM, 0x1044, "Meter runtime statistics changed. 0x04 - Reserved.");
        assertEventDescription(SYSTEM, 0x1045, "Meter runtime statistics changed. 0x05 - Reserved.");
        assertEventDescription(SYSTEM, 0x1046, "Meter runtime statistics changed. 0x06 - Reserved.");
        assertEventDescription(SYSTEM, 0x1047, "Meter runtime statistics changed. 0x07 - Reserved.");
        assertEventDescription(SYSTEM, 0x1048, "Meter runtime statistics changed. 0x08 - Reserved.");
        assertEventDescription(SYSTEM, 0x1049, "Meter runtime statistics changed. 0x09 - Reserved.");
        assertEventDescription(SYSTEM, 0x104A, "Meter runtime statistics changed. 0x0a - Reserved.");
        assertEventDescription(SYSTEM, 0x104B, "Meter runtime statistics changed. 0x0b - Reserved.");
        assertEventDescription(SYSTEM, 0x104C, "Meter runtime statistics changed. 0x0c - Reserved.");
        assertEventDescription(SYSTEM, 0x104D, "Meter runtime statistics changed. 0x0d - Reserved.");
        assertEventDescription(SYSTEM, 0x104E, "Meter runtime statistics changed. 0x0e - Reserved.");
        assertEventDescription(SYSTEM, 0x104F, "Meter runtime statistics changed. 0x0f - Reserved.");
    }

    @Test
    public void testFirstRelayEvents() {
        assertEventCode(SYSTEM, 0x1139, 210);
        assertEventCode(SYSTEM, 0x113A, 210);
        assertEventCode(SYSTEM, 0x113B, 210);
        assertEventCode(SYSTEM, 0x113C, 210);
        assertEventCode(SYSTEM, 0x113D, 210);
        assertEventCode(SYSTEM, 0x113E, 210);
        assertEventCode(SYSTEM, 0x113F, 210);
        assertEventCode(SYSTEM, 0x11B9, 208);
        assertEventCode(SYSTEM, 0x11BA, 208);
        assertEventCode(SYSTEM, 0x11BB, 208);
        assertEventCode(SYSTEM, 0x11BC, 208);
        assertEventCode(SYSTEM, 0x11BD, 208);
        assertEventCode(SYSTEM, 0x11BE, 208);
        assertEventCode(SYSTEM, 0x11BF, 208);
        assertEventCode(SYSTEM, 0x1179, 209);
        assertEventCode(SYSTEM, 0x117A, 209);
        assertEventCode(SYSTEM, 0x117B, 209);
        assertEventCode(SYSTEM, 0x117C, 209);
        assertEventCode(SYSTEM, 0x117D, 209);
        assertEventCode(SYSTEM, 0x117E, 209);
        assertEventCode(SYSTEM, 0x117F, 209);
        assertEventCode(SYSTEM, 0x11F9, 207);
        assertEventCode(SYSTEM, 0x11FA, 207);
        assertEventCode(SYSTEM, 0x11FB, 207);
        assertEventCode(SYSTEM, 0x11FC, 207);
        assertEventCode(SYSTEM, 0x11FD, 207);
        assertEventCode(SYSTEM, 0x11FE, 207);
        assertEventCode(SYSTEM, 0x11FF, 207);
        assertEventDescription(SYSTEM, 0x1139, "Relay changed. Relay control via register F050-8- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113A, "Relay changed. Disconnect button was pressed- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113B, "Relay changed. Connect button was pressed- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113C, "Relay changed. Calendar (tariff) changed- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113D, "Relay changed. The physical relay changed state- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113E, "Relay changed. Reason:3e- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x113F, "Relay changed. Relay Stuck recorded- Relay number: 1, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x11B9, "Relay changed. Relay control via register F050-8- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BA, "Relay changed. Disconnect button was pressed- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BB, "Relay changed. Connect button was pressed- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BC, "Relay changed. Calendar (tariff) changed- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BD, "Relay changed. The physical relay changed state- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BE, "Relay changed. Reason:3e- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x11BF, "Relay changed. Relay Stuck recorded- Relay number: 1, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x1179, "Relay changed. Relay control via register F050-8- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117A, "Relay changed. Disconnect button was pressed- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117B, "Relay changed. Connect button was pressed- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117C, "Relay changed. Calendar (tariff) changed- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117D, "Relay changed. The physical relay changed state- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117E, "Relay changed. Reason:3e- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x117F, "Relay changed. Relay Stuck recorded- Relay number: 1, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x11F9, "Relay changed. Relay control via register F050-8- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FA, "Relay changed. Disconnect button was pressed- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FB, "Relay changed. Connect button was pressed- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FC, "Relay changed. Calendar (tariff) changed- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FD, "Relay changed. The physical relay changed state- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FE, "Relay changed. Reason:3e- Relay number: 1, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x11FF, "Relay changed. Relay Stuck recorded- Relay number: 1, status: enabled - connected");
    }

    @Test
    public void testSecondsRelayEvents() {
        assertEventCode(SYSTEM, 0x1239, 210);
        assertEventCode(SYSTEM, 0x123A, 210);
        assertEventCode(SYSTEM, 0x123B, 210);
        assertEventCode(SYSTEM, 0x123C, 210);
        assertEventCode(SYSTEM, 0x123D, 210);
        assertEventCode(SYSTEM, 0x123E, 210);
        assertEventCode(SYSTEM, 0x123F, 210);
        assertEventCode(SYSTEM, 0x12B9, 208);
        assertEventCode(SYSTEM, 0x12BA, 208);
        assertEventCode(SYSTEM, 0x12BB, 208);
        assertEventCode(SYSTEM, 0x12BC, 208);
        assertEventCode(SYSTEM, 0x12BD, 208);
        assertEventCode(SYSTEM, 0x12BE, 208);
        assertEventCode(SYSTEM, 0x12BF, 208);
        assertEventCode(SYSTEM, 0x1279, 209);
        assertEventCode(SYSTEM, 0x127A, 209);
        assertEventCode(SYSTEM, 0x127B, 209);
        assertEventCode(SYSTEM, 0x127C, 209);
        assertEventCode(SYSTEM, 0x127D, 209);
        assertEventCode(SYSTEM, 0x127E, 209);
        assertEventCode(SYSTEM, 0x127F, 209);
        assertEventCode(SYSTEM, 0x12F9, 207);
        assertEventCode(SYSTEM, 0x12FA, 207);
        assertEventCode(SYSTEM, 0x12FB, 207);
        assertEventCode(SYSTEM, 0x12FC, 207);
        assertEventCode(SYSTEM, 0x12FD, 207);
        assertEventCode(SYSTEM, 0x12FE, 207);
        assertEventCode(SYSTEM, 0x12FF, 207);
        assertEventDescription(SYSTEM, 0x1239, "Relay changed. Relay control via register F050-8- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123A, "Relay changed. Disconnect button was pressed- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123B, "Relay changed. Connect button was pressed- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123C, "Relay changed. Calendar (tariff) changed- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123D, "Relay changed. The physical relay changed state- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123E, "Relay changed. Reason:3e- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x123F, "Relay changed. Relay Stuck recorded- Relay number: 2, status: disabled - disconnected");
        assertEventDescription(SYSTEM, 0x12B9, "Relay changed. Relay control via register F050-8- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BA, "Relay changed. Disconnect button was pressed- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BB, "Relay changed. Connect button was pressed- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BC, "Relay changed. Calendar (tariff) changed- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BD, "Relay changed. The physical relay changed state- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BE, "Relay changed. Reason:3e- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x12BF, "Relay changed. Relay Stuck recorded- Relay number: 2, status: enabled - disconnected");
        assertEventDescription(SYSTEM, 0x1279, "Relay changed. Relay control via register F050-8- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127A, "Relay changed. Disconnect button was pressed- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127B, "Relay changed. Connect button was pressed- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127C, "Relay changed. Calendar (tariff) changed- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127D, "Relay changed. The physical relay changed state- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127E, "Relay changed. Reason:3e- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x127F, "Relay changed. Relay Stuck recorded- Relay number: 2, status: disabled - connected");
        assertEventDescription(SYSTEM, 0x12F9, "Relay changed. Relay control via register F050-8- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FA, "Relay changed. Disconnect button was pressed- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FB, "Relay changed. Connect button was pressed- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FC, "Relay changed. Calendar (tariff) changed- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FD, "Relay changed. The physical relay changed state- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FE, "Relay changed. Reason:3e- Relay number: 2, status: enabled - connected");
        assertEventDescription(SYSTEM, 0x12FF, "Relay changed. Relay Stuck recorded- Relay number: 2, status: enabled - connected");
    }

    @Test
    public void testPortTimeChangedEvents() {
        assertEventCode(SYSTEM, 0x20C0, 4);
        assertEventCode(SYSTEM, 0x20C1, 4);
        assertEventCode(SYSTEM, 0x20C2, 4);
        assertEventCode(SYSTEM, 0x20D0, 4);
        assertEventCode(SYSTEM, 0x20D1, 4);
        assertEventCode(SYSTEM, 0x20D2, 4);
        assertEventCode(SYSTEM, 0x20D3, 4);
        assertEventDescription(SYSTEM, 0x20C0, "System time changed from command on port. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x20C1, "System time changed from pulsing input. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x20C2, "System time changed from ripple count. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x20C3, "System time changed from RESERVED. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x20D0, "System time changed from command on port. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x20D1, "System time changed from pulsing input. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x20D2, "System time changed from ripple count. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x20D3, "System time changed from RESERVED. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x20E0, "System time changed from command on port. (Port: SCADA)");
        assertEventDescription(SYSTEM, 0x20E1, "System time changed from pulsing input. (Port: SCADA)");
        assertEventDescription(SYSTEM, 0x20E2, "System time changed from ripple count. (Port: SCADA)");
        assertEventDescription(SYSTEM, 0x20E3, "System time changed from RESERVED. (Port: SCADA)");
    }


    @Test
    public void testSystemTimeChangedEvents() {
        assertEventCode(SYSTEM, 0x20CF, 5);
        assertEventCode(SYSTEM, 0x20DF, 5);
        assertEventCode(SYSTEM, 0x20EF, 5);
        assertEventDescription(SYSTEM, 0x20CF, "System time changed to this time. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x20DF, "System time changed to this time. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x20EF, "System time changed to this time. (Port: SCADA)");
    }


    @Test
    public void testEfaLatchedEvents() {
        assertEventCode(SYSTEM, 0x3000, 23);
        assertEventCode(SYSTEM, 0x3001, 32);
        assertEventCode(SYSTEM, 0x3002, 10);
        assertEventCode(SYSTEM, 0x3003, 8);
        assertEventCode(SYSTEM, 0x3004, 36);
        assertEventCode(SYSTEM, 0x3005, 13);
        assertEventCode(SYSTEM, 0x3006, 13);
        assertEventCode(SYSTEM, 0x3007, 39);
        assertEventCode(SYSTEM, 0x3008, 26);
        assertEventCode(SYSTEM, 0x3009, 30);
        assertEventCode(SYSTEM, 0x300A, 23);
        assertEventCode(SYSTEM, 0x300B, 20);
        assertEventCode(SYSTEM, 0x300C, 20);
        assertEventCode(SYSTEM, 0x300D, 20);
        assertEventCode(SYSTEM, 0x300E, 14);
        assertEventCode(SYSTEM, 0x300F, 0);
        assertEventCode(SYSTEM, 0x3010, 23);
        assertEventCode(SYSTEM, 0x3011, 23);
        assertEventCode(SYSTEM, 0x3012, 23);
        assertEventCode(SYSTEM, 0x3014, 23);
        assertEventCode(SYSTEM, 0x3015, 23);
        assertEventCode(SYSTEM, 0x3016, 23);
        assertEventCode(SYSTEM, 0x3017, 23);
        assertEventCode(SYSTEM, 0x3018, 23);
        assertEventCode(SYSTEM, 0x3019, 23);
        assertEventCode(SYSTEM, 0x301A, 23);
        assertEventCode(SYSTEM, 0x301B, 23);
        assertEventCode(SYSTEM, 0x301C, 23);
        assertEventCode(SYSTEM, 0x301D, 23);
        assertEventCode(SYSTEM, 0x301E, 23);
        assertEventCode(SYSTEM, 0x301F, 23);
        assertEventCode(SYSTEM, 0x302A, 14);
        assertEventCode(SYSTEM, 0x302B, 14);
        assertEventCode(SYSTEM, 0x302C, 14);
        assertEventCode(SYSTEM, 0x302D, 14);
        assertEventCode(SYSTEM, 0x302E, 14);
        assertEventCode(SYSTEM, 0x302F, 14);

        assertEventDescription(SYSTEM, 0x3000, "EFA: User defined/magnetic tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3001, "EFA: Battery failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3002, "EFA: Pulsing output overflow. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3003, "EFA: Data flash failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3004, "EFA: Program flash failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3005, "EFA: RAM or LCD failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3006, "EFA: Modem failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3007, "EFA: Calibration data loss. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3008, "EFA: Reverse power. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3009, "EFA: Clock failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300A, "EFA: Tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300B, "EFA: Incorrect phase rotation. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300C, "EFA: VT failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300D, "EFA: Voltage tolerance error. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300E, "EFA: Asymmetric power. (LATCHED)");
        assertEventDescription(SYSTEM, 0x300F, "EFA: Reference failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3010, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3011, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3012, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3014, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3015, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3016, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3017, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3018, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x3019, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301A, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301B, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301C, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301D, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301E, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x301F, "EFA: Advanced tamper. (LATCHED)");
        assertEventDescription(SYSTEM, 0x302A, "EFA: Current missing. (LATCHED)");
        assertEventDescription(SYSTEM, 0x302B, "EFA: Relay failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x302C, "EFA: Source Impedance. (LATCHED)");
        assertEventDescription(SYSTEM, 0x302D, "EFA: 3-phase neutral mismatch (LATCHED)");
        assertEventDescription(SYSTEM, 0x302E, "EFA: Wireless failure. (LATCHED)");
        assertEventDescription(SYSTEM, 0x302F, "EFA: Over current. (LATCHED)");
    }

    @Test
    public void testEfaLatchClearedEvents() {
        assertEventCode(SYSTEM, 0x3040, 23);
        assertEventCode(SYSTEM, 0x3041, 32);
        assertEventCode(SYSTEM, 0x3042, 10);
        assertEventCode(SYSTEM, 0x3043, 8);
        assertEventCode(SYSTEM, 0x3044, 36);
        assertEventCode(SYSTEM, 0x3045, 13);
        assertEventCode(SYSTEM, 0x3046, 13);
        assertEventCode(SYSTEM, 0x3047, 39);
        assertEventCode(SYSTEM, 0x3048, 26);
        assertEventCode(SYSTEM, 0x3049, 30);
        assertEventCode(SYSTEM, 0x304A, 23);
        assertEventCode(SYSTEM, 0x304B, 20);
        assertEventCode(SYSTEM, 0x304C, 20);
        assertEventCode(SYSTEM, 0x304D, 20);
        assertEventCode(SYSTEM, 0x304E, 14);
        assertEventCode(SYSTEM, 0x304F, 0);
        assertEventCode(SYSTEM, 0x3050, 23);
        assertEventCode(SYSTEM, 0x3051, 23);
        assertEventCode(SYSTEM, 0x3052, 23);
        assertEventCode(SYSTEM, 0x3054, 23);
        assertEventCode(SYSTEM, 0x3055, 23);
        assertEventCode(SYSTEM, 0x3056, 23);
        assertEventCode(SYSTEM, 0x3057, 23);
        assertEventCode(SYSTEM, 0x3058, 23);
        assertEventCode(SYSTEM, 0x3059, 23);
        assertEventCode(SYSTEM, 0x305A, 23);
        assertEventCode(SYSTEM, 0x305B, 23);
        assertEventCode(SYSTEM, 0x305C, 23);
        assertEventCode(SYSTEM, 0x305D, 23);
        assertEventCode(SYSTEM, 0x305E, 23);
        assertEventCode(SYSTEM, 0x305F, 23);
        assertEventCode(SYSTEM, 0x306A, 14);
        assertEventCode(SYSTEM, 0x306B, 14);
        assertEventCode(SYSTEM, 0x306C, 14);
        assertEventCode(SYSTEM, 0x306D, 14);
        assertEventCode(SYSTEM, 0x306E, 14);
        assertEventCode(SYSTEM, 0x306F, 14);

        assertEventDescription(SYSTEM, 0x3040, "EFA: User defined/magnetic tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3041, "EFA: Battery failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3042, "EFA: Pulsing output overflow. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3043, "EFA: Data flash failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3044, "EFA: Program flash failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3045, "EFA: RAM or LCD failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3046, "EFA: Modem failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3047, "EFA: Calibration data loss. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3048, "EFA: Reverse power. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3049, "EFA: Clock failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304A, "EFA: Tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304B, "EFA: Incorrect phase rotation. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304C, "EFA: VT failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304D, "EFA: Voltage tolerance error. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304E, "EFA: Asymmetric power. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x304F, "EFA: Reference failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3050, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3051, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3052, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3054, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3055, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3056, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3057, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3058, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x3059, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305A, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305B, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305C, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305D, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305E, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x305F, "EFA: Advanced tamper. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306A, "EFA: Current missing. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306B, "EFA: Relay failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306C, "EFA: Source Impedance. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306D, "EFA: 3-phase neutral mismatch (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306E, "EFA: Wireless failure. (LATCH CLEARED)");
        assertEventDescription(SYSTEM, 0x306F, "EFA: Over current. (LATCH CLEARED)");

    }

    @Test
    public void testEfaBecameActiveEvents() {
        assertEventCode(SYSTEM, 0x3080, 23);
        assertEventCode(SYSTEM, 0x3081, 32);
        assertEventCode(SYSTEM, 0x3082, 10);
        assertEventCode(SYSTEM, 0x3083, 8);
        assertEventCode(SYSTEM, 0x3084, 36);
        assertEventCode(SYSTEM, 0x3085, 13);
        assertEventCode(SYSTEM, 0x3086, 13);
        assertEventCode(SYSTEM, 0x3087, 39);
        assertEventCode(SYSTEM, 0x3088, 26);
        assertEventCode(SYSTEM, 0x3089, 30);
        assertEventCode(SYSTEM, 0x308A, 23);
        assertEventCode(SYSTEM, 0x308B, 20);
        assertEventCode(SYSTEM, 0x308C, 20);
        assertEventCode(SYSTEM, 0x308D, 20);
        assertEventCode(SYSTEM, 0x308E, 14);
        assertEventCode(SYSTEM, 0x308F, 0);
        assertEventCode(SYSTEM, 0x3090, 23);
        assertEventCode(SYSTEM, 0x3091, 23);
        assertEventCode(SYSTEM, 0x3092, 23);
        assertEventCode(SYSTEM, 0x3094, 23);
        assertEventCode(SYSTEM, 0x3095, 23);
        assertEventCode(SYSTEM, 0x3096, 23);
        assertEventCode(SYSTEM, 0x3097, 23);
        assertEventCode(SYSTEM, 0x3098, 23);
        assertEventCode(SYSTEM, 0x3099, 23);
        assertEventCode(SYSTEM, 0x309A, 23);
        assertEventCode(SYSTEM, 0x309B, 23);
        assertEventCode(SYSTEM, 0x309C, 23);
        assertEventCode(SYSTEM, 0x309D, 23);
        assertEventCode(SYSTEM, 0x309E, 23);
        assertEventCode(SYSTEM, 0x309F, 23);
        assertEventCode(SYSTEM, 0x30AA, 14);
        assertEventCode(SYSTEM, 0x30AB, 14);
        assertEventCode(SYSTEM, 0x30AC, 14);
        assertEventCode(SYSTEM, 0x30AD, 14);
        assertEventCode(SYSTEM, 0x30AE, 14);
        assertEventCode(SYSTEM, 0x30AF, 14);

        assertEventDescription(SYSTEM, 0x3080, "EFA: User defined/magnetic tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3081, "EFA: Battery failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3082, "EFA: Pulsing output overflow. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3083, "EFA: Data flash failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3084, "EFA: Program flash failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3085, "EFA: RAM or LCD failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3086, "EFA: Modem failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3087, "EFA: Calibration data loss. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3088, "EFA: Reverse power. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3089, "EFA: Clock failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308A, "EFA: Tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308B, "EFA: Incorrect phase rotation. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308C, "EFA: VT failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308D, "EFA: Voltage tolerance error. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308E, "EFA: Asymmetric power. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x308F, "EFA: Reference failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3090, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3091, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3092, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3094, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3095, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3096, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3097, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3098, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x3099, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309A, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309B, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309C, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309D, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309E, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x309F, "EFA: Advanced tamper. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AA, "EFA: Current missing. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AB, "EFA: Relay failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AC, "EFA: Source Impedance. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AD, "EFA: 3-phase neutral mismatch (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AE, "EFA: Wireless failure. (BECAME ACTIVE)");
        assertEventDescription(SYSTEM, 0x30AF, "EFA: Over current. (BECAME ACTIVE)");
    }

    @Test
    public void testEfaBecameInactiveEvents() {
        assertEventCode(SYSTEM, 0x30C0, 23);
        assertEventCode(SYSTEM, 0x30C1, 32);
        assertEventCode(SYSTEM, 0x30C2, 10);
        assertEventCode(SYSTEM, 0x30C3, 8);
        assertEventCode(SYSTEM, 0x30C4, 36);
        assertEventCode(SYSTEM, 0x30C5, 13);
        assertEventCode(SYSTEM, 0x30C6, 13);
        assertEventCode(SYSTEM, 0x30C7, 39);
        assertEventCode(SYSTEM, 0x30C8, 26);
        assertEventCode(SYSTEM, 0x30C9, 30);
        assertEventCode(SYSTEM, 0x30CA, 23);
        assertEventCode(SYSTEM, 0x30CB, 20);
        assertEventCode(SYSTEM, 0x30CC, 20);
        assertEventCode(SYSTEM, 0x30CD, 20);
        assertEventCode(SYSTEM, 0x30CE, 14);
        assertEventCode(SYSTEM, 0x30CF, 0);
        assertEventCode(SYSTEM, 0x30D0, 23);
        assertEventCode(SYSTEM, 0x30D1, 23);
        assertEventCode(SYSTEM, 0x30D2, 23);
        assertEventCode(SYSTEM, 0x30D4, 23);
        assertEventCode(SYSTEM, 0x30D5, 23);
        assertEventCode(SYSTEM, 0x30D6, 23);
        assertEventCode(SYSTEM, 0x30D7, 23);
        assertEventCode(SYSTEM, 0x30D8, 23);
        assertEventCode(SYSTEM, 0x30D9, 23);
        assertEventCode(SYSTEM, 0x30DB, 23);
        assertEventCode(SYSTEM, 0x30DC, 23);
        assertEventCode(SYSTEM, 0x30DD, 23);
        assertEventCode(SYSTEM, 0x30DE, 23);
        assertEventCode(SYSTEM, 0x30DF, 23);
        assertEventCode(SYSTEM, 0x30EA, 14);
        assertEventCode(SYSTEM, 0x30EB, 14);
        assertEventCode(SYSTEM, 0x30EC, 14);
        assertEventCode(SYSTEM, 0x30ED, 14);
        assertEventCode(SYSTEM, 0x30EE, 14);
        assertEventCode(SYSTEM, 0x30EF, 14);

        assertEventDescription(SYSTEM, 0x30C0, "EFA: User defined/magnetic tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C1, "EFA: Battery failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C2, "EFA: Pulsing output overflow. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C3, "EFA: Data flash failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C4, "EFA: Program flash failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C5, "EFA: RAM or LCD failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C6, "EFA: Modem failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C7, "EFA: Calibration data loss. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C8, "EFA: Reverse power. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30C9, "EFA: Clock failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CA, "EFA: Tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CB, "EFA: Incorrect phase rotation. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CC, "EFA: VT failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CD, "EFA: Voltage tolerance error. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CE, "EFA: Asymmetric power. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30CF, "EFA: Reference failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D0, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D1, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D2, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D4, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D5, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D6, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D7, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D8, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30D9, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DA, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DB, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DC, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DD, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DE, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30DF, "EFA: Advanced tamper. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30EA, "EFA: Current missing. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30EB, "EFA: Relay failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30EC, "EFA: Source Impedance. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30ED, "EFA: 3-phase neutral mismatch (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30EE, "EFA: Wireless failure. (BECAME INACTIVE)");
        assertEventDescription(SYSTEM, 0x30EF, "EFA: Over current. (BECAME INACTIVE)");
    }

    @Test
    public void testRadioModuleTamperAlarm() {
        assertEventCode(SYSTEM, 0x3100, 23);
        assertEventCode(SYSTEM, 0x311F, 23);
        assertEventCode(SYSTEM, 0x3140, 23);
        assertEventCode(SYSTEM, 0x315F, 23);
        assertEventDescription(SYSTEM, 0x3100, "Radio module tamper alarm for radio channel 0 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x311F, "Radio module tamper alarm for radio channel 31 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x3140, "Radio module tamper alarm for radio channel 0 - Alarm went active.");
        assertEventDescription(SYSTEM, 0x315F, "Radio module tamper alarm for radio channel 31 - Alarm went active.");
    }

    @Test
    public void testRadioModuleLowBatteryAlarm() {
        assertEventCode(SYSTEM, 0x3200, 32);
        assertEventCode(SYSTEM, 0x321F, 32);
        assertEventCode(SYSTEM, 0x3240, 32);
        assertEventCode(SYSTEM, 0x325F, 32);
        assertEventDescription(SYSTEM, 0x3200, "Radio module low battery alarm for radio channel 0 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x321F, "Radio module low battery alarm for radio channel 31 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x3240, "Radio module low battery alarm for radio channel 0 - Alarm went active.");
        assertEventDescription(SYSTEM, 0x325F, "Radio module low battery alarm for radio channel 31 - Alarm went active.");
    }

    @Test
    public void testRadioModuleTimeoutOfSyncAlarm() {
        assertEventCode(SYSTEM, 0x3300, 30);
        assertEventCode(SYSTEM, 0x331F, 30);
        assertEventCode(SYSTEM, 0x3340, 30);
        assertEventCode(SYSTEM, 0x335F, 30);
        assertEventDescription(SYSTEM, 0x3300, "Radio module time out of sync alarm for radio channel 0 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x331F, "Radio module time out of sync alarm for radio channel 31 - Alarm went inactive");
        assertEventDescription(SYSTEM, 0x3340, "Radio module time out of sync alarm for radio channel 0 - Alarm went active.");
        assertEventDescription(SYSTEM, 0x335F, "Radio module time out of sync alarm for radio channel 31 - Alarm went active.");
    }

    @Test
    public void testFirmwareChanged() {
        assertEventCode(SYSTEM, 0x4000, 41);
        assertEventCode(SYSTEM, 0x40FF, 41);
        assertEventDescription(SYSTEM, 0x4000, "The meter firmware changed to revision 0x00");
        assertEventDescription(SYSTEM, 0x40FF, "The meter firmware changed to revision 0xff");
    }

    @Test
    public void testBootloaderChanged() {
        assertEventCode(SYSTEM, 0x4100, 41);
        assertEventDescription(SYSTEM, 0x4100, "The meter bootloader was upgraded.");
    }

    @Test
    public void testAutomaticBillingReset() {
        assertEventCode(SYSTEM, 0x5000, 17);
        assertEventDescription(SYSTEM, 0x5000, "Automatic billing reset occurred.");
    }

    @Test
    public void testManualBillingResetButton() {
        assertEventCode(SYSTEM, 0x5001, 17);
        assertEventDescription(SYSTEM, 0x5001, "Manual billing reset occurred from the billing reset button.");
    }

    @Test
    public void testManualBillingResetCommand() {
        assertEventCode(SYSTEM, 0x5080, 17);
        assertEventCode(SYSTEM, 0x5090, 17);
        assertEventCode(SYSTEM, 0x50A0, 17);
        assertEventDescription(SYSTEM, 0x5080, "Manual billing reset occurred. (Port: OPTICAL)");
        assertEventDescription(SYSTEM, 0x5090, "Manual billing reset occurred. (Port: MODEM)");
        assertEventDescription(SYSTEM, 0x50A0, "Manual billing reset occurred. (Port: SCADA)");
    }

    @Test
    public void testUpsStateEvent() {
        assertEventCode(SYSTEM, 0xB200, 219);
        assertEventCode(SYSTEM, 0xB201, 220);
        assertEventDescription(SYSTEM, 0xB200, "Meter running full powered from the UPS battery.");
        assertEventDescription(SYSTEM, 0xB201, "Mains power restored while running on the UPS.");
    }

    @Test
    public void testInputAlarmsEvent() {
        assertEventCode(SYSTEM, 0xB212, 221);
        assertEventCode(SYSTEM, 0xB213, 221);
        assertEventCode(SYSTEM, 0xB214, 221);
        assertEventDescription(SYSTEM, 0xB212, "Latched alarm from input 1");
        assertEventDescription(SYSTEM, 0xB213, "Unlatched alarm from input 1");
        assertEventDescription(SYSTEM, 0xB214, "Momentary pulse alarm from input 1");
    }

    @Test
    public void testUdpAlarmFailedEvent() {
        assertEventCode(SYSTEM, 0xB210, 222);
        assertEventDescription(SYSTEM, 0xB210, "Attempted to send a UDP alarm but never got an ACK from the server.");
    }

    @Test
    public void testLogonUserEvent() {
        assertEventCode(SETUP, 0x2000, 211);
        assertEventCode(SETUP, 0x2010, 211);
        assertEventCode(SETUP, 0x2020, 211);
        assertEventDescription(SETUP, 0x2000, "User 0 logged on. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2010, "User 0 logged on. (Port: MODEM)");
        assertEventDescription(SETUP, 0x2020, "User 0 logged on. (Port: SCADA)");
    }

    @Test
    public void testPortChangeEvent() {
        assertEventCode(SETUP, 0x2040, 212);
        assertEventCode(SETUP, 0x2041, 212);
        assertEventCode(SETUP, 0x2042, 212);
        assertEventCode(SETUP, 0x2050, 212);
        assertEventCode(SETUP, 0x2051, 212);
        assertEventCode(SETUP, 0x2052, 212);
        assertEventCode(SETUP, 0x2060, 212);
        assertEventCode(SETUP, 0x2061, 212);
        assertEventCode(SETUP, 0x2062, 212);
        assertEventDescription(SETUP, 0x2040, "User changed setup 1. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2041, "User changed setup 2. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2042, "User changed setup 3. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2050, "User changed setup 1. (Port: MODEM)");
        assertEventDescription(SETUP, 0x2051, "User changed setup 2. (Port: MODEM)");
        assertEventDescription(SETUP, 0x2052, "User changed setup 3. (Port: MODEM)");
        assertEventDescription(SETUP, 0x2060, "User changed setup 1. (Port: SCADA)");
        assertEventDescription(SETUP, 0x2061, "User changed setup 2. (Port: SCADA)");
        assertEventDescription(SETUP, 0x2062, "User changed setup 3. (Port: SCADA)");
    }

    @Test
    public void testLogoffUserEvent() {
        assertEventCode(SETUP, 0x2081, 213);
        assertEventCode(SETUP, 0x2091, 213);
        assertEventCode(SETUP, 0x20A1, 213);
        assertEventDescription(SETUP, 0x2081, "User logged off. X command received. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2091, "User logged off. X command received. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A1, "User logged off. X command received. (Port: SCADA)");
    }

    @Test
    public void testSessionLostConnectionEvent() {
        assertEventCode(SETUP, 0x2083, 214);
        assertEventCode(SETUP, 0x2093, 214);
        assertEventCode(SETUP, 0x20A3, 214);
        assertEventDescription(SETUP, 0x2083, "User logged off. Lost connection. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2093, "User logged off. Lost connection. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A3, "User logged off. Lost connection. (Port: SCADA)");
    }

    @Test
    public void testSessionLogoffTimeoutExpiredEvent() {
        assertEventCode(SETUP, 0x2082, 215);
        assertEventCode(SETUP, 0x2092, 215);
        assertEventCode(SETUP, 0x20A2, 215);
        assertEventDescription(SETUP, 0x2082, "User logged off. Inactivity timeout. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2092, "User logged off. Inactivity timeout. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A2, "User logged off. Inactivity timeout. (Port: SCADA)");
    }

    @Test
    public void testSessionLogoffAccessDeniedEvent() {
        assertEventCode(SETUP, 0x2080, 216);
        assertEventCode(SETUP, 0x2090, 216);
        assertEventCode(SETUP, 0x20A0, 216);
        assertEventDescription(SETUP, 0x2080, "User access denied. Bad password. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2090, "User access denied. Bad password. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A0, "User access denied. Bad password. (Port: SCADA)");
    }

    @Test
    public void testSessionUserIdChangeEvent() {
        assertEventCode(SETUP, 0x2084, 217);
        assertEventCode(SETUP, 0x2094, 217);
        assertEventCode(SETUP, 0x20A4, 217);
        assertEventDescription(SETUP, 0x2084, "User logged off. Login under another name. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2094, "User logged off. Login under another name. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A4, "User logged off. Login under another name. (Port: SCADA)");
    }

    @Test
    public void testSessionLoggedOffRegisterWriteEvent() {
        assertEventCode(SETUP, 0x2085, 218);
        assertEventCode(SETUP, 0x2095, 218);
        assertEventCode(SETUP, 0x20A5, 218);
        assertEventDescription(SETUP, 0x2085, "User logged off. Logoff via register write. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2095, "User logged off. Logoff via register write. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A5, "User logged off. Logoff via register write. (Port: SCADA)");
    }

    @Test
    public void testSessionLoggedOffUpgradeWriteEvent() {
        assertEventCode(SETUP, 0x2086, 218);
        assertEventCode(SETUP, 0x2096, 218);
        assertEventCode(SETUP, 0x20A6, 218);
        assertEventDescription(SETUP, 0x2086, "User logged off. Logoff via register write for firmware update. (Port: OPTICAL)");
        assertEventDescription(SETUP, 0x2096, "User logged off. Logoff via register write for firmware update. (Port: MODEM)");
        assertEventDescription(SETUP, 0x20A6, "User logged off. Logoff via register write for firmware update. (Port: SCADA)");
    }

    @Test
    public void testVoltageChangeEvent() {
        assertEventCode(TRIG, 0x6000, 22);
        assertEventCode(TRIG, 0x6100, 22);
        assertEventCode(TRIG, 0x6200, 22);
        assertEventCode(TRIG, 0x6080, 21);
        assertEventCode(TRIG, 0x6180, 21);
        assertEventCode(TRIG, 0x6280, 21);

        assertEventCode(TRIG, 0x6800, 144);

        assertEventDescription(TRIG, 0x6000, "Voltage swell change start phase A");
        assertEventDescription(TRIG, 0x6100, "Voltage swell change start phase B");
        assertEventDescription(TRIG, 0x6200, "Voltage swell change start phase C");
        assertEventDescription(TRIG, 0x6080, "Voltage sag change start phase A");
        assertEventDescription(TRIG, 0x6180, "Voltage sag change start phase B");
        assertEventDescription(TRIG, 0x6280, "Voltage sag change start phase C");

        assertEventDescription(TRIG, 0x6800, "THD/Unbalance trigger.");

    }

    @Test
    public void testMeterRestartEvent() {
        assertEventCode(DIAG, 0xB300, 2);
        assertEventCode(DIAG, 0xB301, 2);
        assertEventCode(DIAG, 0xB302, 2);

        assertEventDescription(DIAG, 0xB300, "Power up while in low power mode.");
        assertEventDescription(DIAG, 0xB301, "Power up from a crash or without battery.");
        assertEventDescription(DIAG, 0xB302, "power up from a crash or without a battery and the stack is corrupted.");
    }

    @Test
    public void testTamperEvents() {
        assertEventCode(TAMPER, 0x3000, 23);
        assertEventCode(TAMPER, 0x3010, 23);
        assertEventCode(TAMPER, 0x3011, 23);
        assertEventCode(TAMPER, 0x3012, 23);
        assertEventCode(TAMPER, 0x3013, 23);
        assertEventCode(TAMPER, 0x3014, 23);
        assertEventCode(TAMPER, 0x3015, 23);
        assertEventCode(TAMPER, 0x3016, 23);
        assertEventCode(TAMPER, 0x3017, 23);
        assertEventCode(TAMPER, 0x3018, 23);
        assertEventCode(TAMPER, 0x3019, 23);
        assertEventCode(TAMPER, 0x301A, 23);
        assertEventCode(TAMPER, 0x301B, 23);
        assertEventCode(TAMPER, 0x301C, 23);
        assertEventCode(TAMPER, 0x301D, 23);
        assertEventCode(TAMPER, 0x301E, 23);
        assertEventCode(TAMPER, 0x301F, 23);

        assertEventDescription(TAMPER, 0x3000, "Tamper event - Tamper detected");
        assertEventDescription(TAMPER, 0x3010, "Tamper event because VT lost phase A. - Tamper detected");
        assertEventDescription(TAMPER, 0x3011, "Tamper event because VT lost phase B. - Tamper detected");
        assertEventDescription(TAMPER, 0x3012, "Tamper event because VT lost phase C. - Tamper detected");
        assertEventDescription(TAMPER, 0x3013, "Tamper event because VT surge phase A. - Tamper detected");
        assertEventDescription(TAMPER, 0x3014, "Tamper event because VT surge phase B. - Tamper detected");
        assertEventDescription(TAMPER, 0x3015, "Tamper event because VT surge phase C. - Tamper detected");
        assertEventDescription(TAMPER, 0x3016, "Tamper event because VT phase bridge. - Tamper detected");
        assertEventDescription(TAMPER, 0x3017, "Tamper event because VT phase order. - Tamper detected");
        assertEventDescription(TAMPER, 0x3018, "Tamper event because CT lost phase A. - Tamper detected");
        assertEventDescription(TAMPER, 0x3019, "Tamper event because CT lost phase B. - Tamper detected");
        assertEventDescription(TAMPER, 0x301A, "Tamper event because CT lost phase C. - Tamper detected");
        assertEventDescription(TAMPER, 0x301B, "Tamper event because CT phase order. - Tamper detected");
        assertEventDescription(TAMPER, 0x301C, "Tamper event because CT current reversal phase A. - Tamper detected");
        assertEventDescription(TAMPER, 0x301D, "Tamper event because CT current reversal phase B. - Tamper detected");
        assertEventDescription(TAMPER, 0x301E, "Tamper event because CT current reversal phase C. - Tamper detected");
        assertEventDescription(TAMPER, 0x301F, "Tamper event - Tamper detected");
    }


    private void assertEventCode(LogBookDescription logBookDescription, int eventCode, int expectedEiserverEventCode) {
        assertEquals(expectedEiserverEventCode, new Event(0, eventCode, logBookDescription, null, tz).getEiServerEventCode());
    }

    private void assertEventDescription(LogBookDescription logBookDescription, int eventCode, String expectedDescription) {
        assertEquals(expectedDescription, new Event(0, eventCode, logBookDescription, null, tz).getEventDescription());
    }
}
