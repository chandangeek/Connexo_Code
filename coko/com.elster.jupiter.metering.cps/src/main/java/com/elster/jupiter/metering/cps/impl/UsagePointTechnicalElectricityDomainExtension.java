package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Quantity;

public class UsagePointTechnicalElectricityDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        CROSS_SECTIONAL_AREA {
            @Override
            public String javaName() {
                return "crossSectionalArea";
            }
        },
        CABLE_LOCATION {
            @Override
            public String javaName() {
                return "cableLocation";
            }
        },
        VOLTAGE_LEVEL {
            @Override
            public String javaName() {
                return "voltageLevel";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String crossSectionalArea;
    private Quantity cableLocation;
    private String voltageLevel;


    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.crossSectionalArea = (String) propertyValues.getProperty(Fields.CROSS_SECTIONAL_AREA.javaName());
        this.cableLocation = (Quantity) propertyValues.getProperty(Fields.CABLE_LOCATION.javaName());
        this.voltageLevel = (String) propertyValues.getProperty(Fields.VOLTAGE_LEVEL.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.CROSS_SECTIONAL_AREA.javaName(), this.crossSectionalArea);
        propertySetValues.setProperty(Fields.CABLE_LOCATION.javaName(), this.cableLocation);
        propertySetValues.setProperty(Fields.VOLTAGE_LEVEL.javaName(), this.voltageLevel);
    }

    @Override
    public void validateDelete() {

    }
}