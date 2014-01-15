package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.LocalizableEnum;
import com.energyict.mdc.common.Environment;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:19
 */
public enum ReadingMethod implements LocalizableEnum {
    ENGINEERING_UNIT(0, "readingMethod.engineering_units"),
    BASIC_DATA(1, "readingMethod.basic_data");

    private int code;
    private String nameKey;

    private ReadingMethod(int code, String nameKey) {
        this.code = code;
        this.nameKey=nameKey;
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

    public static ReadingMethod fromDb(int dbCode) {
        switch (dbCode) {
            case 1:
                return BASIC_DATA;
            default:
                return ENGINEERING_UNIT;

        }
    }
}
