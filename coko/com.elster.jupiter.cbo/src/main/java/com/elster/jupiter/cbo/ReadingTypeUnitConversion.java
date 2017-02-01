/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import java.util.EnumSet;

public class ReadingTypeUnitConversion {
    private enum FlowUnit {
        CUBICMETERPERHOUR("CUBICMETERPERHOUR"),
        NORMALCUBICMETERPERHOUR("NORMALCUBICMETERPERHOUR"),
        JOULEPERHOUR("JOULEPERHOUR"),
        PASCALPERHOUR("PASCALPERHOUR"),
        VOLTSQUARE("VOLTSQUARE"),
        QUANTITYPOWER("QUANTITYPOWER"),
        AMPERESQUARE("AMPERESQUARE"),
        WATT("WATT"),
        VOLTAMPERE("VOLTAMPERE"),
        VOLTAMPEREREACTIVE("VOLTAMPEREREACTIVE"),
        PERHOUR("PERHOUR"),
        KILOGRAMPERHOUR("KILOGRAMPERHOUR"),
        TONPERHOUR("TONPERHOUR"),
        LITERPERHOUR("LITERPERHOUR"),
        GALLONPERHOUR("GALLONPERHOUR"),
        US_GALLONPERHOUR("US_GALLONPERHOUR"),
        IMP_GALLONPERHOUR("IMP_GALLONPERHOUR"),
        CUBICINCHPERHOUR("CUBICINCHPERHOUR"),
        CUBICYARDPERHOUR("CUBICYARDPERHOUR"),
        ACREFEETPERHOUR("ACREFEETPERHOUR");

        private String name;

        private FlowUnit(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ReadingTypeUnitConversion() {

    }

    public static boolean isFlowUnit(ReadingTypeUnit readingTypeUnit) {
        EnumSet<FlowUnit> flowUnits = EnumSet.allOf(FlowUnit.class);
        for (FlowUnit flowUnit : flowUnits) {
            if (flowUnit.getName().equals(readingTypeUnit.name())) {
                return true;
            }
        }
        return false;
    }
}

