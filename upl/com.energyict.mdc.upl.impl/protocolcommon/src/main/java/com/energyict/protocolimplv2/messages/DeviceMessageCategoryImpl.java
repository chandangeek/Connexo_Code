package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceMessageCategory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (14:15)
 */
public class DeviceMessageCategoryImpl implements DeviceMessageCategory {
    private final int id;
    private final TranslationKey nameTranslationKey;
    private final TranslationKey descriptionTranslationKey;
    private final List<DeviceMessageSpecSupplier> factories;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public DeviceMessageCategoryImpl(int id, TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey, List<DeviceMessageSpecSupplier> factories, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.id = id;
        this.nameTranslationKey = nameTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
        this.factories = factories;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.nlsService
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.nameTranslationKey)
                .format();
    }

    @Override
    public String getNameResourceKey() {
        return nameTranslationKey.getKey();
    }

    @Override
    public String getDescription() {
        return this.nlsService
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.descriptionTranslationKey)
                .format();
    }

    @Override
    public List<DeviceMessageSpec> getMessageSpecifications() {
        return this.factories
                .stream()
                .map(factory -> factory.get(this.propertySpecService, this.nlsService, this.converter))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceMessageCategoryImpl that = (DeviceMessageCategoryImpl) o;

        return getId() == that.getId();

    }

    @Override
    public int hashCode() {
        return getId();
    }
}