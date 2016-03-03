package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.Size;

public class UsagePointMeterGnrDomainExtension implements PersistentDomainExtension<UsagePoint> {

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

    public UsagePointMeterGnrDomainExtension() {
        super();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String manufacturer;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String model;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String _class;

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setModel((String) propertyValues.getProperty(Fields.MANUFACTURER.javaName()));
        this.setManufacturer((String) propertyValues.getProperty(Fields.MODEL.javaName()));
        this.set_class((String) propertyValues.getProperty(Fields.CLASS.javaName()));
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
