package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that the FirmwareComTaskExecution must use the 'Firmware management' ComTask
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ComTaskMustBeFirmwareManagementValidation.class })
public @interface ComTaskMustBeFirmwareManagement {

    String message() default "{" + MessageSeeds.Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
