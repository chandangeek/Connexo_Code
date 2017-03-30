/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataBlock.java
 *
 * Created on 28 oktober 2005, 9:39
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
public class DataBlock {

    private Number[] summations;
    private Demands[] demands;
    private Coincidents[] coincidents;

    /** Creates a new instance of DataBlock */
    public DataBlock(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();



        setSummations(new Number[art.getNrOfSummations()]);
        for(int i=0;i<getSummations().length;i++) {
            getSummations()[i] = C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat1(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        setDemands(new Demands[art.getNrOfDemands()]);
        for(int i=0;i<getDemands().length;i++) {
            getDemands()[i] = new Demands(data, offset, tableFactory);
            offset+=Demands.getSize(tableFactory);
        }
        setCoincidents(new Coincidents[art.getNrOfCoinValues()]);
        for(int i=0;i<getCoincidents().length;i++) {
            getCoincidents()[i] = new Coincidents(data, offset, tableFactory);
            offset+=Coincidents.getSize(tableFactory);
        }
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataBlock: \n");
        for(int i=0;i<getSummations().length;i++) {
            strBuff.append("    summations["+i+"]="+getSummations()[i]+"\n");
        }
        for(int i=0;i<getDemands().length;i++) {
            strBuff.append("    demands["+i+"]="+getDemands()[i]+"\n");
        }
        for(int i=0;i<getCoincidents().length;i++) {
            strBuff.append("    coincidents["+i+"]="+getCoincidents()[i]+"\n");
        }
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();

        return art.getNrOfSummations() * C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1())+
               art.getNrOfDemands() * Demands.getSize(tableFactory)+
               art.getNrOfCoinValues() * Coincidents.getSize(tableFactory);
    }

    public Number[] getSummations() {
        return summations;
    }

    public void setSummations(Number[] summations) {
        this.summations = summations;
    }

    public Demands[] getDemands() {
        return demands;
    }

    public void setDemands(Demands[] demands) {
        this.demands = demands;
    }

    public Coincidents[] getCoincidents() {
        return coincidents;
    }

    public void setCoincidents(Coincidents[] coincidents) {
        this.coincidents = coincidents;
    }

}
