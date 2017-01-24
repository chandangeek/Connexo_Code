/*
 * NonRecurringDate.java
 *
 * Created on 30 oktober 2005, 2:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class NonRecurringDate {

    private CalendarAction calendarAction;
    private Date nonRecurringDate;


    /** Creates a new instance of NonRecurringDate */
    public NonRecurringDate(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setNonRecurringDate(C12ParseUtils.getDateFromDate(data, offset, tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
        offset += C12ParseUtils.getDateSize();
        setCalendarAction(new CalendarAction(data, offset, tableFactory));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("NonRecurringDate: \n");
        strBuff.append("    calendarAction="+getCalendarAction()+"\n");
        strBuff.append("    nonRecurringDate="+getNonRecurringDate()+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return CalendarAction.getSize(tableFactory)+C12ParseUtils.getDateSize();
    }

    public CalendarAction getCalendarAction() {
        return calendarAction;
    }

    public void setCalendarAction(CalendarAction calendarAction) {
        this.calendarAction = calendarAction;
    }

    public Date getNonRecurringDate() {
        return nonRecurringDate;
    }

    public void setNonRecurringDate(Date nonRecurringDate) {
        this.nonRecurringDate = nonRecurringDate;
    }
}
