package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import static com.google.common.base.Preconditions.*;

public interface Effectivity {
	/*
	 * The getInterval() method is the contract for implementers of this Interface
	 * It should be called by users 
	 */
	Interval getInterval();
	
	/*
	 * API
	 */
	default Range<Instant> getRange() {
		return getInterval().toClosedOpenRange();
	}
	
	default boolean isEffectiveAt(Instant instant) {
		return getRange().contains(instant);
	}
	
	default boolean overlaps(Range<Instant> otherRange) {
		return ImmutableRangeSet.of(getRange()).subRangeSet(otherRange).isEmpty();
	}
	
	default Optional<Range<Instant>> intersection(Range<Instant> otherRange) {
		return Optional.of(getRange())
			.filter( thisRange -> thisRange.isConnected(otherRange))
			.map(thisRange -> thisRange.intersection(otherRange))
			.filter(range -> !range.isEmpty());
	}
	/*
	 * Helper method for validating range
	 */
	static Range<Instant> requireValid(Range<Instant> range) {
		Objects.requireNonNull(range);
		if (range.hasLowerBound()) {
			checkArgument(range.lowerBoundType() == BoundType.CLOSED, "Range must be closed at lower bound");
		}
		if (range.hasUpperBound()) {
			checkArgument(range.upperBoundType() == BoundType.OPEN, "Range must be open at upper bound");
		}
		return range;
	}
}
