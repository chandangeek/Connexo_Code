/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


class TemporalListReference<T extends Effectivity> extends AbstractTemporalAspect<T> implements TemporalReference<T> {

	TemporalListReference() {
		super();
	}
	
	@Override
	public Optional<T> effective(Instant when) {
		List<T> candidates = allEffective(when);
		if (candidates.size() > 1) {
			throw new IllegalStateException("More than one effective aspect at" + when);
		}
		return  candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(0));
	}
	
	@Override
	public boolean add(T element) {
		if (effective(element.getRange()).isEmpty()) {
			return super.add(element);
		} else {
			throw new IllegalArgumentException();
		}
	}
		
}
