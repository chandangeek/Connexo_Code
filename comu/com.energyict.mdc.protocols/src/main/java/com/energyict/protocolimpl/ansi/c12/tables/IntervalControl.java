/*
 * IntervalControl.java
 *
 * Created on 26 oktober 2005, 15:38
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
public class IntervalControl {

    static public final int SIZE=2;

    private int subInterval; // 8 bit Number of minutes in the subinterval
    private int intervalMultiplier; // 8 bit the multiplier by which the subInterval is multiplied
    private int intervalLength; // 16 bit Length of the demand interval in minutes
    boolean slidingDemand;

    /** Creates a new instance of IntervalControl */
    public IntervalControl(byte[] data, boolean slidingDemand, int dataOrder) throws IOException {
        this.slidingDemand=slidingDemand;
        if (slidingDemand) {
            setSubInterval(C12ParseUtils.getInt(data,0));
            setIntervalMultiplier(C12ParseUtils.getInt(data,1));
        }
        else { // the GE KV meter tests if block demand flag is set before proceeding. However, the standard does not include that test!
            setIntervalLength(C12ParseUtils.getInt(data,0,2,dataOrder));
        }
    }

    public String toString() {
        return "IntervalControl (slidingDemand="+slidingDemand+"): subInterval="+getSubInterval()+", intervalMultiplier="+getIntervalMultiplier()+", intervalLength="+getIntervalLength()+"\n";
    }

    public int getSubInterval() {
        return subInterval;
    }

    public void setSubInterval(int subInterval) {
        this.subInterval = subInterval;
    }

    public int getIntervalMultiplier() {
        return intervalMultiplier;
    }

    public void setIntervalMultiplier(int intervalMultiplier) {
        this.intervalMultiplier = intervalMultiplier;
    }

    public int getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(int intervalLength) {
        this.intervalLength = intervalLength;
    }

}
