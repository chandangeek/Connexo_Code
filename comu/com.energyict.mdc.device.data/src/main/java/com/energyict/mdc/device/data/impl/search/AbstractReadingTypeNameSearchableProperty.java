/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class AbstractReadingTypeNameSearchableProperty<T> extends AbstractNameSearchableProperty<T> {

    public AbstractReadingTypeNameSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(clazz, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.READING_TYPE_NAME;
    }

}