package com.elster.jupiter.orm.associations;

import java.util.Date;
import java.util.List;

import com.google.common.base.Optional;

class TemporalListReference<T extends Effectivity> extends AbstractTemporalAspect<T> implements TemporalReference<T> {

	TemporalListReference() {
		super();
	}
	
	@Override
	public Optional<T> effective(Date when) {
		List<T> candidates = allEffective(when);
		if (candidates.size() > 1) {
			throw new IllegalStateException("More than one effective aspect at" + when);
		}
		return  candidates.isEmpty() ? Optional.<T>absent() : Optional.of(candidates.get(0));
	}
	
	@Override
	public boolean add(T element) {
		if (effective(element.getInterval()).isEmpty()) {
			return super.add(element);
		} else {
			throw new IllegalArgumentException();
		}
	}
		
}
