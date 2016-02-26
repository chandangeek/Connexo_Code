package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointMeterTechInformationGTWDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        RECESSED_LENGTH {
            @Override
            public String javaName() {
                return "recessedLength";
            }
        },
        CONNECTION_TYPE {
            @Override
            public String javaName() {
                return "connectionType";
            }
        },
        CONVERSION_METROLOGY {
            @Override
            public String javaName() {
                return "conversionMetrology";
            }
        },
        CAPACITY_MINIMAL {
            @Override
            public String javaName() {
                return "capacityMinimal";
            }
        },
        CAPACITY_NOMINAL {
            @Override
            public String javaName() {
                return "capacityNominal";
            }
        },
        CAPACITY_MAXIMAL {
            @Override
            public String javaName() {
                return "capacityMaximal";
            }
        },
        PRESSURE_MAXIMAL {
            @Override
            public String javaName() {
                return "pressureMaximal";
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

    private String recessedLength;
    private String connectionType;
    private String conversionMetrology;
    private String capacityMinimal;
    private String capacityNominal;
    private String capacityMaximal;
    private String pressureMaximal;

    public UsagePointMeterTechInformationGTWDomainExtension() {
        super();
    }

    public String getRecessedLength() {
        return recessedLength;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getConversionMetrology() {
        return conversionMetrology;
    }

    public String getCapacityMinimal() {
        return capacityMinimal;
    }

    public String getCapacityNominal() {
        return capacityNominal;
    }

    public String getCapacityMaximal() {
        return capacityMaximal;
    }

    public String getPressureMaximal() {
        return pressureMaximal;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.recessedLength = (String) propertyValues.getProperty(Fields.RECESSED_LENGTH.javaName());
        this.connectionType = (String) propertyValues.getProperty(Fields.CONNECTION_TYPE.javaName());
        this.conversionMetrology = (String) propertyValues.getProperty(Fields.CONVERSION_METROLOGY.javaName());
        this.capacityMinimal = (String) propertyValues.getProperty(Fields.CAPACITY_MINIMAL.javaName());
        this.capacityNominal = (String) propertyValues.getProperty(Fields.CAPACITY_NOMINAL.javaName());
        this.capacityMaximal = (String) propertyValues.getProperty(Fields.CAPACITY_MAXIMAL.javaName());
        this.pressureMaximal = (String) propertyValues.getProperty(Fields.PRESSURE_MAXIMAL.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.RECESSED_LENGTH.javaName(), this.getRecessedLength());
        propertySetValues.setProperty(Fields.CONNECTION_TYPE.javaName(), this.getConnectionType());
        propertySetValues.setProperty(Fields.CONVERSION_METROLOGY.javaName(), this.getConversionMetrology());
        propertySetValues.setProperty(Fields.CAPACITY_MINIMAL.javaName(), this.getCapacityMinimal());
        propertySetValues.setProperty(Fields.CAPACITY_NOMINAL.javaName(), this.getCapacityNominal());
        propertySetValues.setProperty(Fields.CAPACITY_MAXIMAL.javaName(), this.getCapacityMaximal());
        propertySetValues.setProperty(Fields.PRESSURE_MAXIMAL.javaName(), this.getPressureMaximal());
    }

    @Override
    public void validateDelete() {

    }
}
