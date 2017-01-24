/*
 * DemandValues.java
 *
 * Created on 26 juni 2006, 10:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Koen
 */
public class DemandValues {

    private List<Interval> intervals;
    private Calendar cal;
    private int tariff;

    /** Creates a new instance of DemandValues */
    public DemandValues(Calendar cal,int tariff) {
        setIntervals(new ArrayList<Interval>());
        this.setCal(cal);
        this.setTariff(tariff);
    }

    public String toString() {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("DemandValues:\n");
        strBuff.append("    Date=").append(getCal().getTime()).append("\n");
        for (int i=0;i<getIntervals().size();i++) {
            Interval val = getIntervals().get(i);
            strBuff.append("    value[").append(i).append("]=").append(val).append("\n");
        }
        return strBuff.toString();
    }

    public void addValue(Interval interval) {
        getIntervals().add(interval);
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    private void setIntervals(List<Interval> intervals) {
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