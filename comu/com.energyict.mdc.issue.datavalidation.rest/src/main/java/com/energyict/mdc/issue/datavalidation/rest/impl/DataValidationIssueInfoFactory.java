package com.energyict.mdc.issue.datavalidation.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.rest.impl.DataValidationIssueInfo.NotEstimatedBlockInfo;
import com.energyict.mdc.issue.datavalidation.rest.impl.DataValidationIssueInfo.NotEstimatedDataInfo;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataValidationIssueInfoFactory {

    private final DeviceService deviceService;

    @Inject
    public DataValidationIssueInfoFactory(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DataValidationIssueInfo asInfo(IssueDataValidation issue, Class<? extends DeviceInfo> deviceInfoClass) {
        DataValidationIssueInfo issueInfo = asShortInfo(issue, deviceInfoClass);
        if (issue.getDevice() == null || !issue.getDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            return issueInfo;
        }
        Optional<Device> device = deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()));
        if (device.isPresent()) {
            Map<ReadingType, List<NotEstimatedBlock>> notEstimatedData = issue.getNotEstimatedBlocks().stream()
                    .collect(Collectors.groupingBy(NotEstimatedBlock::getReadingType));
            for (Map.Entry<ReadingType, List<NotEstimatedBlock>> entry : notEstimatedData.entrySet()) {
                Optional<Channel> channel = findChannel(device.get(), entry.getKey());
                if (channel.isPresent()) {
                    NotEstimatedDataInfo info = createNotEstimatedDataInfoOfChannel(entry.getKey(), entry.getValue(), channel.get());
                    issueInfo.notEstimatedData.add(info);
                } else {
                    findRegister(device.get(), entry.getKey()).ifPresent(register -> {
                        NotEstimatedDataInfo info = createNotEstimatedDataInfoOfRegister(entry.getKey(), entry.getValue(), register);
                        issueInfo.notEstimatedData.add(info);
                    });
                }
            }
        }
        Collections.<NotEstimatedDataInfo>sort(issueInfo.notEstimatedData,
                (info1, info2) -> info1.readingType.aliasName.compareTo(info2.readingType.aliasName));
        return issueInfo;
    }

    private NotEstimatedDataInfo createNotEstimatedDataInfoOfChannel(ReadingType readingType, List<NotEstimatedBlock> blocks, Channel channel) {
        NotEstimatedDataInfo info = new NotEstimatedDataInfo();
        info.channelId = channel.getId();
        info.readingType = new ReadingTypeInfo(readingType);
        info.notEstimatedBlocks = blocks.stream().map(block -> {
            NotEstimatedBlockInfo blockInfo = new NotEstimatedBlockInfo();
            blockInfo.startTime = block.getStartTime();
            blockInfo.endTime = block.getEndTime();
            blockInfo.amountOfSuspects = ChronoUnit.MILLIS.between(block.getStartTime(), block.getEndTime()) / channel.getInterval().getMilliSeconds();
            return blockInfo;
        }).sorted((block1, block2) -> block1.startTime.compareTo(block2.startTime)).collect(Collectors.toList());
        return info;
    }

    private NotEstimatedDataInfo createNotEstimatedDataInfoOfRegister(ReadingType readingType, List<NotEstimatedBlock> blocks, Register register) {
        NotEstimatedDataInfo info = new NotEstimatedDataInfo();
        info.registerId = register.getRegisterSpecId();
        info.readingType = new ReadingTypeInfo(readingType);
        info.notEstimatedBlocks = blocks.stream().map(block -> {
            NotEstimatedBlockInfo blockInfo = new NotEstimatedBlockInfo();
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

    public List<DataValidationIssueInfo> asInfo(List<? extends IssueDataValidation> issues) {
        return issues.stream().map(issue -> this.asShortInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }

    private DataValidationIssueInfo asShortInfo(IssueDataValidation issue, Class<? extends DeviceInfo> deviceInfoClass) {
        return new DataValidationIssueInfo<>(issue, deviceInfoClass);
    }

    private Optional<Channel> findChannel(Device device, ReadingType readingType) {
        return device.getChannels().stream().filter(channel -> {
            ReadingType channelReadingType = channel.getReadingType();
            if (channelReadingType.equals(readingType)) {
                return true;
            }
            Optional<ReadingType> calculatedReadingType = channelReadingType.getCalculatedReadingType();
            return calculatedReadingType.isPresent() && calculatedReadingType.get().equals(readingType);
        }).findFirst();
    }

    private Optional<Register> findRegister(Device device, ReadingType readingType) {
        return device.getRegisters().stream().filter(register -> register.getReadingType().equals(readingType)).findFirst();
    }
}
