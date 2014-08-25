package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class UnitField extends AbstractField<UnitField> {

    public static final int LENGTH = 1;
    private static final int KILO_SCALE_FACTOR = 3;
    private static final int NO_SCALE = 0;

    private int unitCode;
    private UnitMapping unitMapping;

    public UnitField() {
        this.unitMapping = UnitMapping.UNKNOWN;
    }

    public UnitField(UnitMapping unitMapping) {
        this.unitMapping = unitMapping;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(unitCode, LENGTH);
    }

    @Override
    public UnitField parse(byte[] rawData, int offset) throws ParsingException {
        unitCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        unitMapping = UnitMapping.fromUnitCode(unitCode);
        if (unitMapping.equals(UnitMapping.UNKNOWN)) {
            throw new ParsingException("Encountered an invalid/unknown unit (uni code " + unitCode + ")");
        }
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getUnitCode() {
        return unitCode;
    }

    public Unit getUnit() {
        return unitMapping.getUnit();
    }

    public UnitMapping getUnitMapping() {
        return unitMapping;
    }

    public enum UnitMapping {
        UNDEFINED(0, BaseUnit.UNITLESS, NO_SCALE),
        KWH_PROVIDED(1, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR),
        KVARH(2, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR),
        KQH(3, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR),
        V2H(4, BaseUnit.VOLTSQUAREHOUR, NO_SCALE),
        I2H(5, BaseUnit.AMPERESQUAREHOUR, NO_SCALE),
        INACTIF(6, BaseUnit.UNITLESS, NO_SCALE),
        UNDEFINED2(7, BaseUnit.UNITLESS, NO_SCALE),
        VH(8, BaseUnit.VOLTHOUR, NO_SCALE),
        IH(9, BaseUnit.AMPEREHOUR, NO_SCALE),
        KVAR_IND(10, BaseUnit.VOLTAMPEREREACTIVE, KILO_SCALE_FACTOR),
        KVAR_CAP(11, BaseUnit.VOLTAMPEREREACTIVE, KILO_SCALE_FACTOR),
        KQH_IND(12, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR),
        KQH_CAP(13, BaseUnit.QUANTITYPOWERHOUR, KILO_SCALE_FACTOR),
        KWH_RECEIVED(14, BaseUnit.WATTHOUR, KILO_SCALE_FACTOR),
        KVARH_IND(15, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR),
        KVARH_CAP(16, BaseUnit.VOLTAMPEREREACTIVEHOUR, KILO_SCALE_FACTOR),
        VH_PHASE_A(17, BaseUnit.VOLTHOUR, NO_SCALE),    //TODO: check if correct
        VH_PHASE_B(18, BaseUnit.VOLTHOUR, NO_SCALE),
        VH_PHASE_C(19, BaseUnit.VOLTHOUR, NO_SCALE),
        IH_PHASE_A(20, BaseUnit.AMPEREHOUR, NO_SCALE),
        IH_PHASE_B(21, BaseUnit.AMPEREHOUR, NO_SCALE),
        IH_PHASE_C(22, BaseUnit.AMPEREHOUR, NO_SCALE),
        FPH(23, BaseUnit.UNITLESS, NO_SCALE),                       //TODO
        HARMONIC_DISTORTION_HOUR(24, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        KVAR_ALL_PHASES(25, BaseUnit.VOLTAMPEREHOUR, KILO_SCALE_FACTOR),
        FPH_REVERSE(26, BaseUnit.UNITLESS, NO_SCALE),   //TODO
        FPH_PHASE_A(27, BaseUnit.UNITLESS, NO_SCALE),   //TODO
        FPH_PHASE_D(28, BaseUnit.UNITLESS, NO_SCALE),   //TODO
        FPH_PHASE_C(29, BaseUnit.UNITLESS, NO_SCALE),   //TODO
        THDH_VOLTAGE_PHASE_A(30, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        THDH_VOLTAGE_PHASE_B(31, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        THDH_VOLTAGE_PHASE_C(32, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        THDH_CURRENT_PHASE_A(33, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        THDH_CURRENT_PHASE_B(34, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        THDH_CURRENT_PHASE_C(35, BaseUnit.UNITLESS, NO_SCALE),  //TODO
        MAX_VH_PHASE_A(36, BaseUnit.VOLTHOUR, NO_SCALE),
        MAX_VH_PHASE_B(37, BaseUnit.VOLTHOUR, NO_SCALE),
        MAX_VH_PHASE_C(38, BaseUnit.VOLTHOUR, NO_SCALE),
        MIN_VH_PHASE_A(39, BaseUnit.VOLTHOUR, NO_SCALE),
        MIN_VH_PHASE_B(40, BaseUnit.VOLTHOUR, NO_SCALE),
        MIN_VH_PHASE_C(41, BaseUnit.VOLTHOUR, NO_SCALE),
        NON_EXISTING_CHANNEL(99, BaseUnit.UNITLESS, NO_SCALE),
        UNKNOWN(99, BaseUnit.UNITLESS, NO_SCALE);

        private final int unitCode;
        private final Unit unit;

        private UnitMapping(int unitCode, int baseUnitCode, int scale) {
            this.unitCode = unitCode;
            this.unit = Unit.get(baseUnitCode, scale);
        }

        public Unit getUnit() {
            return unit;
        }

        public int getUnitCode() {
            return unitCode;
        }

        public static UnitMapping fromUnitCode(int statusCode) {
            for (UnitMapping version : UnitMapping.values()) {
                if (version.getUnitCode() == statusCode) {
                    return version;
                }
            }
            return UnitMapping.UNKNOWN;
        }
    }
}