package com.elster.jupiter.metering.cps.impl.metrology;

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
import java.time.Instant;

public class UsagePointDecentProdDomExt implements PersistentDomainExtension<UsagePoint> {

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

    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String installedPower;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String convertorPower;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String typeOfDecentralizedProduction;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private Instant commissioningDate;

    public UsagePointDecentProdDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public void setTypeOfDecentralizedProduction(String typeOfDecentralizedProduction) {
        this.typeOfDecentralizedProduction = typeOfDecentralizedProduction;
    }

    public void setInstalledPower(String installedPower) {
        this.installedPower = installedPower;
    }

    public void setConvertorPower(String convertorPower) {
        this.convertorPower = convertorPower;
    }

    public void setCommissioningDate(Instant commissioningDate) {
        this.commissioningDate = commissioningDate;
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
        this.setInstalledPower((String) propertyValues.getProperty(Fields.INSTALLED_POWER.javaName()));
        this.setConvertorPower((String) propertyValues.getProperty(Fields.CONVERTOR_POWER.javaName()));
        this.setTypeOfDecentralizedProduction((String) propertyValues.getProperty(Fields.TYPE_OF_DECENTRALIZED_PROD
                .javaName()));
        this.setCommissioningDate((Instant) propertyValues.getProperty(Fields.COMMISSIONING_DATE.javaName()));

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
