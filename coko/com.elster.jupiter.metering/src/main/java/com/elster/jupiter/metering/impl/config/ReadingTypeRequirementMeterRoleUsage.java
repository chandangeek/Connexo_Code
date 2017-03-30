/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;

@SelfValid
public class ReadingTypeRequirementMeterRoleUsage implements SelfObjectValidator {

    public enum Fields {
        METER_ROLE("meterRole"),
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        READING_TYPE_REQUIREMENT("readingTypeRequirement");

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
    private final Thesaurus thesaurus;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public ReadingTypeRequirementMeterRoleUsage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

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
        UsagePointMetrologyConfiguration metrologyConfiguration = (UsagePointMetrologyConfiguration) getReadingTypeRequirement().getMetrologyConfiguration();
        if (!containsMyMeterRole(metrologyConfiguration)) {
            context.disableDefaultConstraintViolation();
            String formattedTemplate = this.thesaurus.getFormat(MessageSeeds.ROLE_IS_NOT_ALLOWED_ON_CONFIGURATION).format(this.getMeterRole().getDisplayName(), metrologyConfiguration.getName());
            context
                    .buildConstraintViolationWithTemplate(formattedTemplate)
                    .addPropertyNode(Fields.METER_ROLE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean containsMyMeterRole(UsagePointMetrologyConfiguration metrologyConfiguration) {
        return metrologyConfiguration.getMeterRoles().stream().anyMatch(getMeterRole()::equals);
    }

}
