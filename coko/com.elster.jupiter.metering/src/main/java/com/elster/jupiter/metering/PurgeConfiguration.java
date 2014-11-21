package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public final class PurgeConfiguration {
	
	private Instant registerLimit;
	private Instant intervalLimit;
	private Instant dailyLimit;
	private Instant eventLimit;
	private Logger logger;
	
	private PurgeConfiguration() {		
	}
	
	public Optional<Instant> getRegisterLimit() {
		return Optional.ofNullable(registerLimit);
		
	}
	
	public Optional<Instant> getIntervalLimit() {
		return Optional.ofNullable(intervalLimit);
	}
	
	public Optional<Instant> getDailyLimit() {
		return Optional.ofNullable(dailyLimit);
	}
	
	public Optional<Instant> getEventLimit() {
		return Optional.ofNullable(eventLimit);
	}
	
	public Optional<Instant> getReadingQualityLimit() {
		List<Instant> instants = Arrays.asList(registerLimit, intervalLimit, dailyLimit);
		// do not purge reading qualities if not all kind of readings are purged
		if (instants.stream().anyMatch(Objects::isNull)) {
			return Optional.empty();
		} else {
			return instants.stream().min(Comparator.naturalOrder());
		}
	}
	
	public Logger getLogger() {
		return logger == null ? Logger.getLogger(getClass().getPackage().getName()) : logger;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		
		private PurgeConfiguration product;
		
		private Builder() {
			this.product = new PurgeConfiguration();
		}
		
		public Builder registerLimit(Instant instant) {
			product.registerLimit = Objects.requireNonNull(instant);
			return this;
		}
		
		public Builder intervalLimit(Instant instant) {
			product.intervalLimit = Objects.requireNonNull(instant);
			return this;
		}
		
		public Builder dailyLimit(Instant instant) {
			product.dailyLimit = Objects.requireNonNull(instant);
			return this;
		}
		
		public Builder eventLimit(Instant instant) {
			product.eventLimit = Objects.requireNonNull(instant);
			return this;
		}
		
		public Builder logger(Logger logger) {
			product.logger = Objects.requireNonNull(logger);
			return this;
		}
		
		public PurgeConfiguration build() {
			return product;
		}
	}

}
