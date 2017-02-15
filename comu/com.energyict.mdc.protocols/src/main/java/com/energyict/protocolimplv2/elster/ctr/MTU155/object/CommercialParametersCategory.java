/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;

public class CommercialParametersCategory extends AbstractUnsignedBINObject {

    public CommercialParametersCategory(CTRObjectID id) {
        this.setId(id);
    }

    @Override
    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get(BaseUnit.UNITLESS);
    }

    @Override
    protected String getSymbol(CTRObjectID id) {
        switch (id.getZ()) {
            case 0: return "PT_cor";
            case 1: return "PT_fut";
            case 2: return "PerFat";
            case 4: return "ID-PT";
            default: return "";
        }
    }

    @Override
    public int[] getValueLengths(CTRObjectID id) {
        switch (id.getZ()) {
            case 0: return gi(73);
            case 1: return gi(73);
            case 2: return gi(1,1,1);
            case 4: return gi(2,2,2,3);
            default: return gi(0);
        }
    }

    @Override
    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null;
    }
}