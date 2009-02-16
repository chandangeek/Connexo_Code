/*
 * Logbook.java
 *
 * Created on 17 november 2004, 9:12
 */

package com.energyict.protocolimpl.dlms.eictz3;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocol.MeterEvent;

/**
 *
 * @author  Koen
 */
public class Logbook {
    
    private static final int DEBUG = 0;
    
    TimeZone timeZone;

    
    /** Creates a new instance of Logbook */
    public Logbook(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    public List getMeterEvents(Array array) throws IOException {
        List meterEvents = new ArrayList(); // of type MeterEvent
        
        for (int i=0;i<array.nrOfDataTypes();i++) { // for all retrieved intervals
        	DateTime dateTime = new DateTime(array.getDataType(i).getStructure().getDataType(0).getOctetString().getBEREncodedByteArray(), 0, timeZone);
        	LogbookEntry logbookEntry = LogbookEntriesFactory.findLogbookEntry(array.getDataType(i).getStructure().getDataType(1).intValue());
        	if (DEBUG>=1) System.out.println(dateTime.getValue().getTime());
        	
        	meterEvents.add(new MeterEvent(dateTime.getValue().getTime(),logbookEntry.getEiCode(),logbookEntry.getNtaCode(),logbookEntry.getDescription()));
        }
        return meterEvents;
    }
    
} // public class Logbook
