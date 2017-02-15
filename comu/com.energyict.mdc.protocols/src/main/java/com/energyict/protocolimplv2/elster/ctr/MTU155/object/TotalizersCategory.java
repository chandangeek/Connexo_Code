/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class TotalizersCategory extends AbstractUnsignedBINObject<TotalizersCategory> {

    public TotalizersCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "Tot_Vm"; break;
            case 1:
                symbol = "Tot_Vb"; break;
            case 3:
                symbol = "Tot_Vme"; break;
            case 4:
                symbol = "Tot_Vbe"; break;
            case 5:
                symbol = "Tot_Vx_fx"; break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(999999999); //Always the same value in this category
    }

    public int[] getValueLengths(CTRObjectID id) {
        return new int[]{4};
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get("m3");
    }
}
