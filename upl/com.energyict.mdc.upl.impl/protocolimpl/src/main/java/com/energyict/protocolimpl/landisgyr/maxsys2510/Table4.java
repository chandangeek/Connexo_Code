package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.MeterEvent;

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
    	// solution is to concat the strings or to shift the event 1 ms, concat does not work since the codes can not be concatenated
    	this.meterEvents=checkOnOverlappingEvents(this.meterEvents);
        return meterEvents;
    }
    
    private List checkOnOverlappingEvents(List me) { // author PST
    	
    	List tempList= new ArrayList();
    	MeterEvent currentMeterEvent, nextMeterEvent, newMeterEvent;
    	int length=me.size()-1;
    	if(length>0){
    		for(int i=0; i<length-1; i++){
    			int inc=1;
    		
    			currentMeterEvent=(MeterEvent) me.get(i);
    			newMeterEvent=currentMeterEvent;
    			nextMeterEvent=(MeterEvent) me.get(i+1);
    		   		
    			if(currentMeterEvent.getTime().getTime()==nextMeterEvent.getTime().getTime()){
    				// overlapping data, add string to the next event
    				//	new MeterEvent(evntDate, eiCode, evntCode, description );
    				Calendar newTime=null;
    				newTime.setTimeInMillis(currentMeterEvent.getTime().getTime()+inc); // add one millisecond
    				inc++;
    				newMeterEvent= new MeterEvent(newTime.getTime(), currentMeterEvent.getEiCode(), currentMeterEvent.getProtocolCode(),currentMeterEvent.getMessage());
    			}else{
    				inc=1;
    				newMeterEvent=currentMeterEvent;    			
    			}
        		tempList.add(newMeterEvent);
    		}    		
    	}
    	if(length>=0){    	
    		tempList.add(me.get(length));// add last event
    	}
		return tempList;
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
