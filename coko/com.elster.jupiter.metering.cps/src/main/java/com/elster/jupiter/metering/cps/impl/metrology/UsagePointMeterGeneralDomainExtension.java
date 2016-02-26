package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointMeterGeneralDomainExtension implements PersistentDomainExtension<UsagePoint> {

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
        CLASS {
            @Override
            public String javaName() {
                return "class";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    public UsagePointMeterGeneralDomainExtension() {
        super();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String get_class() {
        return _class;
    }

    @IsPresent
    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String manufacturer;
    private String model;
    private String _class;


    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.model = (String) propertyValues.getProperty(Fields.MANUFACTURER.javaName());
        this.manufacturer = (String) propertyValues.getProperty(Fields.MODEL.javaName());
        this._class = (String) propertyValues.getProperty(Fields.CLASS.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.MODEL.javaName(), this.getModel());
        propertySetValues.setProperty(Fields.MANUFACTURER.javaName(), this.getManufacturer());
        propertySetValues.setProperty(Fields.CLASS.javaName(), this.get_class());
    }

    @Override
    public void validateDelete() {

    }
}
