package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import java.math.BigDecimal;

/**
 * Models the specification of a register that contains numerical data.
 *
 * @author Geert
 */
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
     * Returns the configured multiplier.
     *
     * @return the receiver's multiplier.
     */
    BigDecimal getMultiplier();

    /**
     * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
     * <p/>
     * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
     *
     * @param multiplier the multiplier to set
     */
    void setMultiplier(BigDecimal multiplier);

    /**
     * Returns the configured multiplier mode.
     *
     * @return the receiver's multiplier mode.
     */
    MultiplierMode getMultiplierMode();

    /**
     * Set the MultiplierMode to the given mode.
     * <p/>
     * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
     * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
     *
     * @param multiplierMode the given MultiplierMode
     */
    void setMultiplierMode(MultiplierMode multiplierMode);

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
         * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
         * <p/>
         * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
         *
         * @param multiplier the multiplier to set
         */
        Builder setMultiplier(BigDecimal multiplier);

        /**
         * Set the MultiplierMode to the given mode.
         * <p/>
         * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
         * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
         *
         * @param multiplierMode the given MultiplierMode
         */
        Builder setMultiplierMode(MultiplierMode multiplierMode);

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
         * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
         * <p/>
         * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
         *
         * @param multiplier the multiplier to set
         */
        Updater setMultiplier(BigDecimal multiplier);

        /**
         * Set the MultiplierMode to the given mode.
         * <p/>
         * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
         * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
         *
         * @param multiplierMode the given MultiplierMode
         */
        Updater setMultiplierMode(MultiplierMode multiplierMode);

        /**
         * Completes the update procoess for the RegisterSpec.
         */
        void update();

    }

}