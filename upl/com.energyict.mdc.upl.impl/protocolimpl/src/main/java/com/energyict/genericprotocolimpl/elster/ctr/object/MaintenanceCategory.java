package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class MaintenanceCategory extends AbstractUnsignedBINObject<MaintenanceCategory> {

    public MaintenanceCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "Data"; break;
            case 1:
                symbol = "Data_CV"; break;
            case 2:
                symbol = "Data_DS"; break;
            case 4:
                symbol = "Data_CN"; break;
            case 5:
                switch (id.getZ()) {
                    case 0:
                        symbol = "TresBatt"; break;
                    case 1:
                        symbol = "OuBatt"; break;
                    case 2:
                        symbol = "VoltBatt"; break;
                }  break;
            case 6:
                symbol = "Data_MC"; break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);

        switch (id.getY()) {
            case 5:
                overflow = 0xFFFFFF; break;
        }
        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength;
        switch (id.getY()) {
            case 5:
                valueLength = new int[]{3, 3, 3, 3};
                break;  //def value = 6 ipv 12  !!?
            default:
                valueLength = new int[]{1, 1, 1, 1, 1};
        }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        int z = id.getZ();
        switch (id.getY()) {
            case 1:
            case 2:
            case 4:
            case 6:
                if (valueNumber == 0) {
                    unit = Unit.get(BaseUnit.YEAR);
                }
                if (valueNumber == 1) {
                    unit = Unit.get(BaseUnit.MONTH);
                }
                if (valueNumber == 2) {
                    unit = Unit.get(BaseUnit.DAY);
                }
                if (valueNumber == 3) {
                    unit = Unit.get(BaseUnit.HOUR);
                }
                if (valueNumber == 4) {
                    unit = Unit.get(BaseUnit.MINUTE);
                }  break;
            case 5:
                unit = Unit.get(BaseUnit.HOUR);
                if (z == 2) {
                    unit = Unit.get(BaseUnit.VOLT);
                } break;
        }
        return unit;

    }

}
