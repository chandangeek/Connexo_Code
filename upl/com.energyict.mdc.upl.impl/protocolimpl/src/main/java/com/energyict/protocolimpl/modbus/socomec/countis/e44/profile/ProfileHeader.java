package com.energyict.protocolimpl.modbus.socomec.countis.e44.profile;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 * @author sva
 * @since 9/10/2014 - 10:35
 */
public class ProfileHeader {

    private int recordCount;
    private int recordSize;
    private int integrationPeriod;
    private PhysicalUnit physicalUnit;
    private int numeratorRate;
    private int denominatorRate;
    private double ctRatio;

    public ProfileHeader() {
    }

    public static ProfileHeader parse(int[] values, int offset, double ctRatio) {
        int ptr = offset;
        ProfileHeader profileHeader = new ProfileHeader();
        profileHeader.setRecordCount(values[ptr++]);
        profileHeader.setRecordSize(values[ptr++]);
        profileHeader.setIntegrationPeriod(values[ptr++]);
        profileHeader.setPhysicalUnit(PhysicalUnit.physicalUnitFromCode(values[ptr++]));
        profileHeader.setNumeratorRate(values[ptr++]);
        profileHeader.setDenominatorRate(values[ptr++]);
        profileHeader.setCtRatio(ctRatio);

        return profileHeader;
    }

    public int getWordLength() {
        return 6;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public int getIntegrationPeriod() {
        return integrationPeriod;
    }

    public void setIntegrationPeriod(int integrationPeriod) {
        this.integrationPeriod = integrationPeriod;
    }

    public Unit getEisUnit() {
        return physicalUnit.getEisUnit();
    }

    public void setPhysicalUnit(PhysicalUnit physicalUnit) {
        this.physicalUnit = physicalUnit;
    }

    public int getNumeratorRate() {
        return numeratorRate;
    }

    public void setNumeratorRate(int numeratorRate) {
        this.numeratorRate = numeratorRate;
    }

    public int getDenominatorRate() {
        return denominatorRate;
    }

    public void setDenominatorRate(int denominatorRate) {
        this.denominatorRate = denominatorRate;
    }

    public void setCtRatio(double ctRatio) {
        this.ctRatio = ctRatio;
    }

    public double getCtRatio() {
        return ctRatio;
    }

    private enum PhysicalUnit {
        U_0(0, Unit.get(BaseUnit.WATT)),
        U_1(1, Unit.get(BaseUnit.WATT)),
        U_2(2, Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
        U_3(3, Unit.get(BaseUnit.VOLTAMPEREREACTIVE)),
        U_4(4, Unit.get(BaseUnit.VOLTAMPERE)),
        U_5(5, Unit.getUndefined()),
        U_6(6, Unit.get(BaseUnit.JOULE)),
        U_7(7, Unit.getUndefined()),
        U_8(8, Unit.get(BaseUnit.CUBICMETER)),
        U_9(9, Unit.get(BaseUnit.NORMALCUBICMETER)),
        U_20(20, Unit.get(BaseUnit.WATT, Constants.KILO_SCALE)),
        U_22(22, Unit.get(BaseUnit.VOLTAMPEREREACTIVE, Constants.KILO_SCALE)),
        U_24(24, Unit.get(BaseUnit.VOLTAMPERE, Constants.KILO_SCALE)),
        U_26(26, Unit.get(BaseUnit.JOULE, Constants.KILO_SCALE)),
        U_27(27, Unit.get(BaseUnit.UNITLESS, Constants.KILO_SCALE)),
        UNDEFINED(-1, Unit.getUndefined());

        private final int unitCode;
        private final Unit unit;

        PhysicalUnit(int unitCode, Unit unit) {
            this.unitCode = unitCode;
            this.unit = unit;
        }

        public int getUnitCode() {
            return unitCode;
        }

        public Unit getEisUnit() {
            return unit;
        }

        public static PhysicalUnit physicalUnitFromCode(int code) {
            for (PhysicalUnit physicalUnit : values()) {
                if (physicalUnit.getUnitCode() == code) {
                    return physicalUnit;
                }
            }
            return UNDEFINED;
        }

        private static class Constants {

            private static final int KILO_SCALE = 3;
        }
    }
}
