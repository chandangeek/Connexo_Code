package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.LocalizableEnum;
import com.energyict.mdc.common.Environment;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:21
 */
public enum ValueCalculationMethod implements LocalizableEnum {
    AUTOMATIC(0, "valueCalculationMethod.automatic") {
        @Override
        public BigDecimal calculateAmount(BigDecimal rawValue, BigDecimal previousRawValue, BigDecimal overflow){
            return calculateAdvance(rawValue, previousRawValue, overflow);
        }
    },
    FORCE_METER_ADVANCE(1, "valueCalculationMethod.force_meter_advance") {
        @Override
        public BigDecimal calculateAmount(BigDecimal rawValue, BigDecimal previousRawValue, BigDecimal overflow) {
            return calculateAdvance(rawValue, previousRawValue, overflow);
        }
    },
    RAW_DATA(2, "valueCalculationMethod.raw_data") {
        @Override
        public BigDecimal calculateAmount(BigDecimal rawValue, BigDecimal previousRawValue, BigDecimal overflow) {
            return rawValue;
        }
    };

    private int code;
    private String nameKey;

    ValueCalculationMethod(int dbCode, String nameKey) {
        this.code = dbCode;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getLocalizedName() {
        return Environment.DEFAULT.get().getTranslation(getNameKey());
    }

    @Override // eg. for in Combo boxes
    public String toString() {
        return getLocalizedName();
    }

    public static ValueCalculationMethod fromDb(int dbCode) {
        switch (dbCode) {
            case 1:
                return FORCE_METER_ADVANCE;
            case 2:
                return RAW_DATA;
            default:
                return AUTOMATIC;
        }
    }


    public boolean calculatesMeterAdvance() {
        return !RAW_DATA.equals(this);

    }


    protected final BigDecimal calculateAdvance(BigDecimal rawValue, BigDecimal previousRaw, BigDecimal overflow) {
        if (previousRaw == null) {
            return null;
        } else {
            BigDecimal diff = null;
            if (overflow != null && rawValue.compareTo(previousRaw) < 0) {
                diff = rawValue.add(overflow).subtract(previousRaw);
            } else {
                diff = rawValue.subtract(previousRaw);
            }
            return diff;
        }
    }

    public abstract BigDecimal calculateAmount(BigDecimal rawValue, BigDecimal previousRawValue, BigDecimal overflow);
}
