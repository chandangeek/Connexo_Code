package com.elster.jupiter.validation.impl;

import static java.util.Comparator.naturalOrder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

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
	
	void moveLastCheckedBefore(Instant instant) {
		meterActivationValidations.forEach( meterActivationValidation -> meterActivationValidation.moveLastCheckedBefore(instant));		
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
    
    @Deprecated
    Stream<IMeterActivationValidation> stream() {
    	return meterActivationValidations.stream();
    }

    private ChannelValidationContainer channelValidationsFor(Channel channel) {
    	return ChannelValidationContainer.of(getChannelValidations(channel));
    }
    
    private List<IChannelValidation> getChannelValidations(Channel channel) {
    	return meterActivationValidations.stream()
    		.map(meterActivation -> meterActivation.getChannelValidation(channel))
    		.filter(Optional::isPresent)
    		.map(Optional::get)
    		.map(IChannelValidation.class::cast)
    		.collect(Collectors.toList());
    }

	
}
	
