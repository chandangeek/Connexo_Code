/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.IntervalInfo;


/**
 * Created by bvn on 5/13/16.
 */
public class MeterActivationInfo extends LinkInfo<Long> {
    public IntervalInfo interval;
    public String meter;
    public LinkInfo usagePoint;
    public LinkInfo endDevice;
    public String meterRole;
}
