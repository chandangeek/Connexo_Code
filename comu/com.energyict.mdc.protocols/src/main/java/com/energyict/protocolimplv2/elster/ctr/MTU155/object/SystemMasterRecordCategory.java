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
public class SystemMasterRecordCategory extends AbstractBCDObject<SystemMasterRecordCategory> {

    public SystemMasterRecordCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";
        int z = id.getZ();
        switch (id.getY()) {
            case 0:
                switch (z) {
                    case 0:
                        symbol = "PDR"; break;
                    case 1:
                        symbol = "NCG"; break;
                    case 3:
                        symbol = "TypeMis"; break;
                    case 2:
                        symbol = "TypeImp"; break;
                    case 4:
                        symbol = "Local"; break;
                    case 5:
                        symbol = "GEO"; break;
                    case 6:
                        symbol = "RagSoc"; break;
                    case 7:
                        symbol = "CodUte"; break;
                } break;
            case 1:
                switch (z) {
                    case 1:
                        symbol = "Mat_P"; break;
                    case 3:
                        symbol = "Mat_T"; break;
                    case 2:
                        symbol = "Mat_DPh"; break;
                    case 4:
                        symbol = "Mat_DPl"; break;
                    case 5:
                        symbol = "Mat_Dens"; break;
                    case 6:
                        symbol = "Mat_Gascro"; break;
                } break;
            case 2:
                switch (z) {
                    case 0:
                        symbol = "An_CONT"; break;
                    case 1:
                        symbol = "An_CORR"; break;
                } break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return null; //There's no overflow values available in this category.
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        int z = id.getZ();
        switch (id.getY()) {
            case 0:
                switch (z) {
                    case 0:
                        valueLength = new int[]{7};
                        break;
                    case 1:
                    case 3:
                    case 2:
                        valueLength = new int[]{1};
                        break;
                    case 4:
                        valueLength = new int[]{60};
                        break;
                    case 5:
                        valueLength = new int[]{3, 3, 2};
                        break;
                    case 6:
                    case 7:
                        valueLength = new int[]{30};
                        break;
                } break;
            case 1:
                valueLength = new int[]{10, 10, 16};
                break;
            case 2:
                switch (z) {
                    case 0:
                        valueLength = new int[]{4, 3, 13};
                        break;
                    case 1:
                        valueLength = new int[]{4, 16};
                        break;
                } break;
        }
        return valueLength;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);
        switch (id.getY()) {
            case 0:
                switch (id.getZ()) {
                    case 5:
                        if (valueNumber == 2) {
                            unit = Unit.get(BaseUnit.METER);
                        }
                }
        }
        return unit;
    }

}
