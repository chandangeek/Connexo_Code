/*
 * RVCommand.java
 *
 * Created on 11 augustus 2005, 16:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.cbo.Utils;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author koen
 */
public class RVCommand extends AbstractCommand {
    
    private static final CommandIdentification commandIdentification = new CommandIdentification("RV",true,false);
    
    private List eventLogs;
    
    /** Creates a new instance of RVCommand */
    public RVCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RVCommand:\n");
        for (int i=0;i<eventLogs.size();i++) {
            EventLog el = (EventLog)eventLogs.get(i);
            strBuff.append(el+"\n");
        }
        return strBuff.toString();
    }

    @Override
    protected void parse(String strData) throws IOException {
        throw new IOException(Utils.format("Command parsing without xmodem data is not supported for command '{0}'", new Object[]{getCommandName()}));
    }

    protected void parse(String strData, byte[] xmodemData) throws IOException {
        int eventLogCode;
        Date eventDate;
        int eventAverageMagnitude;
        long eventDuration;
        int durationUnit;
        int offset=1024; // first block only contains the eventlog size...
//        if ((length%16) != 0)
//            throw new IOException("RVCommand, parse(), data length is not divisable by 16, data.length="+length);
  
        BufferedReader br = new BufferedReader(new StringReader(strData));
        int logSize = Integer.parseInt(br.readLine())+1024;
        
        setEventLogs(new ArrayList());
        
        while(offset<logSize) {
            durationUnit = ProtocolUtils.getInt(xmodemData,3+offset,1);
            eventDuration = ParseUtils.getBCD2LongLE(xmodemData,4+offset,4);
            eventAverageMagnitude = ProtocolUtils.getInt(xmodemData,8+offset,1);
            Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getMarkV().getTimeZone());
            int year = ProtocolUtils.getInt(xmodemData,9+offset,1);
            cal.set(Calendar.YEAR,(year>=50)?year+1900:year+2000);
            cal.set(Calendar.MONTH,ProtocolUtils.getInt(xmodemData,10+offset,1)-1);
            cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(xmodemData,11+offset,1));
            cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(xmodemData,12+offset,1));
            cal.set(Calendar.MINUTE,ProtocolUtils.getInt(xmodemData,13+offset,1));
            cal.set(Calendar.SECOND,ProtocolUtils.getInt(xmodemData,14+offset,1));
            eventDate = cal.getTime();
            eventLogCode = ProtocolUtils.getInt(xmodemData,15+offset,1);

            getEventLogs().add(new EventLog(eventLogCode, eventDate, eventAverageMagnitude, eventDuration, durationUnit));
            offset+=16;
        }
        
        setEventLogs(eventLogs);
    } // protected void parse(String strData) throws IOException
    
    public void setNrOfRecords(int nrOfRecords) {
        commandIdentification.setArguments(new String[]{Integer.toString(nrOfRecords)});
    }
    
    public List getMeterEvents() {
        List meterEvents = new ArrayList();
        Iterator it = eventLogs.iterator();
        while(it.hasNext()) {
            EventLog eventLog = (EventLog)it.next();
            meterEvents.add(new MeterEvent(eventLog.getEventDate(),
                                           EventLogCodes.getEventLogMapping(eventLog.getEventLogCode()).getEiCode(),
                                           EventLogCodes.getEventLogMapping(eventLog.getEventLogCode()).getDescription()));
        }
        return meterEvents;
    }
    
    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public List getEventLogs() {
        return eventLogs;
    }

    public void setEventLogs(List eventLogs) {
        this.eventLogs = eventLogs;
    }

    @Override
    protected String getCommandName() {
        return "RV";
    }
}