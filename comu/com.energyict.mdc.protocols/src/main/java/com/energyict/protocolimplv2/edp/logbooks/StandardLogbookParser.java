/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimplv2.edp.CX20009;

import java.util.HashMap;
import java.util.Map;

public class StandardLogbookParser extends AbstractLogbookParser {

    private static final ObisCode STANDARD_LOGBOOK = ObisCode.fromString("0.0.99.98.0.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Standard logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.FATAL_ERROR, "Reboot the meter with data failure"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.OTHER, "Reboot the meter without data failure"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.POWERDOWN, "Power failure in all phases"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered when there is a power failure in phase L1"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered when there is a power failure in phase L2"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered when there is a power failure in phase L3"));
        DESCRIPTIONS.put(7, new EventInfo(MeterEvent.OTHER, "Neutral loss"));
        DESCRIPTIONS.put(8, new EventInfo(MeterEvent.BATTERY_VOLTAGE_LOW, "The event is registered when the power reserve of the battery is reduced to 10%"));
        DESCRIPTIONS.put(9, new EventInfo(MeterEvent.HARDWARE_ERROR, "Event registered whenever internal errors occur that are associated with the necessity of meter replacement"));
        DESCRIPTIONS.put(21, new EventInfo(MeterEvent.OTHER, "Registered event that verifies the recovery of supply in the phase L1"));
        DESCRIPTIONS.put(22, new EventInfo(MeterEvent.OTHER, "Registered event that verifies the recovery of supply in the phase L2"));
        DESCRIPTIONS.put(23, new EventInfo(MeterEvent.OTHER, "Registered event that verifies the recovery of supply in the phase L3"));
        DESCRIPTIONS.put(24, new EventInfo(MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Event registered when daylight saving change from winter to summer on a programmed date/time"));
        DESCRIPTIONS.put(25, new EventInfo(MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Event registered when daylight saving change from summer to winter on a programmed date/time"));
        DESCRIPTIONS.put(26, new EventInfo(MeterEvent.OTHER, "Recovery of supply in at least one of the phases"));
        DESCRIPTIONS.put(27, new EventInfo(MeterEvent.OTHER, "Restoration of the neutral"));
        DESCRIPTIONS.put(28, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change on HAN communication port parameters (address or speed)"));
        DESCRIPTIONS.put(29, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change on HAN communication port access profile"));
        DESCRIPTIONS.put(30, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when there is a changing of other parameters that don't have specific events"));
        DESCRIPTIONS.put(31, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of communication port parameters"));
        DESCRIPTIONS.put(32, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the reading password"));
        DESCRIPTIONS.put(33, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the writing password"));
        DESCRIPTIONS.put(34, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the firmware password"));
        DESCRIPTIONS.put(35, new EventInfo(MeterEvent.BATTERY_VOLTAGE_LOW, "Event registered if in a presence of the battery failure alarm it's verified a power reserve of 50%"));
        DESCRIPTIONS.put(36, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the configuration of automatic DST - Daylight Saving Time"));
        DESCRIPTIONS.put(38, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of integration period of the load profile"));
        DESCRIPTIONS.put(39, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change in the transformation ratio"));
        DESCRIPTIONS.put(40, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the clock synchronization mode"));
        DESCRIPTIONS.put(41, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Chang of the identification/description of tariff configuration (calendar_name_active) of contract 1"));
        DESCRIPTIONS.put(44, new EventInfo(MeterEvent.BILLING_ACTION, "Registered an event each time an automatic monthly closing in the contract 1 occurs"));
        DESCRIPTIONS.put(45, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a configuration change of contract 1 is effected (transition from passive to active)"));
        DESCRIPTIONS.put(47, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when a change is made in the holiday table of passive contract 1"));
        DESCRIPTIONS.put(48, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event is registered when a change is made in the tariff configuration of passive contract 1"));
        DESCRIPTIONS.put(49, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when passive contract 1 is cleared"));
        DESCRIPTIONS.put(50, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made in the date/time of the end of billing of passive contract 1"));
        DESCRIPTIONS.put(51, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made in the activation date of passive contract 1"));
        DESCRIPTIONS.put(52, new EventInfo(MeterEvent.BILLING_ACTION, "Registered an event each time an automatic monthly closing in the contract 2 occurs"));
        DESCRIPTIONS.put(53, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a configuration change of contract 2 is effected (transition from passive to active)"));
        DESCRIPTIONS.put(54, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when a change is made in the holiday table of passive contract 2"));
        DESCRIPTIONS.put(55, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when a change is made in the tariff configuration of passive contract 2"));
        DESCRIPTIONS.put(56, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when passive contract 2 is cleared"));
        DESCRIPTIONS.put(57, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made in the date/time of the end of billing of passive contract 2"));
        DESCRIPTIONS.put(58, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made in the activation date of passive contract 2"));
        DESCRIPTIONS.put(90, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when time threshold for over/under voltage is changed"));
        DESCRIPTIONS.put(91, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when time threshold for long power failures is changed"));
        DESCRIPTIONS.put(92, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when reference voltage changed"));
        DESCRIPTIONS.put(93, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the threshold to determine the overvoltages is changed"));
        DESCRIPTIONS.put(94, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the threshold to determine the undervoltages is changed"));
        DESCRIPTIONS.put(95, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the threshold to determine the long power failures is changed"));
        DESCRIPTIONS.put(99, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Passwords reset and taking the manufacturing values"));
        DESCRIPTIONS.put(100, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Parameters take manufacturing values and billing data and load profiles values come to zero"));
        DESCRIPTIONS.put(101, new EventInfo(MeterEvent.OTHER, "Event registered when the change of tariff season of the contract 1 occurs"));
        DESCRIPTIONS.put(102, new EventInfo(MeterEvent.OTHER, "Event registered when the change of tariff season of the contract 2 occurs"));
        DESCRIPTIONS.put(104, new EventInfo(MeterEvent.BILLING_ACTION, "Event registered each time a manual monthly end of billing at all contracts occurs\n"));
        DESCRIPTIONS.put(108, new EventInfo(MeterEvent.MAXIMUM_DEMAND_RESET, "Event associated with the reset of the maximum demand registers of the contract 1"));
        DESCRIPTIONS.put(109, new EventInfo(MeterEvent.MAXIMUM_DEMAND_RESET, "Event associated with the reset of the maximum demand registers of the contract 2"));
        DESCRIPTIONS.put(110, new EventInfo(MeterEvent.LOADPROFILE_CLEARED, "Event associated with the reset of the load profile registers"));
        DESCRIPTIONS.put(111, new EventInfo(MeterEvent.REGISTER_OVERFLOW, "Event registered when a total register reaches its limit"));
    }

    public StandardLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return STANDARD_LOGBOOK;
    }
}