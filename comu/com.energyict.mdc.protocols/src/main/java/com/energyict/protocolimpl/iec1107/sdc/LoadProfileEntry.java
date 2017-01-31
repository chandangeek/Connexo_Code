/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileEntry.java
 *
 * Created on 28 oktober 2004, 17:02
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.interval.IntervalStateBits;
/**
 *
 * @author  Koen
 */
public class LoadProfileEntry {
    private static final int STATUS_NORMAL=0;
    private static final int STATUS_TIMESET=1;
    private static final int STATUS_OTHER_ERROR=2;
    private static final int STATUS_POWER_FAILURE=4;

    Quantity quantity=null;
    int status=0;

    /** Creates a new instance of LoadProfileEntry */
    public LoadProfileEntry() {
        this(null,-1);
    }
    public LoadProfileEntry(Quantity quantity,int status) {
        this.quantity=quantity;
        this.status=status;
    }

    public boolean isMissing() {
        return (quantity==null) && (status==-1);
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus() {
        return status;
    }

    public String toString() {
        return "status="+getStatus()+", quantity="+getQuantity();
    }

    public int getEiStatus() {
        switch(getStatus()) {

            case STATUS_NORMAL:
               return 0;

            case STATUS_OTHER_ERROR:
               return IntervalStateBits.OTHER;

            case STATUS_POWER_FAILURE:
               return IntervalStateBits.POWERDOWN|IntervalStateBits.POWERUP;

            case STATUS_TIMESET:
               return IntervalStateBits.SHORTLONG;

            default:
               return IntervalStateBits.OTHER;
        }
    }
}
