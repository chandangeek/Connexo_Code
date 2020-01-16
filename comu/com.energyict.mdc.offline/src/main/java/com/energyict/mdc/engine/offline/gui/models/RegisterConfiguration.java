package com.energyict.mdc.engine.offline.gui.models;


import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 29/11/12
 * Time: 15:27
 */
public interface RegisterConfiguration {

    /**
     * Returns the number of digits for this spec
     *
     * @return the number of digits
     */
    public int getNumberOfDigits();

    /**
     * Returns the number of fraction digits for this spec
     *
     * @return the number of fraction digits
     */
    public int getNumberOfFractionDigits();

    /**
     * Returns the configured multiplier.
     *
     * @return the receiver's multiplier.
     */
    BigDecimal getMultiplier();

    /**
     * Returns the configured multiplier mode.
     *
     * @return the receiver's multiplier mode.
     */
    MultiplierMode getMultiplierMode();

    /**
     * Returns the overflow value
     *
     * @return the overflow value
     */
    public BigDecimal getOverflowValue();

    /**
     * @return true if the multiplier value on this configuration overrules the spec, false otherwise.
     */
    public boolean isMultiplierOverruled();

    /**
     * @return true if the number of digits on this configuration overrules the spec, false otherwise.
     */
    public boolean isNumberOfDigitsOverruled();

    /**
     * @return true if the number of fraction digits on this configuration overrules the spec, false otherwise.
     */
    public boolean isNumberOfFractionDigitsOverruled();

    /**
     * @return true if the multiplier mode on this configuration overrules the spec, false otherwise.
     */
    public boolean isMultiplierModeOverruled();

    /**
     * @return true if the overflow value on this configuration overrules the spec, false otherwise.
     */
    public boolean isOverflowOverruled();
}
