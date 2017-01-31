/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.List;

public class TemporalArrayList <T extends Effectivity> extends AbstractTemporalAspect<T> implements TemporalList<T> {

	TemporalArrayList() {
		super();
	}
	
	@Override
	public List<T> effective(Instant when) {
		return allEffective(when);
	}

}
