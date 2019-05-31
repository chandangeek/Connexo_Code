/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.issue.HistoricalIssueServiceCall;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.NotEstimatedBlock;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenIssueServiceCallImpl extends IssueServiceCallImpl implements OpenIssueServiceCall {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<NotEstimatedBlock> notEstimatedBlocks = new ArrayList<>();

    @Inject
    public OpenIssueServiceCallImpl(DataModel dataModel, IssueServiceCallService issueServiceCallService) {
        super(dataModel, issueServiceCallService);
    }

    @Override
    OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    @Override
    public HistoricalIssueServiceCall close(IssueStatus status) {
//        HistoricalIssueServiceCallImpl historicalIssueServiceCall = getDataModel().getInstance(HistoricalIssueServiceCallImpl.class);
//        historicalIssueServiceCall.copy(this);
//        this.delete(); // Remove reference to baseIssue
//        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
//        historicalIssueServiceCall.setIssue(historicalBaseIssue);
//        historicalIssueServiceCall.save();
//        return historicalIssueServiceCall;
        return null;
    }

    @Override
    public HistoricalIssue closeInternal(IssueStatus status) {
        return null;
    }

//    @Override
//    public void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp) {
//        Range<Instant> interval = computeNotEstimatedBlockInterval(channel, timeStamp);
//
//        List<Pair<Range<Instant>, OpenIssueNotEstimatedBlockImpl>> connectedBlocks = notEstimatedBlocks.stream()
//                .filter(block -> block.getChannel().getId() == channel.getId())
//                .filter(block -> block.getReadingType().equals(readingType))
//                .map(block -> Pair.of(Range.openClosed(block.getStartTime(), block.getEndTime()), block))
//                .filter(block -> block.getFirst().isConnected(interval))
//                .sorted((block1, block2) -> block1.getFirst().lowerEndpoint().compareTo(block2.getFirst().lowerEndpoint()))
//                .collect(Collectors.toList());
//
//        Range<Instant> newInterval = connectedBlocks.stream()
//                .map(pair -> pair.getFirst())
//                .reduce((interval1, interval2) -> interval1.span(interval2))
//                .map(range -> range.span(interval))
//                .orElse(interval);
//
//        connectedBlocks.stream().forEach(block -> notEstimatedBlocks.remove(block.getLast()));
//        createNewBlock(channel, readingType, newInterval.lowerEndpoint(), newInterval.upperEndpoint());
//    }
//
//    @Override
//    public void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp) {
//        Range<Instant> interval = computeNotEstimatedBlockInterval(channel, timeStamp);
//
//        Optional<OpenIssueNotEstimatedBlockImpl> enclosingBlock = notEstimatedBlocks.stream()
//                .filter(block -> block.getChannel().getId() == channel.getId())
//                .filter(block -> block.getReadingType().equals(readingType))
//                .filter(block -> Range.openClosed(block.getStartTime(), block.getEndTime()).encloses(interval))
//                .findFirst();
//
//        if (enclosingBlock.isPresent()) {
//            Range<Instant> enclosingRange = Range.openClosed(enclosingBlock.get().getStartTime(), enclosingBlock.get().getEndTime());
//            notEstimatedBlocks.remove(enclosingBlock.get());
//            Range<Instant> lowerRange = Range.openClosed(enclosingRange.lowerEndpoint(), interval.lowerEndpoint());
//            if (!lowerRange.isEmpty()) {
//                createNewBlock(channel, readingType, lowerRange.lowerEndpoint(), lowerRange.upperEndpoint());
//            }
//            Range<Instant> upperRange = Range.openClosed(interval.upperEndpoint(), enclosingRange.upperEndpoint());
//            if (!upperRange.isEmpty()) {
//                createNewBlock(channel, readingType, upperRange.lowerEndpoint(), upperRange.upperEndpoint());
//            }
//        }
//    }

    @Override
    public List<NotEstimatedBlock> getNotEstimatedBlocks() {
        return Collections.unmodifiableList(notEstimatedBlocks);
    }

    @Override
    public void setDevice(com.elster.jupiter.metering.EndDevice device) {

    }

    @Override
    public void setUsagePoint(com.elster.jupiter.metering.UsagePoint usagePoint) {

    }

//    private Range<Instant> computeNotEstimatedBlockInterval(Channel channel, Instant timeStamp) {
//        Range<Instant> interval;
//        if (channel.isRegular()) {
//            interval = Range.openClosed(timeStamp.minus(channel.getIntervalLength().get()), timeStamp);
//        } else {
//            List<BaseReadingRecord> readingsBefore = channel.getReadingsBefore(timeStamp, 1);
//            if (readingsBefore.isEmpty()) {
//                interval = Range.openClosed(Instant.EPOCH, timeStamp);
//            } else {
//                interval = Range.openClosed(readingsBefore.get(0).getTimeStamp(), timeStamp);
//            }
//        }
//        return interval;
//    }
//
//    private void createNewBlock(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
//        OpenIssueNotEstimatedBlockImpl block = getDataModel().getInstance(OpenIssueNotEstimatedBlockImpl.class);
//        block.init(this, channel, readingType, startTime, endTime);
//        notEstimatedBlocks.add(block);
//    }
}
