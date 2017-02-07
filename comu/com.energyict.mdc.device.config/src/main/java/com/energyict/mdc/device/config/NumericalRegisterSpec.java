/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.Optional;

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

    /**
     * Returns the overflow value
     *
     * @return the overflow value
     */
    public Optional<BigDecimal> getOverflowValue();

    boolean isUseMultiplier();

    Optional<ReadingType> getCalculatedReadingType();

    /**
     * Defines a Builder interface to construct a {@link NumericalRegisterSpec}.
     */
    interface Builder {

        Builder overruledObisCode(ObisCode overruledObisCode);

        Builder numberOfFractionDigits(int numberOfFractionDigits);

        Builder overflowValue(BigDecimal overflowValue);

        Builder useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType);

        Builder noMultiplier();

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

        Updater useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType);

        Updater noMultiplier();

        /**
         * Completes the update procoess for the RegisterSpec.
         */
        void update();

    }

}