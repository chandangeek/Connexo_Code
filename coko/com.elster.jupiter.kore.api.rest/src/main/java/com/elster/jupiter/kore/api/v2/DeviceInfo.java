/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class DeviceInfo extends LinkInfo<Long> {
    public long id;
    public String mRID;
    public String name;
    public LocationShortInfo location;
    public UsagePointShortInfo usagePoint;
}
