package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.topology.DataLoggerReference;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Validates the new dataLoggerReference doesn't violate the unique key constraint (and provide a proper error message when it's not so)
 * This could only be the case if you try to link a dataloggerSlave with the same datalogger at the same Instant
 */
public class UniqueStartTimeValidator implements ConstraintValidator<ValidateUniqueStartTime, DataLoggerReferenceImpl> {

    @Inject
    private DataModel dataModel;

    @Override
    public void initialize(ValidateUniqueStartTime constraintAnnotation) {

    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext context) {

        // did not want to put this into the topology service as it is a unique call that only make sense when we try to avoid the unique key constraint
        Condition condition = where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(dataLoggerReference.getGateway())
                .and(where("interval.start").in(Range.closed(dataLoggerReference.getRange().lowerEndpoint().toEpochMilli(), dataLoggerReference.getRange().lowerEndpoint().toEpochMilli())));
        Optional<DataLoggerReference> duplicateReference = this.dataModel.mapper(DataLoggerReference.class).select(condition).stream().collect(Collectors.toList())
                .stream().filter(reference -> reference.getOrigin().getmRID().equals(dataLoggerReference.getOrigin().getmRID()) && reference.getRange().lowerEndpoint().equals(dataLoggerReference
                        .getRange().lowerEndpoint())).findAny();
        if (duplicateReference.isPresent()) {
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DATA_LOGGER_UNIQUE_KEY_VIOLATION + "}").addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
