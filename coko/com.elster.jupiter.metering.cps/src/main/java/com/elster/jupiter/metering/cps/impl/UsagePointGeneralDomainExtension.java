/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UsagePointGeneralDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },

        PREPAY {
            @Override
            public String javaName() {
                return "prepay";
            }
        },

        MARKET_CODE_SECTOR {
            @Override
            public String javaName() {
                return "marketCodeSector";
            }
        },

        METERING_POINT_TYPE {
            @Override
            public String javaName() {
                return "meteringPointType";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();

    @NotNull(message = "{CannotBeNull}")
    private Boolean prepay;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String marketCodeSector;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String meteringPointType;

    private Interval interval;

    public UsagePointGeneralDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Boolean getPrepay() {
        return prepay;
    }

    public void setPrepay(Boolean prepay) {
        this.prepay = prepay;
    }

    public String getMarketCodeSector() {
        return marketCodeSector;
    }

    public void setMarketCodeSector(String marketCodeSector) {
        this.marketCodeSector = marketCodeSector;
    }

    public String getMeteringPointType() {
        return meteringPointType;
    }

    public void setMeteringPointType(String meteringPointType) {
        this.meteringPointType = meteringPointType;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setPrepay((Boolean) propertyValues.getProperty(Fields.PREPAY.javaName()));
        this.setMarketCodeSector((String) propertyValues.getProperty(Fields.MARKET_CODE_SECTOR.javaName()));
        this.setMeteringPointType((String) propertyValues.getProperty(Fields.METERING_POINT_TYPE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PREPAY.javaName(), this.getPrepay());
        propertySetValues.setProperty(Fields.MARKET_CODE_SECTOR.javaName(), this.getMarketCodeSector());
        propertySetValues.setProperty(Fields.METERING_POINT_TYPE.javaName(), this.getMeteringPointType());
    }

    @Override
    public void validateDelete() {

    }
}
