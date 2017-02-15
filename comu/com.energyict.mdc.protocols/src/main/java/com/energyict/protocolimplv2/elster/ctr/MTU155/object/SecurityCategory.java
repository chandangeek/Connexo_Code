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
public class SecurityCategory extends AbstractStringObject<SecurityCategory> {

    public SecurityCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                symbol = "PSSW" + Integer.toString(id.getY());
                break;

            case 6:
                symbol = "PUK_S";
                break;
            case 7:
                symbol = "PIN";
                break;
            case 8:
                switch (id.getZ()) {
                    case 6:
                        symbol = "KEYF";
                        break;
                    case 0x0A:
                        symbol = "KEYT";
                        break;
                    default:
                        symbol = "KEYC";
                        break;
                }
                break;
            case 9:
                symbol = "S_Stat";
                break;
            case 10:
                symbol = "T_antf";
                break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        switch (id.getY()) {
            case 9:
                overflow = 0xFF;
                break;
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        switch (id.getY()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                valueLength = new int[]{6};
                break;
            case 6:
                valueLength = new int[]{8};
                break;
            case 7:
                valueLength = new int[]{4};
                break;
            case 8:
                valueLength = new int[]{16};
                break;
            case 9:
                valueLength = new int[]{2};
                break;
            case 10:
                valueLength = new int[]{4};
                break;
        }
        return valueLength;
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get(BaseUnit.UNITLESS);
    }

}
