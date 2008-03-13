package com.energyict.echelon;

/**
 * 
 * Constant class for all the events that exist in the NES system.
 * See the NES Provisioning Tool Guide (pdf).
 * 
 * @author Steven W
 *
 */
public class EventConstants {

	/* Status Events */
	 static final int PRIMARY_POWER_DOWN = 1;
	 static final int PRIMARY_POWER_UP = 2;
	 static final int TIME_CHANGED_OLD = 3;
	 static final int TIME_CHANGED_NEW = 4;
	 static final int METER_ACCESSED_FOR_WRITE = 8;
	 static final int PROCEDURE_INVOKED = 9;
	 static final int TABLE_WRITTEN_TO = 10;
	 static final int RESET_LIST_POINTERS = 14;
	 static final int UPDATE_LIST_POINTERS = 15;
	 static final int EVENT_LOG_ERASED = 18;
	 static final int EVENT_LOG_POINTERS_CHANGED = 19;
	 static final int SELF_READ_OCCURRED = 21;
	 static final int DAYLIGHT_SAVING_TIME_ON = 22;
	 static final int DAYLIGHT_SAVING_TIME_OFF = 23;
	 static final int SEASON_CHANGE = 24;
	 static final int SPECIAL_SCHEDULE_ACTIVATION = 26;
	 static final int TIER_SWITCH_CHANGE = 27;
	 static final int PENDING_TABLE_ACTIVATION = 28;
	 static final int PENDING_TABLE_CLEAR = 29;
	 static final int METER_REPROGRAMMED = 36;
	 static final int LOAD_DISCONNECT_OPEN = 66;
	 static final int LOAD_DISCONNECT_DISCONNECT_STATE_CHANGED = 66;
	 static final int CONTROL_RELAY_OPEN = 67;
	 static final int INVALID_PASSWORD = 72;
	 static final int CODE_BANK_CHANGED = 78;
	 static final int LOAD_PROFILE_BACKFILL_FAILED = 79;
	 static final int M_BUS_AUTO_DISCOVERY_COMPLETE = 82;
	 static final int MANUFACTURER_LOG_ENTRY_OCCURRED = 87;
	 static final int LOG_DIMENSION_CHANGED = 88;
	 static final int MEP_STATUS = 96;
	 static final int MAXIMUM_POWER_LEVEL_THRESHOLD_SWITCHED = 97;
	
	/* Alarm Events */
	 static final int CONFIGURATION_ERROR = 37;
	 static final int SYSTEM_RESET = 38;
	 static final int RAM_FAILURE = 39;
	 static final int BOOTROM_CRC_FAILURE = 40;
	 static final int NON_VOLATILE_MEMORY_ERROR = 41;
	 static final int CLOCK_ERROR = 42;
	 static final int MEASUREMENT_ERROR = 43;
	 static final int LOW_BATTERY = 44;
	 static final int COVER_REMOVED = 47;
	 static final int REVERSE_ENERGY = 48;
	 static final int DATA_BACKUP_INCOMPLETE = 49;
	 static final int DISCONNECT_MISMATCH = 50;
	 static final int LOAD_PROFILE_OVERFLOW = 64;
	 static final int PHASE_LOSS = 68;
	 static final int PHASE_INVERSION = 69;
	 static final int PLC_DRIVER_COMMS_FAILURE = 70;
	 static final int GENERAL_ERROR = 71;
	 static final int REMOTE_COMMUNICATIONS_INACTIVE = 73;
	 static final int CURRENT_ON_MISSING_OR_UNUSED_PHASE = 74;
	 static final int PULSE_INPUT_1_TAMPER = 75;
	 static final int PULSE_INPUT_2_TAMPER = 76;
	 static final int SOFTWARE_CRC_ERROR = 77;
	 static final int MEP_INSTALLED_OR_REMOVED = 80;
	 static final int M_BUS_ALARM = 81;
	 static final int PHASE_ROTATION_CHANGED = 83;
	 static final int PREPAY_CREDIT_EXHAUSTED = 84;
	 static final int PREPAY_WARNING_ACKNOWLEDGED = 85;
	 static final int ACCESS_LOCKOUT_OVERRIDE = 90;
	 static final int POWER_QUALITY_STATE_CHANGED = 91;
	 static final int VOLTAGE_SAG = 98;
	 static final int VOLTAGE_SWELL = 99;	
}
