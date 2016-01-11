package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;

/**
 * Models a {@link Reading} for numerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface NumericalReading extends Reading {

    Quantity getQuantity();

    Quantity getQuantityFor(ReadingType readingType);

    BigDecimal getValue();

}