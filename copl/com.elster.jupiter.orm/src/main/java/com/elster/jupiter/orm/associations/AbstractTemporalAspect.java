package com.elster.jupiter.orm.associations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;

public class AbstractTemporalAspect<T extends Effectivity> implements TemporalAspect<T> {
	
	private final List<T> values = new ArrayList<>();
	
	AbstractTemporalAspect() {	
	}
		
	@Override
	public boolean add(T element) {
		return values.add(element);
	}
	
	List<T> allEffective(Date when) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (T each : values) {
			if (each.getInterval().isEffective(when)) {
				builder.add(each);
			}
		}
		return builder.build();
	}


	@Override
	public List<T> effective(Interval interval) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (T each : values) {
			if (each.getInterval().overlaps(interval)) {
				builder.add(each);
			}
		}
		return builder.build();
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
