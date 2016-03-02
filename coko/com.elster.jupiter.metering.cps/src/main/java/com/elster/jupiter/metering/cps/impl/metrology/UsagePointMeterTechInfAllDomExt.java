package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointMeterTechInfAllDomExt implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        METER_MECHANISM {
            @Override
            public String javaName() {
                return "meterMechanism";
            }
        },
        METER_TYPE {
            @Override
            public String javaName() {
                return "meterType";
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

    private String meterMechanism;
    private String meterType;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getMeterType() {
        return meterType;
    }

    public String getMeterMechanism() {
        return meterMechanism;
    }

    public void setMeterMechanism(String meterMechanism) {
        this.meterMechanism = meterMechanism;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setMeterMechanism((String) propertyValues.getProperty(Fields.METER_MECHANISM.javaName()));
        this.setMeterType((String) propertyValues.getProperty(Fields.METER_TYPE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.METER_MECHANISM.javaName(), this.getMeterMechanism());
        propertySetValues.setProperty(Fields.METER_TYPE.javaName(), this.getMeterType());
    }

    @Override
    public void validateDelete() {

    }
}
