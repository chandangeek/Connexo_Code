package com.elster.jupiter.orm.associations;

import java.time.Instant;
import java.util.Optional;

public interface TemporalReference<T extends Effectivity> extends TemporalAspect<T> {
	Optional<T> effective(Instant when);
}
