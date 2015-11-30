/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class Window {


    /*
    Define the originate windows. The A3 supports 2 windows per port. A window
    defines the time periods when a call is allowed. One entry can be used to
    specify a window that wraps around midnight. For example, if there are 2 call
    windows, one from 10 PM - 2AM and one from 10AM - 2 PM, the windows
    would be entered as:
    WINDOWS[0].BEGIN_WINDOW_TIME = 22 00
    WINDOWS[0].DURATION = 04 00
    WINDOWS[1].BEGIN_WINDOW_TIME = 10 00
    WINDOWS[1].DURATION = 04 00
    */
    private Date beginWindowTime; // 2 bytes The time of day the window starts (STIME). Byte 1 = binary hours (0-23), byte 2= binary minutes (0-59).
    private int windowDuration; // 2 bytes Size of the window, HH MM (binary hours followed by binary minutes.)
    private int windowDay; // 1 byte This field allows the window to be defined for specific days (Sunday - Saturday). The A3 will not support specific limits and sets this field to 0x7F..



    /** Creates a new instance of SourceDefinitionEntry */
    public Window(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int timeFormat = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        setBeginWindowTime(C12ParseUtils.getDateFromSTime(data,offset, timeFormat,tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
        offset+=C12ParseUtils.getSTimeSize(timeFormat);
        setWindowDuration(C12ParseUtils.getInt(data,offset,2, dataOrder)); offset+=2;
        setWindowDay(C12ParseUtils.getInt(data,offset++));
    }

    public String toString() {
        return "Window:\n" +
                "   beginWindowTime=" + getBeginWindowTime() + "\n" +
                "   windowDay=" + getWindowDay() + "\n" +
                "   windowDuration=" + getWindowDuration() + "\n";
    }
    public static int getSize(TableFactory tableFactory) {
        return 5;
    }

    public int getWindowDuration() {
        return windowDuration;
    }

    public void setWindowDuration(int windowDuration) {
        this.windowDuration = windowDuration;
    }

    public int getWindowDay() {
        return windowDay;
    }

    public void setWindowDay(int windowDay) {
        this.windowDay = windowDay;
    }

    public Date getBeginWindowTime() {
        return beginWindowTime;
    }

    public void setBeginWindowTime(Date beginWindowTime) {
        this.beginWindowTime = beginWindowTime;
    }



}
