package com.elster.jupiter.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class RangeSets {

	public static <T extends Comparable<? super T>> RangeSet<T> intersection(RangeSet<T> rangeSet1 , RangeSet<T> rangeSet2) {
		RangeSet<T> result = TreeRangeSet.create();
		Iterator<Range<T>> iterator1 = rangeSet1.asRanges().iterator();
		while (iterator1.hasNext()) {
			Range<T> range1 = iterator1.next();
			Iterator<Range<T>> iterator2 = rangeSet2.asRanges().iterator();
			while (iterator2.hasNext()) {
				Range<T> range2 = iterator2.next();
				if (range1.isConnected(range2)) {
					Range<T> intersection = range1.intersection(range2);
					if (!intersection.isEmpty()) {
						result.add(intersection);
					}
				}
			}
		}
		return result;
	} 
	
	public static <T extends Comparable<? super T>> RangeSet<T> of(Range<T> ... ranges) {
		ImmutableRangeSet.Builder<T> builder = ImmutableRangeSet.builder();
		Arrays.stream(ranges).forEach(range -> builder.add(range));
		return builder.build();
	}

}
