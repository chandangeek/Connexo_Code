/*
 * EventNumber.java
 * Created on 18 oktober 2004, 14:41
 */

package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Koen
 */
public final class EventNumber {

	private static final List<EventNumber>	EVENTS	= new ArrayList<EventNumber>();
	static {
		EVENTS.add(new EventNumber(1, "Power Down", MeterEvent.POWERDOWN));
		EVENTS.add(new EventNumber(2, "Power Up", MeterEvent.POWERUP));
		EVENTS.add(new EventNumber(3, "Daylight saving time enabled or disabled", MeterEvent.SETCLOCK));
		EVENTS.add(new EventNumber(4, "Clock adjusted (old date/time)", MeterEvent.SETCLOCK_BEFORE));
		EVENTS.add(new EventNumber(5, "Clock adjusted (new date/time)", MeterEvent.SETCLOCK_AFTER));
		EVENTS.add(new EventNumber(6, "Clock invalid", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(7, "Replace Battery", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(8, "Battery voltage low", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(9, "TOU activated", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(10, "Error register cleared", MeterEvent.CLEAR_DATA));
		EVENTS.add(new EventNumber(11, "Alarm register cleared", MeterEvent.CLEAR_DATA));
		EVENTS.add(new EventNumber(12, "Program memory error", MeterEvent.ROM_MEMORY_ERROR));
		EVENTS.add(new EventNumber(13, "RAM  error", MeterEvent.RAM_MEMORY_ERROR));
		EVENTS.add(new EventNumber(14, "NV memory error", MeterEvent.ROM_MEMORY_ERROR));
		EVENTS.add(new EventNumber(15, "Watchdog error", MeterEvent.WATCHDOGRESET));
		EVENTS.add(new EventNumber(16, "Measurement system error", MeterEvent.HARDWARE_ERROR));
		EVENTS.add(new EventNumber(17, "Firmware ready for activation", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(18, "Firmware activated", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(40, "Terminal cover removed", MeterEvent.TERMINAL_OPENED));
		EVENTS.add(new EventNumber(41, "Terminal cover closed", MeterEvent.TERMINAL_OPENED));
		EVENTS.add(new EventNumber(42, "Strong DC field detected", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(43, "No strong DC field anymore", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(44, "Meter cover removed", MeterEvent.COVER_OPENED));
		EVENTS.add(new EventNumber(45, "Meter cover closed", MeterEvent.COVER_OPENED));
		EVENTS.add(new EventNumber(46, "n times wrong password", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(60, "Manual disconnection", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(61, "Manual connection", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(62, "Remote disconnection", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(63, "Remote connection", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(64, "Local disconnection", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(65, "Limiter threshold exceeded", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(66, "Limiter threshold ok", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(67, "Limiter threshold changed", MeterEvent.OTHER));

		EVENTS.add(new EventNumber(100, "Communication error M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(101, "Communication ok M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(102, "Replace Battery M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(103, "Fraud attempt M-Bus channel 1", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(104, "Clock adjusted M-Bus channel 1", MeterEvent.SETCLOCK));
        EVENTS.add(new EventNumber(105, "Permanent Error M-Bus channel 1", MeterEvent.OTHER));

		EVENTS.add(new EventNumber(110, "Communication error M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(111, "Communication ok M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(112, "Replace Battery M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(113, "Fraud attempt M-Bus channel 2", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(114, "Clock adjusted M-Bus channel 2", MeterEvent.SETCLOCK));
        EVENTS.add(new EventNumber(115, "Permanent Error M-Bus channel 2", MeterEvent.OTHER));

		EVENTS.add(new EventNumber(120, "Communication error M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(121, "Communication ok M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(122, "Replace Battery M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(123, "Fraud attempt M-Bus channel 3", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(124, "Clock adjusted M-Bus channel 3", MeterEvent.SETCLOCK));
        EVENTS.add(new EventNumber(125, "Permanent Error M-Bus channel 3", MeterEvent.OTHER));

		EVENTS.add(new EventNumber(130, "Communication error M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(131, "Communication ok M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(132, "Replace Battery M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(133, "Fraud attempt M-Bus channel 4", MeterEvent.TAMPER));
		EVENTS.add(new EventNumber(134, "Clock adjusted M-Bus channel 4", MeterEvent.SETCLOCK));
        EVENTS.add(new EventNumber(135, "Permanent Error M-Bus channel 4", MeterEvent.OTHER));

		EVENTS.add(new EventNumber(160, "Manual disconnection M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(161, "Manual connection M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(162, "Remote disconnection M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(163, "Remote disconnection M-Bus channel 1", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(164, "Valve alarm M-Bus channel 1", MeterEvent.METER_ALARM));

		EVENTS.add(new EventNumber(170, "Manual disconnection M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(171, "Manual connection M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(172, "Remote disconnection M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(173, "Remote disconnection M-Bus channel 2", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(174, "Valve alarm M-Bus channel 2", MeterEvent.METER_ALARM));

		EVENTS.add(new EventNumber(180, "Manual disconnection M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(181, "Manual connection M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(182, "Remote disconnection M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(183, "Remote disconnection M-Bus channel 3", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(184, "Valve alarm M-Bus channel 3", MeterEvent.METER_ALARM));

		EVENTS.add(new EventNumber(190, "Manual disconnection M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(191, "Manual connection M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(192, "Remote disconnection M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(193, "Remote disconnection M-Bus channel 4", MeterEvent.OTHER));
		EVENTS.add(new EventNumber(194, "Valve alarm M-Bus channel 4", MeterEvent.METER_ALARM));

		EVENTS.add(new EventNumber(230, "Change of impulse constant", MeterEvent.CONFIGURATIONCHANGE));
		EVENTS.add(new EventNumber(231, "Meter cover removed", MeterEvent.COVER_OPENED));
		EVENTS.add(new EventNumber(232, "Parameter changed", MeterEvent.CONFIGURATIONCHANGE));
		EVENTS.add(new EventNumber(233, "Error conditions fnfe", MeterEvent.APPLICATION_ALERT_START));
		EVENTS.add(new EventNumber(234, "End of error conditions", MeterEvent.APPLICATION_ALERT_STOP));
		EVENTS.add(new EventNumber(235, "Demand reset", MeterEvent.BILLING_ACTION));
		EVENTS.add(new EventNumber(236, "Fatal device error", MeterEvent.FATAL_ERROR));
		EVENTS.add(new EventNumber(237, "Running reserve exhausted", MeterEvent.OTHER));


        EVENTS.add(new EventNumber(238, "Limiter threshold over L1", MeterEvent.OTHER));
        EVENTS.add(new EventNumber(239, "Limiter threshold under L1", MeterEvent.OTHER));
        EVENTS.add(new EventNumber(240, "Limiter threshold over L2", MeterEvent.OTHER));
        EVENTS.add(new EventNumber(241, "Limiter threshold under L2", MeterEvent.OTHER));
        EVENTS.add(new EventNumber(242, "Limiter threshold over L3", MeterEvent.OTHER));
        EVENTS.add(new EventNumber(243, "Limiter threshold under L3", MeterEvent.OTHER));

        EVENTS.add(new EventNumber(255, "Event log cleared", MeterEvent.CLEAR_DATA));

	}

	private int id;
	private String idDescription;
	private int	meterEventCode;

	/**
	 * Creates a new instance of {@link EventNumber}
	 *
	 * @param id
	 * @param idDescription
	 * @param meterEventCode
	 */
	private EventNumber(int id, String idDescription, int meterEventCode) {
		this.id = id;
		this.idDescription = idDescription;
		this.meterEventCode = meterEventCode;
	}

	/**
	 * @param id
	 * @return
	 */
	private static EventNumber getEventNumber(int id) {
		Iterator<EventNumber> it = EVENTS.iterator();
		while (it.hasNext()) {
			EventNumber en = it.next();
			if (en.getId() == id) {
				return en;
			}
		}
		return null;
	}

	/**
	 * @param id
	 * @param dateTime
	 * @return
	 */
	public static MeterEvent toMeterEvent(int id, Date dateTime) {
		EventNumber eventNumber = EventNumber.getEventNumber(id);
		if (eventNumber == null) {
			eventNumber = new EventNumber(id, "unknown event", MeterEvent.OTHER);
		}
		return new MeterEvent(dateTime, eventNumber.getMeterEventCode(), eventNumber.getId(), eventNumber.getIdDescription());
	}

	/**
	 * Getter for property id.
	 *
	 * @return Value of property id.
	 */
	private int getId() {
		return id;
	}

	/**
	 * Getter for property idDescription.
	 *
	 * @return Value of property idDescription.
	 */
	private java.lang.String getIdDescription() {
		return idDescription;
	}

	/**
	 * @return
	 */
	public int getMeterEventCode() {
		return meterEventCode;
	}

}
