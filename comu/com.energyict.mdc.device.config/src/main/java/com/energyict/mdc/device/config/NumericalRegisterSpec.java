package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.RegisterType;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;

/**
 * Models the specification of a register that contains numerical data.
 *
 * @author Geert
 */
@ProviderType
public interface NumericalRegisterSpec extends RegisterSpec {

    /**
     * Tests if this is a cumulative register mapping
     *
     * @return true if cumulative, false otherwise.
     */
    public boolean isCumulative();

    /**
     * Returns the number of digits for this spec
     *
     * @return the number of digits
     */
    public int getNumberOfDigits();

    void setNumberOfDigits(int numberOfDigits);

    /**
     * Returns the number of fraction digits for this spec
     *
     * @return the number of fraction digits
     */
    public int getNumberOfFractionDigits();

    void setNumberOfFractionDigits(int numberOfFractionDigits);

    /**
     * Returns the overflow value
     *
     * @return the overflow value
     */
    public BigDecimal getOverflowValue();

    void setOverflowValue(BigDecimal overflowValue);

    /**
     * Defines a Builder interface to construct a {@link NumericalRegisterSpec}.
     */
    interface Builder {

        Builder setRegisterType(RegisterType registerType);

        Builder setOverruledObisCode(ObisCode overruledObisCode);

        Builder setNumberOfDigits(int numberOfDigits);

        Builder setNumberOfFractionDigits(int numberOfFractionDigits);

        Builder setOverflowValue(BigDecimal overflowValue);

        /**
         * Does final validation and <i>creates</i> the {@link NumericalRegisterSpec}
         *
         * @return the RegisterSpec
         */
        NumericalRegisterSpec add();

    }

    /**
     * Defines a updater component to update a {@link NumericalRegisterSpec}.
     */
    interface Updater {

        /**
         * Defines a updater component to update a {@link RegisterSpec}.
         */
        Updater setOverruledObisCode(ObisCode overruledObisCode);

        Updater setNumberOfDigits(int numberOfDigits);

        Updater setNumberOfFractionDigits(int numberOfFractionDigits);

        Updater setOverflowValue(BigDecimal overflowValue);

        /**
         * Completes the update procoess for the RegisterSpec.
         */
        void update();

    }

}