package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 *
 * @author koen
 */
public class RegisterUnitParser {
    
    public static Unit parse(char code) {
        switch(code) {
            case 'A':
                return Unit.get(BaseUnit.AMPERE);
            case 'B':
                return Unit.get(BaseUnit.LITERPERHOUR);
            case 'D':
                return Unit.get(BaseUnit.DEGREE);
            case 'G':
                return Unit.get(BaseUnit.CUBICMETERPERHOUR);
            case 'H':
                return Unit.get(BaseUnit.HERTZ);
            case 'I':
                return Unit.get(BaseUnit.JOULEPERHOUR);
            case 'J':
                return Unit.get(BaseUnit.JOULE);
            case 'L':
                return Unit.get(BaseUnit.LITER);
            case 'M':
                return Unit.get(BaseUnit.MINUTE);
            case 'N':
                return Unit.getUndefined();
            case 'O':    
                return Unit.get(BaseUnit.CUBICMETER);
            case 'P':    
                return Unit.get(BaseUnit.PERCENT);
            case 'Q': // power factor   
                return Unit.getUndefined();
            case 'R':    
                return Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
            case 'S':    
                return Unit.get(BaseUnit.VOLTAMPERE);
            case 'T':    
                return Unit.get(BaseUnit.SECOND);
            case 'U': // unknown   
                return Unit.getUndefined();
            case 'V':
                return Unit.get(BaseUnit.VOLT);
            case 'W':
                return Unit.get(BaseUnit.WATT);
            case 'X':
                return Unit.get(BaseUnit.WATTHOUR);
            case 'Y':
                return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
            case 'Z':
                return Unit.get(BaseUnit.VOLTAMPEREHOUR);
            default: 
                return Unit.getUndefined();
        }
    }
}
