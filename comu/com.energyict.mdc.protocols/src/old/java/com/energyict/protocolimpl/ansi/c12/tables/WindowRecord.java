/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Window.java
 *
 * Created on 23 februari 2006, 13:40
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
public class WindowRecord {

    private Date beginWindowTime;
    private Date windowDuration;
    private ControlBitfield windowDays; // bit 0: sunday, bit 1:monday, bit 2:tuesday, bit 3:wednesday, bit 4:thursday, bit 5:friday, bit 6:saturday


    /** Creates a new instance of Window */
    public WindowRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();

        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int timeFormat = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();

        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            beginWindowTime = C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(data,offset, timeFormat,tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);
        else
            beginWindowTime = C12ParseUtils.getDateFromSTime(data,offset, timeFormat,tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);


        offset+=C12ParseUtils.getSTimeSize(timeFormat);

        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            windowDuration = C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(data,offset, timeFormat,tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);
        else
            windowDuration = C12ParseUtils.getDateFromSTime(data,offset, timeFormat,tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);

        offset+=C12ParseUtils.getSTimeSize(timeFormat);
        windowDays = new ControlBitfield(data,offset,tableFactory);
        offset+=ControlBitfield.getSize(tableFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("WindowRecord:\n");
        strBuff.append("   beginWindowTime="+getBeginWindowTime()+"\n");
        strBuff.append("   windowDays="+getWindowDays()+"\n");
        strBuff.append("   windowDuration="+getWindowDuration()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        int timeFormat = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        int size=0;
        size+=C12ParseUtils.getSTimeSize(timeFormat);
        size+=C12ParseUtils.getSTimeSize(timeFormat);
        size++;
        return size;
    }

    public Date getBeginWindowTime() {
        return beginWindowTime;
    }

    public Date getWindowDuration() {
        return windowDuration;
    }

    public ControlBitfield getWindowDays() {
        return windowDays;
    }
}
