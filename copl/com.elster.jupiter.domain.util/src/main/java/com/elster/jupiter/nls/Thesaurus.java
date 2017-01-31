/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ProviderType;

import javax.validation.MessageInterpolator;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@ProviderType
public interface Thesaurus extends MessageInterpolator {

    //
    // Locale is obtained from security.thread
    // Note that the query behind this is more complex than at first sight.
    // It needs to honor the Locale (or LanguageTag) hierarchy.
    //

    String getString(String key, String defaultMessage);

    String getString(Locale locale, String key, String defaultMessage);

    NlsMessageFormat getFormat(MessageSeed seed);

    NlsMessageFormat getFormat(TranslationKey key);

    /**
     * @return map containing the key and the translation to the current locale (as obtained from security.thread) for all the keys of the thesausrus
     * @since v3.0
     */
    Map<String, String> getTranslationsForCurrentLocale();

    /**
     * @param key the key to check
     * @return true if this thesaurus has the passed key
     * @since v3.0
     */
    boolean hasKey(String key);

    Thesaurus join(Thesaurus thesaurus);

    DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter);

}