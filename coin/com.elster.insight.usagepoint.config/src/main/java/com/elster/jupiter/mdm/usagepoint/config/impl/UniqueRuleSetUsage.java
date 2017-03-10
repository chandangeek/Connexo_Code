/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {UniqueRuleSetUsageValidator.class})
public @interface UniqueRuleSetUsage {

    String message() default "{" + MessageSeeds.Keys.DUPLICATE_VALIDATION_RULE_USAGE + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}