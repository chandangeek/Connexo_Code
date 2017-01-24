/*
 * Demands.java
 *
 * Created on 27 oktober 2005, 17:02
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
public class Demands {

    private Date[] eventTimes;
    private Number cumDemand;
    private Number continueCumDemand;
    private Number[] demands;


    /** Creates a new instance of Demands */
    public Demands(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (art.isDateTimeFieldFlag()) {
            setEventTimes(new Date[art.getNrOfOccur()]);
            for (int i=0;i<getEventTimes().length;i++) {


                if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
                    getEventTimes()[i] = C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);
                else
                    getEventTimes()[i] = C12ParseUtils.getDateFromSTime(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder);
                offset+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());
            }
        }
        if (art.isCumulativeDemandFlag()) {
            setCumDemand(C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat1(),dataOrder));
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        if (art.isContinueCumulativeDemandFlag()) {
            setContinueCumDemand(C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat1(),dataOrder));
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        setDemands(new Number[art.getNrOfOccur()]);
        for (int i=0;i<getDemands().length;i++) {
            getDemands()[i] = C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat2(),dataOrder);
            offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2());
        }

    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Demands (min/max): \n");
        if (getEventTimes() != null) {
            for (int i=0;i<getEventTimes().length;i++) {
                strBuff.append("    eventTimes["+i+"]="+getEventTimes()[i]+"\n");
            }
        }
        strBuff.append("    cumDemand="+getCumDemand()+"\n");
        strBuff.append("    continueCumDemand="+getContinueCumDemand()+"\n");
        for (int i=0;i<getDemands().length;i++) {
            strBuff.append("    maxDemandsValue["+i+"]="+getDemands()[i]+"\n");
        }

        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int size=0;
        if (art.isDateTimeFieldFlag()) {
            size+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat())*art.getNrOfOccur();
        }
        if (art.isCumulativeDemandFlag()) {
            size+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        if (art.isContinueCumulativeDemandFlag()) {
            size+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        }
        size+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat2())*art.getNrOfOccur();
        return size;
    }

    public Date[] getEventTimes() {
        return eventTimes;
    }

    public void setEventTimes(Date[] eventTimes) {
        this.eventTimes = eventTimes;
    }

    public Number getCumDemand() {
        return cumDemand;
    }

    public void setCumDemand(Number cumDemand) {
        this.cumDemand = cumDemand;
    }

    public Number getContinueCumDemand() {
        return continueCumDemand;
    }

    public void setContinueCumDemand(Number continueCumDemand) {
        this.continueCumDemand = continueCumDemand;
    }

    public Number[] getDemands() {
        return demands;
    }

    public void setDemands(Number[] demands) {
        this.demands = demands;
    }
}
