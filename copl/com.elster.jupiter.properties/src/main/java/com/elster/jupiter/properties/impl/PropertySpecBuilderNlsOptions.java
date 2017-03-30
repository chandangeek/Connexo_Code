/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.ValueFactory;

import java.util.Optional;

/**
 * Provides an implementation for the {@link PropertySpecBuilderWizard.NlsOptions} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (12:44)
 */
class PropertySpecBuilderNlsOptions<T> implements PropertySpecBuilderWizard.NlsOptions<T> {

    private final ValueFactory<T> valueFactory;

    PropertySpecBuilderNlsOptions(ValueFactory<T> valueFactory) {
        super();
        this.valueFactory = valueFactory;
    }

    @Override
    public PropertySpecBuilderWizard.ThesaurusBased<T> named(TranslationKey nameTranslationKey) {
        return new TranslationKeyOnly(nameTranslationKey);
    }

    @Override
    public PropertySpecBuilderWizard.ThesaurusBased<T> named(String name, TranslationKey displayNameTranslationKey) {
        return new NameAndTranslationKey(name, displayNameTranslationKey);
    }

    @Override
    public PropertySpecBuilderWizard.HardCoded<T> named(String name, String displayName) {
        return new StringBased(name, displayName);
    }

    private class TranslationKeyOnly implements PropertySpecBuilderWizard.ThesaurusBased<T> {
        private final TranslationKey translationKey;
        private TranslationKey descriptionTranslationKey;

        private TranslationKeyOnly(TranslationKey translationKey) {
            super();
            this.translationKey = translationKey;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> describedAs(TranslationKey descriptionTranslationKey) {
            this.descriptionTranslationKey = descriptionTranslationKey;
            return this;
        }

        @Override
        public PropertySpecBuilder<T> fromThesaurus(Thesaurus thesaurus) {
            return new PropertySpecBuilderImpl<>(valueFactory)
                            .setNameAndDescription(
                                    NameAndDescription.thesaurusBased(
                                            thesaurus,
                                            this.translationKey,
                                            Optional.ofNullable(this.descriptionTranslationKey)));
        }
    }

    private class NameAndTranslationKey implements PropertySpecBuilderWizard.ThesaurusBased<T> {
        private final String name;
        private final TranslationKey translationKey;
        private TranslationKey descriptionTranslationKey;

        private NameAndTranslationKey(String name, TranslationKey translationKey) {
            super();
            this.name = name;
            this.translationKey = translationKey;
        }

        @Override
        public PropertySpecBuilderWizard.ThesaurusBased<T> describedAs(TranslationKey descriptionTranslationKey) {
            this.descriptionTranslationKey = descriptionTranslationKey;
            return this;
        }

        @Override
        public PropertySpecBuilder<T> fromThesaurus(Thesaurus thesaurus) {
            return new PropertySpecBuilderImpl<>(valueFactory)
                            .setNameAndDescription(
                                    NameAndDescription.thesaurusBased(
                                            thesaurus,
                                            this.name,
                                            this.translationKey,
                                            Optional.ofNullable(this.descriptionTranslationKey)));
        }
    }

    private class StringBased implements PropertySpecBuilderWizard.HardCoded<T> {
        private final String name;
        private final String displayName;

        private StringBased(String name, String displayName) {
            super();
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public PropertySpecBuilder<T> describedAs(String description) {
            return new PropertySpecBuilderImpl<>(valueFactory)
                    .setNameAndDescription(
                            NameAndDescription.stringBased(
                                    this.name,
                                    this.displayName,
                                    description));
        }
    }

}