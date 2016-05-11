package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.ConstraintValidatorContext;

@SelfValid
public class ReadingTypeRequirementMeterRoleUsage implements SelfObjectValidator {

    public enum Fields {
        METER_ROLE("meterRole"),
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        READING_TYPE_REQUIREMENT("readingTypeRequirement"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    // we need this field to create a reverse map reference, it possible to receive metrology configuration from reading type requirement
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<UsagePointMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MeterRole> meterRole = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();

    public ReadingTypeRequirementMeterRoleUsage init(MeterRole meterRole, ReadingTypeRequirement readingTypeRequirement) {
        this.meterRole.set(meterRole);
        this.metrologyConfiguration.set((UsagePointMetrologyConfiguration) readingTypeRequirement.getMetrologyConfiguration());
        this.readingTypeRequirement.set(readingTypeRequirement);
        return this;
    }

    public MeterRole getMeterRole() {
        return this.meterRole.orNull();
    }

    public ReadingTypeRequirement getReadingTypeRequirement() {
        return this.readingTypeRequirement.orNull();
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        if (getMeterRole() != null && getReadingTypeRequirement() != null) {
            return validateThatMeterRoleIsAssignedToMetrologyConfiguration(context);
        }
        return true;
    }

    private boolean validateThatMeterRoleIsAssignedToMetrologyConfiguration(ConstraintValidatorContext context) {
        if (!((UsagePointMetrologyConfiguration) getReadingTypeRequirement().getMetrologyConfiguration()).getMeterRoles()
                .stream()
                .anyMatch(getMeterRole()::equals)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CAN_NOT_ADD_REQUIREMENT_WITH_THAT_ROLE + "}")
                    .addPropertyNode(Fields.METER_ROLE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
