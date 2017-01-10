package com.energyict.mdc.channels.nls;

/**
 * Provides the id of the Thesaurus that is expected to provide
 * the translations for the I/O errors that are produced by connection types.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (11:42)
 */
public enum Thesaurus {
    ID {
        @Override
        public String toString() {
            return "MIO";
        }
    };
}