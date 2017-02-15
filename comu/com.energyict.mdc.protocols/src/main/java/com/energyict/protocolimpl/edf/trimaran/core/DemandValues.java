/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DemandValues.java
 *
 * Created on 26 juni 2006, 10:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Koen
 */
public class DemandValues {

    private List intervals;
    private Calendar cal;
    private int tariff;

    /** Creates a new instance of DemandValues */
    public DemandValues(Calendar cal,int tariff) {
        setIntervals(new ArrayList());
        this.setCal(cal);
        this.setTariff(tariff);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandValues:\n");
        strBuff.append("    Date="+getCal().getTime()+"\n");
        for (int i=0;i<getIntervals().size();i++) {
            Interval val = (Interval)getIntervals().get(i);
            strBuff.append("    value["+i+"]="+val+"\n");
        }
        return strBuff.toString();
    }

    public void addValue(Interval interval) {
        getIntervals().add(interval);
    }

    public List getIntervals() {
        return intervals;
    }

    private void setIntervals(List intervals) {
        this.intervals = intervals;
    }

    public Calendar getCal() {
        return cal;
    }

    private void setCal(Calendar cal) {
        this.cal = cal;
    }

    public int getTariff() {
        return tariff;
    }

    public void setTariff(int tariff) {
        this.tariff = tariff;
    }
}
