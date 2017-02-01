/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the specification of a register that contains numerical data.
 *
 * @author Geert
 */
@ProviderType
public interface TextualRegisterSpec extends RegisterSpec {

    /**
     * Defines a Builder interface to construct a {@link TextualRegisterSpec}.
     */
    interface Builder {


        Builder setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Does final validation and <i>creates</i> the {@link TextualRegisterSpec}
         *
         * @return the TextualRegisterSpec
         */
        TextualRegisterSpec add();
    }

    /**
     * Defines a updater component to update a {@link TextualRegisterSpec}.
     */
    interface Updater {

        /**
         * Completes the update process for the RegisterSpec.
         */
        void update();

        Updater overruledObisCode(ObisCode overruledObisCode);
    }

}