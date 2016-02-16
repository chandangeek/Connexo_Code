package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointSimplePersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        USAGE_POINT {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        NAME {
            @Override
            public String javaName() {
                return "name";
            }
        },
        COMBOBOX {
            @Override
            public String javaName() {
                return "combobox";
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
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();
    @NotEmpty(message = "{CanNotBeEmpty}")
    private String name;
    private String combobox;
    private boolean enhancedSupport;

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.name = (String) propertyValues.getProperty(Fields.NAME.javaName());
        this.enhancedSupport = (Boolean) propertyValues.getProperty(Fields.ENHANCED_SUPPORT.javaName());
        this.combobox = (String) propertyValues.getProperty(Fields.COMBOBOX.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.NAME.javaName(), this.name);
        propertySetValues.setProperty(Fields.ENHANCED_SUPPORT.javaName(), this.enhancedSupport);
        propertySetValues.setProperty(Fields.COMBOBOX.javaName(), this.combobox);
    }

    @Override
    public void validateDelete() {
        // it always ok to delete these values
    }
}
