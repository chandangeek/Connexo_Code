/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CalendarAction.java
 *
 * Created on 30 oktober 2005, 2:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CalendarAction {

    private int calendarAction; // 8 bit
    private int calendarControl; // bit 4..0
    private boolean demandResetFlag; // bit 5
    private boolean selfReadFlag; // bit6


    /** Creates a new instance of CalendarAction */
    public CalendarAction(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setCalendarAction(C12ParseUtils.getInt(data,offset));
        setCalendarControl(getCalendarAction() & 0x1F);
        setDemandResetFlag((getCalendarAction() & 0x20) == 0x20);
        setSelfReadFlag((getCalendarAction() & 0x40) == 0x40);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CalendarAction: calendarAction=0x"+Integer.toHexString(getCalendarAction())+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

    public int getCalendarAction() {
        return calendarAction;
    }

    public void setCalendarAction(int calendarAction) {
        this.calendarAction = calendarAction;
    }

    public int getCalendarControl() {
        return calendarControl;
    }

    public void setCalendarControl(int calendarControl) {
        this.calendarControl = calendarControl;
    }

    public boolean isDemandResetFlag() {
        return demandResetFlag;
    }

    public void setDemandResetFlag(boolean demandResetFlag) {
        this.demandResetFlag = demandResetFlag;
    }

    public boolean isSelfReadFlag() {
        return selfReadFlag;
    }

    public void setSelfReadFlag(boolean selfReadFlag) {
        this.selfReadFlag = selfReadFlag;
    }
}
