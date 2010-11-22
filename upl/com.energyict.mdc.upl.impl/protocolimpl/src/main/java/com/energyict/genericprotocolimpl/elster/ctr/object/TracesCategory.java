package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class TracesCategory extends AbstractUnsignedBINObject<TracesCategory> {

    public TracesCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "Trace";
                break;
            case 1:
                symbol = "TraceA";
                break;
            case 2:
                symbol = "ArrayA";
                break;
        }
        return symbol;
    }


    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        if (valueNumber == 0) {
            overflow = 6;
        }
        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        return new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        return Unit.get(BaseUnit.UNITLESS);        //There's no units in this category
    }

}
