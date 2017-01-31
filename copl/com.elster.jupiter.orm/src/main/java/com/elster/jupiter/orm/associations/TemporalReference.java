/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.Optional;

/*
 * used to implement a temporal relation where only one instance can be effective at a given time
 */
public interface TemporalReference<T extends Effectivity> extends TemporalAspect<T> {
	Optional<T> effective(Instant when);
}
