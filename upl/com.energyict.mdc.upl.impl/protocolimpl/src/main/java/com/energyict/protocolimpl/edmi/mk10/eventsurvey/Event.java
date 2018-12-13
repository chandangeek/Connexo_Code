package com.energyict.protocolimpl.edmi.mk10.eventsurvey;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

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

	public static final int SYSTEM = 0;
	public static final int SETUP = 1;
	public static final int BILLING = 2;
	public static final int TRIG = 3;
	public static final int DIAG = 4;
	public static final String[] LOGNAMES = {"SYSTEM", "SETUP", "BILLING/TAMPER", "TRIG", "DIAG", "UNKNOWN"};
	
    private int eiServerEventCode;
    private int protocolEventCode;
    private Date eventDate;
	private int eventLogNr;
	private String eventLogName;
	private String eventDescription;
	
	public Event(Date date, int code, int lognr) throws IOException {
		if (lognr>4 || lognr<0) lognr = 5;
		this.eventLogNr = lognr;
		this.eventLogName = LOGNAMES[this.getEventLogNr()];
		this.eventDate = date;
		this.protocolEventCode = code;
        this.parseEventDetails(code);
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

	public int getEventLogNr() {
		return eventLogNr;
	}

	public String getEventLogName() {
		return eventLogName;
	}

    public int getEiServerEventCode() {
        return eiServerEventCode;
    }

    private String getPortString(int eventtype) {
        if ((eventtype & PORT_MASK) == PORT_OPTICAL) return " (Port: OPTICAL)";
        if ((eventtype & PORT_MASK) == PORT_MODEM) return " (Port: MODEM)";
		if ((eventtype & PORT_MASK) == PORT_SCADA) return " (Port: SCADA)";
		if ((eventtype & PORT_MASK) >= PORT_RESERVED) return " (Port: RESERVED)";
		return " (Port: INVALID)";
	}
	
	private String getTimeChangeReason(int eventtype) {
		if ((eventtype & TIME_CHANGE_MASK) == 0) return "from command on port.";
		if ((eventtype & TIME_CHANGE_MASK) == 1) return "from pulsing input.";
		if ((eventtype & TIME_CHANGE_MASK) == 2) return "from ripple count.";
		if ((eventtype & TIME_CHANGE_MASK) == 0x0F) return "to this time.";
		return "from RESERVED.";
	}

    private String getTamperState(int eventtype) {
        if ((eventtype & TAMPER_STATE_MASK) == 0) return " - Tamper detected";
        if ((eventtype & TAMPER_STATE_MASK) == 0x40) return " - Tamper restored";
        return " Reserved";
    }
	
	public void parseEventDetails(int eventtype) throws IOException {
        this.eventDescription = this.eventLogName + " - ";
        String description = "";

        if ((eventtype & 0xFFF0) == 0x1000) {
            this.eiServerEventCode = MeterEvent.POWERDOWN;
            this.eventDescription += "The meter was switched off";
            return;
        }
        if ((eventtype & 0xFFF0) == 0x1010) {
            this.eiServerEventCode = MeterEvent.POWERUP;
            this.eventDescription += "The meter powered up. Reason: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
            return;
        }
        if ((eventtype & 0xFFF0) == 0x1020) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Recovered some parameters. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
            if ((eventtype & FLASH_BACKUP_BAD) == FLASH_BACKUP_BAD) description += " - Flash backup bad";
            if ((eventtype & ENERGY_ACCUMULATION_RESTORED) == ENERGY_ACCUMULATION_RESTORED) description += " - Energy accumulation restored";
            if ((eventtype & CONTROL_DATA_RESTORED) == CONTROL_DATA_RESTORED) description += " - Control data restored";
            this.eventDescription += description;
            return;
        }
        if ((eventtype & 0xFFF0) == 0x1030) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Battery backed up copy AND flash copy of some parameters was lost. Parameters: 0x" + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
            if ((eventtype & FLASH_BACKUP_BAD) == FLASH_BACKUP_BAD) description += " - Flash backup bad";
            if ((eventtype & ENERGY_ACCUMULATION_RESTORED) == ENERGY_ACCUMULATION_RESTORED) description += " - Energy accumulation restored";
            if ((eventtype & CONTROL_DATA_RESTORED) == CONTROL_DATA_RESTORED) description += " - Control data restored";
            this.eventDescription += description;
            return;
        }
        if ((eventtype & 0xFFF0) == 0x1040) {
            this.eiServerEventCode = MeterEvent.OTHER;
            description = "Meter runtime statistics changed. " + ProtocolUtils.buildStringHex(eventtype & 0x000F, 2);
            if ((eventtype & 0x000F) == RUNTIME_STATISTICS_ON_TIME) description += " - ON time changed.";
            if ((eventtype & 0x000F) == RUNTIME_STATISTICS_OFF_TIME) description += " - OFF time changed.";
            if ((eventtype & 0x000F) == RUNTIME_STATISTICS_NUMBER_OF_POWERUPS) description += " - Number of powerups changed.";
            if ((eventtype & 0x000F) >= RUNTIME_STATISTICS_RESERVED) description += " - Reserved.";
            this.eventDescription += description;
            return;
        }
        if ((eventtype & 0xF000) == 0x1000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            int relayNumber = eventtype & 0x0F00;
            description = " - Relay status: ";
            description = ((eventtype & 0x0080) == 0x0080) ? "enabled" : "disabled";
            description += ((eventtype & 0x0040) == 0x0040) ? " - connected" : " - disconnected";

            int reasonCode = eventtype & 0x003F;
            if (reasonCode < 57) this.eventDescription += "Relay change made by an external command. Reason:" + ProtocolUtils.buildStringHex(reasonCode, 2) + description;
            if (reasonCode == 57) this.eventDescription += "Relay changed. Relay control via register F050-8" + description;
            if (reasonCode == 58) this.eventDescription += "Relay changed. Disconnect button was pressed" + description;
            if (reasonCode == 59) this.eventDescription += "Relay changed. Connect button was pressed" + description;
            if (reasonCode == 60) this.eventDescription += "Relay changed. Calendar (tariff) changed" + description;
            if (reasonCode == 61) this.eventDescription += "Relay changed. The physical relay changed state" + description;
            if (reasonCode == 62) this.eventDescription += "Relay changed. Reason:" + ProtocolUtils.buildStringHex(reasonCode, 2) + description;
            if (reasonCode == 63) this.eventDescription += "Relay changed. Relay Stuck recorded" + description;
            return;
        }

        if ((eventtype & 0xFFC0) == 0x2000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User" + (eventtype & USER_NUMBER_MASK) + " logged on." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCC) == 0x2040) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User changed setup" + ((eventtype & CHANGED_SETUP_MASK) + 1) + "." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2080) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User acces denied. Bad password." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2081) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. X command received." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2082) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Inactivity timeout." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2083) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Lost connection." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2084) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Login under another name." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2085) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Logoff via register write." + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFCF) == 0x2086) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "User logged off. Logoff via register write for firmware update." + getPortString(eventtype);
            return;
        }

        if ((eventtype & 0xFFFF) == 0x20CF) {
            this.eiServerEventCode = MeterEvent.SETCLOCK_AFTER;
            this.eventDescription += "System time changed " + getTimeChangeReason(eventtype) + getPortString(eventtype);
            return;
        }
        if ((eventtype & 0xFFC0) == 0x20C0) {
            this.eiServerEventCode = MeterEvent.SETCLOCK_BEFORE;
            this.eventDescription += "System time changed " + getTimeChangeReason(eventtype) + getPortString(eventtype);
            return;
        }

        if ((eventtype & 0xFF00) == 0x3000) {
            description = "";
            switch (eventtype & 0x00C0) {
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

            switch (eventtype & 0x003F) {
                case 0:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: User defined/magnetic tamper." + description;
                    return;
                case 1:
                    this.eiServerEventCode = MeterEvent.BATTERY_VOLTAGE_LOW;
                    this.eventDescription += "EFA: Battery failure." + description;
                    return;
                case 2:
                    this.eiServerEventCode = MeterEvent.REGISTER_OVERFLOW;
                    this.eventDescription += "EFA: Pulsing output overflow." + description;
                    return;
                case 3:
                    this.eiServerEventCode = MeterEvent.RAM_MEMORY_ERROR;
                    this.eventDescription += "EFA: Data flash failure." + description;
                    return;
                case 4:
                    this.eiServerEventCode = MeterEvent.PROGRAM_MEMORY_ERROR;
                    this.eventDescription += "EFA: Program flash failure." + description;
                    return;
                case 5:
                    this.eiServerEventCode = MeterEvent.HARDWARE_ERROR;
                    this.eventDescription += "EFA: RAM or LCD failure." + description;
                    return;
                case 6:
                    this.eiServerEventCode = MeterEvent.HARDWARE_ERROR;
                    this.eventDescription += "EFA: Modem failure." + description;
                    return;
                case 7:
                    this.eiServerEventCode = MeterEvent.MEASUREMENT_SYSTEM_ERROR;
                    this.eventDescription += "EFA: Calibration data loss." + description;
                    return;
                case 8:
                    this.eiServerEventCode = MeterEvent.REVERSE_RUN;
                    this.eventDescription += "EFA: Reverse power." + description;
                    return;
                case 9:
                    this.eiServerEventCode = MeterEvent.CLOCK_INVALID;
                    this.eventDescription += "EFA: Clock failure." + description;
                    return;
                case 10:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: Tamper." + description;
                    return;
                case 11:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: Incorrect phase rotation." + description;
                    return;
                case 12:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: VT failure." + description;
                    return;
                case 13:
                    this.eiServerEventCode = MeterEvent.PHASE_FAILURE;
                    this.eventDescription += "EFA: Voltage tolerance error." + description;
                    return;
                case 14:
                    this.eiServerEventCode = MeterEvent.METER_ALARM;
                    this.eventDescription += "EFA: Asymmetric power." + description;
                    return;
                case 15:
                    this.eiServerEventCode = MeterEvent.OTHER;
                    this.eventDescription += "EFA: Reference failure." + description;
                    return;
                case 16: case 17:case 18:case 19:case 20:case 21:case 22:case 23:
                case 24:case 25:case 26:case 27:case 28:case 29:case 30:case 31:
                    this.eiServerEventCode = MeterEvent.TAMPER;
                    this.eventDescription += "EFA: Advanced tamper." + description;
                    return;
                case 63:
                    this.eiServerEventCode = MeterEvent.OTHER;
                    this.eventDescription += "EFA: All latched flags were cleared / Over current" + description;
                    return;
            }
        }
        if ((eventtype & 0xFF00) == 0x3100) {
            this.eiServerEventCode = MeterEvent.TAMPER;
            description = "Radio module tamper alarm for radio channel " + (eventtype & 0x003F);
            description += ((eventtype & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
            return;
        }
        if ((eventtype & 0xFF00) == 0x3200) {
            this.eiServerEventCode = MeterEvent.BATTERY_VOLTAGE_LOW;
            description = "Radio module low battery alarm for radio channel " + (eventtype & 0x003F);
            description += ((eventtype & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
            return;
        }
        if ((eventtype & 0xFF00) == 0x3300) {
            this.eiServerEventCode = MeterEvent.CLOCK_INVALID;
            description = "Radio module time out of sync alarm for radio channel " + (eventtype & 0x003F);
            description += ((eventtype & 0x0040) == 0x0040) ? " - Alarm went active." : " - Alarm went inactive";
            this.eventDescription += description;
            return;
        }

        if ((eventtype & 0xFF00) == 0x4000) {
            this.eiServerEventCode = MeterEvent.FIRMWARE_ACTIVATED;
            this.eventDescription += "The meter firmware changed to revision 0x" + ProtocolUtils.buildStringHex(eventtype & 0x00FF, 2);
            return;
        }
        if (eventtype == 0x4100) {
            this.eiServerEventCode = MeterEvent.FIRMWARE_ACTIVATED;
            this.eventDescription += "The meter bootloader was upgraded.";
            return;
        }

        if (eventtype == 0x5000) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Automatic billing reset occurred.";
            return;
        }
        if (eventtype == 0x5001) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Manual billing reset occured from the billing reset button.";
            return;
        }
        if ((eventtype & 0xFFCF) == 0x5080) {
            this.eiServerEventCode = MeterEvent.BILLING_ACTION;
            this.eventDescription += "Manual billing reset occurred." + getPortString(eventtype);
            return;
        }

        if ((eventtype & 0xF800) == 0x6000) {
            if ((eventtype & 0x0080) == 0x80) {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SAG;
                this.eventDescription += "Voltage sag change start";
            } else {
                this.eiServerEventCode = MeterEvent.VOLTAGE_SWELL;
                this.eventDescription += "Voltage swell change start";
            }
            return;
        }
        if ((eventtype & 0xF800) == 0x6800) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "THD/Unbalance trigger.";
            return;
        }
        if ((eventtype & 0xF000) == 0x7000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription +=  "Voltage change end. The eventtime is the duration of the voltage change instead of date/time time !!!";
            return;
        }
        if ((eventtype & 0xF000) == 0x7800) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription +=  "TDH/Unbalance trigger";
            return;
        }
        if ((eventtype & 0xF000) == 0x8000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Reserved event.";
            return;
        }
        if ((eventtype & 0xF000) == 0x9000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Reserved event.";
            return;
        }

        if ((eventtype & 0xF000) == 0xA000) {
            this.eiServerEventCode = MeterEvent.CONFIGURATIONCHANGE;
            switch (eventtype & 0x00FF) {
                case 0: this.eventDescription += "Setup change: Event logs cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 1: this.eventDescription += "Setup change: Load survey 1. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 2: this.eventDescription += "Setup change: Load survey 2. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 3:this.eventDescription += "Setup change: TOU setup changed / cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 4: this.eventDescription += "Setup change: Billing history cleared. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 5: this.eventDescription += "Setup change: Pulsing generator reset. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 6: this.eventDescription += "Setup change: TOU calendar changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 7: this.eventDescription += "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 8: this.eventDescription += "Setup change: Hardware setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 9: this.eventDescription += "Setup change: Calibration changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 10: this.eventDescription += "Setup change: Scaling factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 11: this.eventDescription += "Setup change: Transformer ratios changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 12: this.eventDescription += "Setup change: Pulse factors changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 13: this.eventDescription += "Setup change: Pulsing inputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 14: this.eventDescription += "Setup change: Pulsing outputs setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 15: this.eventDescription += "Setup change: Enunciators changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 16: this.eventDescription += "Setup change: Optical port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 17: this.eventDescription += "Setup change: Modem port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 18: this.eventDescription += "Setup change: LCD screens changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 19: this.eventDescription += "Setup change: Alarm setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 20: this.eventDescription += "Setup change: Security setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 21: this.eventDescription += "Setup change: Timer setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 22: this.eventDescription += "Setup change: Time setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 23: this.eventDescription += "Setup change: SCADA port setup changed. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
                case 24:case 25:case 26:case 27:case 28:case 29:case 30:case 31:case 32:
                    this.eventDescription += "Setup change: Spare. Mask: 0x" + ProtocolUtils.buildStringHex((eventtype & 0x0F00) >> 8, 2); return;
            }
        }

        if ((eventtype & 0xFFF0) == 0xB300) {
            this.eiServerEventCode = MeterEvent.POWERUP;
            if ((eventtype & 0x000F) == 0) {
                this.eventDescription += "Power up while in low power mode.";
                return;
            } else if ((eventtype & 0x000F) == 1) {
                this.eventDescription += "Power up from a crash or without battery.";
                return;
            } else if ((eventtype & 0x000F) == 2) {
                this.eventDescription += "power up from a crash or without a battery and the stack is corrupted.";
                return;
            }
        }
        if ((eventtype & 0xFFFF) == 0xB210) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Attempted to send a UDP alarm but never got an ACK from the server.";
            return;
        }
        if ((eventtype & 0xFFF0) == 0xB200) {
            this.eiServerEventCode = MeterEvent.OTHER;
            if ((eventtype & 0x000F) == 0) {
                this.eventDescription += "Meter running full powered from the UPS battery.";
                return;
            } else if ((eventtype & 0x000F) == 1) {
                this.eventDescription += "Mains power restored while running on the UPS.";
                return;
            }
        }
        if ((eventtype & 0xFF00) == 0xB200) {
            this.eiServerEventCode = MeterEvent.OTHER;
            if ((eventtype & 0x000F) == 2) {
                this.eventDescription += "Latched alarm from input " + (eventtype & 0x00F0);
                return;
            } else if ((eventtype & 0x000F) == 3) {
                this.eventDescription += "Unlatched alarm from input " + (eventtype & 0x00F0);
                return;
            } else if ((eventtype & 0x000F) == 4) {
                this.eventDescription += "Momentary pulse alarm from input " + (eventtype & 0x00F0);
                return;
            }
        }

        if ((eventtype & 0xF000) == 0x3000) {
            this.eiServerEventCode = MeterEvent.TAMPER;
            int tamperEvent = (eventtype & 0x003F);
            if (tamperEvent < 16 || tamperEvent > 30) {
                this.eventDescription += "Tamper event" + getTamperState(eventtype);
            } else if (tamperEvent == 16) {
                this.eventDescription += "Tamper event because VT lost phase A." + getTamperState(eventtype);
            } else if (tamperEvent == 17) {
                this.eventDescription += "Tamper event because VT lost phase B." + getTamperState(eventtype);
            } else if (tamperEvent == 18) {
                this.eventDescription += "Tamper event because VT lost phase C." + getTamperState(eventtype);
            } else if (tamperEvent == 19) {
                this.eventDescription += "Tamper event because VT surge phase A." + getTamperState(eventtype);
            } else if (tamperEvent == 20) {
                this.eventDescription += "Tamper event because VT surge phase B." + getTamperState(eventtype);
            } else if (tamperEvent == 21) {
                this.eventDescription += "Tamper event because VT surge phase C." + getTamperState(eventtype);
            } else if (tamperEvent == 22) {
                this.eventDescription += "Tamper event because VT phase bridge." + getTamperState(eventtype);
            } else if (tamperEvent == 23) {
                this.eventDescription += "Tamper event because VT phase order." + getTamperState(eventtype);
            } else if (tamperEvent == 24) {
                this.eventDescription += "Tamper event because CT lost phase A." + getTamperState(eventtype);
            } else if (tamperEvent == 25) {
                this.eventDescription += "Tamper event because CT lost phase B." + getTamperState(eventtype);
            } else if (tamperEvent == 26) {
                this.eventDescription += "Tamper event because CT lost phase C." + getTamperState(eventtype);
            } else if (tamperEvent == 27) {
                this.eventDescription += "Tamper event because CT phase order." + getTamperState(eventtype);
            } else if (tamperEvent == 28) {
                this.eventDescription += "Tamper event because CT current reversal A." + getTamperState(eventtype);
            } else if (tamperEvent == 29) {
                this.eventDescription += "Tamper event because CT current reversal B." + getTamperState(eventtype);
            } else if (tamperEvent == 30) {
                this.eventDescription += "Tamper event because CT current reversal C." + getTamperState(eventtype);
            }
            return;
        }
        if ((eventtype & 0xF000) == 0x8000) {
            this.eiServerEventCode = MeterEvent.OTHER;
            this.eventDescription += "Tamper extended information record. The eventtime is a coded quantity instead of date/time time !!!";
            return;
        }

        this.eiServerEventCode = MeterEvent.OTHER;
        this.eventDescription += "Unknown event: 0x" + ProtocolUtils.buildStringHex(eventtype, 4) + " !!!";
    }
}
