package com.energyict.protocolimpl.base;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 5-jan-2011
 * Time: 15:00:03
 */
public class CachedMeterTime {

    private long meterTime;
    private long readTime;

    public CachedMeterTime(Date meterTime) {
        this.meterTime = meterTime.getTime();
        this.readTime = System.currentTimeMillis();
    }

    public Date getTime() {
        return new Date(meterTime + (System.currentTimeMillis() - readTime));
    }

}
