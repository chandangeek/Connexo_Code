/*
 * RegisterUnitParser.java
 *
 * Created on 22 maart 2006, 11:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 *
 * @author koen
 */
public class RegisterUnitParser {

    /** Creates a new instance of RegisterUnitParser */
    public RegisterUnitParser() {
    }

    public Unit parse(char code) {
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
                return Unit.get("");
            case 'O':
                return Unit.get(BaseUnit.CUBICMETER);
            case 'P':
                return Unit.get(BaseUnit.PERCENT);
            case 'Q': // power factor
                return Unit.get("");
            case 'R':
                return Unit.get("var");
            case 'S':
                return Unit.get("VA");
            case 'T':
                return Unit.get(BaseUnit.SECOND);
            case 'U': // unknown
                return Unit.get("");
            case 'V':
                return Unit.get(BaseUnit.VOLT);
            case 'W':
                return Unit.get(BaseUnit.WATT);
            case 'X':
                return Unit.get(BaseUnit.WATTHOUR);
            case 'Y':
                return Unit.get("varh");
            case 'Z':
                return Unit.get("VAh");
            default:
                return Unit.get("");
        }
    }

}
