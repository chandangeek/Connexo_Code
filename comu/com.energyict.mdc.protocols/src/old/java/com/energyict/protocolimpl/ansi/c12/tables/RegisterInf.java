/*
 * RegisterInfo.java
 *
 * Created on 28 oktober 2005, 15:26
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
public class RegisterInf {

    private Date endDateTime;
    private int season; // 8 bit

    /** Creates a new instance of RegisterInfo */
    public RegisterInf(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (art.isDateTimeFieldFlag()) {
            if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
                setEndDateTime(C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
            else
                setEndDateTime(C12ParseUtils.getDateFromSTime(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
            offset+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());
        }

        if (art.isSeasonInfoFieldFlag()) {
            setSeason(C12ParseUtils.getInt(data,offset));
            offset++;
        }
    }

    public String toString() {
       StringBuffer strBuff = new StringBuffer();
       strBuff.append("RegisterInfo: endDateTime="+getEndDateTime()+", season="+getSeason()+"\n");

       return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        int size=0;
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        if (art.isDateTimeFieldFlag())
            size+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());
        if (art.isSeasonInfoFieldFlag())
            size++;
        return size;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

}
