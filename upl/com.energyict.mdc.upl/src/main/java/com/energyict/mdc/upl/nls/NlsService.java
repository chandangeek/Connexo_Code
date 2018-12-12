package com.energyict.mdc.upl.nls;

/**
 * Provides services to translate {@link TranslationKey}s to
 * all the different languages supported by the platform.
 * Nls is short for Natural Language Support.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:40)
 */
public interface NlsService {

    Thesaurus getThesaurus(String id);

}