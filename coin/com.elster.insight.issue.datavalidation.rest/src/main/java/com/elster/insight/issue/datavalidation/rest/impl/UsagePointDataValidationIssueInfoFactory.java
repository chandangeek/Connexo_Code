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
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
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
import java.time.temporal.TemporalUnit;
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

    private ExceptionFactory exceptionFactory;


    public UsagePointDataValidationIssueInfoFactory() {
    }

    @Inject
    public UsagePointDataValidationIssueInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointIssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN));
        this.readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        this.exceptionFactory = new ExceptionFactory(nlsService.getThesaurus(UsagePointIssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    public UsagePointDataValidationIssueInfo asInfo(UsagePointIssueDataValidation issue, Class<? extends DeviceInfo> deviceInfoClass) {
        UsagePointDataValidationIssueInfo issueInfo = asShortInfo(issue, deviceInfoClass);
        if (issue.getUsagePoint() == null || !issue.getUsagePoint().isPresent()) {
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
                    MetrologyPurpose metrologyPurpose = this.getMetrologyPurpose(usagePoint.get());
                    usagePoint.get().getEffectiveMetrologyConfigurations()
                            .stream()
                            .map(effectiveMC -> getDeliverablesFromEffectiveMC(effectiveMC, metrologyPurpose))
                            .forEach(deliverables -> deliverables
                                    .stream()
                                    // .filter(deliverable -> !deliverable.getReadingType().isRegular())
                                    .filter(deliverable -> deliverable.getReadingType().equals(entry.getKey()))
                                    .forEach(deliverable -> issueInfo.notEstimatedData.add(createNotEstimatedDataInfoOfRegister(entry.getKey(), entry.getValue(), deliverable.getId()))));
                }
            }
        }
        Collections.<UsagePointDataValidationIssueInfo.NotEstimatedDataInfo>sort(issueInfo.notEstimatedData,
                (info1, info2) -> info1.readingType.aliasName.compareTo(info2.readingType.aliasName));
        return issueInfo;
    }

    public EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfigurationByUsagePointOrThrowException(UsagePoint usagePoint) {
        return usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
    }

    private MetrologyPurpose getMetrologyPurpose(UsagePoint usagePoint) {
        EffectiveMetrologyConfigurationOnUsagePoint currentEffectiveMC = findEffectiveMetrologyConfigurationByUsagePointOrThrowException(usagePoint);
        MetrologyContract metrologyContract = findMetrologyContractOrThrowException(currentEffectiveMC);
        return metrologyContract.getMetrologyPurpose();
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                //.filter(contract -> contract.getId() == contractId)
                // .findAny()
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONTRACT_FOR_USAGEPOINT, effectiveMC.getUsagePoint().getName()));
    }

    private List<ReadingTypeDeliverable> getDeliverablesFromEffectiveMC(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose metrologyPurpose) {
        return this.findMetrologyContractForPurpose(effectiveMC, metrologyPurpose)
                .map(MetrologyContract::getDeliverables)
                .orElse(Collections.emptyList());
    }

    private Optional<MetrologyContract> findMetrologyContractForPurpose(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose metrologyPurpose) {
        return effectiveMC.getMetrologyConfiguration()
                .getContracts()
                .stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(metrologyPurpose))
                .findAny();
    }

    private UsagePointDataValidationIssueInfo.NotEstimatedDataInfo createNotEstimatedDataInfoOfChannel(ReadingType readingType, List<UsagePointNotEstimatedBlock> blocks, Channel channel) {
        UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = new UsagePointDataValidationIssueInfo.NotEstimatedDataInfo();
        info.channelId = channel.getId();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.notEstimatedBlocks = blocks.stream().map(block -> {
            UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo blockInfo = new UsagePointDataValidationIssueInfo.NotEstimatedBlockInfo();
            blockInfo.startTime = block.getStartTime();
            blockInfo.endTime = block.getEndTime();
            List<TemporalUnit> units = channel.getIntervalLength().get().getUnits();
            if (units.contains(ChronoUnit.SECONDS)) {
                blockInfo.amountOfSuspects = ChronoUnit.SECONDS.between(block.getStartTime(), block.getEndTime()) / channel.getIntervalLength()
                        .get()
                        .get(ChronoUnit.SECONDS);
            } else if (units.contains(ChronoUnit.DAYS)) {
                blockInfo.amountOfSuspects = ChronoUnit.DAYS.between(block.getStartTime(), block.getEndTime()) / channel.getIntervalLength()
                        .get()
                        .get(ChronoUnit.DAYS);
            } else if (units.contains(ChronoUnit.MONTHS)) {
                blockInfo.amountOfSuspects = ChronoUnit.MONTHS.between(block.getStartTime(), block.getEndTime()) / channel.getIntervalLength()
                        .get()
                        .get(ChronoUnit.MONTHS);
            } else if (units.contains(ChronoUnit.YEARS)) {
                blockInfo.amountOfSuspects = ChronoUnit.YEARS.between(block.getStartTime(), block.getEndTime()) / channel.getIntervalLength()
                        .get()
                        .get(ChronoUnit.YEARS);
            }

            return blockInfo;
        }).sorted((block1, block2) -> block1.startTime.compareTo(block2.startTime)).collect(Collectors.toList());
        return info;
    }

    private UsagePointDataValidationIssueInfo.NotEstimatedDataInfo createNotEstimatedDataInfoOfRegister(ReadingType readingType, List<UsagePointNotEstimatedBlock> blocks, long registerId) {
        UsagePointDataValidationIssueInfo.NotEstimatedDataInfo info = new UsagePointDataValidationIssueInfo.NotEstimatedDataInfo();
        info.registerId = registerId;
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
