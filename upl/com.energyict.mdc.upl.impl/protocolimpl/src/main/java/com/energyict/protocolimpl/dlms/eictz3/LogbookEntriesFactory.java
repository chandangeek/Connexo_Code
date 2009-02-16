package com.energyict.protocolimpl.dlms.eictz3;

import java.util.*;

import com.energyict.protocol.MeterEvent;

public class LogbookEntriesFactory {

	
	static List<LogbookEntry> entries = new ArrayList();
	
	
	static {
		
		entries.add(new LogbookEntry(255,"Event log cleared",MeterEvent.CLEAR_DATA));
		entries.add(new LogbookEntry(1,"Power Down",MeterEvent.POWERDOWN));
		entries.add(new LogbookEntry(2,"Power Up",MeterEvent.POWERUP));
		entries.add(new LogbookEntry(3,"Daylight saving time enabled or disabled",MeterEvent.OTHER));
		entries.add(new LogbookEntry(4,"Clock adjusted (olddate/time)",MeterEvent.SETCLOCK_BEFORE));
		entries.add(new LogbookEntry(5,"Clock adjusted (newdate/time)",MeterEvent.SETCLOCK_AFTER));
		entries.add(new LogbookEntry(6,"Clock invalid",MeterEvent.OTHER));
		entries.add(new LogbookEntry(7,"Replace Battery",MeterEvent.OTHER));
		entries.add(new LogbookEntry(8,"Battery voltage low",MeterEvent.OTHER));
		entries.add(new LogbookEntry(9,"TOU activated",MeterEvent.OTHER));
		entries.add(new LogbookEntry(10,"Error register cleared",MeterEvent.CLEAR_DATA));
		entries.add(new LogbookEntry(11,"Alarm register cleared",MeterEvent.CLEAR_DATA));
		entries.add(new LogbookEntry(12,"Program memory error",MeterEvent.ROM_MEMORY_ERROR));
		entries.add(new LogbookEntry(13,"RAM error",MeterEvent.RAM_MEMORY_ERROR));
		entries.add(new LogbookEntry(14,"NV memory error",MeterEvent.ROM_MEMORY_ERROR));
		entries.add(new LogbookEntry(15,"Watchdog error",MeterEvent.WATCHDOGRESET));
		entries.add(new LogbookEntry(16,"Measurement system error",MeterEvent.HARDWARE_ERROR));
		entries.add(new LogbookEntry(17,"Firmware ready for activation",MeterEvent.OTHER));
		entries.add(new LogbookEntry(18,"Firmware activated",MeterEvent.APPLICATION_ALERT_START));
		entries.add(new LogbookEntry(40,"Terminal cover removed",MeterEvent.TERMINAL_OPENED));
		entries.add(new LogbookEntry(41,"Terminal cover closed",MeterEvent.OTHER));
		entries.add(new LogbookEntry(42,"Strong DC field detected",MeterEvent.OTHER));
		entries.add(new LogbookEntry(43,"No strong DC field anymore",MeterEvent.OTHER));
		entries.add(new LogbookEntry(44,"Meter cover removed",MeterEvent.COVER_OPENED));
		entries.add(new LogbookEntry(45,"Meter cover closed",MeterEvent.OTHER));
		entries.add(new LogbookEntry(46,"n times wrong password",MeterEvent.OTHER));
		entries.add(new LogbookEntry(60,"Manual disconnection",MeterEvent.OTHER));
		entries.add(new LogbookEntry(61,"Manual connection",MeterEvent.OTHER));
		entries.add(new LogbookEntry(62,"Remote disconnection",MeterEvent.OTHER));
		entries.add(new LogbookEntry(63,"Remote connection",MeterEvent.OTHER));
		entries.add(new LogbookEntry(64,"Local disconnection",MeterEvent.OTHER));
		entries.add(new LogbookEntry(65,"Limiter threshold exceeded",MeterEvent.OTHER));
		entries.add(new LogbookEntry(66,"Limiter threshold ok",MeterEvent.OTHER));
		entries.add(new LogbookEntry(67,"Limiter threshold changed",MeterEvent.OTHER));
		entries.add(new LogbookEntry(100,"Communication error MBus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(101,"Communication ok M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(102,"Replace Battery M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(103,"Fraud attempt M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(104,"Clock adjusted M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(110,"Communication error Mbus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(111,"Communication ok M-bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(112,"Replace Battery M-Bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(113,"Fraud attempt M-Bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(114,"Clock adjusted M-Bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(120,"Communication error Mbus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(121,"Communication ok M-bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(122,"Replace Battery M-Bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(123,"Fraud attempt M-Bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(124,"Clock adjusted M-Bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(130,"Communication error Mbus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(131,"Communication ok M-bus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(132,"Replace Battery M-Bus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(133,"Fraud attempt M-Bus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(134,"Clock adjusted M-Bus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(160,"Manual disconnection MBus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(161,"Manual connection M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(162,"Remote disconnection MBus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(163,"Remote disconnection MBus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(164,"Valve alarm M-Bus channel 1",MeterEvent.OTHER));
		entries.add(new LogbookEntry(170,"Manual disconnection MBus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(171,"Manual connection M-Bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(172,"Remote disconnection MBus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(173,"Remote disconnection MBus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(174,"Valve alarm M-Bus channel 2",MeterEvent.OTHER));
		entries.add(new LogbookEntry(180,"Manual disconnection MBus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(181,"Manual connection M-Bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(182,"Remote disconnection MBus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(183,"Remote disconnection MBus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(184,"Valve alarm M-Bus channel 3",MeterEvent.OTHER));
		entries.add(new LogbookEntry(190,"Manual disconnection MBus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(191,"Manual connection M-Bus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(192,"Remote disconnection MBus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(193,"Remote disconnection MBus channel 4",MeterEvent.OTHER));
		entries.add(new LogbookEntry(194,"Valve alarm M-Bus channel 4",MeterEvent.OTHER));		
	}
	
	static public LogbookEntry findLogbookEntry(int ntaCode) {
		Iterator<LogbookEntry> it = entries.iterator();
		while(it.hasNext()) {
			LogbookEntry logbookEntry = it.next();
			if (logbookEntry.getNtaCode() == ntaCode)
				return logbookEntry;
		}
		return new LogbookEntry(ntaCode,"Undefined nta eventlog code",MeterEvent.OTHER);
	}
	
	
}
