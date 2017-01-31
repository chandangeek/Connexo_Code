/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * documentation annotation to mark a class as containing literal sql fragments
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface LiteralSql {
}
