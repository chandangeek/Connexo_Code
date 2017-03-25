/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.time.Instant;

public class UsagePointTransitionInfo extends LinkInfo<Long> {
    public String name;
    public Instant effectiveTimestamp;
    public boolean transitionNow = true;
}
