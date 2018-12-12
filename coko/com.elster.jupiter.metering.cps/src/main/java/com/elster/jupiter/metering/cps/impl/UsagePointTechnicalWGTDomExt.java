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
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.Size;

public class UsagePointTechnicalWGTDomExt extends AbstractPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        PIPE_SIZE {
            @Override
            public String javaName() {
                return "pipeSize";
            }
        },
        PIPE_TYPE {
            @Override
            public String javaName() {
                return "pipeType";
            }
        },
        PRESSURE_LEVEL {
            @Override
            public String javaName() {
                return "pressureLevel";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String pipeSize;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String pipeType;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String pressureLevel;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public String getPipeSize() {
        return pipeSize;
    }

    public void setPipeSize(String pipeSize) {
        this.pipeSize = pipeSize;
    }

    public String getPipeType() {
        return pipeType;
    }

    public void setPipeType(String pipeType) {
        this.pipeType = pipeType;
    }

    public String getPressureLevel() {
        return pressureLevel;
    }

    public void setPressureLevel(String pressureLevel) {
        this.pressureLevel = pressureLevel;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        usagePoint.set(domainInstance);
        this.setPipeSize((String) propertyValues.getProperty(Fields.PIPE_SIZE.javaName()));
        this.setPipeType((String) propertyValues.getProperty(Fields.PIPE_TYPE.javaName()));
        this.setPressureLevel((String) propertyValues.getProperty(Fields.PRESSURE_LEVEL.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PIPE_SIZE.javaName(), this.getPipeSize());
        propertySetValues.setProperty(Fields.PIPE_TYPE.javaName(), this.getPipeType());
        propertySetValues.setProperty(Fields.PRESSURE_LEVEL.javaName(), this.getPressureLevel());
    }

    @Override
    public void validateDelete() {

    }
}

