package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MeterActivationValidationImpl implements IMeterActivationValidation {

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
    public ChannelValidation addChannelValidation(Channel channel) {
        ChannelValidation channelValidation = ChannelValidationImpl.from(dataModel, this, channel);
        doGetChannelValidations().add(channelValidation);

        return channelValidation;
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
        return Collections.unmodifiableSet(new HashSet<>(doGetChannelValidations()));
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
        ChannelValidation channelValidation = findOrAddValidationFor(channel);
        Date lastChecked = null;
        Interval intervalToValidate = new Interval(getEarliestDate(channelValidation.getLastChecked(), interval.getStart()), interval.getEnd());
        for (IValidationRule validationRule : getActiveRules()) {
            lastChecked = validationRule.validateChannel(channel, intervalToValidate);
            if (lastChecked != null) {
                earliestLastChecked = getEarliestDate(earliestLastChecked, lastChecked);
            }
        }

        if (earliestLastChecked != null) {
            channelValidation.setLastChecked(earliestLastChecked);
        }
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

    private ChannelValidation findOrAddValidationFor(final Channel channel) {
        ChannelValidation channelValidation = findValidationFor(channel);
        return channelValidation == null ? addChannelValidation(channel) : channelValidation;
    }

    private ChannelValidation findValidationFor(final Channel channel) {
        for (ChannelValidation channelValidation : getChannelValidations()) {
            if (channelValidation.getChannel().equals(channel)) {
                return channelValidation;
            }
        }
        return null;
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
    public void setActive(boolean status) {
        this.active = status;
    }
}
