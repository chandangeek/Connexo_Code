package com.elster.jupiter.orm.associations;

import java.util.Date;
import com.google.common.base.Optional;

public interface TemporalReference<T extends Effectivity> extends TemporalAspect<T> {
	Optional<T> effective(Date when);
}
