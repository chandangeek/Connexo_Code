package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointTestPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        METROLOGY_CONFIG {
            @Override
            public String javaName() {
                return "metrologyConfiguration";
            }
        },
        NAME {
            @Override
            public String javaName() {
                return "name";
            }
        },
        ENHANCED_SUPPORT {
            @Override
            public String javaName() {
                return "enhancedSupport";
            }
        },;

        public abstract String javaName();

        public String databaseName() {
            return name();
        }

    }

    @IsPresent
    private Reference<UsagePoint> metrologyConfiguration = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @NotEmpty
    private String name;
    private boolean enhancedSupport;

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.metrologyConfiguration.set(domainInstance);
        this.name = (String) propertyValues.getProperty(Fields.NAME.javaName());
        this.enhancedSupport = (Boolean) propertyValues.getProperty(Fields.ENHANCED_SUPPORT.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.NAME.javaName(), this.name);
        propertySetValues.setProperty(Fields.ENHANCED_SUPPORT.javaName(), this.enhancedSupport);
    }

    @Override
    public void validateDelete() {
        // it always ok to delete these values
    }
}
