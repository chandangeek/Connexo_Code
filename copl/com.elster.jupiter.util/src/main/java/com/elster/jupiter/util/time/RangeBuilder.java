/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import com.google.common.collect.Range;

import java.time.Instant;

public final class RangeBuilder {
	private Instant earliest;
	private Instant latest;

	public void add(Instant when) {
		if (earliest == null || when.isBefore(earliest)) {
			earliest = when;
		}
		if (latest == null || when.isAfter(latest)) {
			latest = when;
		}
	}

	public void add(Instant when, long length) {
		add(when);
		add(when.plusMillis(length));
	}

	public boolean hasRange() {
		return earliest != null && latest != null;
	}

	public Range<Instant> getRange() {
		if (earliest == null || latest == null) {
			throw new IllegalStateException();
		}
		return Range.closed(earliest, latest);
	}

}
