package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

public class MeterActivationValidationContainer {
	
	private final List<IMeterActivationValidation> meterActivationValidations;
	
	private MeterActivationValidationContainer(List<IMeterActivationValidation> meterActivationValidations) {
		this.meterActivationValidations = meterActivationValidations;
	}
	
	static MeterActivationValidationContainer of(List<IMeterActivationValidation> meterActivationValidations) {
		return new MeterActivationValidationContainer(meterActivationValidations);
	}
	
	void validate() {
		meterActivationValidations.forEach(IMeterActivationValidation::validate);
	}
	
	public void validate(ReadingType readingType) {
		meterActivationValidations.forEach(meterActivationValidation -> meterActivationValidation.validate(readingType));		
	}
	
	void moveLastCheckedBefore(Map<Channel,Range<Instant>> ranges) {
		meterActivationValidations.forEach( meterActivationValidation -> meterActivationValidation.moveLastCheckedBefore(ranges));		
	}
	
	void updateLastChecked(Instant instant) {
		meterActivationValidations.stream()
			.filter(IMeterActivationValidation::isActive)
			.forEach(meterActivationValidation -> meterActivationValidation.updateLastChecked(instant));
	}
	
	void activate() {
		meterActivationValidations.forEach(meterActivation -> {
			meterActivation.activate();
			meterActivation.save();
		});
	}
	
    void updateLastChecked(Channel channel, Instant date) {
        channelValidationsFor(channel).updateLastChecked(date);
    }
    
    boolean isValidationActive(Channel channel) {
    	return channelValidationsFor(channel).isValidationActive();
    }
    
    Optional<Instant> getLastChecked() {
    	return meterActivationValidations.stream()
    		.filter(IMeterActivationValidation::isActive)
            .filter(m -> m.getChannelValidations().stream().anyMatch(IChannelValidation::hasActiveRules))
            .map(IMeterActivationValidation::getMinLastChecked)
            .filter(Objects::nonNull)
            .min(naturalOrder());       		
    }

    Optional<Instant> getLastChecked(Channel channel) {
    	return channelValidationsFor(channel).getLastChecked();
    }
    
    boolean isAllDataValidated() {
    	return meterActivationValidations.stream().allMatch(IMeterActivationValidation::isAllDataValidated);
    }

    ChannelValidationContainer channelValidationsFor(Channel channel) {
    	return ChannelValidationContainer.of(getChannelValidations(channel));
    }
    
    private List<IChannelValidation> getChannelValidations(Channel channel) {
    	return meterActivationValidations.stream()
    		.map(meterActivation -> meterActivation.getChannelValidation(channel))
    		.flatMap(asStream())
    		.map(IChannelValidation.class::cast)
    		.collect(Collectors.toList());
    }

    List<ValidationRuleSet> ruleSets() {
    	return meterActivationValidations.stream()
    		.map(IMeterActivationValidation::getRuleSet)
    		.collect(Collectors.toList());
    }

	public void update() {
		meterActivationValidations.forEach(IMeterActivationValidation::save);
	}
}
	
