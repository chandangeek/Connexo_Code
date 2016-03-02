package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointTechnicalWGTDomainExtension implements PersistentDomainExtension<UsagePoint> {
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

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String pipeSize;
    private String pipeType;
    private String pressureLevel;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
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

