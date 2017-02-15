/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * S200EventsFactory.java
 *
 * Created on 1 augustus 2006, 11:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class S200EventsFactory {

    static List<S200EventMapping> list = new ArrayList<>();
    static {

        list.add(new S200EventMapping(1, MeterEvent.POWERDOWN));
        list.add(new S200EventMapping(2, MeterEvent.POWERUP));
        list.add(new S200EventMapping(3, MeterEvent.SETCLOCK_BEFORE));
        list.add(new S200EventMapping(4, MeterEvent.SETCLOCK_AFTER));
        list.add(new S200EventMapping(5, MeterEvent.HARDWARE_ERROR,"recorder clock malfunction"));
        list.add(new S200EventMapping(6, MeterEvent.ROM_MEMORY_ERROR,"PROM error detected"));
        list.add(new S200EventMapping(7, MeterEvent.RAM_MEMORY_ERROR, "RAM error detected"));
        list.add(new S200EventMapping(8, MeterEvent.PROGRAM_FLOW_ERROR, "program malfunction detected"));
        list.add(new S200EventMapping(0xbf, MeterEvent.HARDWARE_ERROR,"Battery malfunction"));
        list.add(new S200EventMapping(9, MeterEvent.REGISTER_OVERFLOW,"Meter input 1 register overflow detected"));
        list.add(new S200EventMapping(0xa, MeterEvent.REGISTER_OVERFLOW,"Meter input 2 register overflow detected"));
        list.add(new S200EventMapping(0xb, MeterEvent.REGISTER_OVERFLOW,"Meter input 3 register overflow detected"));
        list.add(new S200EventMapping(0xc, MeterEvent.REGISTER_OVERFLOW,"Meter input 4 register overflow detected"));
        list.add(new S200EventMapping(0x1a, MeterEvent.REGISTER_OVERFLOW,"Meter input 5 register overflow detected"));
        list.add(new S200EventMapping(0x1b, MeterEvent.REGISTER_OVERFLOW,"Meter input 6 register overflow detected"));
        list.add(new S200EventMapping(0x1c, MeterEvent.REGISTER_OVERFLOW,"Meter input 7 register overflow detected"));
        list.add(new S200EventMapping(0x1d, MeterEvent.REGISTER_OVERFLOW,"Meter input 8 register overflow detected"));
        list.add(new S200EventMapping(0xd, MeterEvent.OTHER,"Status input 1 closure"));
        list.add(new S200EventMapping(0xe, MeterEvent.OTHER,"Status input 1 opening"));
        list.add(new S200EventMapping(0xf, MeterEvent.OTHER,"Status input 2 closure"));
        list.add(new S200EventMapping(0x10, MeterEvent.OTHER,"Status input 2 opening"));
        list.add(new S200EventMapping(0x11, MeterEvent.OTHER,"Status input 3 closure"));
        list.add(new S200EventMapping(0x12, MeterEvent.OTHER,"Status input 3 opening"));
        list.add(new S200EventMapping(0x13, MeterEvent.OTHER,"Status input 4 closure"));
        list.add(new S200EventMapping(0x14, MeterEvent.OTHER,"Status input 4 opening"));
        list.add(new S200EventMapping(0x15, MeterEvent.OTHER,"Status input 5 closure"));
        list.add(new S200EventMapping(0x16, MeterEvent.OTHER,"Status input 5 opening"));
        list.add(new S200EventMapping(0x17, MeterEvent.OTHER,"Unit accessed"));
        list.add(new S200EventMapping(0x18, MeterEvent.OTHER,"password changed"));
        list.add(new S200EventMapping(0x19, MeterEvent.OTHER,"operating parameter changed"));
        list.add(new S200EventMapping(0x20, MeterEvent.CONFIGURATIONCHANGE));
        list.add(new S200EventMapping(0x21, MeterEvent.OTHER,"begin record command executed"));
        list.add(new S200EventMapping(0x22, MeterEvent.OTHER,"attempt acess with invalid password"));
        list.add(new S200EventMapping(0x23, MeterEvent.OTHER,"meter input static for 12 hours or more"));

        // events not supported by n4nn versions
        list.add(new S200EventMapping(0x1E, MeterEvent.OTHER, "Horn On"));
        list.add(new S200EventMapping(0x1F, MeterEvent.OTHER, "Horn Of"));

        for (int i=0x24;i<=0x29;i++) {
            list.add(new S200EventMapping(i, MeterEvent.METER_ALARM, "Channel 0x" + Integer.toHexString(i - 0x23) + " exceeds setpoint"));
        }

        list.add(new S200EventMapping(0x2A, MeterEvent.OTHER,"line of message printed"));

        for (int i=0x2b;i<=0x4a;i++) {
            list.add(new S200EventMapping(i, MeterEvent.OTHER, "load control pattern 0x" + Integer.toHexString(i - 0x2b) + " started"));
        }

        list.add(new S200EventMapping(0x4B, MeterEvent.OTHER,"horn turned off automatically"));
        list.add(new S200EventMapping(0x4C, MeterEvent.OTHER,"abort command executed"));
        list.add(new S200EventMapping(0x4D, MeterEvent.OTHER,"schedule loaded"));
        list.add(new S200EventMapping(0x4E, MeterEvent.OTHER,"load control relay closed"));
        list.add(new S200EventMapping(0x4F, MeterEvent.OTHER,"load control relay opened"));
        list.add(new S200EventMapping(0x50, MeterEvent.OTHER,"printer not available for local message"));
        list.add(new S200EventMapping(0x51, MeterEvent.OTHER,"battery low (S200 series)"));
        list.add(new S200EventMapping(0x52, MeterEvent.OTHER,"eprom read (S200 series)"));
        list.add(new S200EventMapping(0x53, MeterEvent.OTHER,"eprom written (S200 series)"));

        for (int i=0x54;i<=0x57;i++) {
            list.add(new S200EventMapping(i, MeterEvent.RAM_MEMORY_ERROR, "ram chip 0x" + Integer.toHexString(i - 0x54) + " error (S200 series)"));
        }

        for (int i=0x58;i<=0x5F;i++) {
            list.add(new S200EventMapping(i, MeterEvent.OTHER, "relay 0x" + Integer.toHexString(((i - 0x58) % 4) + 1) + " enabled if relay is enabled for load control"));
        }

        list.add(new S200EventMapping(0x60, MeterEvent.OTHER,"SSR cold started (S200 series - n5nn"));
    }


    private S200EventsFactory() {
    }

    public static S200EventMapping findEventMapping(int s200EventCode) {
        for (S200EventMapping s200EventMapping : list) {
            if (s200EventMapping.getS200EventCode() == s200EventCode) {
                return s200EventMapping;
            }
        }
        return new S200EventMapping(s200EventCode, MeterEvent.OTHER,"Unknown event code "+s200EventCode);
    }

}
