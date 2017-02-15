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
public class BandTotalizerCategory extends AbstractUnsignedBINObject<BandTotalizerCategory> {

    public BandTotalizerCategory(CTRObjectID id) {
        this.setId(id);
    }

    /**
     * Returns the overflow value for a specific value of a CTR Object
     * @param id: the id of the CTR Object
     * @param valueNumber: index number of the value (an object can have multiple value fields)
     * @param unit: relevant to check if the overflow value is common
     * @return the matching overflow value
     */
    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(999999999);
    }

    public int[] getValueLengths(CTRObjectID id) {
        return new int[]{4};
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get(BaseUnit.CUBICMETER, getQlf().getKmoltFactor());
    }

    /**
     * Returns the symbol that matches the object
     * @param id: the object's id
     * @return the matching symbol
     */
    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "Tot_Vm_F1"; break;
            case 1:
                symbol = "Tot_Vm_F2"; break;
            case 2:
                symbol = "Tot_Vm_F3"; break;
            case 3:
                symbol = "Tot_Vb_F1"; break;
            case 4:
                symbol = "Tot_Vb_F2"; break;
            case 5:
                symbol = "Tot_Vb_F3"; break;
            case 6:
                symbol = "Tot_Vme_F1"; break;
            case 7:
                symbol = "Tot_Vme_F2"; break;
            case 8:
                symbol = "Tot_Vme_F3"; break;
            case 9:
                symbol = "Tot_Vbe_F1"; break;
            case 0x0A:
                symbol = "Tot_Vbe_F2"; break;
            case 0x0B:
                symbol = "Tot_Vbe_F3"; break;

        }
        return symbol;
    }

}
