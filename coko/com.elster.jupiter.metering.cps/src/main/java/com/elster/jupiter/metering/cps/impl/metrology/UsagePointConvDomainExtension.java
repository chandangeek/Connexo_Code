package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

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
        NO_OF_CORRECTED_DIALS {
            @Override
            public String javaName() {
                return "noOfCorrectedDials";
            }
        },
        NO_OF_UNCORRECTED_DIALS {
            @Override
            public String javaName() {
                return "noOfUncorrectedDials";
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

    private String serialNumber;
    private String noOfCorrectedDials;
    private String noOfUncorrectedDials;

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

    public String getNoOfCorrectedDials() {
        return noOfCorrectedDials;
    }

    public void setNoOfCorrectedDials(String noOfCorrectedDials) {
        this.noOfCorrectedDials = noOfCorrectedDials;
    }

    public String getNoOfUncorrectedDials() {
        return noOfUncorrectedDials;
    }

    public void setNoOfUncorrectedDials(String noOfUncorrectedDials) {
        this.noOfUncorrectedDials = noOfUncorrectedDials;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setSerialNumber((String) propertyValues.getProperty(Fields.SERIAL_NUMBER.javaName()));
        this.setNoOfCorrectedDials((String) propertyValues.getProperty(Fields.NO_OF_CORRECTED_DIALS.javaName()));
        this.setNoOfUncorrectedDials((String) propertyValues.getProperty(Fields.NO_OF_UNCORRECTED_DIALS.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.SERIAL_NUMBER.javaName(), this.getSerialNumber());
        propertySetValues.setProperty(Fields.NO_OF_CORRECTED_DIALS.javaName(), this.getNoOfCorrectedDials());
        propertySetValues.setProperty(Fields.NO_OF_UNCORRECTED_DIALS.javaName(), this.getNoOfUncorrectedDials());
    }

    @Override
    public void validateDelete() {

    }
}
