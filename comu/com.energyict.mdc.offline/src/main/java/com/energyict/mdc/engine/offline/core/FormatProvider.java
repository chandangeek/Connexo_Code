package com.energyict.mdc.engine.offline.core;

import java.text.DecimalFormatSymbols;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 *
 * @since 10/12/12 12:04 PM
 */
public interface FormatProvider {

    AtomicReference<FormatProvider> instance = new AtomicReference<>();

    FormatPreferences getFormatPreferences();

    DecimalFormatSymbols getDecimalFormatSymbols();

    char getDecimalSeparator();

    char getGroupingSeparator();
}
