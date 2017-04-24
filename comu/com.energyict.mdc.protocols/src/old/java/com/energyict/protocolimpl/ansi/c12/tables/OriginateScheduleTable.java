/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * OriginateScheduleTable.java
 *
 * Created on 23 februari 2006, 14:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class OriginateScheduleTable extends AbstractTable {

    private Date sAnchorDate;
    private RecurringDateRecord[] recurringDateRecords;
    private NonRecurringDateRecord[] nonRecurringDateRecords;
    private EventsRecord[] eventsRecords;
    private WeeklyScheduleRecord[] weeklyScheduleRecords;

    /** Creates a new instance of OriginateScheduleTable */
    public OriginateScheduleTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(94));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OriginateScheduleTable:\n");
        strBuff.append("   SAnchorDate="+getSAnchorDate()+"\n");
        for (int i=0;i<getEventsRecords().length;i++)
            strBuff.append("   eventsRecords["+i+"]="+getEventsRecords()[i]+"\n");
        for (int i=0;i<getNonRecurringDateRecords().length;i++)
            strBuff.append("   nonRecurringDateRecords["+i+"]="+getNonRecurringDateRecords()[i]+"\n");
        for (int i=0;i<getRecurringDateRecords().length;i++)
            strBuff.append("   recurringDateRecords["+i+"]="+getRecurringDateRecords()[i]+"\n");
        for (int i=0;i<getWeeklyScheduleRecords().length;i++)
            strBuff.append("   weeklyScheduleRecords["+i+"]="+getWeeklyScheduleRecords()[i]+"\n");
        return strBuff.toString();
    }



    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualTelephoneTable att = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        int offset=0;

        if (att.getTelephoneRecord().getTelephoneFlagsBitfield().isSAnchorDateFlag()) {
            sAnchorDate = C12ParseUtils.getDateFromDate(tableData,offset,getTableFactory().getC12ProtocolLink().getTimeZone(),cfgt.getDataOrder());
            offset+=C12ParseUtils.getDateSize();
        }

        recurringDateRecords = new RecurringDateRecord[att.getTelephoneRecord().getNumberOfRecurringDates()];
        for (int i=0;i<getRecurringDateRecords().length;i++) {
            getRecurringDateRecords()[i] = new RecurringDateRecord(tableData,offset,getTableFactory());
            offset+=RecurringDateRecord.getSize(getTableFactory());
        }

        nonRecurringDateRecords = new NonRecurringDateRecord[att.getTelephoneRecord().getNumberOfNonRecurringDates()];
        for (int i=0;i<getNonRecurringDateRecords().length;i++) {
            getNonRecurringDateRecords()[i] = new NonRecurringDateRecord(tableData,offset,getTableFactory());
            offset+=NonRecurringDateRecord.getSize(getTableFactory());
        }

        eventsRecords = new EventsRecord[att.getTelephoneRecord().getNumberOfEvents()];
        for (int i=0;i<getEventsRecords().length;i++) {
            getEventsRecords()[i] = new EventsRecord(tableData,offset,getTableFactory());
            offset+=EventsRecord.getSize(getTableFactory());
        }

        weeklyScheduleRecords = new WeeklyScheduleRecord[att.getTelephoneRecord().getNumberOfWeeklySchedules()];
        for (int i=0;i<getWeeklyScheduleRecords().length;i++) {
            getWeeklyScheduleRecords()[i] = new WeeklyScheduleRecord(tableData,offset,getTableFactory());
            offset+=WeeklyScheduleRecord.getSize(getTableFactory());
        }


    }

    public Date getSAnchorDate() {
        return sAnchorDate;
    }

    public RecurringDateRecord[] getRecurringDateRecords() {
        return recurringDateRecords;
    }

    public NonRecurringDateRecord[] getNonRecurringDateRecords() {
        return nonRecurringDateRecords;
    }

    public EventsRecord[] getEventsRecords() {
        return eventsRecords;
    }

    public WeeklyScheduleRecord[] getWeeklyScheduleRecords() {
        return weeklyScheduleRecords;
    }
}
