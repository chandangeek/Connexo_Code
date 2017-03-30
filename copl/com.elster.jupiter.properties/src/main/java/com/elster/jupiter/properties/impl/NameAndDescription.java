/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;

/**
 * Holds name and description information
 * during the build process of a {@link com.elster.jupiter.properties.PropertySpec}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (12:49)
 */
class NameAndDescription {

    private String name;
    private String displayName;
    private String description;

    static NameAndDescription thesaurusBased(Thesaurus thesaurus, TranslationKey nameTranslationKey, Optional<TranslationKey> descriptionTranslationKey) {
        return thesaurusBased(thesaurus, nameTranslationKey.getKey(), nameTranslationKey, descriptionTranslationKey);
    }

    static NameAndDescription thesaurusBased(Thesaurus thesaurus, String name, TranslationKey displayNameTranslationKey, Optional<TranslationKey> descriptionTranslationKey) {
        return stringBased(
                name,
                thesaurus.getFormat(displayNameTranslationKey).format(),
                descriptionTranslationKey
                        .map(key -> thesaurus.getFormat(key).format())
                        .orElse(null));
    }

    static NameAndDescription stringBased(String name, String displayName, String description) {
        NameAndDescription nameAndDescription = new NameAndDescription();
        nameAndDescription.name = name;
        nameAndDescription.displayName = displayName;
        nameAndDescription.description = description;
        return nameAndDescription;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

}