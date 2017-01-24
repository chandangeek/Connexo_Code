package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.cbo.Quantity;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.MeterType;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 18/02/11
 * Time: 11:01
 */
public class DigitsCalculator {

    private static final int DEF_METER_NUMBER_OF_DIGITS = 8;
    private static final int MIN_METER_NUMBER_OF_DIGITS = 5;
    private static final int MAX_METER_NUMBER_OF_DIGITS = 9;
    private static final int CONVERTER_NUMBER_OF_DIGITS = 9;

    /**
     * Calculate the number of digits of the gas device.
     *
     * @param meterType The MeterType of the gas device
     * @param maxFlow   The maximum flow (in m³/h) of the gas device
     * @return the calculated number of digits for the given parameters or the default value if the parameters are out of range
     */
    public static int getMeterNumberOfDigits(MeterType meterType, Quantity maxFlow, Quantity meterWeight) {
        return getByRuleOfThumb(meterWeight);
    }

    /**
     * Approximate the number of digits by using a rule of thumb formula
     *
     * @param meterWeight
     * @return A value from 5 to 9, representing the number of digits (default = 8)
     */
    public static int getByRuleOfThumb(Quantity meterWeight) {
        int digits = DEF_METER_NUMBER_OF_DIGITS;
        if ((meterWeight != null) && (meterWeight.getAmount() != null)) {
            BigDecimal weight = meterWeight.getAmount();
            int log10 = (int) Math.floor(Math.log10(weight.doubleValue()));
            if (weight.compareTo(BigDecimal.ZERO) > 0) {
                digits = 7 - log10;
                digits = (digits < 0) ? 0 : digits;
            }
            if ((digits < MIN_METER_NUMBER_OF_DIGITS) || (digits > MAX_METER_NUMBER_OF_DIGITS)) {
                digits = DEF_METER_NUMBER_OF_DIGITS;
            }
        }
        return digits;
    }

    /**
     * The convertor number of digits are fixed to 9
     *
     * @return
     */
    public static int getConvertorNumberOfDigits() {
        return CONVERTER_NUMBER_OF_DIGITS;
    }
}
