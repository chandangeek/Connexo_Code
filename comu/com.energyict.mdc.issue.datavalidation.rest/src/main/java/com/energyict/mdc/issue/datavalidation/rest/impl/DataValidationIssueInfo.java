package com.energyict.mdc.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataValidationIssueInfo<T extends DeviceInfo> extends IssueInfo<T, IssueDataValidation> {

    public List<NotEstimatedDataInfo> notEstimatedData = new ArrayList<>();

    public DataValidationIssueInfo(IssueDataValidation issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
    }

    public static class NotEstimatedDataInfo {

        public long channelId;

        public ReadingTypeInfo readingType;

        public List<NotEstimatedBlockInfo> notEstimatedBlocks;

    }

    public static class NotEstimatedBlockInfo {

        public Instant startTime;

        public Instant endTime;

        public long amountOfSuspects;

    }
}
