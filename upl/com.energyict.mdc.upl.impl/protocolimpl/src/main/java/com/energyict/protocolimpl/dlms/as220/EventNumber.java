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
public class EventNumber {

    static final int MAINLOGBOOK=0;
    static final int VOLTAGECUTLOGBOOK=1;
    static final int COVEROGBOOK=2;

    static List<EventNumber> events = new ArrayList<EventNumber>();
    static {
        events.add(new EventNumber(51,"phase 1 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        events.add(new EventNumber(53,"phase 2 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        events.add(new EventNumber(55,"phase 3 fail",VOLTAGECUTLOGBOOK,MeterEvent.PHASE_FAILURE));
        events.add(new EventNumber(52,"phase 1 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        events.add(new EventNumber(54,"phase 2 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        events.add(new EventNumber(56,"phase 3 restore",VOLTAGECUTLOGBOOK,MeterEvent.OTHER));
        events.add(new EventNumber(21,"cover opened",COVEROGBOOK,MeterEvent.OTHER));
        events.add(new EventNumber(22,"cover closed",COVEROGBOOK,MeterEvent.OTHER));
    }



    private static final String[] strTypes={" (Alert)"," (Error)",""," (Error/Alert)"};

    int type;
    int id;
    String idDescription;
    int meterEventCode;

    /** Creates a new instance of EventLog */
    private EventNumber(int id, String idDescription, int type, int meterEventCode) {
        this.id=id;
        this.idDescription=idDescription;
        this.meterEventCode=meterEventCode;
        this.type=type;
    }

    static private EventNumber getEventNumber(int id) {
        Iterator<EventNumber> it = events.iterator();
        while(it.hasNext()) {
            EventNumber en = it.next();
            if (en.getId() == id) {
				return en;
			}
        }
        return null;
    }

    static public MeterEvent toMeterEvent(int id,Date dateTime) {
        EventNumber eventNumber = EventNumber.getEventNumber(id);
        if (eventNumber==null) {
			eventNumber = new EventNumber(id,"unknown event",-1,MeterEvent.OTHER);
		}
        return new MeterEvent(dateTime,eventNumber.getMeterEventCode(),eventNumber.getId(),eventNumber.getIdDescription());
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    private int getId() {
        return id;
    }

    private int getType() {
        return type;
    }

    private String getStrType() {
        return strTypes[getType()];
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    private void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for property idDescription.
     * @return Value of property idDescription.
     */
    private java.lang.String getIdDescription() {
        return idDescription;
    }

    /**
     * Setter for property idDescription.
     * @param idDescription New value of property idDescription.
     */
    private void setIdDescription(java.lang.String idDescription) {
        this.idDescription = idDescription;
    }



	public int getMeterEventCode() {
		return meterEventCode;
	}

}
