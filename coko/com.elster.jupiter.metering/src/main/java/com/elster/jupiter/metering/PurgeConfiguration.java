/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.time.Period;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public final class PurgeConfiguration {
	
	private Period registerRetention;
	private Period intervalRetention;
	private Period dailyRetention;
	private Period eventRetention;
	private Logger logger;
	
	private PurgeConfiguration() {		
	}
	
	public Optional<Period> registerRetention() {
		return Optional.ofNullable(registerRetention);
		
	}
	
	public Optional<Period> intervalRetention() {
		return Optional.ofNullable(intervalRetention);
	}
	
	public Optional<Period> dailyRetention() {
		return Optional.ofNullable(dailyRetention);
	}
	
	public Optional<Period> eventRetention() {
		return Optional.ofNullable(eventRetention);
	}
	
	public Optional<Period> readingQualityRetention() {
		List<Period> periods = Arrays.asList(registerRetention, intervalRetention, dailyRetention);
		// do not purge reading qualities if not all kind of readings are purged
		if (periods.stream().anyMatch(Objects::isNull)) {
			return Optional.empty();
		} else {
			return periods.stream().max(Comparator.comparing(period -> period.toTotalMonths() * 30 + period.getDays()));
		}
	}
	
	public int registerDays() {
		return days(registerRetention);
	}
	
	public int intervalDays() {
		return days(intervalRetention);
	}
	
	public int dailyDays() {
		return days(dailyRetention);
	}
	
	private int days(Period period) {
		if (period == null) {
			throw new IllegalStateException();
		}
		return (int) period.toTotalMonths() * 30 + period.getDays();				
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
		
		public Builder registerRetention(Period period) {
			product.registerRetention = Objects.requireNonNull(period);
			return this;
		}
		
		public Builder intervalRetention(Period period) {
			product.intervalRetention = Objects.requireNonNull(period);
			return this;
		}
		
		public Builder dailyRetention(Period period) {
			product.dailyRetention = Objects.requireNonNull(period);
			return this;
		}
		
		public Builder eventRetention(Period period) {
			product.eventRetention = Objects.requireNonNull(period);
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
