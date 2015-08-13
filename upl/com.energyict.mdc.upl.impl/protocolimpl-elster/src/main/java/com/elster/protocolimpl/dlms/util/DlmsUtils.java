package com.elster.protocolimpl.dlms.util;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

public class DlmsUtils {

    /**
     * Convert the given String to the respective {@link com.energyict.cbo.Unit}.<br>
     * Implemented units:<br>
     * <li> {@link com.energyict.cbo.BaseUnit#CUBICMETER} <li> {@link com.energyict.cbo.BaseUnit#WATTHOUR} <li>
     * {@link com.energyict.cbo.BaseUnit#WATT} <br>
     * <br>
     * The last two can have a scaler of 3 when 'k' is added in the string
     *
     * @param strUnit - the given strUnit
     * @return the Unit
     */
    public static Unit getUnitFromString(String strUnit) {
        int scaler;
        if ((strUnit.equalsIgnoreCase("m3")) ||
                strUnit.equalsIgnoreCase("m\u00B3")) {
            return Unit.get(BaseUnit.CUBICMETER);
        } else if (strUnit.equalsIgnoreCase("bar")) {
            return Unit.get(BaseUnit.BAR);
        } else if ((strUnit.equalsIgnoreCase("{C"))
                || (strUnit.equalsIgnoreCase("\u00B0C"))
                || (strUnit.equalsIgnoreCase("C"))
                || (strUnit.equalsIgnoreCase("celsius"))
                || (strUnit.equalsIgnoreCase("Grad C"))) {
            return Unit.get(BaseUnit.DEGREE_CELSIUS);
        } else if ((strUnit.equalsIgnoreCase("{F"))
                || (strUnit.equalsIgnoreCase("F"))) {
            return Unit.get(BaseUnit.FAHRENHEIT);
        } else if ((strUnit.equalsIgnoreCase("{K"))
                || (strUnit.equalsIgnoreCase("K"))) {
            return Unit.get(BaseUnit.KELVIN);
        } else if (strUnit.equalsIgnoreCase("s")) {
            return Unit.get(BaseUnit.SECOND);
        } else if (strUnit.equalsIgnoreCase("Wh/m\u00B3")) {
            return Unit.get(BaseUnit.WATTHOURPERCUBICMETER);
        } else if (strUnit.equalsIgnoreCase("MOL %")) {
            return Unit.get(BaseUnit.MOLPERCENT);
        } else if (strUnit.equalsIgnoreCase("%")) {
            return Unit.get(BaseUnit.PERCENT);
        } else if (strUnit.equalsIgnoreCase("g/m\u00B3")) {
            return Unit.get(BaseUnit.GRAMPERSQUARECENTIMETER);
        } else if (strUnit.indexOf("Wh") > -1) {
            scaler = (strUnit.indexOf("k") > -1) ? 3 : 0;
            return Unit.get(BaseUnit.WATTHOUR, scaler);
        } else if (strUnit.indexOf("W") > -1) {
            scaler = (strUnit.indexOf("k") > -1) ? 3 : 0;
            return Unit.get(BaseUnit.WATT, scaler);
        } else if ((strUnit.indexOf("m3|h") > -1) ||
                    (strUnit.indexOf("m\u00B3/h") > -1)) {
            return Unit.get(BaseUnit.CUBICMETERPERHOUR);
        } else {
            return Unit.getUndefined();
        }
    }

    public static Unit getUnitFromDlmsUnit(com.elster.dlms.cosem.classes.class03.Unit unit)
    {
        int unitId = unit.getId();
        if (unitId > 52)
        {
            switch (unitId)
            {
                case 255:
                case 254:
                    break;
                case 57:
                    unitId = 520;
                    break;
                default:
                    unitId = 255;
            }
        }
        return Unit.get(unitId);
    }

}
