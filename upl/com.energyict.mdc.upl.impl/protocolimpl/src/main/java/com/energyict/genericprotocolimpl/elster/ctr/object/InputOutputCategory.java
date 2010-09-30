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
public class InputOutputCategory extends AbstractUnsignedBINObject<InputOutputCategory> {

    public InputOutputCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 6:
                switch (id.getZ()) {
                    case 2:
                        symbol = "Conf_T"; break;
                } break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return new BigDecimal(0);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{1, 3, 3, 3, 3};
        return valueLength;
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit;
        unit = Unit.get(BaseUnit.KELVIN);
        if (valueNumber == 0) {
            unit = null;
        }

        return unit;
    }

}
