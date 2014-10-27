package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.Range;

import javax.inject.Inject;

import org.hamcrest.core.IsSame;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Predicate;

final class ChannelValidationImpl implements IChannelValidation {

    private long id;
    private Reference<Channel> channel = ValueReference.absent();
    private Reference<IMeterActivationValidation> meterActivationValidation = ValueReference.absent();
    private Instant lastChecked;
    private boolean activeRules;

    @SuppressWarnings("unused")
    @Inject
    ChannelValidationImpl() {
    }

    ChannelValidationImpl init(IMeterActivationValidation meterActivationValidation, Channel channel) {
        if (!channel.getMeterActivation().equals(meterActivationValidation.getMeterActivation())) {
            throw new IllegalArgumentException();
        }
        this.meterActivationValidation.set(meterActivationValidation);
        this.channel.set(channel);
        this.activeRules = true;
        return this;
    }

    static ChannelValidationImpl from(DataModel dataModel, IMeterActivationValidation meterActivationValidation, Channel channel) {
        return dataModel.getInstance(ChannelValidationImpl.class).init(meterActivationValidation, channel);
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public IMeterActivationValidation getMeterActivationValidation() {
        return meterActivationValidation.get();
    }

    @Override
    public Instant getLastChecked() {
        return lastChecked;
    }

    public Channel getChannel() {
        return channel.get();
    }

    @Override
    public boolean hasActiveRules() {
        activeRules = getMeterActivationValidation().getRuleSet().getRules().stream()
                .filter(ValidationRule::isActive)
                .flatMap(r -> r.getReadingTypes().stream())
                .anyMatch(t -> getChannel().getReadingTypes().contains(t));
        return activeRules;
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
        return channel.equals(((ChannelValidationImpl) o).channel) && meterActivationValidation.equals(((ChannelValidationImpl) o).meterActivationValidation);

    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, meterActivationValidation);
    }
    
    
    @Override
    public void updateLastChecked(Instant instant) {
    	if (lastChecked != null && lastChecked.isAfter(instant)) {
    		channel.get().findReadingQuality(Range.greaterThan(instant)).stream()
    			.filter(this::isRelevant)
    			.forEach(ReadingQualityRecord::delete);
    	}
    	this.lastChecked = instant;
    }
    
    private boolean isRelevant(ReadingQualityRecord readingQuality) {
    	return readingQuality.hasReasonabilityCategory() || readingQuality.hasValidationCategory(); 
    }
    	        
}
