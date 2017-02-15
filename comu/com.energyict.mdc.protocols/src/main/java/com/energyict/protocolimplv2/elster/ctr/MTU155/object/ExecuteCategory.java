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
public class ExecuteCategory extends AbstractUnsignedBINObject {

    public ExecuteCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch(id.getY()) {
            case 0: switch(id.getZ()) {
                case 1: symbol = "F_SYNC"; break;
                case 8: symbol = "F_Reset"; break;
            }
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);
        switch(id.getZ()) {
            case 1:
                if (valueNumber == 4) {overflow = 8;}
                if (valueNumber == 8) {overflow = 100;}
                if (valueNumber == 9) {overflow = 2;}  break;
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{};
        switch(id.getY()) {
            case 0: switch(id.getZ()) {
                case 1: valueLength = new int[]{1,1,1,1,1,1,1,1,1,1}; break;
                case 8: valueLength = new int[]{1}; break;
            }  break;
        }
        return valueLength;
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        switch(id.getZ()) {
            case 1:
                if (valueNumber == 1) {unit = Unit.get(BaseUnit.YEAR);}
                if (valueNumber == 2) {unit = Unit.get(BaseUnit.MONTH);}
                if (valueNumber == 3) {unit = Unit.get(BaseUnit.DAY);}
                if (valueNumber == 5) {unit = Unit.get(BaseUnit.HOUR);}
                if (valueNumber == 6) {unit = Unit.get(BaseUnit.MINUTE);}
                if (valueNumber == 7) {unit = Unit.get(BaseUnit.SECOND);} break;
        }
        return unit;
    }

}
