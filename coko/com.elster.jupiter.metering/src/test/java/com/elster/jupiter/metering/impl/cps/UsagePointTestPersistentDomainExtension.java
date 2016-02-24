package com.elster.jupiter.metering.impl.cps;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

public class UsagePointTestPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @NotEmpty
    private String name;
    private boolean enhancedSupport;
    private Interval interval;

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.name = (String) propertyValues.getProperty(CustomPropertySetAttributes.NAME.propertyKey());
        this.enhancedSupport = (Boolean) propertyValues.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), this.name);
        propertySetValues.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), this.enhancedSupport);
    }

    @Override
    public void validateDelete() {
        // it always ok to delete these values
    }
}
