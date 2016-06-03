package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ChannelValidationContainer {

	private final List<? extends ChannelValidation> channelValidations;

	private ChannelValidationContainer(List<? extends ChannelValidation> channelValidations) {
		this.channelValidations = channelValidations;
	}

	static ChannelValidationContainer of(List<? extends ChannelValidation> channelValidations) {
		return new ChannelValidationContainer(channelValidations);
	}
	
	void updateLastChecked(Instant date) {
		 channelValidations.stream()
				 .filter(ChannelValidation::hasActiveRules)
				 .forEach(channelValidation -> {
					 channelValidation.updateLastChecked(date);
					 channelValidation.getChannelsContainerValidation().save();
				 });
	}
	
	boolean isValidationActive() {
		return channelValidations.stream().anyMatch(ChannelValidation::hasActiveRules);
	}
	
	Optional<Instant> getLastChecked() {
		// if any is null, then we should return Optional.empty()
		return channelValidations.stream()
				.map(ChannelValidation::getLastChecked)
			.map(instant -> instant == null ? Instant.MIN : instant)
			.min(Comparator.<Instant>naturalOrder())
			.flatMap(instant -> Instant.MIN.equals(instant) ? Optional.empty() : Optional.of(instant));
	}
	
	boolean isEmpty() {
		return channelValidations.isEmpty();
	}

	public Stream<ChannelValidation> stream() {
		return channelValidations.stream().map(Function.<ChannelValidation>identity());
	}
}
