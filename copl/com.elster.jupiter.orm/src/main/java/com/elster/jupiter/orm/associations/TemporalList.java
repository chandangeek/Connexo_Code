package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.List;

/*
 * used to implement a temporal association where multiple instances exists that are effective at the same time
 */
public interface TemporalList<T extends Effectivity> extends TemporalAspect<T> {
	List<T> effective(Instant when);
}
