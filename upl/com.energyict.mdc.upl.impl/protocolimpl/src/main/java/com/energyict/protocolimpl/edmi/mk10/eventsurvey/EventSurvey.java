/*
 * EventSurvey.java
 *
 * Created on 31 maart 2006, 14:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.eventsurvey;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.mk10.core.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.protocolimpl.edmi.mk10.command.*;
/**
 *
 * @author koen
 */
public class EventSurvey {
    
    private static final int BASE_REGISTER_ID = 0xD810;

    private CommandFactory commandFactory;
    private int registerId = BASE_REGISTER_ID;
    private LinkedHashSet[] eventset = new LinkedHashSet[5];
    
    /** Creates a new instance of EventSurvey */
    public EventSurvey(CommandFactory commandFactory) throws IOException {
        this.setCommandFactory(commandFactory);
        registerId = BASE_REGISTER_ID;
        init();
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventSurvey:\n");
        
        return strBuff.toString();
    }
    
    private void init() throws IOException {
    	for (int lognr = 0; lognr <  5; lognr++) {
    		FileAccessReadCommand farc;
    		eventset[lognr] = new LinkedHashSet();
        	eventset[lognr].clear();
    		
        	long firstentry = getCommandFactory().getReadCommand(registerId + 0x0005 + lognr).getRegister().getBigDecimal().longValue();
        	long lastentry = getCommandFactory().getReadCommand(registerId + 0x000A + lognr).getRegister().getBigDecimal().longValue();
        	
        	do {
        		farc = this.getCommandFactory().getFileAccessReadCommand(lognr + 2, firstentry, 0xFFFF);
        		firstentry = farc.getStartRecord() + farc.getNumberOfRecords(); 
        		eventset[lognr].addAll(getEventData(farc.getData(), lognr));
        	} while (firstentry < lastentry);
        }
    }
    
    private Set getEventData(byte[] data_in, int eventlognr) throws IOException {
    	Set set = new HashSet();
		TimeZone tz = this.commandFactory.getMk10().getTimeZone();
        Calendar cal = ProtocolUtils.getCleanCalendar(tz);
    	set.clear();
    	
    	int ptr = 0;
		while (ptr<data_in.length){
    		byte[] eventcodebytes = ProtocolUtils.getSubArray2(data_in, ptr, 2);
			int eventcode = ProtocolUtils.getIntLE(eventcodebytes, 0, 2);
    		int eventtime = ProtocolUtils.getIntLE(data_in, ptr + 2, 4);
    		Date eventdate = DateTimeBuilder.getDateFromSecondsSince1996(tz, eventtime);
    		
    		Event event = new Event(eventdate, eventcode, eventlognr);
    		ptr += 6;

    		// Filter the user logon/logoff events to prevent unused events
    		// Every time the mk10 protocol connects, it generates at least two events
    		// in the log (Logon and logoff)

    		switch (eventcode & 0xFFE0) {
    			case 0x2000: break;
				case 0x2080: break;
				default: set.add(event); break;
			}
    	}
    	return set;
    }
    
    public LinkedHashSet readFile(Date from) throws IOException {
    	LinkedHashSet set = new LinkedHashSet();
    	for (int lognr = 0; lognr <  5; lognr++) {
        	Iterator it = this.eventset[lognr].iterator();
    		while(it.hasNext()) {
    			Event event = (Event) it.next();
        		if (!from.after(event.getEventDate())) {
        			set.add(event);
        		}
    		}
    	}
        return set;
    }
    
	public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }
    
}
