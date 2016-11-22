package com.energyict.mdc.upl.nls;

/**
 * Models an entry in a {@link Thesaurus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:55)
 */
public interface TranslationKey {

    /**
     * Gets the object that uniquely identifies this key in its {@link Thesaurus}.
     *
     * @return The key
     */
    String getKey();

    /**
     * Gets the default translation of this key
     * that will be returned if the key's translation
     * is not available in the requested language.
     *
     * @return The default translatin
     */
    String getDefaultFormat();

}