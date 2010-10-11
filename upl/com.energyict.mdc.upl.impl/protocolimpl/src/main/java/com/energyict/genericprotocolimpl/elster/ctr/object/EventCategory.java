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
public class EventCategory extends AbstractUnsignedBINObject {

    public EventCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch(id.getY()) {
            case 0: switch(id.getZ()) {
                case 1: symbol = "EventiS"; break;
                case 2: symbol = "EventsA"; break;
                case 3: symbol = "EventsT"; break;
            } break;
            case 1: symbol = "NEM";  break;
        }
        return symbol;
    }


    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);
        switch(id.getY()) {
            case 0: switch(id.getZ()) {
                case 1:
                case 2:
                    if (valueNumber == 3) {overflow = 54;}
                    if (valueNumber == 4) {overflow = 120;}
                case 3:
                    if (valueNumber == 3) {overflow = 54;}
                    if (valueNumber == 4) {overflow = 120;}
                    if (valueNumber == 6) {overflow = 15;}
            }
        }
        return new BigDecimal(overflow);
    }

    public int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{};
        int z = id.getZ();

        switch(id.getY()) {
            case 0: switch(z) {
                case 3: valueLength = new int[]{1,1,1,1,1,1,1,1,2,1,4,1,4,1,4,1,4,1,4,1,4}; break;
                default: {valueLength = new int[]{1,1,1,1,1,2,1,1,4,4};}
            }  break;
            case 1: valueLength = new int[]{2}; break;
            case 2: valueLength = new int[]{2}; break;
            case 3: valueLength = new int[]{1}; break;
        }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        switch(id.getY()) {
            case 0:
                if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);}
        }
        return unit;
    }

}
