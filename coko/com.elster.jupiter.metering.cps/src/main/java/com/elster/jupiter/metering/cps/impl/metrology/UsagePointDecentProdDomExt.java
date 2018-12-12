/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.MessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.HasQuantityValueMin;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class UsagePointDecentProdDomExt extends AbstractPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        INS_POWER {
            @Override
            public String javaName() {
                return "installationPower";
            }
        },
        CONVERTOR_POWER {
            @Override
            public String javaName() {
                return "convertorPower";
            }
        },
        DEC_PROD {
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

    @NotNull(message = "{CannotBeNull}")
    @HasQuantityValueMin(min = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private Quantity installationPower;
    @NotNull(message = "{CannotBeNull}")
    @HasQuantityValueMin(min = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private Quantity convertorPower;
    @NotNull(message = "{CannotBeNull}")
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String typeOfDecentralizedProduction;
    @NotNull(message = "{CannotBeNull}")
    private Instant commissioningDate;

    public UsagePointDecentProdDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public void setTypeOfDecentralizedProduction(String typeOfDecentralizedProduction) {
        this.typeOfDecentralizedProduction = typeOfDecentralizedProduction;
    }

    public void setCommissioningDate(Instant commissioningDate) {
        this.commissioningDate = commissioningDate;
    }

    public String getTypeOfDecentralizedProduction() {
        return typeOfDecentralizedProduction;
    }

    public Instant getCommissioningDate() {
        return commissioningDate;
    }

    public Quantity getConvertorPower() {
        return convertorPower;
    }

    public void setConvertorPower(Quantity convertorPower) {
        this.convertorPower = convertorPower;
    }

    public Quantity getInstallationPower() {
        return installationPower;
    }

    public void setInstallationPower(Quantity installationPower) {
        this.installationPower = installationPower;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setInstallationPower((Quantity) propertyValues.getProperty(Fields.INS_POWER.javaName()));
        this.setConvertorPower((Quantity) propertyValues.getProperty(Fields.CONVERTOR_POWER.javaName()));
        this.setTypeOfDecentralizedProduction((String) propertyValues.getProperty(Fields.DEC_PROD
                .javaName()));
        this.setCommissioningDate((Instant) propertyValues.getProperty(Fields.COMMISSIONING_DATE.javaName()));

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.INS_POWER.javaName(), this.getInstallationPower());
        propertySetValues.setProperty(Fields.CONVERTOR_POWER.javaName(), this.getConvertorPower());
        propertySetValues.setProperty(Fields.DEC_PROD.javaName(), this.getTypeOfDecentralizedProduction());
        propertySetValues.setProperty(Fields.COMMISSIONING_DATE.javaName(), this.getCommissioningDate());
    }

    @Override
    public void validateDelete() {

    }
}
