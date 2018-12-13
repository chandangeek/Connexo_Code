/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.Size;

public class UsagePointTechElDomExt extends AbstractPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum FieldNames {
        DOMAIN("usagePoint", "usage_point"),
        CROSS_SECTIONAL_AREA("crossSectionalArea", "cross_sectional_area"),
        VOLTAGE_LEVEL("voltageLevel", "voltage_level"),
        CABLE_LOCATION("cableLocation", "cable_location");

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

    private Quantity crossSectionalArea;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String voltageLevel;
    private Quantity cableLocation;

    public UsagePointTechElDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Quantity getCrossSectionalArea() {
        return crossSectionalArea;
    }

    public void setCrossSectionalArea(Quantity crossSectionalArea) {
        this.crossSectionalArea = crossSectionalArea;
    }

    public String getVoltageLevel() {
        return voltageLevel;
    }

    public void setVoltageLevel(String voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    public Quantity getCableLocation() {
        return cableLocation;
    }

    public void setCableLocation(Quantity cableLocation) {
        this.cableLocation = cableLocation;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setCrossSectionalArea((Quantity) propertyValues.getProperty(FieldNames.CROSS_SECTIONAL_AREA.javaName()));
        this.setVoltageLevel((String) propertyValues.getProperty(FieldNames.VOLTAGE_LEVEL.javaName()));
        this.setCableLocation((Quantity) propertyValues.getProperty(FieldNames.CABLE_LOCATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CROSS_SECTIONAL_AREA.javaName(), this.getCrossSectionalArea());
        propertySetValues.setProperty(FieldNames.VOLTAGE_LEVEL.javaName(), this.getVoltageLevel());
        propertySetValues.setProperty(FieldNames.CABLE_LOCATION.javaName(), this.getCableLocation());
    }

    @Override
    public void validateDelete() {

    }
}