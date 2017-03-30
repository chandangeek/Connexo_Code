/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

/**
 * Contains intermediate steps to get to a {@link PropertySpecBuilder}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-08 (09:05)
 */
@ProviderType
public interface PropertySpecBuilderWizard {

    /**
     * Here is where the client code chooses between
     * a {@link PropertySpec} that is backed by the
     * {@link com.elster.jupiter.nls.NlsService}
     * or for a hard coded approach.
     * @param <T> The value domain of the PropertySpec
     */
    interface NlsOptions<T> {
        /**
         * Specifies the name of the {@link PropertySpec}.
         * The key of the {@link TranslationKey} is used
         * as the name of the PropertySpec.
         *
         * @param nameTranslationKey The TranslationKey
         * @return The next step in the building process
         */
        ThesaurusBased<T> named(TranslationKey nameTranslationKey);

        /**
         * Specifies the name of the {@link PropertySpec}.
         *
         * @param name The name of the PropertySpec
         * @param displayNameTranslationKey The TranslationKey for the display name
         * @return The next step in the building process
         */
        ThesaurusBased<T> named(String name, TranslationKey displayNameTranslationKey);

        /**
         * Specifies the name and display name of the {@link PropertySpec}.
         *
         * @param name The name of the PropertySpec
         * @param displayName The display name of the PropertySpec
         * @return The next step in the building process
         */
        HardCoded<T> named(String name, String displayName);
    }

    /**
     * More options when the client code has chosen for
     * a {@link PropertySpec} that is backed by the
     * {@link com.elster.jupiter.nls.NlsService}.
     * @param <T> The value domain of the PropertySpec
     */
    interface ThesaurusBased<T> {
        /**
         * Specifies the description of the {@link PropertySpec}.
         *
         * @param descriptionTranslationKey The TranslationKey for the description
         * @return This step of the building process to allow method chaining
         */
        ThesaurusBased<T> describedAs(TranslationKey descriptionTranslationKey);

        /**
         * Specifies the {@link Thesaurus} that contains the translations
         * of the previously specified {@link TranslationKey}s.
         *
         * @param thesaurus The Thesaurus
         * @return The PropertySpecBuilder
         */
        PropertySpecBuilder<T> fromThesaurus(Thesaurus thesaurus);
    }

    /**
     * More options when the client code has chosen for
     * the hard coded approach.
     * @param <T> The value domain of the PropertySpec
     */
    interface HardCoded<T> {
        /**
         * Specifies the description of the {@link PropertySpec}.
         *
         * @param description The description
         * @return The PropertySpecBuilder
         */
        PropertySpecBuilder<T> describedAs(String description);
    }

}