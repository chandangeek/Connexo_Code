package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.IntervalInfo;

/**
 * Created by bvn on 5/13/16.
 */
public class MeterActivationInfo extends LinkInfo<Long> {
    public IntervalInfo interval;
    public Long meter;
    public LinkInfo usagePoint;
}
