/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Functionality to create and maintain {@link MeterEvent}s for the DL220
 * 
 * @author gna
 * @since 10-mrt-2010
 * 
 */
public class DL220MeterEventList {

	private static final String EVENT_PREFIX = "0x";

	
	/* Events used to determine a timeShift */
	private static final String VALUE_ARCHIVE1_CHANGED_AFTER = "0x8202";
	private static final String VALUE_ARCHIVE2_CHANGED_AFTER = "0x8204";
	
	private static final String VALUE_ARCHIVE1_CHANGED_BEFORE = "0x8302";
	private static final String VALUE_ARCHIVE2_CHANGED_BEFORE = "0x8304";
	
	private static final String[] VALUE_ARCHIVE_X_CHANGED_AFTER = {VALUE_ARCHIVE1_CHANGED_AFTER, VALUE_ARCHIVE2_CHANGED_AFTER};
	private static final String[] VALUE_ARCHIVE_X_CHANGED_BEFORE = {VALUE_ARCHIVE1_CHANGED_BEFORE, VALUE_ARCHIVE2_CHANGED_BEFORE};
	
	/* Other events */
	private static final String VALUE_OUTPUT1_OVERLOAD_START = "0x0301";
	private static final String VALUE_OUTPUT1_OVERLOAD_END = "0x2301";
	private static final String VALUE_OUTPUT2_OVERLOAD_START = "0x0302";
	private static final String VALUE_OUTPUT2_OVERLOAD_END = "0x2302";
	private static final String VALUE_DEVIATION_PULSE1_COMPARISON_START = "0x2401";
	private static final String VALUE_DEVIATION_PULSE1_COMPARISON_END = "0x0401";
	private static final String VALUE_DEVIATION_PULSE2_COMPARISON_START = "0x2402";
	private static final String VALUE_DEVIATION_PULSE2_COMPARISON_END = "0x0402";
	private static final String VALUE_I1_WARNING_LIMIT_VIOLATED_START = "0x2501";
	private static final String VALUE_I1_WARNING_LIMIT_VIOLATED_END = "0x0501";
	private static final String VALUE_I2_WARNING_LIMIT_VIOLATED_START = "0x2502";
	private static final String VALUE_I2_WARNING_LIMIT_VIOLATED_END = "0x0502";
	private static final String VALUE_I1_WARNING_SIGNAL_ACTIVE_START = "0x2701";
	private static final String VALUE_I1_WARNING_SIGNAL_ACTIVE_END = "0x0701";
	private static final String VALUE_I2_WARNING_SIGNAL_ACTIVE_START = "0x2702";
	private static final String VALUE_I2_WARNING_SIGNAL_ACTIVE_END = "0x0702";
	private static final String VALUE_WARNING_MODEM_BATTERY_START = "0x2804"; 
	private static final String VALUE_WARNING_MODEM_BATTERY_END = "0x0804";
	private static final String VALUE_I1_LIMIT_VIOLATED_START = "0x2B01";
	private static final String VALUE_I1_LIMIT_VIOLATED_END = "0x0B01";
	private static final String VALUE_I2_LIMIT_VIOLATED_START = "0x2B02";
	private static final String VALUE_I2_LIMIT_VIOLATED_END = "0x0B02";
	private static final String VALUE_I1_REPORT_SIGNAL_ACTIVE_START = "0x2C01";
	private static final String VALUE_I1_REPORT_SIGNAL_ACTIVE_END = "0x0C01";
	private static final String VALUE_I2_REPORT_SIGNAL_ACTIVE_START = "0x2C02";
	private static final String VALUE_I2_REPORT_SIGNAL_ACTIVE_END = "0x0C02";
	private static final String VALUE_PROGRAMMING_LOCK_OPEN_START = "0x2D01";
	private static final String VALUE_PROGRAMMING_LOCK_OPEN_END = "0x0D01";
	private static final String VALUE_MANUFACTURER_LOCK_OPEN_START = "0x2D02";
	private static final String VALUE_MANUFACTURER_LOCK_OPEN_END = "0x0D02";
	private static final String VALUE_SUPPLIER_LOCK_OPEN_START = "0x2D03";
	private static final String VALUE_SUPPLIER_LOCK_OPEN_END = "0x0D03";
	private static final String VALUE_CUSTOMER_LOCK_OPEN_START = "0x2D04";
	private static final String VALUE_CUSTOMER_LOCK_OPEN_END = "0x0D04";
	private static final String VALUE_CALL_TIME_WINDOW1_START = "0x2F01";
	private static final String VALUE_CALL_TIME_WINDOW1_END = "0x0D01";
	private static final String VALUE_CALL_TIME_WINDOW2_START = "0x2F02";
	private static final String VALUE_CALL_TIME_WINDOW2_END = "0x0F02";
	private static final String VALUE_RESTART_START = "0x3002";
	private static final String VALUE_RESTART_END = "0x1002";
	private static final String VALUE_DATA_RESTORED_START = "0x3202";
	private static final String VALUE_DATA_RESTORED_END = "0x1202";
	private static final String VALUE_HARDWARE_FAULT_START = "0x3502";
	private static final String VALUE_HARDWARE_FAULT_END = "0x1502";
	private static final String VALUE_SOFTWARE_FAULT_START = "0x3602";
	private static final String VALUE_SOFTWARE_FAULT_END = "0x1602";
	private static final String VALUE_SETTINGS_FAULT_START = "0x3702";
	private static final String VALUE_SETTINGS_FAULT_END = "0x1702";
	private static final String VALUE_BATTERY_WARNING_START = "0x3802";
	private static final String VALUE_BATTERY_WARNING_END = "0x1802";
	private static final String VALUE_CLOCK_NOT_SET_START = "0x3A02";
	private static final String VALUE_CLOCK_NOT_SET_END = "0x1A02";
	private static final String VALUE_PTB_LOGBOOK_FULL_START = "0x3B02";
	private static final String VALUE_PTB_LOGBOOK_FULL_END = "0x1B02";
	private static final String VALUE_DATA_TRANSMISSION_RUNNING_START = "0x3C02";
	private static final String VALUE_DATA_TRANSMISSION_RUNNING_END = "0x1C02";
	private static final String VALUE_BATTERY_OPERATION_START = "0x3E02";
	private static final String VALUE_BATTERY_OPERATION_END = "0x1E02";
	private static final String VALUE_SUMMER_TIME_START = "0x3F02";
	private static final String VALUE_SUMMER_TIME_END = "0x1F02";
	private static final String VALUE_BACKUP_TIME_CHANGES = "0x8001";
	private static final String VALUE_BACKUP_TIME_BECOMES_LARGER = "0x8101";
	private static final String VALUE_CHANGE_OF_MEASUREMENT1_BACKWARDS = "0x8005";
	private static final String VALUE_END_OF_MEASUREMENT1 = "0x8105";
	private static final String VALUE_CHANGE_OF_MEASUREMENT2_BACKWARDS = "0x8006";
	private static final String VALUE_END_OF_MEASUREMENT2 = "0x8106";
	private static final String VALUE_CALL_TIME_WINDOW1_CHANGES = "0x800A";
	private static final String VALUE_CALL_TIME_WINDOW1_BECOMES_LARGER = "0x810A";
	private static final String VALUE_CALL_TIME_WINDOW2_CHANGES = "0x800B";
	private static final String VALUE_CALL_TIME_WINDOW2_BECOMES_LARGER = "0x810B";
	private static final String VALUE_MONITORING1_CHANGES = "0x800C";
	private static final String VALUE_MONITORING1_BECOMES_LARGER = "0x810C3";
	private static final String VALUE_MONITORING2_CHANGES = "0x800D";
	private static final String VALUE_MONITORING2_BECOMES_LARGER = "0x810D";
	private static final String VALUE_MONTH_BOUNDARY1_CORRECTION_BACKWARDS = "0x8011";
	private static final String VALUE_MONTH_BOUNDARY1_EXPIRED = "0x8111";
	private static final String VALUE_MONTH_BOUNDARY2_CORRECTION_BACKWARDS = "0x8012";
	private static final String VALUE_MONTH_BOUNDARY2_EXPIRED = "0x8112";
	private static final String VALUE_DAY_BOUNDARY1_CHANGED_BACKWARDS = "0x8015";
	private static final String VALUE_DAY_BOUNDARY1_EXPIRED = "0x8115";
	private static final String VALUE_DAY_BOUNDARY2_CHANGED_BACKWARDS = "0x8016";
	private static final String VALUE_DAY_BOUNDARY2_EXPIRED = "0x8116";
	private static final String VALUE_LOGBOOK_CHANGED_AFTER = "0x820A";
	private static final String VALUE_LOGBOOK_CHANGED_BEFORE = "0x830A";
	private static final String VALUE_ARCHIVE1_SAVING_VALUE = "0x8502";
	private static final String VALUE_ARCHIVE2_SAVING_VALUE = "0x8504";

	private static final int BEFORE_EVENT_INDEX = 0;
	private static final int AFTER_EVENT_INDEX = 1;
	private List<DL220Record> measurementChangeEvents = new ArrayList<DL220Record>();

	private List<MeterEvent> eventList = new ArrayList<MeterEvent>();
	
	private final int meterIndex;

	/**
	 * Constructor with a given meterIndex
	 * 
	 * @param index
	 * 			- the index of the meter
	 */
	public DL220MeterEventList(int index) {
		this.meterIndex = index;
	}	

	/**
	 * Create an event entry with from the given intervalrecord
	 * 
	 * @param dir
	 *            - the {@link DL220Record}
	 * @throws IOException 
	 */
	public void addRawEvent(DL220Record dir) throws IOException {
		if (VALUE_ARCHIVE_X_CHANGED_BEFORE[this.meterIndex].equalsIgnoreCase(dir.getEvent())) {
			measurementChangeEvents = new ArrayList<DL220Record>();
			measurementChangeEvents.add(dir);
		} else if (VALUE_ARCHIVE_X_CHANGED_AFTER[this.meterIndex].equalsIgnoreCase(dir.getEvent())) {
			measurementChangeEvents.add(dir);
			checkMeasurementChangeEvents();
		} else {
			addMeterEventToList(dir.getEndTime(), dir.getEvent());
		}
	}

	/**
	 * Add/Create a {@link MeterEvent} from the given input
	 * 
	 * @param eventTimeStamp
	 *            - the event timeStamp
	 * 
	 * @param eventId
	 *            - the eventId
	 */
	protected void addMeterEventToList(Date eventTimeStamp, String eventId) {
		if(VALUE_OUTPUT1_OVERLOAD_START.equalsIgnoreCase(eventId)){
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "O1: Error(overload) Started"));			
		} else if (VALUE_OUTPUT1_OVERLOAD_END.equals(eventId)){
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "O1: Error(overload) Ended"));
		} else if (VALUE_OUTPUT2_OVERLOAD_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "O2: Error(overload) Started"));
		} else if (VALUE_OUTPUT2_OVERLOAD_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "O2: Error(overload) Ended"));
		} else if (VALUE_DEVIATION_PULSE1_COMPARISON_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Deviation on pulse comparison Started"));
		} else if (VALUE_DEVIATION_PULSE1_COMPARISON_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Deviation on pulse comparison Ended"));
		} else if (VALUE_DEVIATION_PULSE2_COMPARISON_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Deviation on pulse comparison Started"));
		} else if (VALUE_DEVIATION_PULSE2_COMPARISON_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Deviation on pulse comparison Ended"));
		} else if (VALUE_I1_WARNING_LIMIT_VIOLATED_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Warning limit violated Started"));
		} else if (VALUE_I1_WARNING_LIMIT_VIOLATED_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Warning limit violated Ended"));
		} else if (VALUE_I2_WARNING_LIMIT_VIOLATED_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Warning limit violated Started"));
		} else if (VALUE_I2_WARNING_LIMIT_VIOLATED_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Warning limit violated Ended"));
		} else if (VALUE_I1_WARNING_SIGNAL_ACTIVE_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Warning signal active Started"));
		} else if (VALUE_I1_WARNING_SIGNAL_ACTIVE_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Warning signal active Ended"));
		} else if (VALUE_I2_WARNING_SIGNAL_ACTIVE_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Warning signal active Started"));
		} else if (VALUE_I2_WARNING_SIGNAL_ACTIVE_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Warning signal active Ended"));
		} else if (VALUE_WARNING_MODEM_BATTERY_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, convertStringEventIdToInteger(eventId), "MODEM BATTERY warning Started"));
		} else if (VALUE_WARNING_MODEM_BATTERY_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, convertStringEventIdToInteger(eventId), "MODEM BATTERY warning Ended"));
		} else if (VALUE_I1_LIMIT_VIOLATED_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Limit violated Started"));
		} else if (VALUE_I1_LIMIT_VIOLATED_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Limit violated Ended"));
		} else if (VALUE_I2_LIMIT_VIOLATED_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Limit violated Started"));
		} else if (VALUE_I2_LIMIT_VIOLATED_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Limit violated Ended"));
		} else if (VALUE_I1_REPORT_SIGNAL_ACTIVE_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Report signal active Started"));
		} else if (VALUE_I1_REPORT_SIGNAL_ACTIVE_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I1: Report signal active Ended"));
		} else if (VALUE_I2_REPORT_SIGNAL_ACTIVE_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Report signal active Started"));
		} else if (VALUE_I2_REPORT_SIGNAL_ACTIVE_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "I2: Report signal active Ended"));
		} else if (VALUE_PROGRAMMING_LOCK_OPEN_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Programming lock opened"));
		} else if (VALUE_PROGRAMMING_LOCK_OPEN_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Programming lock closed"));
		} else if (VALUE_MANUFACTURER_LOCK_OPEN_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Manufacturer lock opened"));
		} else if (VALUE_MANUFACTURER_LOCK_OPEN_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Manufacturer lock closed"));
		} else if (VALUE_SUPPLIER_LOCK_OPEN_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Suppliers lock opened"));
		} else if (VALUE_SUPPLIER_LOCK_OPEN_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Suppliers lock closed"));
		} else if (VALUE_CUSTOMER_LOCK_OPEN_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Customers lock opened"));
		} else if (VALUE_CUSTOMER_LOCK_OPEN_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Customers lock closed"));
		} else if (VALUE_CALL_TIME_WINDOW1_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 1 started"));
		} else if (VALUE_CALL_TIME_WINDOW1_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 1 ended"));
		} else if (VALUE_CALL_TIME_WINDOW2_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 2 started"));
		} else if (VALUE_CALL_TIME_WINDOW2_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 2 ended"));
		} else if (VALUE_RESTART_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Restart started"));
		} else if (VALUE_RESTART_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Restart ended"));
		} else if (VALUE_DATA_RESTORED_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Data restore started"));
		} else if (VALUE_DATA_RESTORED_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Data restore ended"));
		} else if (VALUE_HARDWARE_FAULT_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, convertStringEventIdToInteger(eventId), "Hardware fault started"));
		} else if (VALUE_HARDWARE_FAULT_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, convertStringEventIdToInteger(eventId), "Hardware fault ended"));
		} else if (VALUE_SOFTWARE_FAULT_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.PROGRAM_FLOW_ERROR, convertStringEventIdToInteger(eventId), "Software fault started"));
		} else if (VALUE_SOFTWARE_FAULT_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.PROGRAM_FLOW_ERROR, convertStringEventIdToInteger(eventId), "Software fault ended"));
		} else if (VALUE_SETTINGS_FAULT_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Settings fault started"));
		} else if (VALUE_SETTINGS_FAULT_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Settings fault ended"));
		} else if (VALUE_BATTERY_WARNING_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Battery warning started"));
		} else if (VALUE_BATTERY_WARNING_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Battery warning ended"));
		} else if (VALUE_CLOCK_NOT_SET_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Clock NOT set started"));
		} else if (VALUE_CLOCK_NOT_SET_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Clock NOT set ended"));
		} else if (VALUE_PTB_LOGBOOK_FULL_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "PTB logbook is full started"));
		} else if (VALUE_PTB_LOGBOOK_FULL_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "PTB logbook is full ended"));
		} else if (VALUE_DATA_TRANSMISSION_RUNNING_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Data transmission started"));
		} else if (VALUE_DATA_TRANSMISSION_RUNNING_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Data transmission ended"));
		} else if (VALUE_BATTERY_OPERATION_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Battery operation started"));
		} else if (VALUE_BATTERY_OPERATION_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Battery operation ended"));
		} else if (VALUE_SUMMER_TIME_START.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Summertime started"));
		} else if (VALUE_SUMMER_TIME_END.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Summertime ended"));
		} else if (VALUE_BACKUP_TIME_CHANGES.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Backup time changed"));
		} else if (VALUE_BACKUP_TIME_BECOMES_LARGER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Backup time became larger"));
		} else if (VALUE_CHANGE_OF_MEASUREMENT1_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Change of measurement period 1 BACKWARDS"));
		} else if (VALUE_END_OF_MEASUREMENT1.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "End of measurement period 1"));
		} else if (VALUE_CHANGE_OF_MEASUREMENT2_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Change of measurement period 2 BACKWARDS"));
		} else if (VALUE_END_OF_MEASUREMENT2.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "End of measurement period 2"));
		} else if (VALUE_CALL_TIME_WINDOW1_CHANGES.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 1 changes"));
		} else if (VALUE_CALL_TIME_WINDOW1_BECOMES_LARGER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 1 becomes larger"));
		} else if (VALUE_CALL_TIME_WINDOW2_CHANGES.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 2 changes"));
		} else if (VALUE_CALL_TIME_WINDOW2_BECOMES_LARGER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Call time window 2 becomes larger"));
		} else if (VALUE_MONITORING1_CHANGES.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Monitoring I1 changes"));
		} else if (VALUE_MONITORING1_BECOMES_LARGER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Monitoring I1 becomes larger"));
		} else if (VALUE_MONITORING2_CHANGES.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Monitoring I2 changes"));
		} else if (VALUE_MONITORING2_BECOMES_LARGER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Monitoring I2 becomes larger"));
		} else if (VALUE_MONTH_BOUNDARY1_CORRECTION_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Month boundary I1, correction backwards"));
		} else if (VALUE_MONTH_BOUNDARY1_EXPIRED.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Month boundary I1, month expired"));
		} else if (VALUE_MONTH_BOUNDARY2_CORRECTION_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Month boundary I2, correction backwards"));
		} else if (VALUE_MONTH_BOUNDARY2_EXPIRED.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Month boundary I2, month expired"));
		} else if (VALUE_DAY_BOUNDARY1_CHANGED_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Day boundary I1, changed backwards"));
		} else if (VALUE_DAY_BOUNDARY1_EXPIRED.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Day boundary I1, day expired"));
		} else if (VALUE_DAY_BOUNDARY2_CHANGED_BACKWARDS.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Day boundary I2, changed backwards"));
		} else if (VALUE_DAY_BOUNDARY2_EXPIRED.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Day boundary I2, day expired"));
		} else if (VALUE_LOGBOOK_CHANGED_AFTER.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "After value for logbook change"));
		} else if (VALUE_LOGBOOK_CHANGED_BEFORE.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Before value for logbook change"));
		} else if (VALUE_ARCHIVE1_SAVING_VALUE.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Measurement1 saving the values"));
		} else if (VALUE_ARCHIVE2_SAVING_VALUE.equalsIgnoreCase(eventId)) {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Measurement2 saving the values"));
		} else {
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), "Unknown event " + eventId));
		}
	}

	/**
	 * Convert the Hexadecimal eventId to an {@link Integer}
	 * 
	 * @param eventId
	 *            - the eventId as a String
	 * 
	 * @return the same eventId as an Integer
	 */
	protected static int convertStringEventIdToInteger(String eventId) {
		String hexValue = eventId.substring(eventId.indexOf(EVENT_PREFIX) + EVENT_PREFIX.length());
		return Integer.valueOf(hexValue, 16);
	}

	/**
	 * There is no specific clock event, only an indication that the record has been changed.
	 * 
	 * Check what type of measurement change event occurred. It can either be a value change or a clock change.
	 * @throws IOException 
	 */
	protected void checkMeasurementChangeEvents() throws IOException {
		if (this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getEndTime().compareTo(
				this.measurementChangeEvents.get(AFTER_EVENT_INDEX).getEndTime()) == 0) { // if you have the same date,
			// then normally the value
			// is changed
			eventList.add(new MeterEvent(this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getEndTime(),
					MeterEvent.OTHER, convertStringEventIdToInteger(this.measurementChangeEvents
							.get(BEFORE_EVENT_INDEX).getEvent()), "IntervalValue has been changed, this "
							+ this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getValue(0)
							+ " is the original value."));
			eventList.add(new MeterEvent(this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getEndTime(),
					MeterEvent.OTHER, convertStringEventIdToInteger(this.measurementChangeEvents
							.get(BEFORE_EVENT_INDEX).getEvent()), "IntervalValue has been changed, this "
							+ this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getValue(0) + " is the new value."));

		} else { // the dates are different so the clock must be adjusted
			eventList.add(new MeterEvent(this.measurementChangeEvents.get(BEFORE_EVENT_INDEX).getEndTime(),
					MeterEvent.SETCLOCK_BEFORE, convertStringEventIdToInteger(this.measurementChangeEvents.get(
							BEFORE_EVENT_INDEX).getEvent()), "Clock has changed, this is the original time."));
			eventList.add(new MeterEvent(this.measurementChangeEvents.get(AFTER_EVENT_INDEX).getEndTime(),
					MeterEvent.SETCLOCK_AFTER, convertStringEventIdToInteger(this.measurementChangeEvents.get(
							AFTER_EVENT_INDEX).getEvent()), "Clock has changed, this is the new time."));
		}
	}


	/**
	 * Getter for the eventList
	 * 
	 * @return the eventList
	 */
	public List<MeterEvent> getEventList() {
		return eventList;
	}

	/**
	 * Setter for the eventList
	 * 
	 * @param eventList 
	 * 				- the eventList to set
	 */
	protected void setEventList(List<MeterEvent> eventList) {
		this.eventList = eventList;
	}
	
}
