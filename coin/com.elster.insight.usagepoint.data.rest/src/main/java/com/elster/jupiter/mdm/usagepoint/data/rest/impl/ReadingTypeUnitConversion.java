package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

public class ReadingTypeUnitConversion {
    public ReadingTypeUnitConversion() {

    }

    public static boolean isVolumeUnit(String unitName) {
        switch (unitName) {
            case "CUBICMETER":
            case "NORMALCUBICMETER":
            case "JOULE":
            case "PASCAL":
            case "VOLTSQUAREHOUR":
            case "QUANTITYPOWERHOUR":
            case "AMPERESQUAREHOUR":
            case "WATTHOUR":
            case "VOLTAMPEREHOUR":
            case "VOLTAMPEREREACTIVEHOUR":
            case "COUNT":
            case "KILOGRAM":
            case "TON":
            case "LITER":
            case "GALLON":
            case "US_GALLON":
            case "IMP_GALLON":
            case "CUBICINCH":
            case "CUBICYARD":
            case "ACREFEET":
                return true;
            default:
                return false;
        }
    }
    public static boolean isFlowUnit(String unitName) {
        switch (unitName) {
            case "CUBICMETERPERHOUR":
            case "NORMALCUBICMETERPERHOUR":
            case "JOULEPERHOUR":
            case "PASCALPERHOUR":
            case "VOLTSQUARE":
            case "QUANTITYPOWER":
            case "AMPERESQUARE":
            case "WATT":
            case "VOLTAMPERE":
            case "VOLTAMPEREREACTIVE":
            case "PERHOUR":
            case "KILOGRAMPERHOUR":
            case "TONPERHOUR":
            case "LITERPERHOUR":
            case "GALLONPERHOUR":
            case "US_GALLONPERHOUR":
            case "IMP_GALLONPERHOUR":
            case "CUBICINCHPERHOUR":
            case "CUBICYARDPERHOUR":
            case "ACREFEETPERHOUR":
                return true;
            default:
                return false;
        }
    }
}

