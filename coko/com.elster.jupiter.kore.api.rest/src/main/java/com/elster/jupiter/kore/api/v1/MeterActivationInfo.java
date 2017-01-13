package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.rest.api.util.v1.IntervalInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

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
