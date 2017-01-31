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
public class TemperatureCategory extends AbstractUnsignedBINObject<TemperatureCategory> {

    public TemperatureCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "T"; break;
            case 3:
                symbol = "T_min"; break;
            case 6:
                symbol = "T_max"; break;
            case 9:
                symbol = "GG"; break;
            case 0x0B:
                switch (id.getZ()) {
                    case 0:
                        symbol = "Tref_all"; break;
                    case 1:
                        symbol = "Tb"; break;
                    case 2:
                        symbol = "Tpcs"; break;
                    case 3:
                        symbol = "Tcb"; break;
                    case 4:
                        symbol = "Trho"; break;
                    case 5:
                        symbol = "Tris"; break;
                    case 6:
                        symbol = "Tpre";  break;
                } break;
            case 0x0C:
                symbol = "Tamb_fun"; break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);
        if (overflow == 0) {
            overflow = 500;
        }

        if (Unit.get(BaseUnit.DEGREE_CELSIUS).equals(unit)) {
            overflow = 10000;
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{4};
        int z = id.getZ();
        switch (id.getY()) {
            case 0:
                valueLength = new int[]{3};
                break;
            case 6:
            case 3:
                valueLength = new int[]{3, 1, 1};
                break;

            case 9:
                switch (z) {
                    case 0x0A:
                        valueLength = new int[]{2, 2};
                        break;
                    default:
                        valueLength = new int[]{3}; break;
                }  break;
            case 0x0B:
                switch (z) {
                    case 0:
                        valueLength = new int[]{3, 3, 3, 3, 3, 3};
                        break;
                    default:
                        valueLength = new int[]{3}; break;
                }  break;

            case 0x0C:
                valueLength = new int[]{3, 3};
                break;

        }
        return valueLength;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit;
        int y = id.getY();
        unit = Unit.get(BaseUnit.KELVIN);

        if (y < 0xB) {
            if (valueNumber == 1) {
                unit = Unit.get(BaseUnit.HOUR);
            }
            if (valueNumber == 2) {
                unit = Unit.get(BaseUnit.MINUTE);
            }
            if (id.getZ() == 0x0A) {
                unit = Unit.get(BaseUnit.UNITLESS);
            }
        }

        //Special case
        if (y == 0x09) {
            unit = Unit.get(BaseUnit.UNITLESS);
            if (id.getZ() < 7) {
                unit = Unit.get(BaseUnit.DEGREE_CELSIUS);
            }
        }
        return unit;
    }

}
