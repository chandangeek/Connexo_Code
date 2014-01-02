package com.elster.jupiter.orm.associations;

import java.util.Date;
import java.util.List;

public interface TemporalList<T extends Effectivity> extends TemporalAspect<T> {
	List<T> effective(Date when);
}
