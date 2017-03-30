/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.NumericalRegisterSpec;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Models a {@link Register} that stores numerical data that relates to
 * billing and is relevant in the context of the billing period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface BillingRegister extends Register<BillingReading, NumericalRegisterSpec> {

    Optional<BigDecimal> getOverflow();

    int getNumberOfFractionDigits();
}