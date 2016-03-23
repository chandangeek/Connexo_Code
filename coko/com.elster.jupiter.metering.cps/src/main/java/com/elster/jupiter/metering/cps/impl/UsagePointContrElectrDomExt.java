package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

public class UsagePointContrElectrDomExt implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        CONTRACTED_POWER {
            @Override
            public String javaName() {
                return "contractedPower";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private Quantity contractedPower;

    private Interval interval;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public Quantity getContractedPower() {
        return contractedPower;
    }

    public void setContractedPower(Quantity contractedPower) {
        this.contractedPower = contractedPower;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setContractedPower((Quantity) propertyValues.getProperty(Fields.CONTRACTED_POWER.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.CONTRACTED_POWER.javaName(), this.getContractedPower());
    }

    @Override
    public void validateDelete() {

    }
}
