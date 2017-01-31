/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public Status status;
    public Instant activationDate;
    public LoadLimit loadLimit;
    public Integer loadTolerance;
    public String callback;

    public class LoadLimit {
        public BigDecimal limit;
        public String unit;
    }
}
