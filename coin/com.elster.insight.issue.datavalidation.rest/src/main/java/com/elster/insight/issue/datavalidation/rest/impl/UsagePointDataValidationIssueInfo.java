/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
//TODO UsagePointInfo
public class UsagePointDataValidationIssueInfo<T extends DeviceInfo> extends IssueInfo<T, UsagePointIssueDataValidation> {

    public List<NotEstimatedDataInfo> notEstimatedData = new ArrayList<>();

    public UsagePointDataValidationIssueInfo(UsagePointIssueDataValidation issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
    }

    public static class NotEstimatedDataInfo {

        public Long channelId, registerId;//info will contain only one of them

        public ReadingTypeInfo readingType;

        public List<NotEstimatedBlockInfo> notEstimatedBlocks;

    }

    public static class NotEstimatedBlockInfo {

        public Instant startTime;

        public Instant endTime;

        public long amountOfSuspects;

    }
}
