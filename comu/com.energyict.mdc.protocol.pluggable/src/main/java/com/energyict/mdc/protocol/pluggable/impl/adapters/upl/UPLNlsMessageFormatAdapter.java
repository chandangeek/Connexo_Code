package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.nls.NlsMessageFormat;

/**
 * @author Stijn Vanhoorelbeke
 * @since 12.09.17 - 17:10
 */
public class UPLNlsMessageFormatAdapter implements NlsMessageFormat {

    private final com.elster.jupiter.nls.NlsMessageFormat actual;

    public static NlsMessageFormat adaptTo(com.elster.jupiter.nls.NlsMessageFormat actual) {
        if (actual instanceof ConnexoNlsMessageFormatAdapter) {
            return ((ConnexoNlsMessageFormatAdapter) actual).getUplMessageFormat();
        } else {
            return new UPLNlsMessageFormatAdapter(actual);
        }

    }

    private UPLNlsMessageFormatAdapter(com.elster.jupiter.nls.NlsMessageFormat actual) {
        this.actual = actual;
    }

    public com.elster.jupiter.nls.NlsMessageFormat getConnexoNlsMessageFormat() {
        return actual;
    }

    @Override
    public String format(Object... parameters) {
        return this.actual.format(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UPLNlsMessageFormatAdapter) {
            return actual.equals(((UPLNlsMessageFormatAdapter) o).actual);
        } else {
            return actual.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return actual != null ? actual.hashCode() : 0;
    }
}