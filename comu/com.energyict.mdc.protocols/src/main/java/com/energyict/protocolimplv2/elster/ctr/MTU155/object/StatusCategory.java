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
public class StatusCategory extends AbstractUnsignedBINObject<StatusCategory> {

    public StatusCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "SD";
                break;
            case 1:
                symbol = "Diagn";
                break;
            case 2:
                symbol = "DiagnR";
                break;
            case 4:
                symbol = "Imp_power";
                break;
            case 5:
                symbol = "PWF";
                break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        switch (id.getY()) {
            case 4:
                if (valueNumber == 0) {
                    overflow = 2;
                }
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{};

        switch (id.getY()) {
            case 0:
                valueLength = new int[]{1};
                break;
            case 1:
                valueLength = new int[]{4};
                break;
            case 2:
                valueLength = new int[]{2};
                break;
            case 4:
                valueLength = new int[]{1, 2};
                break;
            case 5:
                valueLength = new int[]{2, 2};
                break;
            case 6:
                valueLength = new int[]{2};
                break;
        }
        return valueLength;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        switch (id.getY()) {
            case 4:
                if (id.getZ() == 1) {
                    if (valueNumber == 1) {
                        unit = Unit.get(BaseUnit.MINUTE);
                    }
                }
                break;
            case 5:
                if (valueNumber == 1) {
                    unit = Unit.get(BaseUnit.HOUR);
                }
                break;
        }
        return unit;
    }
}