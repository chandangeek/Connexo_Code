/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class ChannelValidationImpl implements ChannelValidation {

    private long id;
    private long channelId;
    private Reference<ChannelsContainerValidation> channelsContainerValidation = ValueReference.absent();
    private Instant lastChecked;
    private boolean lastValidationComplete;
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

    private List<IValidationRule> activeRulesOfVersion(ValidationRuleSetVersion version) {
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
            removeValidationRelatedReadingQualities(Range.greaterThan(newValue));
        }
        this.lastChecked = newValue;
        return true;
    }

    private void removeValidationRelatedReadingQualities(Range<Instant> range) {
        getChannel().findReadingQualities()
                .ofQualitySystem(channelsContainerValidation.get().getRuleSet().getQualityCodeSystem())
                .inTimeInterval(range)
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION))
                .collect()
                .forEach(ReadingQualityRecord::delete);
    }

    @Override
    public boolean moveLastCheckedBefore(Instant instant) {
        if (instant.isAfter(lastChecked)) {
            return false;
        }
        Instant lastCheckedCandidate = getChannel().isRegular() ? getChannel().getPreviousDateTime(instant) : instant.minusMillis(1);
        Instant minLastChecked = minLastChecked();
        return updateLastChecked(minLastChecked.isAfter(lastCheckedCandidate) ? minLastChecked : lastCheckedCandidate);
    }

    @Override
    public void validate(RangeSet<Instant> ranges, Logger logger) {
        ranges.asRanges().stream()
                .map(range -> validate(range, logger))
                .max(Comparator.naturalOrder())
                .ifPresent(this::updateLastChecked);
        RangeSet<Instant> notValidatedRanges = ranges.subRangeSet(Range.greaterThan(getLastChecked()));
        lastValidationComplete = notValidatedRanges.asRanges().isEmpty()
                || notValidatedRanges.asRanges().stream().allMatch(range -> channel.getReadings(range).isEmpty());
    }

    @Override
    public boolean isLastValidationComplete() {
        return lastValidationComplete;
    }

    private Instant validate(Range<Instant> validationRange, Logger logger) {
        if (validationRange.hasUpperBound()) {
            Instant upperBound = channel.truncateToIntervalLength(validationRange.upperEndpoint());
            return Ranges.nonEmptyIntersection(Range.greaterThan(lastChecked), Range.atMost(upperBound), validationRange)
                    .map(dataRange -> getChannelsContainerValidation().getRuleSet().getRuleSetVersions().stream()
                            .map(currentVersion -> Ranges.nonEmptyIntersection(dataRange, currentVersion.getRange())
                                    .flatMap(rangeToValidate -> {
                                        ChannelValidator validator = new ChannelValidator(channel, rangeToValidate);
                                        Optional<Pair<Instant, Instant>> minMaxLastChecked = activeRulesOfVersion(currentVersion).stream()
                                                .map(validationRule -> validator.validateRule(validationRule, logger))
                                                .map(instant -> Pair.of(instant, instant))
                                                .reduce((a, b) -> Pair.of(
                                                        min(a.getFirst(), b.getFirst()),
                                                        max(a.getLast(), b.getLast())
                                                ));
                                        minMaxLastChecked.map(minMax -> Range.openClosed(minMax.getFirst(), minMax.getLast()))
                                                .filter(Predicates.not(Range::isEmpty))
                                                .ifPresent(this::removeValidationRelatedReadingQualities);
                                        return minMaxLastChecked.map(Pair::getFirst); // minimum by rules
                                    }))
                            .flatMap(Functions.asStream())
                            .min(Comparator.naturalOrder()) // minimum by versions TODO CXO-6665: wat?
                            // it prevents from validation with several versions at one shot.
                            // as per agreement with Igor Nesterov, a proper behavior might be
                            // not to validate with later version until all data are validated within the range of the previous one,
                            // and to keep the latest resulting lastChecked as master lastChecked.
                            .orElse(upperBound))
                    .orElse(lastChecked);
        }
        return lastChecked;
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }
}
