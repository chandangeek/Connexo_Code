/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.Size;

public class UsagePointMeterGnrDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        MANUFACTURER {
            @Override
            public String javaName() {
                return "manufacturer";
            }
        },
        MODEL {
            @Override
            public String javaName() {
                return "model";
            }
        },
        CLAZZ {
            @Override
            public String javaName() {
                return "clazz";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    Reference<UsagePoint> usagePoint = ValueReference.absent();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String manufacturer;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String model;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String clazz;

    public UsagePointMeterGnrDomainExtension() {
        super();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setModel((String) propertyValues.getProperty(Fields.MANUFACTURER.javaName()));
        this.setManufacturer((String) propertyValues.getProperty(Fields.MODEL.javaName()));
        this.setClazz((String) propertyValues.getProperty(Fields.CLAZZ.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.MODEL.javaName(), this.getModel());
        propertySetValues.setProperty(Fields.MANUFACTURER.javaName(), this.getManufacturer());
        propertySetValues.setProperty(Fields.CLAZZ.javaName(), this.getClazz());
    }

    @Override
    public void validateDelete() {

    }
}
