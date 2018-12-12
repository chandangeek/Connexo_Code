/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class RangeSets {

	public static <T extends Comparable<? super T>> RangeSet<T> intersection(RangeSet<T> rangeSet1 , RangeSet<T> rangeSet2) {
		return rangeSet2.asRanges().stream()
			.<RangeSet<T>>collect(
				TreeRangeSet::create, 
				(rangeSet,range) -> rangeSet.addAll(rangeSet1.subRangeSet(range)),
				(set1, set2) -> set1.addAll(set2));		
	} 	

}
