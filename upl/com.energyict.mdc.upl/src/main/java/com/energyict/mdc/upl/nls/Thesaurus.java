package com.energyict.mdc.upl.nls;

/**
 * Models a dictionary that supports translation of {@link TranslationKey}
 * to a set of supported languages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:45)
 */
public interface Thesaurus {

    NlsMessageFormat getFormat(TranslationKey key);

}