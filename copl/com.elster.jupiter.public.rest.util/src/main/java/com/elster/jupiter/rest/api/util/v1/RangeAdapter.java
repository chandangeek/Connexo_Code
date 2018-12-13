/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

/**
 * This adapter can marshall a Range as used in effectivity, It will therefore always create a Closed-Open interval.
 * Created by bvn on 6/3/16.
 */
public class RangeAdapter extends XmlAdapter<Range<Instant>, RangeInfo> {

    @Override
    public RangeInfo unmarshal(Range<Instant> range) throws Exception {
        RangeInfo rangeInfo = new RangeInfo();
        if (range.hasUpperBound()) {
            rangeInfo.upperEnd = range.upperEndpoint();
        }
        if (range.hasLowerBound()) {
            rangeInfo.lowerEnd = range.lowerEndpoint();
        }
        return rangeInfo;
    }

    @Override
    public Range<Instant> marshal(RangeInfo info) throws Exception {
        if (info == null) {
            return null;
        }
        if (info.upperEnd != null && info.lowerEnd != null) {
            return Range.closedOpen(info.lowerEnd, info.upperEnd);
        } else if (info.upperEnd != null) {
            return Range.upTo(info.upperEnd, BoundType.OPEN);
        } else if (info.lowerEnd != null) {
            return Range.downTo(info.lowerEnd, BoundType.CLOSED);
        }
        return null;
    }
}
