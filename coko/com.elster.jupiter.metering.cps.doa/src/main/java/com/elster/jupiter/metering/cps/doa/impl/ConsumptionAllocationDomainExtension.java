/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

class ConsumptionAllocationDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        USAGEPOINT {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },

        CLIENT_TYPE {
            @Override
            public String javaName() {
                return "clientType";
            }
        },

        ESTIMATED_ANNUAL_CONSUMPTION {
            @Override
            public String databaseName() {
                return "EAC";
            }

            @Override
            public String javaName() {
                return "estimatedAnnualConsumption";
            }
        },

        SYNTHETIC_LOAD_PROFILE {
            @Override
            public String databaseName() {
                return "SLP";
            }

            @Override
            public String javaName() {
                return "slp";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();

    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String clientType;
    @NotNull(message = "{CannotBeNull}")
    private BigDecimal estimatedAnnualConsumption;
    @IsPresent
    private Reference<SyntheticLoadProfile> slp = Reference.empty();

    ConsumptionAllocationDomainExtension() {
        super();
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.clientType = (String) propertyValues.getProperty(Fields.CLIENT_TYPE.javaName());
        this.estimatedAnnualConsumption = (BigDecimal) propertyValues.getProperty(Fields.ESTIMATED_ANNUAL_CONSUMPTION.javaName());
        this.slp.set((SyntheticLoadProfile) propertyValues.getProperty(Fields.SYNTHETIC_LOAD_PROFILE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.CLIENT_TYPE.javaName(), this.clientType);
        propertySetValues.setProperty(Fields.ESTIMATED_ANNUAL_CONSUMPTION.javaName(), this.estimatedAnnualConsumption);
        propertySetValues.setProperty(Fields.SYNTHETIC_LOAD_PROFILE.javaName(), this.slp.get());
    }

    @Override
    public void validateDelete() {
        // No validation on delete required for now
    }

}