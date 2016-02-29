package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointGeneralDomainExtension implements PersistentDomainExtension<UsagePoint> {
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

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private Boolean prepay;
    private String marketCodeSector;
    private String meteringPointType;

    public Boolean getPrepay() {
        return this.prepay;
    }

    public String getMarketCodeSector() {
        return this.marketCodeSector;
    }

    public String getMeteringPointType() {
        return this.meteringPointType;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.prepay = (Boolean) propertyValues.getProperty(Fields.PREPAY.javaName());
        this.marketCodeSector = (String) propertyValues.getProperty(Fields.MARKET_CODE_SECTOR.javaName());
        this.meteringPointType = (String) propertyValues.getProperty(Fields.METERING_POINT_TYPE.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.PREPAY.javaName(), getPrepay());
        propertySetValues.setProperty(Fields.MARKET_CODE_SECTOR.javaName(), getMarketCodeSector());
        propertySetValues.setProperty(Fields.METERING_POINT_TYPE.javaName(), getMeteringPointType());
    }

    @Override
    public void validateDelete() {

    }
}
