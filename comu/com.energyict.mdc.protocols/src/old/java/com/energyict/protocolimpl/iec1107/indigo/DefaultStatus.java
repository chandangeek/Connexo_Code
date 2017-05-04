/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DefaultStatus.java
 *
 * Created on 7 juli 2004, 12:16
 */

package com.energyict.protocolimpl.iec1107.indigo;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class DefaultStatus extends AbstractLogicalAddress {

    // partially implemented

    int totalNrOfResets;
    int todayNrOfResets;

    /** Creates a new instance of DefaultStatus */
    public DefaultStatus(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        return "DefaultStatus: totalResets="+getTotalNrOfResets()+", todayResets="+getTodayNrOfResets();
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        setTotalNrOfResets((int)data[48]&0xFF);
        setTodayNrOfResets((int)data[48]&0xFF);
    }

    /**
     * Getter for property totalNrOfResets.
     * @return Value of property totalNrOfResets.
     */
    public int getTotalNrOfResets() {
        return totalNrOfResets;
    }

    /**
     * Setter for property totalNrOfResets.
     * @param totalNrOfResets New value of property totalNrOfResets.
     */
    public void setTotalNrOfResets(int totalNrOfResets) {
        this.totalNrOfResets = totalNrOfResets;
    }

    /**
     * Getter for property todayNrOfResets.
     * @return Value of property todayNrOfResets.
     */
    public int getTodayNrOfResets() {
        return todayNrOfResets;
    }

    /**
     * Setter for property todayNrOfResets.
     * @param todayNrOfResets New value of property todayNrOfResets.
     */
    public void setTodayNrOfResets(int todayNrOfResets) {
        this.todayNrOfResets = todayNrOfResets;
    }

}
