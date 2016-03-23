package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UsagePointMetrologyGeneralDomExt implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        READ_CYCLE {
            @Override
            public String javaName() {
                return "readCycle";
            }
        },
        INFORMATION_FREQUENCY {
            @Override
            public String javaName() {
                return "informationFrequency";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String readCycle;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String informationFrequency;

    public UsagePointMetrologyGeneralDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getReadCycle() {
        return readCycle;
    }

    public void setReadCycle(String readCycle) {
        this.readCycle = readCycle;
    }

    public String getInformationFrequency() {
        return informationFrequency;
    }

    public void setInformationFrequency(String informationFrequency) {
        this.informationFrequency = informationFrequency;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setReadCycle((String) propertyValues.getProperty(Fields.READ_CYCLE.javaName()));
        this.setInformationFrequency((String) propertyValues.getProperty(Fields.INFORMATION_FREQUENCY.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.READ_CYCLE.javaName(), this.getReadCycle());
        propertySetValues.setProperty(Fields.INFORMATION_FREQUENCY.javaName(), this.getInformationFrequency());
    }

    @Override
    public void validateDelete() {

    }
}
