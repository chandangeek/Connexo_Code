package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.rest.api.util.v1.RangeInfo;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.List;
import java.util.Optional;

/**
 * Helper class to find previous and next effective details for a UsagePoint
 * <p>
 * Created by bvn on 6/2/16.
 */
public class EffectivityHelper {

    Optional<UsagePointDetail> previousDetails(UsagePointDetail detail) {
        List<? extends UsagePointDetail> details = detail.getUsagePoint()
                .getDetail(Range.upTo(detail.getRange().lowerEndpoint(), BoundType.OPEN));
        if (!details.isEmpty()) {
            return Optional.of(details.get(details.size() - 1));
        }
        return Optional.empty();
    }

    Optional<UsagePointDetail> nextDetails(UsagePointDetail detail) {
        if (detail.getRange().hasUpperBound()) {
            List<? extends UsagePointDetail> details = detail.getUsagePoint()
                    .getDetail(Range.downTo(detail.getRange().upperEndpoint(), BoundType.CLOSED));
            if (!details.isEmpty()) {
                return Optional.of(details.get(0));
            }
        }
        return Optional.empty();
    }

    RangeInfo getEffectiveRange(Effectivity effectivity) {
        RangeInfo info = new RangeInfo();
        if (effectivity.getRange().hasLowerBound()) {
            info.lowerEnd = effectivity.getRange().lowerEndpoint();
        }
        if (effectivity.getRange().hasUpperBound()) {
            info.upperEnd = effectivity.getRange().upperEndpoint();
        }
        return info;
    }
}
