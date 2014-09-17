package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.comparators.NullSafeOrdering;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MeterActivationValidationImpl implements IMeterActivationValidation {

    private static final Ordering<ChannelValidation> ORDER_BY_LASTCHECKED_NULL_IS_GREATEST_WITH_ACTIVE_RULES = new Ordering<ChannelValidation>() {
        @Override
        public int compare(ChannelValidation left, ChannelValidation right) {
            return NullSafeOrdering.NULL_IS_GREATEST.<Date>get().compare(getLastChecked(left), getLastChecked(right));
        }

        private Date getLastChecked(ChannelValidation validation) {
            return validation == null || !validation.hasActiveRules() ? null : validation.getLastChecked();
        }
    };

    private long id;
    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private long ruleSetId;
    private transient IValidationRuleSet ruleSet;
    private UtcInstant lastRun;
    private List<ChannelValidation> channelValidations = new ArrayList<>();
    private transient boolean saved = true;
    private UtcInstant obsoleteTime;

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
        return Optional.fromNullable(channelValidation);
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
        this.obsoleteTime = new UtcInstant(clock.now());
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
        return Collections.unmodifiableSet(new HashSet<ChannelValidation>(doGetChannelValidations()));
    }

    @Override
    public void validate(Interval interval) {
        if (!isActive()) {
            return;
        }
        for (Channel channel : getMeterActivation().getChannels()) {
            validateChannel(interval, channel);
        }
        lastRun = new UtcInstant(clock.now());
        save();
    }

    private void validateChannel(Interval interval, Channel channel) {
        Date earliestLastChecked = null;
        Iterable<IValidationRule> activeRules = getActiveRules();
        if (hasApplicableRules(channel, activeRules)) {
            ChannelValidationImpl channelValidation = findOrAddValidationFor(channel);
            Date lastChecked = null;
            Interval intervalToValidate = new Interval(getEarliestDate(channelValidation.getLastChecked(), interval.getStart()), interval.getEnd());
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

    private boolean hasApplicableRules(Channel channel, Iterable<IValidationRule> activeRules) {
        for (IValidationRule activeRule : activeRules) {
            if (isApplicable(activeRule, channel)) {
                return true;
            }
        }
        return false;
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

    private Iterable<IValidationRule> getActiveRules() {
        return Iterables.filter(getRuleSet().getRules(), new Predicate<IValidationRule>() {
            @Override
            public boolean apply(IValidationRule input) {
                return input.isActive();
            }
        });
    }

    private Date getEarliestDate(Date first, Date second) {
        return first == null ? second : Ordering.natural().min(second, first);
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
    public void updateLastChecked(Date lastChecked) {
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
            Comparator<Date> dateComparator = NullSafeOrdering.NULL_IS_GREATEST.get();
            for (ChannelValidation channelValidation : getChannelValidations()) {
                Date lastDateTime = channelValidation.getChannel().getTimeSeries().getLastDateTime();
                Date lastChecked = channelValidation.getLastChecked();
                if (channelValidation.hasActiveRules() && dateComparator.compare(lastChecked, lastDateTime) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Date getMinLastChecked() {
        if (!getChannelValidations().isEmpty()) {
            return ORDER_BY_LASTCHECKED_NULL_IS_GREATEST_WITH_ACTIVE_RULES.min(getChannelValidations()).getLastChecked();
        } else if (lastRun != null) {
            return lastRun.toDate();
        } else {
            return clock.now();
        }
    }

    @Override
    public Date getLastRun() {
        return lastRun.toDate();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    private void setActive(boolean status) {
        this.active = status;
    }
}
