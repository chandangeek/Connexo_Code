package com.energyict.echelon;

import java.util.Date;

import com.energyict.protocol.MeterEvent;

/**
 * Mapper class for mapping events coming from an Echelon device to EiServer
 * event codes. It also maps a message to the Echelon protocol code (cfr. NES
 * Provisioning Tool Guide).
 * 
 * @author Steven W
 * 
 */
public class EventMapper {

	private static EventMapper soleInstance;

	private EventMapper() {
	}

	public static EventMapper getInstance() {
		if (soleInstance == null) {
			synchronized (EventMapper.class) {
				if (soleInstance == null) {
					soleInstance = new EventMapper();
				}
			}
		}
		return soleInstance;
	}

	public MeterEvent Map(Date timeStamp, int protocolCode, String eventData) {
		int eiCode = toEiCode(protocolCode);
		String message = toMessage(protocolCode, eventData);
		MeterEvent event = new MeterEvent(timeStamp, eiCode, protocolCode,
				message);
		return event;
	}

	private int toEiCode(int protocolCode) {
		int eiCode = MeterEvent.OTHER;

		switch (protocolCode) {
		case EventConstants.PRIMARY_POWER_DOWN:
			eiCode = MeterEvent.POWERDOWN;
			break;
		case EventConstants.PRIMARY_POWER_UP:
			eiCode = MeterEvent.POWERUP;
			break;
		case EventConstants.TIME_CHANGED_OLD:
		case EventConstants.TIME_CHANGED_NEW:
			eiCode = MeterEvent.SETCLOCK;
			break;
		case EventConstants.RESET_LIST_POINTERS:
			eiCode = MeterEvent.MAXIMUM_DEMAND_RESET;
			break;
		case EventConstants.EVENT_LOG_ERASED:
		case EventConstants.PENDING_TABLE_CLEAR:
			eiCode = MeterEvent.CLEAR_DATA;
			break;
		case EventConstants.SELF_READ_OCCURRED:
			eiCode = MeterEvent.BILLING_ACTION;
			break;
		case EventConstants.DAYLIGHT_SAVING_TIME_ON:
		case EventConstants.DAYLIGHT_SAVING_TIME_OFF:
		case EventConstants.SEASON_CHANGE:
		case EventConstants.SPECIAL_SCHEDULE_ACTIVATION:
		case EventConstants.TIER_SWITCH_CHANGE:
		case EventConstants.PENDING_TABLE_ACTIVATION:
		case EventConstants.METER_REPROGRAMMED:
		case EventConstants.CODE_BANK_CHANGED:
		case EventConstants.LOG_DIMENSION_CHANGED:
		case EventConstants.MEP_STATUS:
			eiCode = MeterEvent.CONFIGURATIONCHANGE;
			break;
		case EventConstants.RAM_FAILURE:
			eiCode = MeterEvent.RAM_MEMORY_ERROR;
			break;
		case EventConstants.BOOTROM_CRC_FAILURE:
			eiCode = MeterEvent.ROM_MEMORY_ERROR;
			break;
		case EventConstants.PHASE_LOSS:
		case EventConstants.PHASE_INVERSION:
		case EventConstants.CURRENT_ON_MISSING_OR_UNUSED_PHASE:
		case EventConstants.PHASE_ROTATION_CHANGED:
			eiCode = MeterEvent.PHASE_FAILURE;
			break;
		case EventConstants.M_BUS_ALARM:
			eiCode = MeterEvent.METER_ALARM;
			break;
		case EventConstants.VOLTAGE_SAG:
			eiCode = MeterEvent.VOLTAGE_SAG;
			break;
		case EventConstants.VOLTAGE_SWELL:
			eiCode = MeterEvent.VOLTAGE_SWELL;
			break;
		}

		return eiCode;
	}

	/*
	 * map the protocolCode to a meaningful message.
	 * Not the most nicest code, but what can one expect from a mapper ;)
	 */
	private String toMessage(int protocolCode, String eventData) {
		String result = "";
		int eventCode = Util.hexToInt(eventData);
		switch (protocolCode) {
		case EventConstants.PRIMARY_POWER_DOWN:
			result = "Power off date and time";
			break;
		case EventConstants.PRIMARY_POWER_UP:
			result = "Power on date and time - ";
			if (eventCode == 3) {
				result += "Power failure";
			} else if (eventCode == 4) {
				result += "Watchdog reset";
			} else if (eventCode == 5) {
				result += "Code bank switchover event";
			} else if (eventCode == 8) {
				result += "Brown-out";
			} else if (eventCode == 11) {
				result += "Bootrom Switchover";
			}
			break;
		case EventConstants.TIME_CHANGED_OLD:
			result = "The previous date and time in effect when the time was changed.";
			break;
		case EventConstants.TIME_CHANGED_NEW:
			result = "If procedure 10 (absolute change) is used for the time change, this event time is the new time that was set. If procedure 2064 (gradual change) is used for the time change, this event time is the resulting new time after completion of the gradual change.";
			break;
		case EventConstants.METER_ACCESSED_FOR_WRITE:
			result = "Date and time an outside source wrote data into a meter table or via a meter procedure into a table.";
			break;
		case EventConstants.PROCEDURE_INVOKED:
			result = "Date and time an outside source invoked a specific meter procedure.";
			result += " " + eventData;
			break;
		case EventConstants.TABLE_WRITTEN_TO:
			result = "Date and time an outside source wrote to a specific meter table.";
			result += " " + eventData;
			break;
		case EventConstants.RESET_LIST_POINTERS:
			result = "List pointers for load profile, self read or event log were reset. This changes the number of unread records to zero.";
			if (eventCode == 2) {
				result += " Self read.";
			} else if (eventCode == 3 || eventCode == 7) {
				result += " Load profile.";
			} else if (eventCode == 8) {
				result += " Event log.";
			} else if (eventCode == 255) {
				result += " Self read, load profile & event log.";
			}
			break;
		case EventConstants.UPDATE_LIST_POINTERS:
			result = "List pointers for load profile, self read, or event log were updated. This changes the number of unread records.";
			if (eventCode == 2) {
				result += " Self read.";
			} else if (eventCode == 3 || eventCode == 7) {
				result += " Load profile.";
			} else if (eventCode == 8) {
				result += " Event log.";
			} else if (eventCode == 255) {
				result += " Self read, load profile & event log.";
			}
			break;
		case EventConstants.EVENT_LOG_ERASED:
			result = "The event log was erased.";
			break;
		case EventConstants.EVENT_LOG_POINTERS_CHANGED:
			result = "Event log pointer position was changed.";
			result += " " + eventData;
			break;
		case EventConstants.SELF_READ_OCCURRED:
			result = "A self-read occurred.";
			break;
		case EventConstants.DAYLIGHT_SAVING_TIME_ON:
			result = "The meter switched into Daylight Saving Time.";
			break;
		case EventConstants.DAYLIGHT_SAVING_TIME_OFF:
			result = "The meter switched out of Daylight Saving Time.";
			break;
		case EventConstants.SEASON_CHANGE:
			result = "A TOU season change occurred.";
			if (eventCode == 0) {
				result += " Season 1.";
			} else if (eventCode == 1) {
				result += " Season 2.";
			} else if (eventCode == 2) {
				result += " Season 3.";
			} else if (eventCode == 3) {
				result += " Season 4.";
			}
			break;
		case EventConstants.SPECIAL_SCHEDULE_ACTIVATION:
			result = "A day deemed as a TOU holiday was activated.";
			if (eventCode == 0) {
				result += " Special schedule A.";
			} else if (eventCode == 1) {
				result += " Special schedule B.";
			}
			break;
		case EventConstants.TIER_SWITCH_CHANGE:
			result = "The meter switched from one tariff to another per day table settings or via procedure (forced tariff change).";
			if (eventCode == 0) {
				result += " Tariff 1.";
			} else if (eventCode == 1) {
				result += " Tariff 2.";
			} else if (eventCode == 2) {
				result += " Tariff 3.";
			} else if (eventCode == 3) {
				result += " Tariff 4.";
			}
			break;
		case EventConstants.PENDING_TABLE_ACTIVATION:
			result = "The TOU pending table was activated.";
			if (eventCode == 4119) {
				result += " Internal use.";
			} else if (eventCode == 4150) {
				result += " Preset TOU schedule changeover.";
			}
			break;
		case EventConstants.PENDING_TABLE_CLEAR:
			result = "The TOU pending table was cleared.";
			if (eventCode == 4119) {
				result += " Internal use.";
			} else if (eventCode == 4150) {
				result += "Preset TOU schedule changeover was cancelled.";
			}
			break;
		case EventConstants.METER_REPROGRAMMED:
			result = "New firmware operating code was written to the meter.";
			result += " " + eventData;
			break;
		case EventConstants.LOAD_DISCONNECT_OPEN:
			result = "The load disconnect contactor has been either opened or closed.";
			if (eventCode == 258) {
				result += " Disconnect switch opened [Reason: Software command]";
			} else if (eventCode == 259) {
				result += " Disconnect switch locked opened [Reason: Software command]";
			} else if (eventCode == 514) {
				result += " Disconnect switch opened [Reason: Maximum Power]";
			} else if (eventCode == 770) {
				result += " Disconnect switch opened [Reason: Prepayment]";
			} else if (eventCode == 771) {
				result += " Disconnect switch locked opened [Reason: Prepayment]";
			} else if (eventCode == 1026) {
				result += " Disconnect switch opened [Reason: Prepayment maximum power]";
			} else if (eventCode == 1281) {
				result += " Disconnect switch closed [Reason: Remote reconnect]";
			} else if (eventCode == 1537) {
				result += " Disconnect switch closed [Reason: Lever/button operation]";
			} else if (eventCode == 1538) {
				result += " Disconnect switch opened [Reason: Lever/button operation]";
			}
			break;
		case EventConstants.CONTROL_RELAY_OPEN:
			result = "Control relay is in open state.";
			break;
		case EventConstants.INVALID_PASSWORD:
			result = "An invalid password was entered during optical communications.";
			break;
		case EventConstants.CODE_BANK_CHANGED:
			result = "Active (executing) code bank has been changed.";
			break;
		case EventConstants.LOAD_PROFILE_BACKFILL_FAILED:
			result = "Load profile was not backfilled completely at power-up because meter was off across midnight.";
			break;
		case EventConstants.M_BUS_AUTO_DISCOVERY_COMPLETE:
			result = "Indicates that the meter has completed an M-Bus auto-discovery process.";
			break;
		case EventConstants.MANUFACTURER_LOG_ENTRY_OCCURRED:
			result = "An entry has been maded in the manufacturer log. ";
			result += eventData;
			break;
		case EventConstants.LOG_DIMENSION_CHANGED:
			result = "The size of the meter’s dimension log has changed.";
			break;
		case EventConstants.MEP_STATUS:
			result = "Status has changed for an M-Bus device.";
			if (eventCode == 0) {
				result += " Device 0 is commissioned and assigned to position 1, 2, 3, or 4.";
			} else if (eventCode == 1) {
				result += " Device 0 commission failed, no available position.";
			} else if (eventCode == 2) {
				result += " Device 0 commission failed, lost communication.";
			} else if (eventCode == 8192) {
				result += " Device 1 is commissioned.";
			} else if (eventCode == 8194) {
				result += " Device 1 commission failed, lost communication.";
			} else if (eventCode == 8196) {
				result += " Device 1 removed logically from meter.";
			} else if (eventCode == 16384) {
				result += " Device 2 is commissioned.";
			} else if (eventCode == 16386) {
				result += " Device 2 commission failed, lost communication.";
			} else if (eventCode == 16388) {
				result += " Device 2 removed logically from meter.";
			} else if (eventCode == 24576) {
				result += " Device 3 is commissioned.";
			} else if (eventCode == 24578) {
				result += " Device 3 commission failed, lost communication.";
			} else if (eventCode == 24580) {
				result += " Device 3 removed logically from meter.";
			} else if (eventCode == 32768) {
				result += " Device 4 is commissioned.";
			} else if (eventCode == 32770) {
				result += " Device 4 commission failed, lost communication.";
			} else if (eventCode == 32772) {
				result += " Device 4 removed logically from meter.";
			}
			break;
		case EventConstants.MAXIMUM_POWER_LEVEL_THRESHOLD_SWITCHED:
			result = "The maximum power level has changed. ";
			if (eventCode == 0) {
				result += "Max power level changed from primary to secondary.";
			} else if (eventCode == 1) {
				result += "Max power level changed from secondary to primary. The event is nog generated if switch is attempted when meter is already in primary mode.";
			}
			break;
		case EventConstants.CONFIGURATION_ERROR:
			result = "Power Line Carrier (PLC) communications IC initialization failed. PLC may be non-functional.";
			result += " " + eventData;
			break;
		case EventConstants.SYSTEM_RESET:
			result = "Watch-dog reset or event buffer overflow occurred. May be due to momentary voltage interruption. An unexpected software error has occurred.";
			if (eventCode == 0) {
				result += " Watch-dog timeout.";
			} else if (eventCode == 1) {
				result += " Event buffer overflow, some events may be lost.";
			} else if (eventCode == 2) {
				result += " Abnormal power interrupt.";
			}
			break;
		case EventConstants.RAM_FAILURE:
			result = "Memory corruption occurred.";
			if (eventCode == 1) {
				result += "LP value corrupted.";
			} else if (eventCode == 3) {
				result += "Disconnect or control relay value corrupted.";
			} else if (eventCode == 4) {
				result += "RAM memory test error.";
			} else if (eventCode == 5) {
				result += "RTC code in NVRAM corrupted.";
			} else if (eventCode == 6) {
				result += "NVRAM alarm variable corrupted.";
			}
			break;
		case EventConstants.BOOTROM_CRC_FAILURE:
			result = "Invalid CRC in Bootrom.";
			break;
		case EventConstants.NON_VOLATILE_MEMORY_ERROR:
			result = "CRC verification failed. Memory may have been corrupted.";
			if (eventCode == 65534) {
				result += " Bootrom FRAM CRC/signature invalid.";
			} else if (eventCode == 65535) {
				result += " All tables.";
			}
			break;
		case EventConstants.CLOCK_ERROR:
			result = "Loss of clock memory data or clock functions have been suspended due to meter having been without AC power for an extended period of time.";
			break;
		case EventConstants.MEASUREMENT_ERROR:
			result = "Metering error occurred.";
			break;
		case EventConstants.LOW_BATTERY:
			result = "Real Time Clock (RTC) back-up battery is below 2.5V.";
			break;
		case EventConstants.COVER_REMOVED:
			result = "The meter terminal cover has been removed. Considered a tamper event.";
			if (eventCode == 1) {
				result += " Cover removed.";
			}
			break;
		case EventConstants.REVERSE_ENERGY:
			result = "Meter has registered reverse power for 10 consecutive seconds. Considered a possible tamper event. This alarm is triggered if there is reverse energy over the power threshold on any of the phases that are set as active.";
			if (eventCode == 1) {
				result += " L1 reverse energy.";
			} else if (eventCode == 2) {
				result += " L2 reverse energy.";
			} else if (eventCode == 3) {
				result += " L1 & L2 reverse energy.";
			} else if (eventCode == 4) {
				result += " L3 reverse energy.";
			} else if (eventCode == 5) {
				result += " L1 & L3 reverse energy.";
			} else if (eventCode == 6) {
				result += " L2 & L3 reverse energy.";
			} else if (eventCode == 7) {
				result += " L1, L2 & L3 reverse energy.";
			}
			break;
		case EventConstants.DATA_BACKUP_INCOMPLETE:
			result = "A data backup procedure did not complete. Up to one hour’s worth of billing and other FRAM data may have been lost.";
			break;
		case EventConstants.DISCONNECT_MISMATCH:
			result = "Load disconnect contactor open/closed state may be incorrect.";
			break;
		case EventConstants.LOAD_PROFILE_OVERFLOW:
			result = "Load profile memory overflow has occurred, and unread records have been overwritten.";
			break;
		case EventConstants.PHASE_LOSS:
			result = "Phase loss detected. Voltage below 61% (± 5%) of the rated voltage has occurred on at least one phase. Considered a possible tamper event.";
			if (eventCode == 1) {
				result += " Line 1 Phase Lost (L1).";
			} else if (eventCode == 2) {
				result += " Line 2 Phase lost (L2).";
			} else if (eventCode == 3) {
				result += " L1 & L2.";
			} else if (eventCode == 4) {
				result += " Line 3 Phase lost (L3).";
			} else if (eventCode == 5) {
				result += " L1 & L3.";
			} else if (eventCode == 6) {
				result += " L2 & L3.";
			}
			break;
		case EventConstants.PHASE_INVERSION:
			result = "Phase inversion detected. Neutral and one phase have been swapped. Considered a possible tamper event.";
			if (eventCode == 2) {
				result += " Plus 120° Inverted.";
			} else if (eventCode == 4) {
				result += "Plus 180° Inverted.";
			} else if (eventCode == 6) {
				result += " Minus 120° Inverted.";
			}
			break;
		case EventConstants.PLC_DRIVER_COMMS_FAILURE:
			result = "Error reading PLC configuration data. Data may be corrupted.";
			break;
		case EventConstants.GENERAL_ERROR:
			result = "Power-down process error and/or display read-back failed. An unexpected power-down sequence occurred.";
			if (eventCode == 0) {
				result += " Power-down.";
			} else if (eventCode == 1) {
				result += " Display.";
			}
			break;
		case EventConstants.REMOTE_COMMUNICATIONS_INACTIVE:
			result = "Remote communications (PLC) inactive for at least 24 hours.";
			break;
		case EventConstants.CURRENT_ON_MISSING_OR_UNUSED_PHASE:
			result = "Current flow greater than 2A detected on a phase with low or no voltage. This usually indicates that a Potential Test Link is open. Considered a possible tamper event.";
			if (eventCode == 1) {
				result += " On line 1 (L1).";
			} else if (eventCode == 2) {
				result += " On line 2 (L2).";
			} else if (eventCode == 3) {
				result += " L1 & L2.";
			} else if (eventCode == 4) {
				result += " On line 3 (L3).";
			} else if (eventCode == 5) {
				result += " L1 & L3.";
			} else if (eventCode == 6) {
				result += " L2 & L3.";
			} else if (eventCode == 7) {
				result += " L1 & L2 & L3.";
			}
			break;
		case EventConstants.PULSE_INPUT_1_TAMPER:
			result = "Tamper condition detected on Pulse Input channel 1.";
			break;
		case EventConstants.PULSE_INPUT_2_TAMPER:
			result = "Tamper condition detected on Pulse Input channel 2.";
			break;
		case EventConstants.SOFTWARE_CRC_ERROR:
			result = "Downloaded firmware update image ID or CRC error during boot-up procedure. The code in the downloaded application may be corrupted.";
			if (eventCode == 0) {
				result += " CRC/ID failure.";
			} else if (eventCode == 1) {
				result += " Digest failure.";
			}
			break;
		case EventConstants.MEP_INSTALLED_OR_REMOVED:
			result = "Indicates that a MEP (Multipurpose Expansion Port) module has been field installed or removed from the electric meter.";
			if (eventCode == 0) {
				result += " MEP module installed.";
			} else if (eventCode == 1) {
				result += " MEP module removed.";
			}
			break;
		case EventConstants.M_BUS_ALARM:
			result = "An M-Bus alarm occurred.";
			if (eventCode == 8192) {
				result += " Device 1 billing read completed, data collected.";
			} else if (eventCode == 8194) {
				result += " Device 1 status read completed, new device alarm.";
			} else if (eventCode == 8195) {
				result += " Device 1 billing data overflow.";
			} else if (eventCode == 8196) {
				result += " Device 1 communication failure.";
			} else if (eventCode == 8197) {
				result += " Device 1 serial number mismatch.";
			} else if (eventCode == 16384) {
				result += " Device 2 billing read completed, data collected.";
			} else if (eventCode == 16386) {
				result += " Device 2 status read completed, new device alarm.";
			} else if (eventCode == 16387) {
				result += " Device 2 billing data overflow.";
			} else if (eventCode == 16388) {
				result += " Device 2 communication failure.";
			} else if (eventCode == 16389) {
				result += " Device 2 serial number mismatch.";
			} else if (eventCode == 24576) {
				result += " Device 3 billing read completed, data collected.";
			} else if (eventCode == 24578) {
				result += " Device 3 status read completed, new device alarm.";
			} else if (eventCode == 24579) {
				result += " Device 3 billing data overflow.";
			} else if (eventCode == 24580) {
				result += " Device 3 communication failure.";
			} else if (eventCode == 24581) {
				result += " Device 3 serial number mismatch.";
			} else if (eventCode == 32768) {
				result += " Device 4 billing read completed, data collected.";
			} else if (eventCode == 32770) {
				result += " Device 4 status read completed, new device alarm.";
			} else if (eventCode == 32771) {
				result += " Device 4 billing data overflow.";
			} else if (eventCode == 32772) {
				result += " Device 4 communication failure.";
			} else if (eventCode == 32773) {
				result += " Device 4 serial number mismatch.";
			}
			break;
		case EventConstants.PHASE_ROTATION_CHANGED:
			result = "Wiring positions for a 3-phase meter have changed, or any of the phases are inactive or missing.";
			if (eventCode == 0) {
				result += " L1L2L3.";
			} else if (eventCode == 1) {
				result += " L3L2L1.";
			} else if (eventCode == 2) {
				result += " Rotation Unknown.";
			}
			break;
		case EventConstants.PREPAY_CREDIT_EXHAUSTED:
			result = "Prepay credit has gone 0 (zero).";
			break;
		case EventConstants.PREPAY_WARNING_ACKNOWLEDGED:
			result = " User has pushed the button on the front of the meter to turn off the audible prepay low credit alarm.";
			break;
		case EventConstants.ACCESS_LOCKOUT_OVERRIDE:
			result = "The access lockout override settings for the meter have been overriden";
			if (eventCode == 0) {
				result += " Override condition gone.";
			} else if (eventCode == 1) {
				result += " Override detected.";
			}
			break;
		case EventConstants.POWER_QUALITY_STATE_CHANGED:
			result = "A power quality event has been detected. Bit mask, bit value 1 means event detected, value 0 means event cleared.";
			result += " " + eventData;
			break;
		case EventConstants.VOLTAGE_SAG:
			result = "The lowest voltage of the last finished sag event. The timestamp indicates when the lowest voltage was detected.";
			result += " " + eventData;
			break;
		case EventConstants.VOLTAGE_SWELL:
			result = "The highest voltage of last finished surge event. The timestamp of this event indicates when the highest voltage is detected.";
			result += " " + eventData;
			break;
		}
		return result;
	}
}
