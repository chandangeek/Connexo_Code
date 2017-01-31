/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

public class AbstractTemporalAspect<T extends Effectivity> implements TemporalAspect<T> {
	
	private final List<T> values = new ArrayList<>();
	
	AbstractTemporalAspect() {	
	}
		
	@Override
	public boolean add(T element) {
		return values.add(element);
	}
	
	List<T> allEffective(Instant when) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (T each : values) {
			if (each.isEffectiveAt(when)) {
				builder.add(each);
			}
		}
		return builder.build();
	}


	@Override
	public List<T> effective(Range<Instant> range) {
		return values.stream()
			.filter(each -> each.getRange().isConnected(range) && !each.getRange().intersection(range).isEmpty())
			.collect(Collectors.toList());
	}


	@Override
	public List<T> all() {
		return ImmutableList.copyOf(values);
	}


	@Override
	public boolean remove(T element) {
		return values.remove(element);
	}

}
