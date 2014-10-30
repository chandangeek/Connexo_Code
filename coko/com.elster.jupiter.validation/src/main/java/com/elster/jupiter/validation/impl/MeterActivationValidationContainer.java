package com.elster.jupiter.validation.impl;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ChannelValidation;
import com.google.common.collect.Ordering;

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
	
	void updateAllLastChecked(Instant instant) {
		meterActivationValidations.forEach(meterActivationValidation -> updateLastChecked(meterActivationValidation, instant));
	}
	
	void updateActiveLastChecked(Instant instant) {
		meterActivationValidations.stream()
			.filter(IMeterActivationValidation::isActive)
			.forEach(meterActivationValidation -> updateLastChecked(meterActivationValidation, instant));
	}

	private void updateLastChecked(IMeterActivationValidation meterActivationValidation, Instant instant) {
		meterActivationValidation.getChannelValidations().stream()
        	.filter(c -> isBefore(instant, c.getLastChecked()))
            .map(IChannelValidation.class::cast)
            .forEach(c -> c.updateLastChecked(instant));
		meterActivationValidation.save();
	}
	
	void activate() {
		meterActivationValidations.forEach(meterActivation -> {
			meterActivation.activate();
			meterActivation.save();
		});
	}
	
	private boolean isBefore(Instant instant, Instant mayBeNull) {
	    	return mayBeNull == null ? false : instant.isBefore(mayBeNull);
	}
	
    void updateActiveLastChecked(Channel channel, Instant date) {
        meterActivationValidations.stream()
    		.filter(IMeterActivationValidation::isActive)
    		.map(m -> m.getChannelValidation(channel))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(ChannelValidation::hasActiveRules)
            .map(ChannelValidationImpl.class::cast)
            .forEach(cv -> {
                cv.updateLastChecked(date);
                cv.getMeterActivationValidation().save();
            });
    }
    
    boolean isValidationActive(Channel channel) {
    	return meterActivationValidations.stream()
			.filter(IMeterActivationValidation::isActive)
            .map(m -> m.getChannelValidation(channel))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(ChannelValidation::hasActiveRules);
    }
    
    Optional<Instant> getLastChecked() {
    	return meterActivationValidations.stream()
    		.filter(IMeterActivationValidation::isActive)
            .filter(m -> m.getChannelValidations().stream().anyMatch(ChannelValidation::hasActiveRules))
            .map(IMeterActivationValidation::getMinLastChecked)
            .filter(Objects::nonNull)
            .min(naturalOrder());       		
    }

    Optional<Instant> getLastChecked(Channel channel) {
    	return meterActivationValidations.stream()
    		.filter(IMeterActivationValidation::isActive)
            .map(m -> m.getChannelValidation(channel))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(ChannelValidation::getLastChecked)
            .filter(Objects::nonNull)
            .min(naturalOrder());
    }

}
	
