/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;

import com.google.common.collect.Range;

import java.time.Instant;

public class EndDeviceEventRecordFilterSpecification {

    public long logBookId = -1L;

    public Range<Instant> range = Range.all();

    public EndDeviceDomain domain = null;

    public EndDeviceSubDomain subDomain = null;

    public EndDeviceEventOrAction eventOrAction = null;

}
