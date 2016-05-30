package com.energyict.mdc.multisense.api.impl;

import java.time.Instant;

/**
 * Created by bvn on 4/15/16.
 */
public class MetrologyConfigurationInfo extends LinkInfo<Long> {
    public String name;
    public Boolean active;
    public String userName;

    public Instant createTime;
    public Instant modTime;
}
