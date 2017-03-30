/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Coincidents.java
 *
 * Created on 27 oktober 2005, 17:09
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
public class Coincidents {

    private Number[] coincidentValues;

    /** Creates a new instance of Coincidents */
    public Coincidents(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setCoincidentValues(new Number[art.getNrOfOccur()]);
        for (int i=0;i<getCoincidentValues().length;i++) {
            getCoincidentValues()[i] = C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat2(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
        }
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Coincidents: \n");
        for (int i=0;i<getCoincidentValues().length;i++) {
            strBuff.append("    coincidentValues["+i+"]="+getCoincidentValues()[i]+"\n");
        }
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();

        return art.getNrOfOccur()*C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
    }

    public Number[] getCoincidentValues() {
        return coincidentValues;
    }

    public void setCoincidentValues(Number[] coincidentValues) {
        this.coincidentValues = coincidentValues;
    }
}
