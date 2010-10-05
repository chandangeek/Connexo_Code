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
public class EnergyCategory extends AbstractUnsignedBINObject {

    public EnergyCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
                case 0: symbol = "E";  break;
                case 1: symbol = "Tot_E"; break;
                case 2: symbol = "ECp"; break;
        }
        return symbol;
    }
    
    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 999999999;
        if (Unit.get(BaseUnit.JOULEPERHOUR, 6).equals(unit)) {overflow = 9999999;}
        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int y = id.getY();
        int[] valueLength;
            switch(y) {
                case 0: valueLength = new int[]{3}; break;
                default: {valueLength = new int[]{4};} 
            }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        int x = id.getX();
        int y = id.getY();
        Unit unit = null;

        if (x == 0x03) {
            unit = Unit.get(BaseUnit.JOULE, 6);
            if (y == 0x00) {unit = Unit.get(BaseUnit.JOULEPERHOUR, 6);}
        }
        return unit;

    }

}
