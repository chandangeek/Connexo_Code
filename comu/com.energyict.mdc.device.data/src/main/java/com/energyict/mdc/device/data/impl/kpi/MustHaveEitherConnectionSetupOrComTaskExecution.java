package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that a {@link DataCollectionKpi} must be configured
 * for one or both of connection setup or communication task execution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (15:09)
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {MustHaveEitherConnectionSetupOrComTaskExecutionValidator.class})
public @interface MustHaveEitherConnectionSetupOrComTaskExecution {

    String message() default "{" + MessageSeeds.Keys.EMPTY_DATA_COLLECTION_KPI + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}