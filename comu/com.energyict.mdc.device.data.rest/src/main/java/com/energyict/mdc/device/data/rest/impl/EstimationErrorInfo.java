/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EstimationErrorInfo {
    public boolean success = false;
    public PagedInfoList ranges;

    public EstimationErrorInfo() {

    }

    public EstimationErrorInfo(List<IntervalInfo> ranges) {
        this.ranges = PagedInfoList.fromCompleteList("ranges", ranges, new QueryParameters() {
            @Override
            public Optional<Integer> getStart() {
                return Optional.of(0);
            }

            @Override
            public Optional<Integer> getLimit() {
                return Optional.of(10);
            }

            @Override
            public List<Order> getSortingColumns() {
                return null;
            }
        });
    }

    public static EstimationErrorInfo from(EstimationErrorException exception) {
        return new EstimationErrorInfo(exception.getRanges().asRanges().stream().map(IntervalInfo::from).collect(Collectors.toList()));
    }
}
