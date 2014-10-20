package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.streams.Predicates.notNull;
import static java.util.Comparator.*;

class MeterActivationValidationImpl implements IMeterActivationValidation {

//    private static final Ordering<ChannelValidation> ORDER_BY_LASTCHECKED_NULL_IS_GREATEST_WITH_ACTIVE_RULES = new Ordering<ChannelValidation>() {
//        @Override
//        public int compare(ChannelValidation left, ChannelValidation right) {
//            return NullSafeOrdering.NULL_IS_GREATEST.<Date>get().compare(getLastChecked(left), getLastChecked(right));
//        }
//
//        private Date getLastChecked(ChannelValidation validation) {
//            return validation == null || !validation.hasActiveRules() ? null : validation.getLastChecked();
//        }
//    };

    private long id;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private long ruleSetId;
    private transient IValidationRuleSet ruleSet;
    private Instant lastRun;
    private List<ChannelValidation> channelValidations = new ArrayList<>();
    private transient boolean saved = true;
    private Instant obsoleteTime;

    private final DataModel dataModel;
    private final Clock clock;
    private boolean active = true;

    @Inject
    MeterActivationValidationImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    MeterActivationValidationImpl init(MeterActivation meterActivation) {
        this.meterActivation.set(meterActivation);
        return this;
    }

    static MeterActivationValidationImpl from(DataModel dataModel, MeterActivation meterActivation) {
        MeterActivationValidationImpl meterActivationValidation = dataModel.getInstance(MeterActivationValidationImpl.class);
        meterActivationValidation.saved = false;
        return meterActivationValidation.init(meterActivation);
    }

    @Override
    public MeterActivation getMeterActivation() {
        return meterActivation.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public IValidationRuleSet getRuleSet() {
        if (ruleSet == null) {
            ruleSet = dataModel.mapper(IValidationRuleSet.class).getOptional(ruleSetId).get();
        }
        return ruleSet;
    }

    @Override
    public void setRuleSet(ValidationRuleSet ruleSet) {
        this.ruleSet = (IValidationRuleSet) ruleSet;
        this.ruleSetId = ruleSet == null ? 0 : ruleSet.getId();
    }

    @Override
    public ChannelValidationImpl addChannelValidation(Channel channel) {
        ChannelValidationImpl channelValidation = ChannelValidationImpl.from(dataModel, this, channel);
        doGetChannelValidations().add(channelValidation);

        return channelValidation;
    }

    @Override
    public Optional<ChannelValidation> getChannelValidation(Channel channel) {
        ChannelValidation channelValidation = getChannelValidations().stream()
                .filter(v -> v.getChannel().getId() == channel.getId())
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(channelValidation);
    }

    private List<ChannelValidation> doGetChannelValidations() {
        if (channelValidations == null) {
            channelValidations = loadChannelValidations();
        }
        return channelValidations;
    }

    private List<ChannelValidation> loadChannelValidations() {
        return new ArrayList<>(dataModel.mapper(ChannelValidation.class).find("meterActivationValidation", this));
    }

    @Override
    public void save() {
        if (!saved) {
            saved = true;
            meterActivationValidationFactory().persist(this);
        } else {
            meterActivationValidationFactory().update(this);
            dataModel.mapper(ChannelValidation.class).update(FluentIterable.from(getChannelValidations()).toList());
        }
    }

    @Override
    public void makeObsolete() {
        this.obsoleteTime = Instant.now(clock);
        this.save();
    }

    @Override
    public boolean isObsolete() {
        return obsoleteTime != null;
    }

    private DataMapper<IMeterActivationValidation> meterActivationValidationFactory() {
        return dataModel.mapper(IMeterActivationValidation.class);
    }

    @Override
    public Set<ChannelValidation> getChannelValidations() {
        return Collections.unmodifiableSet(new HashSet<>(doGetChannelValidations()));
    }

    @Override
    public void validate(Range<Instant> interval) {
        if (!isActive()) {
            return;
        }
        getMeterActivation().getChannels().stream()
                .forEach(c -> validateChannel(c, interval));
        lastRun = Instant.now(clock);
        save();
    }

    @Override
    public void validate(Range<Instant> interval, String readingTypeCode) {
        if (!isActive()) {
            return;
        }
        getMeterActivation().getChannels().stream()
                .filter(c -> c.getReadingTypes().stream().map(ReadingType::getMRID).anyMatch(readingTypeCode::equals))
                .forEach(c -> validateChannel(c, interval));
        save();
    }

    // given interval is interpreted as consumption interval for cumulative
    private void validateChannel(Channel channel, Range<Instant> interval) {
        Instant earliestLastChecked = null;
        List<IValidationRule> activeRules = getActiveRules();
        if (hasApplicableRules(channel, activeRules)) {
            ChannelValidationImpl channelValidation = findOrAddValidationFor(channel);
            Range<Instant> intervalToValidate = intervalToValidate(channelValidation, interval); // this interval is to be interpreted closed-closed
            Instant lastChecked = null;
            for (IValidationRule validationRule : activeRules) {
                lastChecked = validationRule.validateChannel(channel, intervalToValidate);
                if (lastChecked != null) {
                    earliestLastChecked = getEarliestDate(earliestLastChecked, lastChecked);
                }
            }

            if (earliestLastChecked != null) {
                channelValidation.setLastChecked(earliestLastChecked);
            }
            channelValidation.setActiveRules(true);
        } else {
            ChannelValidationImpl channelValidation = findValidationFor(channel);
            if (channelValidation != null) {
                channelValidation.setActiveRules(false);
            }
        }
    }

    private Range<Instant> intervalToValidate(ChannelValidationImpl channelValidation, Range<Instant> interval) {
        Range<Instant> maxScope = channelValidation.getMeterActivationValidation().getMeterActivation().getRange();
        if (!does(maxScope).overlap(interval)) {
            return Range.closedOpen(interval.lowerEndpoint(), interval.lowerEndpoint());
        }
        Range<Instant> clipped = maxScope.intersection(interval);
        Instant lastChecked = getLatestDate(channelValidation.getLastChecked(), firstReadingTime(channelValidation));
        Instant start = getEarliestDate(lastChecked, adjusted(channelValidation, clipped.lowerEndpoint()));
        if (clipped.hasUpperBound()) {
            Instant end = getLatestDate(channelValidation.getChannel().getLastDateTime(), clipped.upperEndpoint());
            return Ranges.closed(start, end);
        } else {
            return Range.atLeast(start);
        }
    }

    private Instant adjusted(ChannelValidationImpl channelValidation, Instant date) {
        int minutes = channelValidation.getChannel().getMainReadingType().getMeasuringPeriod().getMinutes();
        return date.plus(minutes, ChronoUnit.MINUTES);
    }

    private Instant firstReadingTime(ChannelValidationImpl channelValidation) {
        return adjusted(channelValidation, channelValidation.getChannel().getMeterActivation().getRange().lowerEndpoint());
    }

    private boolean hasApplicableRules(Channel channel, List<IValidationRule> activeRules) {
        return activeRules.stream()
                .anyMatch(r -> isApplicable(r, channel));
    }

    private boolean isApplicable(IValidationRule activeRule, Channel channel) {
        Set<ReadingType> activeRuleReadingTypes = activeRule.getReadingTypes();
        for (ReadingType readingType : channel.getReadingTypes()) {
            if (activeRuleReadingTypes.contains(readingType)) {
                return true;
            }
        }
        return false;
    }

    private List<IValidationRule> getActiveRules() {
        return getRuleSet().getRules().stream()
                .filter(IValidationRule::isActive)
                .collect(Collectors.toList());
    }

    private Instant getEarliestDate(Instant first, Instant second) {
        return first == null ? second : Ordering.natural().min(second, first);
    }

    private Instant getLatestDate(Instant first, Instant second) {
        Comparator<Instant> comparator = nullsFirst(naturalOrder());
        return Ordering.from(comparator).max(first, second);
    }

    private ChannelValidationImpl findOrAddValidationFor(final Channel channel) {
        ChannelValidationImpl channelValidation = findValidationFor(channel);
        return channelValidation == null ? addChannelValidation(channel) : channelValidation;
    }

    private ChannelValidationImpl findValidationFor(final Channel channel) {
        return getChannelValidations().stream()
                .filter(c -> c.getChannel().getId() == channel.getId())
                .map(ChannelValidationImpl.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateLastChecked(Instant lastChecked) {
        for (ChannelValidation channelValidation : getChannelValidations()) {
            ((ChannelValidationImpl) channelValidation).setLastChecked(lastChecked);
        }
    }

    @Override
    public boolean isAllDataValidated() {
        if (isActive()) {
            if (lastRun == null) {
                return false;
            }
            Comparator<? super Instant> comparator = nullsLast(naturalOrder());
            return getChannelValidations().stream()
                    .noneMatch(c -> c.hasActiveRules() && comparator.compare(c.getLastChecked(), c.getChannel().getLastDateTime()) < 0);
        }
        return getChannelValidations().stream()
                .noneMatch(ChannelValidation::hasActiveRules);
    }

    @Override
    public Instant getMinLastChecked() {
        return lastCheckedStream()
                .min(naturalOrder())
                .orElse(null);
    }

    @Override
    public Instant getMaxLastChecked() {
        return lastCheckedStream()
                .max(naturalOrder())
                .orElse(null);
    }

    private Stream<Instant> lastCheckedStream() {
        return getChannelValidations().stream()
                .filter(notNull())
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked)
                .filter(notNull());
    }

    @Override
    public Instant getLastRun() {
        return lastRun;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        setActive(true);
        getMeterActivation().getChannels().stream()
                .forEach(c -> getChannelValidation(c).orElseGet(() -> addChannelValidation(c)));
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    private void setActive(boolean status) {
        this.active = status;
    }
}
