package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 4/15/16.
 */
public class MetrologyConfigurationInfo extends LinkInfo<Long> {
    public String name;
    public Boolean active;
    public String userName;

    public Instant createTime;
    public Instant modTime;

    public List<String> meterRoles;
}
