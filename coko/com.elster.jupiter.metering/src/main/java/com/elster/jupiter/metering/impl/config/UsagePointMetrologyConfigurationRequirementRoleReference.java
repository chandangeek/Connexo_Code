package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.ConstraintValidatorContext;

@SelfValid
public class UsagePointMetrologyConfigurationRequirementRoleReference implements SelfObjectValidator {

    public enum Fields {
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        METER_ROLE("meterRole"),
        READING_TYPE_REQUIREMENT("readingTypeRequirement"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<UPMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MeterRole> meterRole = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeRequirement> readingTypeRequirement = ValueReference.absent();

    public UsagePointMetrologyConfigurationRequirementRoleReference init(UPMetrologyConfiguration metrologyConfiguration, MeterRole meterRole, ReadingTypeRequirement readingTypeRequirement) {
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.meterRole.set(meterRole);
        this.readingTypeRequirement.set(readingTypeRequirement);
        return this;
    }

    public UPMetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    public MeterRole getMeterRole() {
        return this.meterRole.orNull();
    }

    public ReadingTypeRequirement getReadingTypeRequirement() {
        return this.readingTypeRequirement.orNull();
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        if (getMetrologyConfiguration() != null && getMeterRole() != null && getReadingTypeRequirement() != null) {
            return validateThatMeterRoleIsAssignedToMetrologyConfiguration(context);
        }
        return true;
    }

    private boolean validateThatMeterRoleIsAssignedToMetrologyConfiguration(ConstraintValidatorContext context) {
        if (!getMetrologyConfiguration().getMeterRoles()
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
