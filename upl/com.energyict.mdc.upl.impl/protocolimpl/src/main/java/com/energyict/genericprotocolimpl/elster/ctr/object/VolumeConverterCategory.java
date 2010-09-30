package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class VolumeConverterCategory extends AbstractUnsignedBINObject<VolumeConverterCategory> {

    public VolumeConverterCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "C"; break;
            case 1:
                switch (id.getZ()) {
                    default:
                        symbol = "Z1"; break;
                    case 6:
                        symbol = "Z_i"; break;
                    case 7:
                        symbol = "Zb";  break;
                }  break;
            case 2:
                symbol = "Z"; break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(0);  //There's no overflow values in this category
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        return new int[]{3};       //There's only one type of value in this category
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return null;               //There's no units in this category
    }

}
