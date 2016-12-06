package com.elster.jupiter.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * documentation annotation to mark an enum as being mapped to columns by its ordinal value, this implies the order of the enum members may not be changed
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface MappedByOrdinal {
}
