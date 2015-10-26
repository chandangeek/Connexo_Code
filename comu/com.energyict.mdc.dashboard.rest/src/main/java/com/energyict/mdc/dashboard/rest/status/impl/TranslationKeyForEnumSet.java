package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (13:17)
 */
public interface TranslationKeyForEnumSet<E extends Enum> {

    String translationFor(E value, Thesaurus thesaurus);

}