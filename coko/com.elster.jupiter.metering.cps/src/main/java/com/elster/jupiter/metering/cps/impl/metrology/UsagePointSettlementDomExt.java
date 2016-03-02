package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

public class UsagePointSettlementDomExt implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        SETTLEMENT_AREA {
            @Override
            public String javaName() {
                return "settlementArea";
            }
        },
        SETTLEMENT_METHOD {
            @Override
            public String javaName() {
                return "settlementMethod";
            }
        },
        GRIDFEE_TIMEFRAME {
            @Override
            public String javaName() {
                return "gridfeeTimeframe";
            }
        },
        GRIDFEE_TARIFFCODE {
            @Override
            public String javaName() {
                return "gridfeeTariffcode";
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

    private String settlementArea;
    private String settlementMethod;
    private String gridfeeTimeframe;
    private String gridfeeTariffcode;

    public UsagePointSettlementDomExt() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getSettlementArea() {
        return settlementArea;
    }

    public String getSettlementMethod() {
        return settlementMethod;
    }

    public String getGridfeeTimeframe() {
        return gridfeeTimeframe;
    }

    public String getGridfeeTariffcode() {
        return gridfeeTariffcode;
    }

    public void setSettlementArea(String settlementArea) {
        this.settlementArea = settlementArea;
    }

    public void setSettlementMethod(String settlementMethod) {
        this.settlementMethod = settlementMethod;
    }

    public void setGridfeeTimeframe(String gridfeeTimeframe) {
        this.gridfeeTimeframe = gridfeeTimeframe;
    }

    public void setGridfeeTariffcode(String gridfeeTariffcode) {
        this.gridfeeTariffcode = gridfeeTariffcode;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setSettlementArea((String) propertyValues.getProperty(Fields.SETTLEMENT_AREA.javaName()));
        this.setSettlementMethod((String) propertyValues.getProperty(Fields.SETTLEMENT_METHOD.javaName()));
        this.setGridfeeTimeframe((String) propertyValues.getProperty(Fields.GRIDFEE_TIMEFRAME.javaName()));
        this.setGridfeeTariffcode((String) propertyValues.getProperty(Fields.GRIDFEE_TARIFFCODE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.SETTLEMENT_AREA.javaName(), this.getSettlementArea());
        propertySetValues.setProperty(Fields.SETTLEMENT_METHOD.javaName(), this.getSettlementMethod());
        propertySetValues.setProperty(Fields.GRIDFEE_TIMEFRAME.javaName(), this.getGridfeeTimeframe());
        propertySetValues.setProperty(Fields.GRIDFEE_TARIFFCODE.javaName(), this.getGridfeeTariffcode());
    }

    @Override
    public void validateDelete() {

    }

}
