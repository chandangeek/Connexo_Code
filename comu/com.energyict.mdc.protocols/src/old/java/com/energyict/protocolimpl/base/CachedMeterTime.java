/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import java.util.Date;

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
