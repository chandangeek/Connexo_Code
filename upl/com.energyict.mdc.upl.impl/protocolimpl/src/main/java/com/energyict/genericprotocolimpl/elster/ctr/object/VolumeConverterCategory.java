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
public class VolumeConverterCategory extends AbstractUnsignedBINObject<VolumeConverterCategory> {

    public VolumeConverterCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "C"; break;
            case 1:
                switch (id.getZ()) {
                    case 6:
                        symbol = "Z_i"; break;
                    case 7:
                        symbol = "Zb";  break;
                    default:
                        symbol = "Z1"; break;
                }  break;
            case 2:
                symbol = "Z"; break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(0);  //There's no overflow values in this category
    }

    public int[] getValueLengths(CTRObjectID id) {
        return new int[]{3};       //There's only one type of value in this category
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get(BaseUnit.UNITLESS);               //There's no units in this category
    }

}
