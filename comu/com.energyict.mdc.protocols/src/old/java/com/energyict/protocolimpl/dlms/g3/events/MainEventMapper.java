package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

/**
 * Copyrights EnergyICT
 * Date: 28/03/12
 * Time: 15:00
 */
public class MainEventMapper extends G3EventMapper {

    public static final EventDescription[] EVENT_DESCRIPTIONS = new EventDescription[]{
            new EventDescription(0, MeterEvent.EVENT_LOG_CLEARED, "Main log cleared"),
            new EventDescription(41, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Switching from winter to summer time"),
            new EventDescription(42, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Switching from summer to winter time"),
            new EventDescription(43, MeterEvent.SETCLOCK_BEFORE, "Set clock: old timestamp"),
            new EventDescription(44, MeterEvent.SETCLOCK_AFTER, "Set clock: new timestamp"),
            new EventDescription(45, MeterEvent.CLOCK_INVALID, "Degraded mode detected (invalid clock)"),
            new EventDescription(60, MeterEvent.RAM_MEMORY_ERROR, "Error during RAM memory access"),
            new EventDescription(61, MeterEvent.ROM_MEMORY_ERROR, "Checksum error in external flash memory"),
            new EventDescription(62, MeterEvent.OTHER, "Default breaker"),
            new EventDescription(63, MeterEvent.OTHER, "Firmware reset"),
            new EventDescription(64, MeterEvent.OTHER, "Hardware reset"),
            new EventDescription(65, MeterEvent.OTHER, "Default sensor measurement"),
            new EventDescription(66, MeterEvent.OTHER, "Contact Sec default"),
            new EventDescription(67, MeterEvent.OTHER, "Default transmission (LAN or Local)"),
            new EventDescription(68, MeterEvent.OTHER, "Abnormal voltage downstream, refusal of closing the breaker"),
            new EventDescription(69, MeterEvent.REVERSE_RUN, "Reverse connection detected, phase to neutral"),
            new EventDescription(70, MeterEvent.REVERSE_RUN, "Inversion input-output connection"),
            new EventDescription(71, MeterEvent.OTHER, "Inconsistency between total index and supplier index or distributor index"),
            new EventDescription(72, MeterEvent.OTHER, "Inconsistency between total index and daily load profile"),
            new EventDescription(80, MeterEvent.OTHER, "OK code activation"),
            new EventDescription(81, MeterEvent.OTHER, "Not OK code activation"),
            new EventDescription(82, MeterEvent.LOADPROFILE_CLEARED, "Erasing load profiles for withdrawal"),
            new EventDescription(83, MeterEvent.LOADPROFILE_CLEARED, "Erasing load profiles for injection"),
            new EventDescription(84, MeterEvent.OTHER, "Enabled supplier schedule"),
            new EventDescription(85, MeterEvent.OTHER, "Enabled distributor schedule"),
            new EventDescription(86, MeterEvent.CONFIGURATIONCHANGE, "Configured in manufacturer mode"),
            new EventDescription(87, MeterEvent.CONFIGURATIONCHANGE, "Configured in consumer mode"),
            new EventDescription(88, MeterEvent.CONFIGURATIONCHANGE, "Teleinformation configured in metrological mode"),
            new EventDescription(89, MeterEvent.CONFIGURATIONCHANGE, "Teleinformation configured in historical mode"),
            new EventDescription(90, MeterEvent.CONFIGURATIONCHANGE, "Teleinformation configured in normal mode"),
            new EventDescription(91, MeterEvent.CONFIGURATIONCHANGE, "Telereport configuration inactive"),
            new EventDescription(92, MeterEvent.CONFIGURATIONCHANGE, "Telereport configuration active, without security"),
            new EventDescription(93, MeterEvent.CONFIGURATIONCHANGE, "Telereport configuration active, with security"),
            new EventDescription(94, MeterEvent.CONFIGURATIONCHANGE, "Successfully changed CC_LAN key"),
            new EventDescription(95, MeterEvent.CONFIGURATIONCHANGE, "Successfully changed CC_LOCAL key")
    };

    @Override
    protected EventDescription[] getEventDescriptions() {
        return EVENT_DESCRIPTIONS;
    }
}