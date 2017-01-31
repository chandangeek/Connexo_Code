/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import aQute.bnd.annotation.ProviderType;

import javax.validation.ConstraintViolation;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Function;

@ProviderType
public interface NlsService {

    String COMPONENTNAME = "NLS";

    Thesaurus getThesaurus(String componentName, Layer layer);

    PrivilegeThesaurus getPrivilegeThesaurus();

    /**
     * Starts the translation process for a single key at a time.
     *
     * @param key The NlsKey
     * @return The builder that supports the translation process
     */
    TranslationBuilder translate(NlsKey key);

    /**
     * Adds translations in bulk from data provided by the InputStream.
     * This will produce errors when a key was already translated
     * to the specified Locale before.
     * The data is expected to be formatted as a CSV file.
     * In other words, each translation is on a single line and has the following columns:
     * <ol>
     * <li>component</li>
     * <li>layer</li>
     * <li>key</li>
     * <li>translation</li>
     * </ol>
     * @param in The InputStream
     * @param locale The Locale
     */
    void addTranslations(InputStream in, Locale locale);

    /**
     * Updates translations in bulk from data provided by the InputStream.
     * This will NOT produce errors when a key was already translate
     * to the specified Locale before. Instread, it will update the
     * old translation to the new translation.
     * The data is expected to be formatted as a CSV file.
     * In other words, each translation is on a single line and has the following columns:
     * <ol>
     * <li>component</li>
     * <li>layer</li>
     * <li>key</li>
     * <li>translation</li>
     * </ol>
     * @param in The InputStream
     * @param locale The Locale
     */
    void updateTranslations(InputStream in, Locale locale);

    String interpolate(ConstraintViolation<?> violation);

    /**
     * Copies the specified {@link NlsKey} into this Thesaurus,
     * mapping the key the result of the mapping function.
     *
     * @param key The NlsKey
     * @param targetComponent The target component
     * @param targetLayer the target layer
     * @param keyMapper The key mapping function
     */
    void copy(NlsKey key, String targetComponent, Layer targetLayer, Function<String, String> keyMapper);

    @ProviderType
    interface TranslationBuilder {
        TranslationBuilder to(Locale locale, String translation);

        void add();
    }

}