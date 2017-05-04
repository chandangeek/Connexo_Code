package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class InputOutputCategory extends AbstractUnsignedBINObject<InputOutputCategory> {

    public InputOutputCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 6:
                switch (id.getZ()) {
                    case 2:
                        symbol = "Conf_T";
                        break;
                }
                break;
            case 7:
                switch (id.getZ()) {
                    case 1:
                        symbol = "Imp_Vm";
                        break;
                    case 2:
                        symbol = "Imp_Vbs";
                        break;
                }
                break;
            case 0:
                switch (id.getZ()) {
                    case 0:
                        symbol = "Stat_DI";
                        break;
                    case 1:
                        symbol = "SA_DI";
                        break;
                }
                break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return BigDecimal.ZERO;
    }

    public int[] getValueLengths(CTRObjectID id) {
        if (id.getY() == 6) {
            if (id.getZ() == 1) {
                return new int[]{1, 1, 3, 3, 3, 3};
            } else {
                return new int[]{1, 3, 3, 3, 3};
            }
        } else if (id.getY() == 7) {
            return new int[]{3};
        }
        return new int[]{1};
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit;
        unit = Unit.get(BaseUnit.KELVIN);
        if (valueNumber == 0) {
            unit = Unit.get(BaseUnit.UNITLESS);
        }

        return unit;
    }

}
