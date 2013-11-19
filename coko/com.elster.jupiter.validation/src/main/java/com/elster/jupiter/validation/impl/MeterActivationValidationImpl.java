package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.FluentIterable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class MeterActivationValidationImpl implements MeterActivationValidation {

    private long id;
    private transient MeterActivation meterActivation;
    private long ruleSetId;
    private transient ValidationRuleSet ruleSet;
    private UtcInstant lastRun;
    private Set<ChannelValidation> channelValidations;
    private transient boolean saved;

    private MeterActivationValidationImpl() {
        saved = true;
    }

    public MeterActivationValidationImpl(MeterActivation meterActivation) {
        id = meterActivation.getId();
        this.meterActivation = meterActivation;
    }

    @Override
    public MeterActivation getMeterActivation() {
        return meterActivation;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        if (ruleSet == null) {
            ruleSet = Bus.getOrmClient().getValidationRuleSetFactory().get(ruleSetId).get();
        }
        return ruleSet;
    }

    @Override
    public void setRuleSet(ValidationRuleSet ruleSet) {
        this.ruleSet = ruleSet;
        this.ruleSetId = ruleSet == null ? 0 : ruleSet.getId();
    }

    @Override
    public ChannelValidation addChannelValidation(Channel channel) {
        ChannelValidation channelValidation = new ChannelValidationImpl(this, channel);
        doGetChannelValidations().add(channelValidation);

        return channelValidation;
    }

    private Set<ChannelValidation> doGetChannelValidations() {
        if (channelValidations == null) {
            channelValidations = loadChanelValidations();
        }
        return channelValidations;
    }

    private HashSet<ChannelValidation> loadChanelValidations() {
        return new HashSet<>(Bus.getOrmClient().getChannelValidationFactory().find("meterActivationValidation", this));
    }

    @Override
    public void save() {
        if (!saved) {
            saved = true;
            meterActivationValidationFactory().persist(this);
            Bus.getOrmClient().getChannelValidationFactory().persist(FluentIterable.from(getChannelValidations()).toList());
        } else {
            meterActivationValidationFactory().update(this);
            HashSet<ChannelValidation> channelValidations = loadChanelValidations();
            DiffList<ChannelValidation> diffList = ArrayDiffList.fromOriginal(channelValidations);
            diffList.clear();
            diffList.addAll(getChannelValidations());
            Bus.getOrmClient().getChannelValidationFactory().persist(FluentIterable.from(diffList.getAdditions()).toList());
            Bus.getOrmClient().getChannelValidationFactory().update(FluentIterable.from(diffList.getRemaining()).toList());
            Bus.getOrmClient().getChannelValidationFactory().remove(FluentIterable.from(diffList.getRemovals()).toList());
        }
    }

    private DataMapper<MeterActivationValidation> meterActivationValidationFactory() {
        return Bus.getOrmClient().getMeterActivationValidationFactory();
    }

    @Override
    public Set<ChannelValidation> getChannelValidations() {
        return Collections.unmodifiableSet(doGetChannelValidations());
    }
}
