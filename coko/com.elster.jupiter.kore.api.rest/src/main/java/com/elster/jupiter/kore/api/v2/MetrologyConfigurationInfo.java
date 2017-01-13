package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

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
