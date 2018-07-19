/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "usagepoint.issue.data.validation.info.factory", service = {InfoFactory.class}, immediate = true)
public class UsagePointDataValidationIssueInfoFactory implements InfoFactory<UsagePointIssueDataValidation> {

    private ReadingTypeInfoFactory readingTypeInfoFactory;

    private volatile Thesaurus thesaurus;


    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointIssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN));
        this.readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
    }

    public UsagePointDataValidationIssueInfoFactory() {
    }

    @Inject
    public UsagePointDataValidationIssueInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public UsagePointDataValidationIssueInfo asInfo(UsagePointIssueDataValidation issue, Class<? extends DeviceInfo> deviceInfoClass) {
        UsagePointDataValidationIssueInfo issueInfo = asShortInfo(issue, deviceInfoClass);
        if (issue.getUsagePoint() == null) {
            return issueInfo;
        }
        Optional<UsagePoint> usagePoint = issue.getUsagePoint();
        if (usagePoint.isPresent()) {
            Map<ReadingType, List<UsagePointNotEstimatedBlock>> notEstimatedData = issue.getNotEstimatedBlocks().stream()
                    .collect(Collectors.groupingBy(UsagePointNotEstimatedBlock::getReadingType));
            for (Map.Entry<ReadingType, List<UsagePointNotEstimatedBlock>> entry : notEstimatedData.entrySet()) {
                Optional<Channel> channel = findChannel(usagePoint.get(), entry.getKey());
                if (channel.isPresent()) {
                    UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = createNotEstimatedDataInfoOfChannel(entry.getKey(), entry.getValue(), channel.get());
                    issueInfo.notEstimatedData.add(info);
                } else {
                   /* findRegister(usagePoint.get(), entry.getKey()).ifPresent(register -> {
                        UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = createNotEstimatedDataInfoOfRegister(entry.getKey(), entry.getValue(), register);
                        issueInfo.notEstimatedData.add(info);
                    });*/
                }
            }
        }
        Collections.<UsagePointDataValidationIssueInfo.NotEstimatedDataInfo>sort(issueInfo.notEstimatedData,
                (info1, info2) -> info1.readingType.aliasName.compareTo(info2.readingType.aliasName));
        return issueInfo;
    }

    private UsagePointDataValidationIssueInfo.NotEstimatedDataInfo createNotEstimatedDataInfoOfChannel(ReadingType readingType, List<UsagePointNotEstimatedBlock> blocks, Channel channel) {
        UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = new UsagePointDataValidationIssueInfo.NotEstimatedDataInfo();
        info.channelId = channel.getId();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.notEstimatedBlocks = blocks.stream().map(block -> {
            UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo blockInfo = new UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo();
            blockInfo.startTime = block.getStartTime();
            blockInfo.endTime = block.getEndTime();
            blockInfo.amountOfSuspects = ChronoUnit.MILLIS.between(block.getStartTime(), block.getEndTime()) / channel.getIntervalLength().get().get(ChronoUnit.MILLIS);
            return blockInfo;
        }).sorted((block1, block2) -> block1.startTime.compareTo(block2.startTime)).collect(Collectors.toList());
        return info;
    }

    private UsagePointDataValidationIssueInfo.NotEstimatedDataInfo createNotEstimatedDataInfoOfRegister(ReadingType readingType, List<UsagePointNotEstimatedBlock> blocks) {
        //, Register register) {
        UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = new UsagePointDataValidationIssueInfo.NotEstimatedDataInfo();
        // info.registerId = register.getRegisterSpecId();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.notEstimatedBlocks = blocks.stream().map(block -> {
            UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo blockInfo = new UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo();
            List<BaseReadingRecord> readings = block.getChannel().getReadings(Range.openClosed(block.getStartTime(), block.getEndTime()));
            if (!readings.isEmpty()) {
                blockInfo.startTime = readings.get(0).getTimeStamp();
                blockInfo.endTime = readings.get(readings.size() - 1).getTimeStamp();
                blockInfo.amountOfSuspects = readings.size();
            }
            return blockInfo;
        }).sorted((block1, block2) -> block1.startTime.compareTo(block2.startTime)).collect(Collectors.toList());
        return info;
    }

    public List<UsagePointDataValidationIssueInfo> asInfo(List<? extends UsagePointIssueDataValidation> issues) {
        return issues.stream().map(issue -> this.asShortInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }

    private UsagePointDataValidationIssueInfo asShortInfo(UsagePointIssueDataValidation issue, Class<? extends DeviceInfo> deviceInfoClass) {
        return new UsagePointDataValidationIssueInfo<>(issue, deviceInfoClass);
    }

    private Optional<Channel> findChannel(UsagePoint usagePoint, ReadingType readingType) {
        return usagePoint.getMeterActivations().stream().map(a -> a.getChannelsContainer().getChannel(readingType)).findFirst().get();

    }

   /* private Optional<Register> findRegister(Device device, ReadingType readingType) {
        return device.getRegisters().stream().filter(register -> register.getReadingType().equals(readingType)).findFirst();
    }*/

    @Override
    public Object from(UsagePointIssueDataValidation usagePointIssueDataValidation) {
        return asInfo(usagePointIssueDataValidation, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<UsagePointIssueDataValidation> getDomainClass() {
        return UsagePointIssueDataValidation.class;
    }
}
