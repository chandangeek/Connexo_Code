package com.energyict.protocolimplv2.edmi.mk10.events;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;

import java.util.Date;
import java.util.TimeZone;

public class Event {

    private static final int FLASH_BACKUP_BAD = 0x0001;
    private static final int ENERGY_ACCUMULATION_RESTORED = 0x0004;
    private static final int CONTROL_DATA_RESTORED = 0x0008;

    private static final int PORT_MASK = 0x0030;
    private static final int PORT_OPTICAL = 0x0000;
    private static final int PORT_MODEM = 0x0010;
    private static final int PORT_SCADA = 0x0020;
    private static final int PORT_RESERVED = 0x0030;

    private static final int USER_NUMBER_MASK = 0x000F;
    private static final int CHANGED_SETUP_MASK = 0x0003;
    private static final int TIME_CHANGE_MASK = 0x000F;

    private static final int EFA_CONDITION_LATCHED = 0x0000;
    private static final int EFA_CONDITION_LATCH_CLEARED = 0x0040;
    private static final int EFA_CONDITION_BECAME_ACTIVE = 0x0080;
    private static final int EFA_CONDITION_BECAME_INACTIVE = 0x00C0;

    private static final int RUNTIME_STATISTICS_ON_TIME = 0;
    private static final int RUNTIME_STATISTICS_OFF_TIME = 1;
    private static final int RUNTIME_STATISTICS_NUMBER_OF_POWERUPS = 2;
    private static final int RUNTIME_STATISTICS_RESERVED = 3;

    private static final int TAMPER_STATE_MASK = 0x00C0;

    private int eiServerEventCode;
    private int protocolEventCode;
    private Date eventDate;
    private LogBookDescription logBookDescription;
    private String eventDescription;

    public Event(int eventTime, int eventCode, LogBookDescription logBookDescription, Event previousEvent, TimeZone tz) {
        this.logBookDescription = logBookDescription;
        this.protocolEventCode = eventCode;
        this.eventDescription = "";

        if (isSpecialEvent(eventCode, logBookDescription)) {
            this.eventDate = previousEvent.getEventDate();  // These events do not contain a event date, but rely on the event date of the previous event
            parseSpecialEventDetails(eventTime, eventCode, logBookDescription);
        } else {
            this.eventDate = DateTimeBuilder.getDateFromSecondsSince1996(tz, eventTime);
            parseEventDetails(logBookDescription, eventCode);
        }
    }

    private boolean isSpecialEvent(int eventCode, LogBookDescription logBookDescription) {
        return (logBookDescription.equals(LogBookDescription.TRIG) && ((eventCode & 0xF000) == 0x7000)) ||
                (logBookDescription.equals(LogBookDescription.TAMPER) && ((eventCode & 0xF000) == 0x8000));
    }

    public void parseEventDetails(LogBookDescription logBookDescription, int eventType) {
        if (logBookDescription.equals(LogBookDescription.TAMPER)) {
            parseTamperEventDetails(eventType);
        } else {
            parseCommonEventDetails(eventType);
        }
    }

    private void parseCommonEventDetails(int eventType) {
        String description;

        if ((eventType & 0xFFF0) == 0x1000) {
            this.eiServerEventCode = MeterEvent.POWERDOWN;
            this.eventDescription += "The meter was switched off";
        } else if ((eventType & 0xFFF0) == 0x1010) {
            this.eiServerEventCode = MeterEvent.POWERUP;
            this.eventDescription += "The meter powered up. Reason: 0x" + ProtocolUtils.buildStringHex(eventType & 0x000F, 2);
        } else if ((eventType & 0xFFF0) == 0x1020) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Recovered some parameters. Parameters: 0x" + ProtocolUtils.buildStringHex(eventType & 0x000F, 2);
            description += mapParameterRecoveryReason(eventType, description);
            this.eventDescription += description;
        } else if ((eventType & 0xFFF0) == 0x1030) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x" + ProtocolUtils.buildStringHex(eventType & 0x000F, 2);
            description += mapParameterRecoveryReason(eventType, description);
            this.eventDescription += description;
        } else if ((eventType & 0xFFF0) == 0x1040) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Meter runtime statistics changed. " + ProtocolUtils.buildStringHex(eventType & 0x000F, 2);
            if ((eventType & 0x000F) == RUNTIME_STATISTICS_ON_TIME) {
                description += " - ON time changed.";
            }
            if ((eventType & 0x000F) == RUNTIME_STATISTICS_OFF_TIME) {
                description += " - OFF time changed.";
            }
            if ((eventType & 0x000F) == RUNTIME_STATISTICS_NUMBER_OF_POWERUPS) {
                description += " - Number of power ups changed.";
            }
            if ((eventType & 0x000F) >= RUNTIME_STATISTICS_RESERVED) {
                description += " - Reserved.";
            }
            this.eventDescription += description;
        } else if ((eventType & 0xFFF0) == 0x1050) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "A ripple code listed in the meter setup was received. Index: 0x " + ProtocolUtils.buildStringHex(eventType & 0x000F, 2);
        } else if ((eventType & 0xF000) == 0x1000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            int relayNumber = eventType & 0x0F00;
            description = "- Relay number: " + relayNumber;
            description += ", status: ";
            description += ((eventType & 0x0080) == 0x0080) ? "enabled" : "disabled";
            description += ((eventType & 0x0040) == 0x0040) ? " - connected" : " - disconnected";

            int reasonCode = eventType & 0x003F;
            if (reasonCode < 57) {
                this.eventDescription += "Relay change made by an external command. Reason:" + ProtocolUtils.buildStringHex(reasonCode, 2) + description;
            }
            if (reasonCode == 57) {
                this.eventDescription += "Relay changed. Relay control via register F050-8" + description;
            }
            if (reasonCode == 58) {
                this.eventDescription += "Relay changed. Disconnect button was pressed" + description;
            }
            if (reasonCode == 59) {
                this.eventDescription += "Relay changed. Connect button was pressed" + description;
            }
            if (reasonCode == 60) {
                this.eventDescription += "Relay changed. Calendar (tariff) changed" + description;
            }
            if (reasonCode == 61) {
                this.eventDescription += "Relay changed. The physical relay changed state" + description;
            }
            if (reasonCode == 62) {
                this.eventDescription += "Relay changed. Reason:" + ProtocolUtils.buildStringHex(reasonCode, 2) + description;
            }
            if (reasonCode == 63) {
                this.eventDescription += "Relay changed. Relay Stuck recorded" + description;
            }
        } else if ((eventType & 0xFFC0) == 0x2000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User " + (eventType & USER_NUMBER_MASK) + " logged on." + getPortString(eventType);
        } else if ((eventType & 0xFFCC) == 0x2040) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User changed setup " + ((eventType & CHANGED_SETUP_MASK) + 1) + "." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2080) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User access denied. Bad password." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2081) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. X command received." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2082) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Inactivity timeout." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2083) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Lost connection." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2084) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Login under another name." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2085) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Logoff via register write." + getPortString(eventType);
        } else if ((eventType & 0xFFCF) == 0x2086) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Logoff via register write for firmware update." + getPortString(eventType);
        } else if ((eventType & 0xF000) == 0xA000) {
            this.eiServerEventCode = MeterEvent.CONFIGURATIONCHANGE;
            switch (eventType & 0x00FF) {
                case 0:
                    this.eventDescription += "Setup change: Event logs cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 1:
                    this.eventDescription += "Setup change: Load survey 1. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 2:
                    this.eventDescription += "Setup change: Load survey 2. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 3:
                    this.eventDescription += "Setup change: TOU setup changed / cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 4:
                    this.eventDescription += "Setup change: Billing history cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 5:
                    this.eventDescription += "Setup change: Pulsing generator reset. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 6:
                    this.eventDescription += "Setup change: TOU calendar changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 7:
                    this.eventDescription += "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 8:
                    this.eventDescription += "Setup change: Hardware setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 9:
                    this.eventDescription += "Setup change: Calibration changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 10:
                    this.eventDescription += "Setup change: Scaling factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 11:
                    this.eventDescription += "Setup change: Transformer ratios changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 12:
                    this.eventDescription += "Setup change: Pulse factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 13:
                    this.eventDescription += "Setup change: Pulsing inputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 14:
                    this.eventDescription += "Setup change: Pulsing outputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 15:
                    this.eventDescription += "Setup change: Annunciators changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 16:
                    this.eventDescription += "Setup change: Optical port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 17:
                    this.eventDescription += "Setup change: Modem port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 18:
                    this.eventDescription += "Setup change: LCD screens changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 19:
                    this.eventDescription += "Setup change: Alarm setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 20:
                    this.eventDescription += "Setup change: Security setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 21:
                    this.eventDescription += "Setup change: Timer setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 22:
                    this.eventDescription += "Setup change: Time setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 23:
                    this.eventDescription += "Setup change: SCADA port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                    this.eventDescription += "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventType & 0x0F00) >> 8, 2);
                    break;
            }
        } else if ((eventType & 0xFFFF) == 0x20CF) {
            this.eiServerEventCode = MeterEvent.SETCLOCK_AFTER;
            this.eventDescription += "System time changed " + getTimeChangeReason(eventType) + getPortString(eventType);
        } else if ((eventType & 0xFFC0) == 0x20C0) {
            this.eiServerEventCode = MeterEvent.SETCLOCK_BEFORE;
            this.eventDescription += "System time changed " + getTimeChangeReason(eventType) + getPortString(eventType);
        } else if ((eventType & 0xFF00) == 0x3000) {
            description = "";
            switch (eventType & 0x00C0) {
                case EFA_CONDITION_LATCHED:
                    description = " (LATCHED)";
                    break;
                case EFA_CONDITION_LATCH_CLEARED:
                    description = " (LATCH CLEARED)";
                    break;
                case EFA_CONDITION_BECAME_ACTIVE:
                    description = " (BECAME ACTIVE)";
                    break;
                case EFA_CONDITION_BECAME_INACTIVE:
                    description = " (BECAME INACTIVE)";
                    break;
            }

            switch (eventType & 0x003F) {
                case 0:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: User defined/magnetic tamper." + description;
                    break;
                case 1:
                    this.eiServerEventCode = MeterEvent.BATTERY_VOLTAGE_LOW;
                    this.eventDescription += "EFA: Battery failure." + description;
                    break;
                case 2:
                    this.eiServerEventCode = MeterEvent.REGISTER_OVERFLOW;
                    this.eventDescription += "EFA: Pulsing output overflow." + description;
                    break;
                case 3:
                    this.eiServerEventCode = MeterEvent.RAM_MEMORY_ERROR;
                    this.eventDescription += "EFA: Data flash failure." + description;
                    break;
                case 4:
                    this.eiServerEventCode = MeterEvent.PROGRAM_MEMORY_ERROR;
                    this.eventDescription += "EFA: Program flash failure." + description;
                    break;
                case 5:
                    this.eiServerEventCode = MeterEvent.HARDWARE_ERROR;
                    this.eventDescription += "EFA: RAM or LCD failure." + description;
                    break;
                case 6:
                    this.eiServerEventCode = MeterEvent.HARDWARE_ERROR;
                    this.eventDescription += "EFA: Modem failure." + description;
                    break;
                case 7:
                    this.eiServerEventCode = MeterEvent.MEASUREMENT_SYSTEM_ERROR;
                    this.eventDescription += "EFA: Calibration data loss." + description;
                    break;
                case 8:
                    this.eiServerEventCode = MeterEvent.REVERSE_RUN;
                    this.eventDescription += "EFA: Reverse power." + description;
                    break;
                case 9:
                    this.eiServerEventCode = MeterEvent.CLOCK_INVALID;
                    this.eventDescription += "EFA: Clock failure." + description;
                    break;
                case 10:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: Tamper." + description;
                    break;
                case 11:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: Incorrect phase rotation." + description;
                    break;
                case 12:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: VT failure." + description;
                    break;
                case 13:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: Voltage tolerance error." + description;
                    break;
                case 14:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Asymmetric power." + description;
                    break;
                case 15:
                    this.eiServerEventCode = MeterEvent.OTHER;
                    this.eventDescription += "EFA: Reference failure." + description;
                    break;
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: Advanced tamper." + description;
                    break;
                case 42:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Current missing." + description;
                    break;
                case 43:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Relay failure." + description;
                    break;
                case 44:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Source Impedance." + description;
                    break;
                case 45:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: 3-phase neutral mismatch" + description;
                    break;
                case 46:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Wireless failure." + description;
                    break;
                case 47:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Over current." + description;
                    break;
                case 63:
                    this.eiServerEventCode = MeterEvent.OTHER;
                    this.eventDescription += "EFA: All latched flags were cleared" + description;
                    break;
            }
        } else if ((eventType & 0xFF00) == 0x3100) {
            this.eiServerEventCode = MeterEvent.TAMPER;
            description = "Radio module tamper alarm for radio channel " + (eventType & 0x003F);
            description += ((eventType & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
        } else if ((eventType & 0xFF00) == 0x3200) {
            this.eiServerEventCode = MeterEvent.BATTERY_VOLTAGE_LOW;
            description = "Radio module low battery alarm for radio channel " + (eventType & 0x003F);
            description += ((eventType & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
        } else if ((eventType & 0xFF00) == 0x3300) {
            this.eiServerEventCode = MeterEvent.CLOCK_INVALID;
            description = "Radio module time out of sync alarm for radio channel " + (eventType & 0x003F);
            description += ((eventType & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
        } else if ((eventType & 0xFF00) == 0x4000) {
            this.eiServerEventCode = MeterEvent.FIRMWARE_ACTIVATED;
            this.eventDescription += "The meter firmware changed to revision 0x" + ProtocolUtils.buildStringHex(eventType & 0x00FF, 2);
        } else if (eventType == 0x4100) {
            this.eiServerEventCode = MeterEvent.FIRMWARE_ACTIVATED;
            this.eventDescription += "The meter bootloader was upgraded.";
        } else if (eventType == 0x5000) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Automatic billing reset occurred.";
        } else if (eventType == 0x5001) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Manual billing reset occurred from the billing reset button.";
        } else if ((eventType & 0xFFCF) == 0x5080) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Manual billing reset occurred." + getPortString(eventType);
        } else if ((eventType & 0xF800) == 0x6000) {
            if ((eventType & 0x0080) == 0x80) {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SAG;
                this.eventDescription += "Voltage sag change start";
                this.eventDescription += mapPhase(eventType & 0x300, 0x300);
            } else {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SWELL;
                this.eventDescription += "Voltage swell change start";
                this.eventDescription += mapPhase(eventType & 0x300, 0x300);
            }
        } else if ((eventType & 0xF800) == 0x6800) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "THD/Unbalance trigger.";
        } else if ((eventType & 0xFFFF) == 0xB210) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Attempted to send a UDP alarm but never got an ACK from the server.";
        } else if ((eventType & 0xFFF0) == 0xB200) {
            this.eiServerEventCode = MeterEvent.OTHER;
            if ((eventType & 0x000F) == 0) {
                this.eventDescription += "Meter running full powered from the UPS battery.";
            } else if ((eventType & 0x000F) == 1) {
                this.eventDescription += "Mains power restored while running on the UPS.";
            }
        } else if ((eventType & 0xFF00) == 0xB200) {
            this.eiServerEventCode = MeterEvent.OTHER;
            if ((eventType & 0x000F) == 2) {
                this.eventDescription += "Latched alarm from input " + (eventType & 0x00F0);
            } else if ((eventType & 0x000F) == 3) {
                this.eventDescription += "Unlatched alarm from input " + (eventType & 0x00F0);
            } else if ((eventType & 0x000F) == 4) {
                this.eventDescription += "Momentary pulse alarm from input " + (eventType & 0x00F0);
            }
        } else if ((eventType & 0xFFF0) == 0xB300) {
            this.eiServerEventCode = MeterEvent.POWERUP;
            if ((eventType & 0x000F) == 0) {
                this.eventDescription += "Power up while in low power mode.";
            } else if ((eventType & 0x000F) == 1) {
                this.eventDescription += "Power up from a crash or without battery.";
            } else if ((eventType & 0x000F) == 2) {
                this.eventDescription += "power up from a crash or without a battery and the stack is corrupted.";
            }
        } else if ((eventType & 0xB500) == 0xB500) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Pulsing input tariff change";
        } else if ((eventType & 0xB600) == 0xB600) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Modem firmware upgrade state changed to " + (eventType & 0x000F);
        } else {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventType, 4);
        }
    }

    private String mapParameterRecoveryReason(int eventType, String description) {
        if ((eventType & FLASH_BACKUP_BAD) == FLASH_BACKUP_BAD) {
            description += " - Flash backup bad";
        }
        if ((eventType & ENERGY_ACCUMULATION_RESTORED) == ENERGY_ACCUMULATION_RESTORED) {
            description += " - Energy accumulation restored";
        }
        if ((eventType & CONTROL_DATA_RESTORED) == CONTROL_DATA_RESTORED) {
            description += " - Control data restored";
        }
        return description;
    }

    private void parseTamperEventDetails(int eventType) {
        if ((eventType & 0xF000) == 0x3000) {
            this.eiServerEventCode = MeterEvent.TAMPER;
            int tamperEvent = (eventType & 0x003F);
            if (tamperEvent < 16 || tamperEvent > 30) {
                this.eventDescription += "Tamper event" + getTamperState(eventType);
            } else if (tamperEvent == 16 || tamperEvent == 17 || tamperEvent == 18) {
                this.eventDescription += "Tamper event because VT lost" + mapPhase(tamperEvent, 16) + "." + getTamperState(eventType);
            } else if (tamperEvent == 19 || tamperEvent == 20 || tamperEvent == 21) {
                this.eventDescription += "Tamper event because VT surge" + mapPhase(tamperEvent, 19) + "." + getTamperState(eventType);
            } else if (tamperEvent == 22) {
                this.eventDescription += "Tamper event because VT phase bridge." + getTamperState(eventType);
            } else if (tamperEvent == 23) {
                this.eventDescription += "Tamper event because VT phase order." + getTamperState(eventType);
            } else if (tamperEvent == 24 || tamperEvent == 25 || tamperEvent == 26) {
                this.eventDescription += "Tamper event because CT" + mapPhase(tamperEvent, 24) + "." + getTamperState(eventType);
            } else if (tamperEvent == 27) {
                this.eventDescription += "Tamper event because CT phase order." + getTamperState(eventType);
            } else if (tamperEvent == 28 || tamperEvent == 29 || tamperEvent == 30) {
                this.eventDescription += "Tamper event because CT current reversal" + mapPhase(tamperEvent, 28) + "." + getTamperState(eventType);
            } else {
                this.eiServerEventCode = MeterEvent.OTHER;
                this.eventDescription += "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventType, 4);
            }
        } else {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventType, 4);
        }
    }

    private String mapPhase(int type, int baseOffset) {
        if (type == baseOffset) {
            return " phase A";
        } else if (type == (baseOffset + 1)) {
            return " phase B";
        } else if (type == (baseOffset + 2)) {
            return " phase C";
        }
        return "";
    }

    private void parseSpecialEventDetails(int codedEventTime, int eventType, LogBookDescription eventLogNr) {
        if ((eventType & 0xF800) == 0x7000) {
            if ((eventType & 0x0080) == 0x80) {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SAG;
                this.eventDescription += "Voltage sag change end";
                this.eventDescription += mapPhase(eventType & 0x300, 0x300);
            } else {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SWELL;
                this.eventDescription += "Voltage swell change end";
                this.eventDescription += mapPhase(eventType & 0x300, 0x300);
            }
            this.eventDescription += " - Duration: " + codedEventTime;
        } else if ((eventType & 0xF800) == 0x7800) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "THD/Unbalance trigger.";
            this.eventDescription += " - Duration: " + codedEventTime;
        } else if ((eventType & 0xF000) == 0x8000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Tamper extended information record - Coded quantity: " + codedEventTime;
        }
    }

    private String getPortString(int eventType) {
        if ((eventType & PORT_MASK) == PORT_OPTICAL) {
            return " (Port: OPTICAL)";
        }
        if ((eventType & PORT_MASK) == PORT_MODEM) {
            return " (Port: MODEM)";
        }
        if ((eventType & PORT_MASK) == PORT_SCADA) {
            return " (Port: SCADA)";
        }
        if ((eventType & PORT_MASK) >= PORT_RESERVED) {
            return " (Port: RESERVED)";
        }
        return " (Port: INVALID)";
    }

    private String getTimeChangeReason(int eventType) {
        if ((eventType & TIME_CHANGE_MASK) == 0) {
            return "from command on port.";
        }
        if ((eventType & TIME_CHANGE_MASK) == 1) {
            return "from pulsing input.";
        }
        if ((eventType & TIME_CHANGE_MASK) == 2) {
            return "from ripple count.";
        }
        if ((eventType & TIME_CHANGE_MASK) == 0x0F) {
            return "to this time.";
        }
        return "from RESERVED.";
    }

    private String getTamperState(int eventType) {
        if ((eventType & TAMPER_STATE_MASK) == 0) {
            return " - Tamper detected";
        }
        if ((eventType & TAMPER_STATE_MASK) == 0x40) {
            return " - Tamper restored";
        }
        return " Reserved";
    }

    public String toString() {
        return this.eventDate.toString() + " " + getEventDescription();
    }

    public int getProtocolEventCode() {
        return protocolEventCode;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public LogBookDescription getLogBookDescription() {
        return logBookDescription;
    }

    public int getEiServerEventCode() {
        return eiServerEventCode;
    }

    public MeterProtocolEvent convertToMeterProtocolEvent() {
        return new MeterProtocolEvent(
                getEventDate(),
                getEiServerEventCode(),
                getProtocolEventCode(),
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(getEiServerEventCode()),
                getEventDescription(),
                0,
                0
        );
    }
}