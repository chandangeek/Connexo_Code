package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.nls.NlsMessageFormat;

import java.util.Locale;

/**
 * Adapter class between {@link com.energyict.mdc.upl.nls.NlsMessageFormat}
 * from the universal protocol layer to the Connexo {@link NlsMessageFormat}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (10:58)
 */
class ConnexoNlsMessageFormatAdapter implements NlsMessageFormat {
    private final com.energyict.mdc.upl.nls.NlsMessageFormat actual;

    ConnexoNlsMessageFormatAdapter(com.energyict.mdc.upl.nls.NlsMessageFormat actual) {
        this.actual = actual;
    }

    @Override
    public String format(Object... args) {
        return this.actual.format(args);
    }

    @Override
    public String format(Locale locale, Object... args) {
        return this.format(args);
    }
}