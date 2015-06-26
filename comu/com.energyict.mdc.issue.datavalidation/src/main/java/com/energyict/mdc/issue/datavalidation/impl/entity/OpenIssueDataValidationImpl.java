package com.energyict.mdc.issue.datavalidation.impl.entity;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenIssueDataValidationImpl extends IssueDataValidationImpl implements OpenIssueDataValidation {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<OpenIssueNotEstimatedBlockImpl> notEstimatedBlocks = new ArrayList<>();

    @Inject
    public OpenIssueDataValidationImpl(DataModel dataModel, IssueDataValidationService issueDataValidationService) {
        super(dataModel, issueDataValidationService);
    }

    @Override
    OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    @Override
    public HistoricalIssueDataValidation close(IssueStatus status) {
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        HistoricalIssueDataValidationImpl historicalDataValidationIssue = getDataModel().getInstance(HistoricalIssueDataValidationImpl.class);
        historicalDataValidationIssue.setIssue(historicalBaseIssue);
        historicalDataValidationIssue.copy(this);
        historicalDataValidationIssue.save();
        return historicalDataValidationIssue;
    }

    @Override
    public void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        Range<Instant> interval = Range.openClosed(startTime.minus(channel.getIntervalLength().get()), endTime);

        List<Pair<Range<Instant>, OpenIssueNotEstimatedBlockImpl>> connectedBlocks = notEstimatedBlocks.stream()
                .filter(block -> block.getChannel().getId() == channel.getId())
                .filter(block -> block.getReadingType().equals(readingType))
                .map(block -> Pair.of(Range.openClosed(block.getStartTime(), block.getEndTime()), block))
                .filter(block -> block.getFirst().isConnected(interval))
                .sorted((block1, block2) -> block1.getFirst().lowerEndpoint().compareTo(block2.getFirst().lowerEndpoint()))
                .collect(Collectors.toList());

        Range<Instant> newInterval = connectedBlocks.stream()
                .map(pair -> pair.getFirst())
                .reduce((interval1, interval2) -> interval1.span(interval2))
                .map(range -> range.span(interval))
                .orElse(interval);

        connectedBlocks.stream().forEach(block -> notEstimatedBlocks.remove(block.getLast()));
        createNewBlock(channel, readingType, newInterval.lowerEndpoint(), newInterval.upperEndpoint());
    }

    @Override
    public void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp) {
        Range<Instant> interval = Range.openClosed(timeStamp.minus(channel.getIntervalLength().get()), timeStamp);

        Optional<OpenIssueNotEstimatedBlockImpl> enclosingBlock = notEstimatedBlocks.stream()
                .filter(block -> block.getChannel().getId() == channel.getId())
                .filter(block -> block.getReadingType().equals(readingType))
                .filter(block -> Range.openClosed(block.getStartTime(), block.getEndTime()).encloses(interval))
                .findFirst();
        if (enclosingBlock.isPresent()) {
            Range<Instant> enclosingRange = Range.openClosed(enclosingBlock.get().getStartTime(), enclosingBlock.get().getEndTime());
            notEstimatedBlocks.remove(enclosingBlock.get());
            Range<Instant> lowerRange = Range.openClosed(enclosingRange.lowerEndpoint(), interval.lowerEndpoint());
            if (!lowerRange.isEmpty()) {
                createNewBlock(channel, readingType, lowerRange.lowerEndpoint(), lowerRange.upperEndpoint());
            }
            Range<Instant> upperRange = Range.openClosed(interval.upperEndpoint(), enclosingRange.upperEndpoint());
            if (!upperRange.isEmpty()) {
                createNewBlock(channel, readingType, upperRange.lowerEndpoint(), upperRange.upperEndpoint());
            }
        }
    }

    @Override
    public List<NotEstimatedBlock> getNotEstimatedBlocks() {
        return Collections.unmodifiableList(notEstimatedBlocks);
    }

    private void createNewBlock(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        OpenIssueNotEstimatedBlockImpl block = getDataModel().getInstance(OpenIssueNotEstimatedBlockImpl.class);
        block.init(this, channel, readingType, startTime, endTime);
        notEstimatedBlocks.add(block);
    }
}
