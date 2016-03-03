package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.validation.constraints.Size;

public class UsagePointTechElectricityDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum FieldNames {
        DOMAIN("usagePoint", "usage_point"),
        CROSS_SECTIONAL_AREA("crossSectionalArea", "cross_sectional_area"),
        VOLTAGE_LEVEL("voltageLevel", "voltage_level");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<UsagePoint> usagePoint = Reference.empty();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String crossSectionalArea;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String voltageLevel;

    public UsagePointTechElectricityDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getCrossSectionalArea() {
        return crossSectionalArea;
    }

    public void setCrossSectionalArea(String crossSectionalArea) {
        this.crossSectionalArea = crossSectionalArea;
    }

    public String getVoltageLevel() {
        return voltageLevel;
    }

    public void setVoltageLevel(String voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setCrossSectionalArea((String) propertyValues.getProperty(FieldNames.CROSS_SECTIONAL_AREA.javaName()));
        this.setVoltageLevel((String) propertyValues.getProperty(FieldNames.VOLTAGE_LEVEL.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CROSS_SECTIONAL_AREA.javaName(), this.getCrossSectionalArea());
        propertySetValues.setProperty(FieldNames.VOLTAGE_LEVEL.javaName(), this.getVoltageLevel());
    }

    @Override
    public void validateDelete() {

    }
}