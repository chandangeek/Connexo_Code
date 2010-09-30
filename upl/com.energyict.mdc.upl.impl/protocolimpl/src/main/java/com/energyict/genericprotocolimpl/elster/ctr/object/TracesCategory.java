package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

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

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
            case 0:
                symbol = "Trace"; break;
            case 1:
                symbol = "TraceA"; break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = 0;
        if (valueNumber == 0) {
            overflow = 6;
        }
        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        return new int[]{1, 32};
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        return null;        //There's no units in this category
    }

}
