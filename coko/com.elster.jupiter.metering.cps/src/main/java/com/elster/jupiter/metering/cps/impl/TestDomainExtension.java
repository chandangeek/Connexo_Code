package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Quantity;

import java.time.Instant;

public class TestDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        QUANTITY {
            @Override
            public String javaName() {
                return "quantity";
            }
        },
        INSTANT {
            @Override
            public String javaName() {
                return "instant";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private Quantity quantity;
    private Instant instant;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return this.registeredCustomPropertySet.get();
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setQuantity((Quantity) propertyValues.getProperty(Fields.QUANTITY.javaName()));
        this.setInstant((Instant) propertyValues.getProperty(Fields.INSTANT.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.QUANTITY.javaName(), this.getQuantity());
        propertySetValues.setProperty(Fields.INSTANT.javaName(), this.getInstant());
    }

    @Override
    public void validateDelete() {

    }
}