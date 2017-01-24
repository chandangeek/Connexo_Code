/*
 * RecurringDate.java
 *
 * Created on 4 november 2005, 11:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RecurringDate {

    private CalendarAction calendarAction;
    private RDate recurringDate;

    /** Creates a new instance of RecurringDate */
    public RecurringDate(byte[] data,int offset,TableFactory tableFactory) throws IOException {
         setRecurringDate(new RDate(data, offset, tableFactory));
         offset+=RDate.getSize(tableFactory);
         setCalendarAction(new CalendarAction(data, offset, tableFactory));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RecurringDate: \n");
        strBuff.append("    calendarAction="+getCalendarAction()+"\n");
        strBuff.append("    recurringDate="+getRecurringDate()+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return CalendarAction.getSize(tableFactory)+RDate.getSize(tableFactory);
    }

    public CalendarAction getCalendarAction() {
        return calendarAction;
    }

    public void setCalendarAction(CalendarAction calendarAction) {
        this.calendarAction = calendarAction;
    }

    public RDate getRecurringDate() {
        return recurringDate;
    }

    public void setRecurringDate(RDate recurringDate) {
        this.recurringDate = recurringDate;
    }
}
