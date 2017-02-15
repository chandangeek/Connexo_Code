/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventIdFactory.java
 *
 * Created on 22 december 2006, 15:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class EventIdFactory {

    static List<EventId> eventIds = new ArrayList<>();

    static {
        eventIds.add(new EventId(0));
        eventIds.add(new EventId(1,"MASS MEM1 EOI"));
        eventIds.add(new EventId(2,"MASS MEM2 EOI"));
        eventIds.add(new EventId(3,"DEMAND EOI"));
        eventIds.add(new EventId(4,"DEMAND RESET", MeterEvent.MAXIMUM_DEMAND_RESET));
        eventIds.add(new EventId(5,8, "LOSS OF PHASE", MeterEvent.PHASE_FAILURE));
        eventIds.add(new EventId(9,"REGISTER FREEZE"));
        eventIds.add(new EventId(10,"CLK ERROR", MeterEvent.HARDWARE_ERROR));
        eventIds.add(new EventId(11,"ENTER TEST MODE", MeterEvent.OTHER));
        eventIds.add(new EventId(12,"LEAVE TEST MODE", MeterEvent.OTHER));
        eventIds.add(new EventId(13,"POWER OUTAGE", MeterEvent.POWERDOWN));
        eventIds.add(new EventId(14,"CALL ON SCHEDULE", MeterEvent.OTHER));
        eventIds.add(new EventId(15,"METER REPROGRAM", MeterEvent.CONFIGURATIONCHANGE));
        eventIds.add(new EventId(16,"INTERROGATION", -1));
        eventIds.add(new EventId(17,"CLOCK ADJUSTMENT", MeterEvent.SETCLOCK));
        eventIds.add(new EventId(18,"OPTICAL ACTIVE"));
        eventIds.add(new EventId(19,"COMM1 ACTIVE"));
        eventIds.add(new EventId(20,"COMM2 ACTIVE"));
        eventIds.add(new EventId(21,44,"ALARM 1..24 ON", MeterEvent.METER_ALARM));
        eventIds.add(new EventId(45,68,"ALARM 1..24 OFF"));
        eventIds.add(new EventId(69,76,"EXTERNAL INPUT 0..7 ON"));
        eventIds.add(new EventId(77,84,"EXTERNAL INPUT 0..7 OFF"));
        eventIds.add(new EventId(85,"EXTERNAL EOI"));
        eventIds.add(new EventId(86,"BEFORE DST ENTRY"));
        eventIds.add(new EventId(87,"AFTER DST ENTRY"));
        eventIds.add(new EventId(88,"BEFORE DST EXIT"));
        eventIds.add(new EventId(89,"AFTER DST EXIT"));
        eventIds.add(new EventId(90,"INVALID PASSWORD",MeterEvent.OTHER));
        eventIds.add(new EventId(91,"DAT ALARM MISSED", MeterEvent.OTHER));

        eventIds.add(new EventId(92,101,"START LOSS COMP LEVEL 0..9"));
        eventIds.add(new EventId(102,"CHANGE LOSS COMP LEVEL"));
        eventIds.add(new EventId(103,"AUTOMATIC TIME ADJUST",MeterEvent.SETCLOCK));
        eventIds.add(new EventId(104,125,"STARTLOSSCOMPLEVEL 10..31"));

        eventIds.add(new EventId(126,"FATALERROREXISTS", MeterEvent.FATAL_ERROR));
        eventIds.add(new EventId(127,"NONFATALERROREXISTS"));

        eventIds.add(new EventId(128,143,"PDS OUTPUT 1..16 ON"));
        eventIds.add(new EventId(144,163,"EXT MASS MEM 1..16 EOI"));
        eventIds.add(new EventId(164,171,"EXTERNAL INPUT 9..16 ON"));
        eventIds.add(new EventId(172,179,"EXTERNAL INPUT 9..16 OFF"));

        eventIds.add(new EventId(180,"DST TABLE UPDATED"));

        eventIds.add(new EventId(181,"LOSS OF TIMESYNC"));
        eventIds.add(new EventId(182,"REGAINED TIMESYNC"));
        eventIds.add(new EventId(183,"BEFORE TIME ADJUSTMENT", MeterEvent.SETCLOCK_BEFORE));
        eventIds.add(new EventId(184,"SESSION OPEN PORTOPT"));
        eventIds.add(new EventId(185,"SESSION CLOSE PORTOPT"));
        eventIds.add(new EventId(186,"SESSION OPEN PORTSP1"));
        eventIds.add(new EventId(187,"SESSION CLOSE PORTSP1"));
        eventIds.add(new EventId(188,"SESSION OPEN PORTSP2"));
        eventIds.add(new EventId(189,"SESSION CLOSE PORTSP2"));

        eventIds.add(new EventId(190,"IEC870 PARAMETER CHANGE", MeterEvent.CONFIGURATIONCHANGE));
        eventIds.add(new EventId(191,"IEC870 STARTUP WITH LOSS OF DATA",MeterEvent.CLEAR_DATA));
        eventIds.add(new EventId(192,"IEC870 STARTUP WITH NO LOSS OF DATA"));
        eventIds.add(new EventId(193,"DSA PRIVATE KEY CHANGED"));
        eventIds.add(new EventId(194,"STARTING PROTOCOL OPT"));
        eventIds.add(new EventId(195,"STARTING PROTOCOL SP1"));

        eventIds.add(new EventId(196,"STARTING PROTOCOL SP2"));
        eventIds.add(new EventId(197,"BEFORE LARGE TIME ADJUSTMEN", MeterEvent.SETCLOCK_BEFORE));
        eventIds.add(new EventId(198,"AFTER LARGE TIME ADJUSTMENT", MeterEvent.SETCLOCK_AFTER));
        eventIds.add(new EventId(199,"TIMESYNC PERIOD MISSED",MeterEvent.OTHER));
        eventIds.add(new EventId(200,"TIMESYNCH ATTEMPT OUTSIDE ADJUSTMENT WINDOW", MeterEvent.OTHER));
        eventIds.add(new EventId(201,"FAILED TIME ADJUSTMENT", MeterEvent.PROGRAM_FLOW_ERROR));
        eventIds.add(new EventId(202,"IEC870 SESSION OPEN LOCAL"));
        eventIds.add(new EventId(203,"IEC870 SESSION CLOSE LOCAL"));
        eventIds.add(new EventId(204,"IEC870 SESSION OPEN REMOTE"));
        eventIds.add(new EventId(205,"IEC870 SESSION CLOSE REMOTE"));
        eventIds.add(new EventId(206,"POWERUPCOMPLETE", MeterEvent.POWERUP));

        eventIds.add(new EventId(207,208,"RESERVED"));

        eventIds.add(new EventId(209,400,"RATE SCHEDULE 1..8 RATE A..X ON"));

        eventIds.add(new EventId(401,592,"RATE SCHEDULE 1..8 RATE A..X OFF"));

        eventIds.add(new EventId(592,632,"INDEPENDENT OUTPUT 1..40 ON"));

        eventIds.add(new EventId(633,672,"INDEPENDENT OUTPUT 1..40 OFF"));

        eventIds.add(new EventId(673,"RATE SCHEDULE 1 COMM OVERRIDE PATTERN ON"));
        eventIds.add(new EventId(674,"RATE SCHEDULE 1 COMM OVERRIDE RATE ON"));
        eventIds.add(new EventId(675,"RATE SCHEDULE 1 COMM OVERRIDE RATE EXCLUSIVE ON"));
        eventIds.add(new EventId(676,"RATE SCHEDULE 1 EVENT OVERRIDE PATTERN ON"));
        eventIds.add(new EventId(677,"RATE SCHEDULE 1 EVENT OVERRIDE RATE ON"));
        eventIds.add(new EventId(678,"RATE SCHEDULE 1 EVENT OVERRIDE RATE EXCLUSIVE ON"));
        eventIds.add(new EventId(679,"RATE SCHEDULE 1 HOLIDAY ON"));
        eventIds.add(new EventId(680,"RATE SCHEDULE 1 SEASON CHANGE"));
        eventIds.add(new EventId(681,736,"Rate schedule 2..8 overrides and holiday ON"));

        eventIds.add(new EventId(737,"RATE SCHEDULE 1 COMM OVERRI DE PATTERN OFF"));
        eventIds.add(new EventId(738,"RATE SCHEDULE 1 COMM OVERRI DE RATE OFF"));
        eventIds.add(new EventId(739,"RATE SCHEDULE 1 COMM OVERRI DE RATE EXCLUSIVE OFF"));
        eventIds.add(new EventId(740,"RATE SCHEDULE 1 EVENT OVERRI DE PATTERN OFF"));
        eventIds.add(new EventId(741,"RATE SCHEDULE 1 EVENT OVERRI DE RATE OFF"));
        eventIds.add(new EventId(742,"RATE SCHEDULE 1 EVENT OVERRI DE RATE EXCLUSIVE OFF"));
        eventIds.add(new EventId(743,"RATE SCHEDULE 1 HOLIDAY OFF"));
        eventIds.add(new EventId(744,"RESERVED12"));

        eventIds.add(new EventId(745,800,"Rate schedule 2..8 overrides and holiday OFF"));

        eventIds.add(new EventId(801,808,"RATE SCHEDULE 1..8 RATE CHANGE"));

        eventIds.add(new EventId(809,"TOU SCHEDULE SWITCHED"));
        eventIds.add(new EventId(810,"METERING STOPPED", MeterEvent.APPLICATION_ALERT_STOP));
        eventIds.add(new EventId(811,"METERING STARTED", MeterEvent.APPLICATION_ALERT_START));
        eventIds.add(new EventId(812,"NONFATALERRORS CLEARED"));
        eventIds.add(new EventId(813,"COMM3 ACTIVE"));
        eventIds.add(new EventId(814,"SESSION OPEN PORTSP3"));
        eventIds.add(new EventId(815,"SESSION CLOSE PORTSP3"));
        eventIds.add(new EventId(816,"NEWENERGY"));

    }

    /** Creates a new instance of EventIdFactory */
    private EventIdFactory() {
    }


    public static EventId findEventId(int id) throws IOException {
        for (EventId eid : eventIds) {
            if ((eid.getIdLow() <= id) && (eid.getIdHigh() >= id)) {
                return eid;
            }
        }
        throw new IOException("EventIdFactory, findEventId, invalid id="+id);
    }

}
