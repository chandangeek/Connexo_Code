package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class BandTotalizerCategory extends AbstractUnsignedBINObject {

    public BandTotalizerCategory(CTRObjectID id) {
        this.setId(id);
    }

    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(999999999);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        return new int[]{4};
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return Unit.get("m3");
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
                case 0: symbol = "Tot_Vm_F1";
                case 1: symbol = "Tot_Vm_F2";
                case 2: symbol = "Tot_Vm_F3";
                case 3: symbol = "Tot_Vb_F1";
                case 4: symbol = "Tot_Vb_F2";
                case 5: symbol = "Tot_Vb_F3";
                case 6: symbol = "Tot_Vme_F1";
                case 7: symbol = "Tot_Vme_F2";
                case 8: symbol = "Tot_Vme_F3";
                case 9: symbol = "Tot_Vbe_F1";
                case 0x0A: symbol = "Tot_Vbe_F2";
                case 0x0B: symbol = "Tot_Vbe_F3";

        }
        return symbol;
    }

}
