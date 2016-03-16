package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UsagePointTechInstElectrDE implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        SUBSTATION_DISTANCE {
            @Override
            public String javaName() {
                return "substationDistance";
            }
        },
        FEEDER {
            @Override
            public String javaName() {
                return "feeder";
            }
        },
        UTILIZATION_CATEGORY {
            @Override
            public String javaName() {
                return "utilizationCategory";
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
    private Quantity substationDistance;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String feeder;
    private Quantity utilizationCategory;

    public UsagePointTechInstElectrDE() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public void setFeeder(String feeder) {
        this.feeder = feeder;
    }

    public String getFeeder() {
        return feeder;
    }

    public Quantity getSubstationDistance() {
        return substationDistance;
    }

    public void setSubstationDistance(Quantity substationDistance) {
        this.substationDistance = substationDistance;
    }

    public Quantity getUtilizationCategory() {
        return utilizationCategory;
    }

    public void setUtilizationCategory(Quantity utilizationCategory) {
        this.utilizationCategory = utilizationCategory;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        usagePoint.set(domainInstance);
        this.setSubstationDistance((Quantity) propertyValues.getProperty(Fields.SUBSTATION_DISTANCE.javaName()));
        this.setFeeder((String) propertyValues.getProperty(Fields.FEEDER.javaName()));
        this.setUtilizationCategory((Quantity) propertyValues.getProperty(Fields.UTILIZATION_CATEGORY.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.SUBSTATION_DISTANCE.javaName(), this.getSubstationDistance());
        propertySetValues.setProperty(Fields.FEEDER.javaName(), this.getFeeder());
        propertySetValues.setProperty(Fields.UTILIZATION_CATEGORY.javaName(), this.getUtilizationCategory());
    }

    @Override
    public void validateDelete() {

    }
}
