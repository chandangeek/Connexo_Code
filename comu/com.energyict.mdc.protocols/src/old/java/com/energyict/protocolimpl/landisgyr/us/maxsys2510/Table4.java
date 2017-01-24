package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Table4 {

    List histRcd = new ArrayList();
    List meterEvents = new ArrayList();

    static Table4 parse( MaxSys maxSys, Assembly assembly ){
        Table4 t = new Table4();

        for( int i = 0; i < 100; i ++ )
            t.add(TypeHistRcd.parse(assembly, maxSys.getTimeZone()));

        return t;
    }

    void add( TypeHistRcd typeHistRcd ){
        histRcd.add( typeHistRcd );
        MeterEvent me = typeHistRcd.toMeterEvent();
        if( me != null )
        meterEvents.add( me );
    }

    List getHistRcd() {
        return histRcd;
    }

    List getMeterEvents( ){
    	// DEBUG, author PST, here a correction should be added on overlapping events (events that happen on the same moment)
    	// solution is to concat the strings or to shift the event 1 second (changed from ms to s, IGH), concat does not work since the codes can not be concatenated
    	this.meterEvents = checkOnOverlappingEvents(this.meterEvents);
        return meterEvents;
    }

    protected List checkOnOverlappingEvents(List meterEvents) {
    	Map eventsMap = new HashMap();
        int size = meterEvents.size();
	    for (int i = 0; i < size; i++) {
	    	MeterEvent event = (MeterEvent) meterEvents.get(i);
	    	Date time = event.getTime();
	    	MeterEvent eventInMap = (MeterEvent) eventsMap.get(time);
	    	while (eventInMap != null) {
	    		time.setTime(time.getTime() + 1000); // add one second
				eventInMap = (MeterEvent) eventsMap.get(time);
	    	}
	    	MeterEvent newMeterEvent=
	    		new MeterEvent(time, event.getEiCode(), event.getProtocolCode(),event.getMessage());
    		eventsMap.put(time, newMeterEvent);
	    }
	    Iterator it = eventsMap.values().iterator();
		List result = new ArrayList();
	    while (it.hasNext())
	        result.add((MeterEvent) it.next());
		return result;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Table4 [\n" );
        Iterator i = meterEvents.iterator();
        while( i.hasNext() ) {
            MeterEvent me = (MeterEvent)i.next();
            rslt.append( "\t" + me.getTime() + " " + me  + " " +  me.getMessage() + "\n" );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
}