package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class AbstractReadingTypeNameSearchableProperty<T> extends AbstractNameSearchableProperty<T> {

    public AbstractReadingTypeNameSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(clazz, propertySpecService, thesaurus);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.READING_TYPE_NAME).format();
    }
}
