/*
 * Interval.java
 *
 * Created on 27 juni 2006, 9:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class Interval {


    private BigDecimal value;
    private int eiStatus;

    /** Creates a new instance of Interval */
    public Interval(int val) {
        this(val, 0);
    }
    public Interval(int val, int eiStatus) {
        setValue(new BigDecimal(""+val));
        this.setEiStatus(eiStatus);
    }

    public String toString() {
        return "Interval: value="+value+", eiStatus="+eiStatus;
    }

    public BigDecimal getValue() {
        return value;
    }

    private void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getEiStatus() {
        return eiStatus;
    }

    private void setEiStatus(int eiStatus) {
        this.eiStatus = eiStatus;
    }

}
