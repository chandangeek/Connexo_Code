package com.energyict.mdc.upl.nls;

import java.util.logging.Level;

/**
 * Models a unique error message that is capable of producing
 * a human readable description (hence the name Seed).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (12:00)
 */
public interface MessageSeed extends TranslationKey {
    int getId();
    Level getLevel();
}