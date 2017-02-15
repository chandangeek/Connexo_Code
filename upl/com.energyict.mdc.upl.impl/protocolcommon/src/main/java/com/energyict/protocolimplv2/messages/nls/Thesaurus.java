package com.energyict.protocolimplv2.messages.nls;

/**
 * Provides the id of the Thesaurus that is expected to provide
 * the translations for the device messages and their property specs.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-30 (11:01)
 */
public enum Thesaurus {
    ID {
        @Override
        public String toString() {
            return "PR1";
        }
    };
}