/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class ChannelValidationImpl implements ChannelValidation {

    private long id;
    private long channelId;
    private Reference<ChannelsContainerValidation> channelsContainerValidation = ValueReference.absent();
    private Instant lastChecked;
    @SuppressWarnings("unused")
    private boolean activeRules;
    private Channel channel;

    @Inject
    ChannelValidationImpl() {
    }

    ChannelValidationImpl init(ChannelsContainerValidation channelsContainerValidation, Channel channel) {
        if (!channel.getChannelsContainer().equals(channelsContainerValidation.getChannelsContainer())) {
            throw new IllegalArgumentException();
        }
        this.channelsContainerValidation.set(channelsContainerValidation);
        this.channelId = channel.getId();
        this.channel = channel;
        this.lastChecked = minLastChecked();
        this.activeRules = true;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ChannelsContainerValidation getChannelsContainerValidation() {
        return channelsContainerValidation.get();
    }

    @Override
    public Instant getLastChecked() {
        return lastChecked;
    }

    public Channel getChannel() {
        if (channel == null) {
            channel = channelsContainerValidation.get().getChannelsContainer().getChannels().stream()
                    .filter(channel -> channel.getId() == channelId)
                    .findFirst()
                    .get();
        }
        return channel;
    }

    @Override
    public boolean hasActiveRules() {
        return !activeRules().isEmpty();
    }

    private List<IValidationRule> activeRules() {
        return getChannelsContainerValidation().getRuleSet().getRules().stream()
                .map(IValidationRule.class::cast)
                .filter(rule -> rule.appliesTo(getChannel()))
                .collect(Collectors.toList());
    }

    private List<IValidationRule> activeRulesOfVersion(IValidationRuleSetVersion version) {
        return version.getRules().stream()
                .map(IValidationRule.class::cast)
                .filter(rule -> rule.appliesTo(getChannel()))
                .collect(Collectors.toList());
    }

    void setActiveRules(boolean activeRules) {
        this.activeRules = activeRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return channelId == ((ChannelValidationImpl) o).channelId && channelsContainerValidation.equals(((ChannelValidationImpl) o).channelsContainerValidation);

    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, channelsContainerValidation);
    }

    private Instant minLastChecked() {
        return channelsContainerValidation.get().getChannelsContainer().getStart();
    }

    @Override
    public boolean updateLastChecked(Instant instant) {
        if (lastChecked.equals(Objects.requireNonNull(instant))) {
            return false;
        }
        Instant newValue = instant.isBefore(minLastChecked()) ? minLastChecked() : instant;
        if (lastChecked.isAfter(newValue)) {
            getChannel().findReadingQualities()
                    .ofQualitySystem(channelsContainerValidation.get().getRuleSet().getQualityCodeSystem())
                    .inTimeInterval(Range.greaterThan(newValue))
                    .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION))
                    .collect()
                    .forEach(ReadingQualityRecord::delete);
        }
        this.lastChecked = newValue;
        return true;
    }

    @Override
    public boolean moveLastCheckedBefore(Instant instant) {
        if (instant.isAfter(lastChecked)) {
            return false;
        }
        Instant lastCheckedCandidate = instant.minusMillis(1);
        Instant minLastChecked = minLastChecked();
        return updateLastChecked(minLastChecked.isAfter(lastCheckedCandidate) ? minLastChecked : lastCheckedCandidate);
//        // Doesn't work for aggregated channels
//        Optional<BaseReadingRecord> reading = getChannel().getReadingsBefore(instant, 1).stream().findFirst();
//        return updateLastChecked(reading.map(BaseReading::getTimeStamp).orElseGet(this::minLastChecked));
    }

    @Override
    public void validate() {
        Instant end = getChannel().getLastDateTime();
        if (end != null && lastChecked.isBefore(end)) {
            Range<Instant> dataRange = Range.openClosed(lastChecked, end);
            List<? extends ValidationRuleSetVersion> versions = getChannelsContainerValidation().getRuleSet().getRuleSetVersions();

            Instant newLastChecked = versions.stream()
                    .map(IValidationRuleSetVersion.class::cast)
                    .filter(cv -> dataRange.isConnected(Range.openClosed(cv.getNotNullStartDate(), cv.getNotNullEndDate())))
                    .flatMap(currentVersion -> {
                        Range<Instant> versionRange = Range.openClosed(currentVersion.getNotNullStartDate(), currentVersion.getNotNullEndDate());
                        Range<Instant> rangeToValidate = dataRange.intersection(versionRange);
                        ChannelValidator validator = new ChannelValidator(getChannel(), rangeToValidate);
                        return activeRulesOfVersion(currentVersion).stream()
                                .map(validator::validateRule);
                    })
                    .min(Comparator.naturalOrder()).orElse(end);

            updateLastChecked(newLastChecked);
        }
    }
}
