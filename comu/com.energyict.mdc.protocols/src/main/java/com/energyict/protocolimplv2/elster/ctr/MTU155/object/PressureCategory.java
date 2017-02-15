/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class PressureCategory extends AbstractUnsignedBINObject<PressureCategory> {

    public PressureCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "P";  break;
            case 1:
                symbol = "P_in"; break;
            case 2:
                symbol = "Pam"; break;
            case 3:
                symbol = "P_min"; break;
            case 4:
                symbol = "P_in_min"; break;
            case 6:
                symbol = "P_max"; break;
            case 7:
                symbol = "P_in_max"; break;
            case 9:
                switch (id.getZ()) {
                    case 0:
                        symbol = "Pref_all"; break;
                    case 1:
                        symbol = "Pb"; break;
                    case 2:
                        symbol = "Pp"; break;
                    case 3:
                        symbol = "Prho"; break;
                    case 4:
                        symbol = "Pris"; break;
                    case 5:
                        symbol = "Ppre"; break;
                }  break;
            case 0x0A:
                symbol = "Pnom"; break;
        }
        return symbol;
    }

    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 300;
        if (valueNumber > 0) {
            overflow = getCommonOverflow(unit);
        }
        if (id.getY() == 2) {
            overflow = 2;
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int y = id.getY();
        int z = id.getZ();
        int[] valueLength;

        switch (y) {
            case 4:
            case 6:
            case 7:
            case 3:
                valueLength = new int[]{3, 1, 1};
                break;
            case 9:
                switch (z) {
                    case 0:
                        valueLength = new int[]{3, 3, 3, 3, 3};
                        break;
                    default:
                        valueLength = new int[]{3};
                        break;
                }  break;  
            default:
                valueLength = new int[]{3};
        }
        return valueLength;
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        int x = id.getX();
        int y = id.getY();
        Unit unit = Unit.get(BaseUnit.UNITLESS);

        if (x == 0x04) {
            unit = Unit.get(BaseUnit.BAR);
            if (y == 0x03 || y == 0x04 || y == 0x06 || y == 0x07) {
                if (valueNumber == 1) {
                    unit = Unit.get(BaseUnit.HOUR);
                }
                if (valueNumber == 2) {
                    unit = Unit.get(BaseUnit.MINUTE);
                }
            }
        }
        return unit;

    }

}
