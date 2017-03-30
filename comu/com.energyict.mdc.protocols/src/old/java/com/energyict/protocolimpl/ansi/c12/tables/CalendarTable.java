/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CalendarTable.java
 *
 * Created on 30 oktober 2005, 2:49
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
public class CalendarTable extends AbstractTable {

    private Date anchorDate;
    private NonRecurringDate[] nonRecurringDates;
    private RecurringDate[] recurringDates;
    private TierSwitch[] tierSwitch;
    private int[][] dailyScheduleIdMatrix;


    /** Creates a new instance of CalendarTable */
    public CalendarTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(54));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CalendarTable: \n");
        strBuff.append("    anchorDate="+getAnchorDate()+"\n");
        for (int i=0;i<getNonRecurringDates().length;i++)
            strBuff.append("    nonRecurringDates["+i+"]="+getNonRecurringDates()[i]+"\n");
        for (int i=0;i<getRecurringDates().length;i++)
            strBuff.append("    recurringDates["+i+"]="+getRecurringDates()[i]+"\n");
        for (int i=0;i<getTierSwitch().length;i++)
            strBuff.append("    tierSwitch["+i+"]="+getTierSwitch()[i]+"\n");
        for (int i=0;i<getDailyScheduleIdMatrix().length;i++) {
            for (int t=0;t<getDailyScheduleIdMatrix()[i].length;t++) {
                strBuff.append("    dailyScheduleIdMatrix["+i+"]["+t+"]="+getDailyScheduleIdMatrix()[i][t]+"\n");
            }
        }

        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int offset=0;
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (atatt.getTimeTOU().isAnchorDateFlag()) {
            setAnchorDate(C12ParseUtils.getDateFromDate(tableData, offset, tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
            offset+=C12ParseUtils.getDateSize();
        }
        setNonRecurringDates(new NonRecurringDate[atatt.getTimeTOU().getNrOfNonRecurringDates()]);
        for (int i=0;i<getNonRecurringDates().length;i++) {
            getNonRecurringDates()[i] = new NonRecurringDate(tableData, offset, getTableFactory());
            offset+=NonRecurringDate.getSize(getTableFactory());
        }
        setRecurringDates(new RecurringDate[atatt.getTimeTOU().getNrOfRecurringDates()]);
        for (int i=0;i<getRecurringDates().length;i++) {
            getRecurringDates()[i] = new RecurringDate(tableData, offset, getTableFactory());
            offset+=RecurringDate.getSize(getTableFactory());
        }
        setTierSwitch(new TierSwitch[atatt.getTimeTOU().getNrOfTierSwitches()]);
        for (int i=0;i<getTierSwitch().length;i++) {
            getTierSwitch()[i] = new TierSwitch(tableData, offset, getTableFactory());
            offset+=TierSwitch.getSize(getTableFactory());
        }
        setDailyScheduleIdMatrix(new int[atatt.getTimeTOU().getNrOfSeasons()][atatt.getTimeTOU().getNrOfSpecialSchedules()+(atatt.getTimeTOU().isSeparateWeekdaysFlag()?7:3)]);
        for (int i=0;i<getDailyScheduleIdMatrix().length;i++) {
            for (int t=0;t<getDailyScheduleIdMatrix()[i].length;t++) {
                getDailyScheduleIdMatrix()[i][t]=C12ParseUtils.getInt(tableData,offset);
                offset++;
            }
        }
    }

    public Date getAnchorDate() {
        return anchorDate;
    }

    public void setAnchorDate(Date anchorDate) {
        this.anchorDate = anchorDate;
    }

    public NonRecurringDate[] getNonRecurringDates() {
        return nonRecurringDates;
    }

    public void setNonRecurringDates(NonRecurringDate[] nonRecurringDates) {
        this.nonRecurringDates = nonRecurringDates;
    }

    public RecurringDate[] getRecurringDates() {
        return recurringDates;
    }

    public void setRecurringDates(RecurringDate[] recurringDates) {
        this.recurringDates = recurringDates;
    }

    public TierSwitch[] getTierSwitch() {
        return tierSwitch;
    }

    public void setTierSwitch(TierSwitch[] tierSwitch) {
        this.tierSwitch = tierSwitch;
    }

    public int[][] getDailyScheduleIdMatrix() {
        return dailyScheduleIdMatrix;
    }

    public void setDailyScheduleIdMatrix(int[][] dailyScheduleIdMatrix) {
        this.dailyScheduleIdMatrix = dailyScheduleIdMatrix;
    }
}
