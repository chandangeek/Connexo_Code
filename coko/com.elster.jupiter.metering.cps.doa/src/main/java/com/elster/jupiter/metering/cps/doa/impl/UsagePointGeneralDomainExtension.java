/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

class UsagePointGeneralDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
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
        },

        ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES {
            @Override
            public String databaseName() {
                return "BE_MARKET_TYPE";
            }

            @Override
            public String javaName() {
                return "belgiumMarketType";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    @SuppressWarnings("unused") // Managed by CustomPropertySetService
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @NotNull(message = "{CannotBeNull}")
    private Boolean prepay;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String marketCodeSector;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String meteringPointType;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String belgiumMarketType;

    UsagePointGeneralDomainExtension() {
        super();
    }

    private Boolean getPrepay() {
        return prepay;
    }

    private void setPrepay(Boolean prepay) {
        this.prepay = prepay;
    }

    private String getMarketCodeSector() {
        return marketCodeSector;
    }

    private void setMarketCodeSector(String marketCodeSector) {
        this.marketCodeSector = marketCodeSector;
    }

    private String getMeteringPointType() {
        return meteringPointType;
    }

    private void setMeteringPointType(String meteringPointType) {
        this.meteringPointType = meteringPointType;
    }

    private String getBelgiumMarketType() {
        return belgiumMarketType;
    }

    private void setBelgiumMarketType(String belgiumMarketType) {
        this.belgiumMarketType = belgiumMarketType;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setPrepay((Boolean) propertyValues.getProperty(Fields.PREPAY.javaName()));
        this.setMarketCodeSector((String) propertyValues.getProperty(Fields.MARKET_CODE_SECTOR.javaName()));
        this.setMeteringPointType((String) propertyValues.getProperty(Fields.METERING_POINT_TYPE.javaName()));
        this.setBelgiumMarketType((String) propertyValues.getProperty(Fields.ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PREPAY.javaName(), this.getPrepay());
        propertySetValues.setProperty(Fields.MARKET_CODE_SECTOR.javaName(), this.getMarketCodeSector());
        propertySetValues.setProperty(Fields.METERING_POINT_TYPE.javaName(), this.getMeteringPointType());
        propertySetValues.setProperty(Fields.ADDED_FOR_AUDIT_TRAIL_TESTING_PURPOSES.javaName(), this.getBelgiumMarketType());
    }

    @Override
    public void validateDelete() {
        // No validation on delete required for now
    }

}