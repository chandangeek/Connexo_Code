package com.elster.jupiter.orm.associations;

import java.util.Date;
import java.util.List;

public class TemporalArrayList <T extends Effectivity> extends AbstractTemporalAspect<T> implements TemporalList<T> {

	TemporalArrayList() {
		super();
	}
	
	@Override
	public List<T> effective(Date when) {
		return allEffective(when);
	}

}
