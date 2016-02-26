package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.time.Instant;

public class UsagePointDecentralizedProductionDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        INSTALLED_POWER {
            @Override
            public String javaName() {
                return "installedPower";
            }
        },
        CONVERTOR_POWER {
            @Override
            public String javaName() {
                return "convertorPower";
            }
        },
        TYPE_OF_DECENTRALIZED_PROD {
            @Override
            public String javaName() {
                return "typeOfDecentralizedProduction";
            }
        },
        COMMISSIONING_DATE {
            @Override
            public String javaName() {
                return "commissioningDate";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    @IsPresent
    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String installedPower;
    private String convertorPower;
    private String typeOfDecentralizedProduction;
    private Instant commissioningDate;

    public UsagePointDecentralizedProductionDomainExtension() {
        super();
    }

    public String getInstalledPower() {
        return installedPower;
    }

    public String getConvertorPower() {
        return convertorPower;
    }

    public String getTypeOfDecentralizedProduction() {
        return typeOfDecentralizedProduction;
    }

    public Instant getCommissioningDate() {
        return commissioningDate;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.installedPower = (String) propertyValues.getProperty(Fields.INSTALLED_POWER.javaName());
        this.convertorPower = (String) propertyValues.getProperty(Fields.CONVERTOR_POWER.javaName());
        this.typeOfDecentralizedProduction = (String) propertyValues.getProperty(Fields.TYPE_OF_DECENTRALIZED_PROD
                .javaName());
        this.commissioningDate = (Instant) propertyValues.getProperty(Fields.COMMISSIONING_DATE.javaName());

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.INSTALLED_POWER.javaName(), this.getInstalledPower());
        propertySetValues.setProperty(Fields.CONVERTOR_POWER.javaName(), this.getConvertorPower());
        propertySetValues.setProperty(Fields.TYPE_OF_DECENTRALIZED_PROD.javaName(), this.getTypeOfDecentralizedProduction());
        propertySetValues.setProperty(Fields.COMMISSIONING_DATE.javaName(), this.getCommissioningDate());
    }

    @Override
    public void validateDelete() {

    }
}
