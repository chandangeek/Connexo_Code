package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.Size;

public class UsagePointConvDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        SERIAL_NUMBER {
            @Override
            public String javaName() {
                return "serialNumber";
            }
        },
        N_C_DI {
            @Override
            public String javaName() {
                return "noOfCorDials";
            }
        },
        N_UNC_DI {
            @Override
            public String javaName() {
                return "noOfUncorDials";
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

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String serialNumber;
    private Quantity noOfCorDials;
    private Quantity noOfUncorDials;

    private Interval interval;

    public UsagePointConvDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Quantity getNoOfCorDials() {
        return noOfCorDials;
    }

    public void setNoOfCorDials(Quantity noOfCorDials) {
        this.noOfCorDials = noOfCorDials;
    }

    public Quantity getNoOfUncorDials() {
        return noOfUncorDials;
    }

    public void setNoOfUncorDials(Quantity noOfUncorDials) {
        this.noOfUncorDials = noOfUncorDials;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setSerialNumber((String) propertyValues.getProperty(Fields.SERIAL_NUMBER.javaName()));
        this.setNoOfCorDials((Quantity) propertyValues.getProperty(Fields.N_C_DI.javaName()));
        this.setNoOfUncorDials((Quantity) propertyValues.getProperty(Fields.N_UNC_DI.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.SERIAL_NUMBER.javaName(), this.getSerialNumber());
        propertySetValues.setProperty(Fields.N_C_DI.javaName(), this.getNoOfCorDials());
        propertySetValues.setProperty(Fields.N_UNC_DI.javaName(), this.getNoOfUncorDials());
    }

    @Override
    public void validateDelete() {

    }
}
