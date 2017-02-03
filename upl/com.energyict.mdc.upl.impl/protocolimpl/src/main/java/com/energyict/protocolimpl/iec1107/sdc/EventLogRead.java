/*
 * EventLogRead.java
 *
 * Created on 2 november 2004, 16:10
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class EventLogRead  extends AbstractDataReadingCommand {
    
    private static final int DEBUG=0;
    public final int NR_OF_LOG_ENTRIES = 10;
    List eventLogEntries = new ArrayList();
    
    /** Creates a new instance of EventLogRead */
    public EventLogRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }
    
    // KV 28072005
    private int parseInt(String str) {
        if (str.toLowerCase().indexOf("0x") >= 0)
            return Integer.parseInt(str.substring(2),16);
        else {
            try {
                return Integer.parseInt(str);
            }
            catch(NumberFormatException e) {
                return Integer.parseInt(str,16);
            }
        }
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        String strExpression = new String(data);
        String[] strEventLogEntries = strExpression.split("\r\n");
        if (DEBUG>=1) System.out.println(strExpression);
        
        //DataParser dp = new DataParser(TimeZoneManager.getTimeZone("GMT"));
        DataParser dp = new DataParser(getDataReadingCommandFactory().getSdc().getTimeZone());
        for (int i=(strEventLogEntries.length-1); i>=0; i--) {
            
            // get sequence nr
            String strId=dp.parseBetweenBrackets(strEventLogEntries[i],0);
            int id = strId.compareTo("") != 0 ? parseInt(strId):-1;
            
            // get event type
            String[] strInfos = dp.parseBetweenBrackets(strEventLogEntries[i],1).split(",");
            int type = strInfos[0].compareTo("") != 0 ? parseInt(strInfos[0]) : -1;
            
            // get infos
            int[] infos = new int[strInfos.length-1];
            for (int t=0;t<infos.length;t++) {

                // KV 28072005
                infos[t] = parseInt(strInfos[t+1]);
            }
            
            // get event timestamp
            String strDateTime = dp.parseBetweenBrackets(strEventLogEntries[i],2);
            Date date = strDateTime.compareTo("") != 0 ? dp.parseDateTime(strDateTime) : null;
            
            // construct EventLogEntry
            if (date != null) {
               EventLogEntry ele = new EventLogEntry(id,type,infos,date); 
               eventLogEntries.add(ele);
               if (DEBUG>=1) 
                   System.out.println("EventLogEntry: "+ele);
            }
           
        } // for (int pos=0; pos<(NR_OF_LOG_ENTRIES*3); pos+=3)
        
    } // public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException
    
    public EventLogEntry getEventLogLatestEntry() throws java.io.IOException {
        try {
           retrieve("ELR","1,1,0");
        }
        catch(ProtocolConnectionException e) {
            if (e.getProtocolErrorCode().compareTo(SdcBase.COMMAND_CANNOT_BE_EXECUTED) != 0)
                throw e;
           // if error ([4]) returned, means eventlog is empty! so, absorb
        }
        
        if (eventLogEntries.size() == 0)
           return null;
        else
           return (EventLogEntry)eventLogEntries.get(0);
    }
    
    public List getEventLogFrom(int seqNr) throws java.io.IOException {
        try {
           eventLogEntries.clear();
           retrieve("ELR",NR_OF_LOG_ENTRIES+",3,"+seqNr);
        }
        catch(ProtocolConnectionException e) {
            if (e.getProtocolErrorCode().compareTo(SdcBase.COMMAND_CANNOT_BE_EXECUTED) != 0)
                throw e;
           // if error ([4]) returned, means eventlog is empty! so, absorb
        }
        return eventLogEntries;
    }
    
}
