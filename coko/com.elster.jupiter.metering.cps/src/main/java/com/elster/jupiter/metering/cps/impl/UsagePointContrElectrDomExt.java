/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.HasQuantityValueMin;
import com.elster.jupiter.util.units.Quantity;

public class UsagePointContrElectrDomExt extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
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

    @HasQuantityValueMin(min = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private Quantity contractedPower;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
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
