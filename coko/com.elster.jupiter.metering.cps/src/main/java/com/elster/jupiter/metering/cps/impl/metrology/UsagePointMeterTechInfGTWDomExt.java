package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.MessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.HasQuantityValueMin;
import com.elster.jupiter.util.units.Quantity;

import javax.validation.constraints.Size;

public class UsagePointMeterTechInfGTWDomExt implements PersistentDomainExtension<UsagePoint> {

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
        CAPACITY_MIN {
            @Override
            public String javaName() {
                return "capacityMin";
            }
        },
        CAPACITY_NOM {
            @Override
            public String javaName() {
                return "capacityNom";
            }
        },
        CAPACITY_MAX {
            @Override
            public String javaName() {
                return "capacityMax";
            }
        },
        PRESSURE_MAX {
            @Override
            public String javaName() {
                return "pressureMax";
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
    private String recessedLength;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String connectionType;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String conversionMetrology;
    //    private Quantity capacityMin;
//    private Quantity capacityNom;
//    private Quantity capacityMax;
    @HasQuantityValueMin(min = 0, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUANTITY_MIN_VALUE + "}")
    private Quantity pressureMax;

    public UsagePointMeterTechInfGTWDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
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

//    public Quantity getCapacityMin() {
//        return capacityMin;
//    }
////
////    public void setCapacityMin(Quantity capacityMin) {
////        this.capacityMin = capacityMin;
////    }
////
//    public Quantity getCapacityNom() {
//        return capacityNom;
//    }
//
//    public void setCapacityNom(Quantity capacityNom) {
//        this.capacityNom = capacityNom;
//    }
//
//    public Quantity getCapacityMax() {
//        return capacityMax;
//    }
//
//    public void setCapacityMax(Quantity capacityMax) {
//        this.capacityMax = capacityMax;
//    }

    public Quantity getPressureMax() {
        return pressureMax;
    }

    public void setPressureMax(Quantity pressureMax) {
        this.pressureMax = pressureMax;
    }

    public void setRecessedLength(String recessedLength) {
        this.recessedLength = recessedLength;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public void setConversionMetrology(String conversionMetrology) {
        this.conversionMetrology = conversionMetrology;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setRecessedLength((String) propertyValues.getProperty(Fields.RECESSED_LENGTH.javaName()));
        this.setConnectionType((String) propertyValues.getProperty(Fields.CONNECTION_TYPE.javaName()));
        this.setConversionMetrology((String) propertyValues.getProperty(Fields.CONVERSION_METROLOGY.javaName()));
//        this.setCapacityMin((Quantity) propertyValues.getProperty(Fields.CAPACITY_MIN.javaName()));
//        this.setCapacityNom((Quantity) propertyValues.getProperty(Fields.CAPACITY_NOM.javaName()));
//        this.setCapacityMax((Quantity) propertyValues.getProperty(Fields.CAPACITY_MAX.javaName()));
        this.setPressureMax((Quantity) propertyValues.getProperty(Fields.PRESSURE_MAX.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.RECESSED_LENGTH.javaName(), this.getRecessedLength());
        propertySetValues.setProperty(Fields.CONNECTION_TYPE.javaName(), this.getConnectionType());
        propertySetValues.setProperty(Fields.CONVERSION_METROLOGY.javaName(), this.getConversionMetrology());
//        propertySetValues.setProperty(Fields.CAPACITY_MIN.javaName(), this.getCapacityMin());
//        propertySetValues.setProperty(Fields.CAPACITY_NOM.javaName(), this.getCapacityNom());
//        propertySetValues.setProperty(Fields.CAPACITY_MAX.javaName(), this.getCapacityMax());
        propertySetValues.setProperty(Fields.PRESSURE_MAX.javaName(), this.getPressureMax());
    }

    @Override
    public void validateDelete() {

    }
}
