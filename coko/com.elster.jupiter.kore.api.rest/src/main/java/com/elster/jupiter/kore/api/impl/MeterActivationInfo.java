package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;


/**
 * Created by bvn on 5/13/16.
 */
public class MeterActivationInfo extends LinkInfo<Long> {
    public IntervalInfo interval;
    public Long meter;
    public LinkInfo usagePoint;
    public LinkInfo endDevice;
    public String meterRole;
}
