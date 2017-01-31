/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.MessageSeeds;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.HasQuantityValueMin;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UsagePointAntennaDomExt extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        ANTENNA_POWER {
            @Override
            public String javaName() {
                return "antennaPower";
            }
        },
        ANTENNA_COUNT {
            @Override
            public String javaName() {
                return "antennaCount";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    Reference<UsagePoint> usagePoint = ValueReference.absent();

    @NotNull(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @HasQuantityValueMin(min = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private Quantity antennaPower;
    @Min(value = 0, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private long antennaCount;

    public UsagePointAntennaDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Quantity getAntennaPower() {
        return antennaPower;
    }

    public void setAntennaPower(Quantity antennaPower) {
        this.antennaPower = antennaPower;
    }

    public long getAntennaCount() {
        return antennaCount;
    }

    public void setAntennaCount(long antennaCount) {
        this.antennaCount = antennaCount;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setAntennaPower((Quantity) propertyValues.getProperty(Fields.ANTENNA_POWER.javaName()));
        this.setAntennaCount((long) propertyValues.getProperty(Fields.ANTENNA_COUNT.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.ANTENNA_POWER.javaName(), this.getAntennaPower());
        propertySetValues.setProperty(Fields.ANTENNA_COUNT.javaName(), this.getAntennaCount());
    }

    @Override
    public void validateDelete() {

    }
}
