package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
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

    boolean isUseMultiplier();

    void setUseMultiplier(boolean useMultiplier);

    ReadingType getCalculatedReadingType();

    void setCalculatedReadingType(ReadingType calculatedReadingType);

    /**
     * Defines a Builder interface to construct a {@link NumericalRegisterSpec}.
     */
    interface Builder {

        Builder overruledObisCode(ObisCode overruledObisCode);

        Builder numberOfFractionDigits(int numberOfFractionDigits);

        Builder overflowValue(BigDecimal overflowValue);

        Builder useMultiplier(boolean useMultiplier);

        Builder calculatedReadingType(ReadingType calculatedReadingType);

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
        Updater overruledObisCode(ObisCode overruledObisCode);

        Updater numberOfFractionDigits(int numberOfFractionDigits);

        Updater overflowValue(BigDecimal overflowValue);

        Updater useMultiplier(boolean useMultiplier);

        Updater calculatedReadingType(ReadingType calculatedReadingType);

        /**
         * Completes the update procoess for the RegisterSpec.
         */
        void update();

    }

}