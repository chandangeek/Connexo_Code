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
public class PressureCategory extends AbstractUnsignedBINObject {

    public PressureCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0: symbol = "P";
            case 1: symbol = "P_in";
            case 2: symbol = "Pam";
            case 3: symbol = "P_min";
            case 4: symbol = "P_in_min";
            case 6: symbol = "P_max";
            case 7: symbol = "P_in_max";
            case 9: switch(id.getZ()) {
                case 0: symbol = "Pref_all";
                case 1: symbol = "Pb";
                case 2: symbol = "Pp";
                case 3: symbol = "Prho";
                case 4: symbol = "Pris";
                case 5: symbol = "Ppre";
            }
            case 0x0A: symbol = "Pnom";
        }
        return symbol;
    }

    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 300;
        if (valueNumber > 0) {
            overflow = getCommonOverflow(unit);
        }
        if (id.getY() == 2) {
            overflow = 2;
        }
        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int y = id.getY();
        int z = id.getZ();
        int[] valueLength;

        switch(y) {
            default: valueLength = new int[]{3};
            case 4:
            case 6:
            case 7:
            case 3: valueLength = new int[]{3,1,1}; break;
            case 9: switch(z) {
                case 0: valueLength = new int[]{3,3,3,3,3}; break;
                default: valueLength = new int[]{3}; break;
            }
        }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        int x = id.getX();
        int y = id.getY();
        Unit unit = null;

        if (x == 0x04) {
            unit = Unit.get(BaseUnit.BAR);
            if (y == 0x03 || y == 0x04 || y == 0x06 || y == 0x07) {
                if (valueNumber == 1) {unit = Unit.get(BaseUnit.HOUR);}
                if (valueNumber == 2) {unit = Unit.get(BaseUnit.MINUTE);}
            }
        }
        return unit;

    }

}
