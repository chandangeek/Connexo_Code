/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.MessageSeeds;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;

public class UsagePointGasCorrectionFactorsDomExt extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
            return "usagePoint";
        }
        },
        CLIMATE_CORRECTION_FACTOR {
            @Override
            public String javaName() {
                return "climateCorrectionFactor";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    Reference<UsagePoint> usagePoint = ValueReference.absent();

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<SyntheticLoadProfile> climateCorrectionFactor = ValueReference.absent();

    public UsagePointGasCorrectionFactorsDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public SyntheticLoadProfile getClimateCorrectionFactor(){
        return climateCorrectionFactor.get();
    }

    public void setClimateCorrectionFactor(SyntheticLoadProfile correctionFactor){
        this.climateCorrectionFactor.set(correctionFactor);
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setClimateCorrectionFactor((SyntheticLoadProfile) propertyValues.getProperty(Fields.CLIMATE_CORRECTION_FACTOR.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.CLIMATE_CORRECTION_FACTOR.javaName(), this.getClimateCorrectionFactor());
    }

    @Override
    public void validateDelete() {

    }
}
