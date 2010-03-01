/*
 * EventNumber.java
 *
 * Created on 18 oktober 2004, 14:41
 */

package com.energyict.protocolimpl.dlms.as220;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.MeterEvent;


/**
 *
 * @author  Koen
 */
public final class EventNumber {

    private static final int VOLTAGECUTLOGBOOK=1;
    private static final int COVEROGBOOK=2;

    private static final List<EventNumber> EVENTS = new ArrayList<EventNumber>();
    static {
        EVENTS.add(new EventNumber(51,"phase 1 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        EVENTS.add(new EventNumber(53,"phase 2 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        EVENTS.add(new EventNumber(55,"phase 3 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        EVENTS.add(new EventNumber(52,"phase 1 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        EVENTS.add(new EventNumber(54,"phase 2 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        EVENTS.add(new EventNumber(56,"phase 3 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        EVENTS.add(new EventNumber(21,"cover opened",COVEROGBOOK,MeterEvent.OTHER));
        EVENTS.add(new EventNumber(22,"cover closed",COVEROGBOOK,MeterEvent.OTHER));
    }

    private int type;
    private int id;
    private String idDescription;
    private int meterEventCode;

	/**
	 * Creates a new instance of {@link EventNumber}
	 *
	 * @param id
	 * @param idDescription
	 * @param type
	 * @param meterEventCode
	 */
	private EventNumber(int id, String idDescription, int type, int meterEventCode) {
		this.id = id;
		this.idDescription = idDescription;
		this.meterEventCode = meterEventCode;
		this.type = type;
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
			eventNumber = new EventNumber(id, "unknown event", -1, MeterEvent.OTHER);
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

	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

}
