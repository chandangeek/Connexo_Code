package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.elster.jupiter.validation.ChannelValidation;

public class ChannelValidationContainer {

	private final List<IChannelValidation> channelValidations;
	
	private ChannelValidationContainer(List<IChannelValidation> channelValidations) {
		this.channelValidations = channelValidations;
	}
	
	static ChannelValidationContainer of(List<IChannelValidation> channelValidations) {
		return new ChannelValidationContainer(channelValidations);
	}
	
	void updateLastChecked(Instant date) {
		 channelValidations.stream()
         	.filter(IChannelValidation::hasActiveRules)
         	.forEach(cv -> {
         		cv.updateLastChecked(date);
         		cv.getMeterActivationValidation().save();
         	});
	}
	
	boolean isValidationActive() {
    	return channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
    }
	
	Optional<Instant> getLastChecked() {
		return channelValidations.stream()
			.map(IChannelValidation::getLastChecked)	
			.min(Comparator.naturalOrder());
	}
	
}
