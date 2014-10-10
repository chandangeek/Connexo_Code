package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.List;

import com.google.common.collect.Range;

public interface TemporalAspect<T extends Effectivity> {
	List<T> effective(Range<Instant> range);
	List<T> all();
	boolean add(T element);
	boolean remove(T element);
}
