/*
 * ClockTable.java
 *
 * Created on 25 oktober 2005, 5:00
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
public class ClockTable extends AbstractTable {


    private TimeDateQualifier timeDateQualifier;
    private Date date;

    /** Creates a new instance of ClockTable */
    public ClockTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(52));
    }

    public String toString() {
        return "ClockTable: date="+getDate()+", timeDateQualifier="+getTimeDateQualifier();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            setDate(C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(tableData,0, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        else
            setDate(C12ParseUtils.getDateFromLTime(tableData,0, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));

        int offset = C12ParseUtils.getLTimeSize(getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat());
        setTimeDateQualifier(new TimeDateQualifier(tableData, offset, getTableFactory()));
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TimeDateQualifier getTimeDateQualifier() {
        return timeDateQualifier;
    }

    public void setTimeDateQualifier(TimeDateQualifier timeDateQualifier) {
        this.timeDateQualifier = timeDateQualifier;
    }


}
