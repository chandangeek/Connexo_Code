package com.energyict.obis;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

public class ObisCodeUnitMapper {

    // first 20 C field codes, applied to electricity related codes
    private final static Unit[] UNITS = {Unit.get(""), // General purpose objects
            Unit.get(BaseUnit.WATT), // active import Q1+Q4
            Unit.get(BaseUnit.WATT), // active export Q2+Q3
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive import Q1+Q2
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive export Q3+Q4
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q1
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q2
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q3
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE), // reactive Q4
            Unit.get(BaseUnit.VOLTAMPERE), // apparent import Q1+Q4 of Q1+Q2+Q3+Q4
            Unit.get(BaseUnit.VOLTAMPERE), // apparent export Q2+Q3
            Unit.get(BaseUnit.AMPERE), // current any phase
            Unit.get(BaseUnit.VOLT), // // voltage any phase
            Unit.get(""), // power factor
            Unit.get(BaseUnit.HERTZ), // supply frequency
            Unit.get(BaseUnit.WATT), // active power abs(Q1+Q4) + abs(Q2+Q3)
            Unit.get(BaseUnit.WATT), // active power abs(Q1+Q4) - abs(Q2+Q3)
            Unit.get(BaseUnit.WATT), // active Q1
            Unit.get(BaseUnit.WATT), // active Q2
            Unit.get(BaseUnit.WATT), // active Q3
            Unit.get(BaseUnit.WATT)}; // active Q4


    public static Unit getUnitElectricity(ObisCode obisCode, int scaler) {
        Unit unit = get(obisCode, scaler);
        double d = obisCode.getD();
        if (((d >= 8) && (d <= 10)) || ((d >= 29) && (d <= 30))) {
            return unit.getVolumeUnit();
        } else {
            return unit;
        }
    }

    private static Unit get(ObisCode obisCode, int scaler) {
        Unit unit = Unit.get("");
        int c = obisCode.getC();
        if ((c >= 0) && (c <= 20)) {
            unit = UNITS[c];
        } else if ((c >= 21) && (c <= 40)) {
            unit = UNITS[(c % 21) + 1];
        } else if ((c >= 41) && (c <= 60)) {
            unit = UNITS[(c % 41) + 1];
        } else if ((c >= 61) && (c <= 80)) {
            unit = UNITS[(c % 61) + 1];
        } else if (c == 81) // angles
        {
            unit = Unit.get(BaseUnit.DEGREE);
        } else if (c == 91) // angles
        {
            unit = Unit.get(BaseUnit.AMPERE);
        } else if (c == 92) // angles
        {
            unit = Unit.get(BaseUnit.VOLT);
        }
        return Unit.get(unit.getDlmsCode(), scaler);
    }

}
