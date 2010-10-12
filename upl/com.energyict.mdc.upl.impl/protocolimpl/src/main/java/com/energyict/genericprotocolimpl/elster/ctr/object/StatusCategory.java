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
public class StatusCategory extends AbstractUnsignedBINObject<StatusCategory> {

    public StatusCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
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


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        switch (id.getY()) {
            case 4:
                if (valueNumber == 0) {
                    overflow = 2;
                }
        }
        return new BigDecimal(overflow);
    }

    public int[] parseValueLengths(CTRObjectID id) {
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

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
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