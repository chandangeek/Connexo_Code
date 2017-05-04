/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

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
        StringBuilder builder = new StringBuilder();
        builder.append("RVCommand:\n");
        for (int i=0;i<eventLogs.size();i++) {
            EventLog el = (EventLog)eventLogs.get(i);
            builder.append(el).append("\n");
        }
        return builder.toString();
    }

    protected void parse(String strData) throws IOException {

        byte[] data = strData.getBytes();

        int eventLogCode;
        Date eventDate;
        int eventAverageMagnitude;
        long eventDuration;
        int durationUnit;
        int length = data.length;
        int offset=1024; // first block only contains the eventlog size...
//        if ((length%16) != 0)
//            throw new IOException("RVCommand, parse(), data length is not divisable by 16, data.length="+length);

        BufferedReader br = new BufferedReader(new StringReader(strData));
        int logSize = Integer.parseInt(br.readLine())+1024;

        setEventLogs(new ArrayList());

        while(offset<logSize) {
            durationUnit = ProtocolUtils.getInt(data,3+offset,1);
            eventDuration = ParseUtils.getBCD2LongLE(data,4+offset,4);
            eventAverageMagnitude = ProtocolUtils.getInt(data,8+offset,1);
            Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getMarkV().getTimeZone());
            int year = ProtocolUtils.getInt(data,9+offset,1);
            cal.set(Calendar.YEAR,(year>=50)?year+1900:year+2000);
            cal.set(Calendar.MONTH,ProtocolUtils.getInt(data,10+offset,1)-1);
            cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,11+offset,1));
            cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,12+offset,1));
            cal.set(Calendar.MINUTE,ProtocolUtils.getInt(data,13+offset,1));
            cal.set(Calendar.SECOND,ProtocolUtils.getInt(data,14+offset,1));
            eventDate = cal.getTime();
            eventLogCode = ProtocolUtils.getInt(data,15+offset,1);

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

}