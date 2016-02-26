package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointTechnicalInstallationElectricityDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        DISTANCE_FROM_THE_SUBSTATION {
            @Override
            public String javaName() {
                return "distanceFromTheSubstation";
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

    @IsPresent
    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String distanceFromTheSubstation;
    private String feeder;
    private String utilizationCategory;

    public String getDistanceFromTheSubstation() {
        return distanceFromTheSubstation;
    }

    public String getFeeder() {
        return feeder;
    }

    public String getUtilizationCategory() {
        return utilizationCategory;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        usagePoint.set(domainInstance);
        this.distanceFromTheSubstation = (String) propertyValues.getProperty(Fields.DISTANCE_FROM_THE_SUBSTATION.javaName());
        this.feeder = (String) propertyValues.getProperty(Fields.FEEDER.javaName());
        this.utilizationCategory = (String) propertyValues.getProperty(Fields.UTILIZATION_CATEGORY.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.DISTANCE_FROM_THE_SUBSTATION.javaName(), this.getDistanceFromTheSubstation());
        propertySetValues.setProperty(Fields.FEEDER.javaName(), this.getFeeder());
        propertySetValues.setProperty(Fields.UTILIZATION_CATEGORY.javaName(), this.getUtilizationCategory());
    }

    @Override
    public void validateDelete() {

    }
}
