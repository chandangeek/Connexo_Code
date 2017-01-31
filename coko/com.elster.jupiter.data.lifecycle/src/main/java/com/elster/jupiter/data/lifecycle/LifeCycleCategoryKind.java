/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle;

import com.elster.jupiter.metering.PurgeConfiguration;

public enum LifeCycleCategoryKind {
	INTERVAL {
		@Override
		public boolean configure(PurgeConfiguration.Builder builder, LifeCycleCategory category) {
			builder.intervalRetention(category.getRetention());
			return true;
		}
	},
	DAILY {
		@Override
		public boolean configure(PurgeConfiguration.Builder builder, LifeCycleCategory category) {
			builder.dailyRetention(category.getRetention());
			return true;
		}
	},
	REGISTER {
		@Override
		public boolean configure(PurgeConfiguration.Builder builder, LifeCycleCategory category) {
			builder.registerRetention(category.getRetention());
			return true;
		}
	},
	ENDDEVICEEVENT,
	LOGGING,
	JOURNAL,
	WEBSERVICES;
	
	public boolean configure(PurgeConfiguration.Builder builder, LifeCycleCategory category) {
		return false;
	}
}
