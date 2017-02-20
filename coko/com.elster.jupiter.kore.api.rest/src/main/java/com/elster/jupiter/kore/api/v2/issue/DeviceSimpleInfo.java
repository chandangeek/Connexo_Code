/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.kore.api.v2.SerializedLocationInfo;
import com.elster.jupiter.kore.api.v2.issue.UsagePointShortInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class DeviceSimpleInfo extends LinkInfo<Long> {
    public long id;
    public String mRID;
    public String name;
    public SerializedLocationInfo location;
    public UsagePointShortInfo usagePoint;
}
