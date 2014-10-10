package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.List;

public interface TemporalList<T extends Effectivity> extends TemporalAspect<T> {
	List<T> effective(Instant when);
}
