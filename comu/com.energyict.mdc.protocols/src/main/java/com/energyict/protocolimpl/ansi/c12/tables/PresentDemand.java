/*
 * PresentDemand.java
 *
 * Created on 28 oktober 2005, 16:19
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
public class PresentDemand {

    Date timeRemaining;
    Number demandValue;

    /** Creates a new instance of PresentDemand */
    public PresentDemand(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        if (art.isTimeRemainingFlag()) {
            timeRemaining=C12ParseUtils.getDateFromTime(data, offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);
            offset+=C12ParseUtils.getTimeSize(cfgt.getTimeFormat());
        }
        demandValue = C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat2(),dataOrder);
        offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PresentDemand: \n");
        strBuff.append("    timeRemaining="+timeRemaining+"\n");
        strBuff.append("    demandValue="+demandValue+"\n");
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int size=0;
        if (art.isTimeRemainingFlag())
            size+=C12ParseUtils.getTimeSize(cfgt.getTimeFormat());
        size+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
        return size;
    }
}
