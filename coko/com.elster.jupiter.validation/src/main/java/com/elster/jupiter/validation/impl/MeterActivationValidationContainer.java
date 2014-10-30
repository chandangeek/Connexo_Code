package com.elster.jupiter.validation.impl;

import static java.util.Comparator.naturalOrder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.validation.ChannelValidation;

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
	
	void updateLastCheckedIfEarlier(Instant instant) {
		meterActivationValidations.forEach(meterActivationValidation -> updateLastCheckedIfEarlier(meterActivationValidation, instant));
	}
	
	void updateLastChecked(Instant instant) {
		meterActivationValidations.stream()
			.filter(IMeterActivationValidation::isActive)
			.forEach(meterActivationValidation -> updateLastChecked(meterActivationValidation, instant));
	}

	private void updateLastCheckedIfEarlier(IMeterActivationValidation meterActivationValidation, Instant instant) {
		meterActivationValidation.getChannelValidations().stream()
        	.filter(c -> isBefore(instant, c.getLastChecked()))
            .map(IChannelValidation.class::cast)
            .forEach(c -> c.updateLastChecked(instant));
		meterActivationValidation.save();
	}
	
	private void updateLastChecked(IMeterActivationValidation meterActivationValidation, Instant instant) {
		meterActivationValidation.getChannelValidations().stream()
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
	
    void updateLastChecked(Channel channel, Instant date) {
        channelValidationsFor(channel).updateLastChecked(date);
    }
    
    boolean isValidationActive(Channel channel) {
    	return channelValidationsFor(channel).isValidationActive();
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
	
