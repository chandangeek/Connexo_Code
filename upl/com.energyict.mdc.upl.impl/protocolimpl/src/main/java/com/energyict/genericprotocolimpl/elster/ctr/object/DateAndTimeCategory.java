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
public class DateAndTimeCategory extends AbstractSignedBINObject {

    public DateAndTimeCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String parseSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
                case 0: switch (id.getZ()) {
                    case 0: symbol = "Date&TimeL";
                    case 1: symbol = "Date&TimeS";
                    case 2: symbol = "Date&TimeP";
                }
                case 1:switch (id.getZ()) {
                    case 2: symbol = "Shift";
                    case 3: symbol = "OFG";
                    case 4: symbol = "Ygas";
                }
                case 2: symbol = "OL";
                case 3: symbol = "Data";
                case 4: symbol = "InS";
                case 5: symbol = "DSO";

        }
        return symbol;
    }

    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);
        
                int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0:
                    if (valueNumber == 3) {overflow = 8;}
                    if (valueNumber == 7) {overflow = 14;}
                    if (valueNumber == 8) {overflow = 2;}
                case 2: if (valueNumber == 0) {overflow = 0xFF;}
            }
            case 1: switch(z) {
                case 2: if (valueNumber == 0) {overflow = 0xFFFF;}
                case 4: if (valueNumber == 0) {overflow = 2;}
            }
            case 2: if (valueNumber == 0) {overflow = 2;}
            case 3: if (valueNumber == 5) {overflow = 0xFF;}
            case 4: switch(z) {
                case 0: if (valueNumber == 0) {overflow = 360;}
            }
        }

        return new BigDecimal(overflow);
    }

    protected int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{};
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: valueLength = new int[]{1,1,1,1,1,1,1,1,1}; break;
                case 1: valueLength = new int[]{1,1,1,1,1}; break;
                case 2: valueLength = new int[]{1,1,1,1,1,1}; break;
            }
            case 1: switch(z) {
                case 2: valueLength = new int[]{2}; break;
                case 3: valueLength = new int[]{1}; break;
                case 4: valueLength = new int[]{1,1,1,1,1,1,1}; break;
            }
            case 2: valueLength = new int[]{1,1,1,1,1}; break;
            case 3: valueLength = new int[]{1,1,1,1,1,1}; break;
            case 4: switch(z) {
                case 0: valueLength = new int[]{2}; break;
                case 1: valueLength = new int[]{2,2,2,2,2,2,2,2}; break;
            }
            case 5: valueLength = new int[]{3,4,4}; break;
        }
        return valueLength;
    }


    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;

        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MINUTE);}
                    if (valueNumber == 6) {unit = Unit.get(BaseUnit.SECOND);}
                case 1:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);}
                case 2:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MINUTE);}
            }

            case 1: switch(z) {
                case 2: unit = Unit.get(BaseUnit.SECOND);
                case 3: unit = Unit.get(BaseUnit.HOUR);
                case 4:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 6) {unit = Unit.get(BaseUnit.DAY);}
            }

            case 2:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.DAY);}
            case 3:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);}
            case 4:
                    unit = Unit.get(BaseUnit.MINUTE);
            case 5:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.SECOND);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.SECOND);}
        }

        
        return unit;
    }

}
