package com.elster.jupiter.orm.associations;

import java.util.List;

import com.elster.jupiter.util.time.Interval;

public interface TemporalAspect<T extends Effectivity> {
	List<T> effective(Interval interval);
	List<T> all();
	boolean add(T element);
	boolean remove(T element);
}
