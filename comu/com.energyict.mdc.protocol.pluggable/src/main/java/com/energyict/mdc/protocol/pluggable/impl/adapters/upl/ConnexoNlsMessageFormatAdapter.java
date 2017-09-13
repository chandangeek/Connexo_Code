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
public class ConnexoNlsMessageFormatAdapter implements NlsMessageFormat {

    private final com.energyict.mdc.upl.nls.NlsMessageFormat actual;

    public static NlsMessageFormat adaptTo(com.energyict.mdc.upl.nls.NlsMessageFormat actual) {
        if (actual instanceof UPLNlsMessageFormatAdapter) {
            return ((UPLNlsMessageFormatAdapter) actual).getConnexoNlsMessageFormat();
        } else {
            return new ConnexoNlsMessageFormatAdapter(actual);
        }
    }

    private ConnexoNlsMessageFormatAdapter(com.energyict.mdc.upl.nls.NlsMessageFormat actual) {
        this.actual = actual;
    }

    public com.energyict.mdc.upl.nls.NlsMessageFormat getUplMessageFormat() {
        return actual;
    }

    @Override
    public String format(Object... args) {
        return this.actual.format(args);
    }

    @Override
    public String format(Locale locale, Object... args) {
        return this.format(args);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConnexoNlsMessageFormatAdapter) {
            return actual.equals(((ConnexoNlsMessageFormatAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}