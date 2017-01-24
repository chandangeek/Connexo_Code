package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 24-mei-2011
 * Time: 15:02:56
 */
public class ProfileDataValue {

    private BigDecimal value;
    private boolean valid = true;

    public ProfileDataValue(int value) {
        if (value == 0x4FFF) {
            valid = false;
        }
        this.value = calcValue(value);
    }


    private BigDecimal calcValue(int rawValue) {
        double sign = ((rawValue & 0xF800) == 0xF800) ? -1 : 1;  //b15 b14 b12 b11 b10 = 11111 ? ==> indicates a negative value
        return new BigDecimal((sign * (rawValue & 0x07FF)) / 16);
    }

    public boolean isValid() {
        return valid;
    }

    public BigDecimal getValue() {
        return value;
    }
}