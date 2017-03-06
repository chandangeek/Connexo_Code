package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.util.List;


/**
 * Provides an implementation for the {@link DeviceMessageSpec} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (13:06)
 */
public class DeviceMessageSpecImpl implements DeviceMessageSpec {
    private final long id;
    private final TranslationKey translationKey;
    private final DeviceMessageCategorySupplier categoryFactory;
    private final List<PropertySpec> propertySpecs;

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public DeviceMessageSpecImpl(long id, TranslationKey translationKey, DeviceMessageCategorySupplier categoryFactory, List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.id = id;
        this.translationKey = translationKey;
        this.categoryFactory = categoryFactory;
        this.propertySpecs = propertySpecs;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public String getName() {
        return this.nlsService
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public TranslationKey getNameTranslationKey() {
        return translationKey;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return categoryFactory.get(this.propertySpecService, this.nlsService, this.converter);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.propertySpecs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof DeviceMessageSpecSupplier) {
            DeviceMessageSpecSupplier that = (DeviceMessageSpecSupplier) o;
            return getId() == that.id();
        } else if (o instanceof DeviceMessageSpec) {
            DeviceMessageSpec that = (DeviceMessageSpec) o;
            return getId() == that.getId();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}