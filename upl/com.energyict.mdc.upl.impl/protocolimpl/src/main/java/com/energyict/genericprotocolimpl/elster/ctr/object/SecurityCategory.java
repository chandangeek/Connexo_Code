package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

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

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 6:
                symbol = "PUK_S"; break;
            case 7:
                symbol = "PIN"; break;
            case 8:
                switch (id.getZ()) {
                    case 6:
                        symbol = "KEYF"; break;
                    case 0x0A:
                        symbol = "KEYT"; break;
                    default:
                        symbol = "KEYC"; break;
                }  break;
            case 9:
                symbol = "S_Stat"; break;
            case 10:
                symbol = "T_antf"; break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        switch (id.getY()) {
            case 9:
                overflow = 0xFF;  break;
        }
        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = null;
        switch (id.getY()) {
            case 6:
                valueLength = new int[]{8};
                break;
            case 7:
                valueLength = new int[]{6};
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


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return null;
    }

}
