package com.energyict.mdc.upl.properties;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Contains intermediate steps to get to a {@link PropertySpecBuilder}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (08:38)
 */
public interface PropertySpecBuilderWizard {

    /**
     * Here is where the client code chooses between
     * a {@link PropertySpec} that is backed by the
     * {@link NlsService} or for a hard coded approach.
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
     * a {@link PropertySpec} that is backed by the {@link NlsService}.
     *
     * @param <T> The value domain of the PropertySpec
     */
    interface ThesaurusBased<T> {
        /**
         * Specifies the description of the {@link PropertySpec}.
         *
         * @param descriptionTranslationKey The TranslationKey for the description
         * @return This step of the building process to allow method chaining
         */
        PropertySpecBuilder<T> describedAs(TranslationKey descriptionTranslationKey);
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