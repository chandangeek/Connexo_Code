package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
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
    private final DeviceMessageSpecPrimaryKey primaryKey;
    private final TranslationKey translationKey;
    private final DeviceMessageCategoryFactory categoryFactory;
    private final List<PropertySpec> propertySpecs;

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public DeviceMessageSpecImpl(long id, DeviceMessageSpecPrimaryKey primaryKey, TranslationKey translationKey, DeviceMessageCategoryFactory categoryFactory, List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, NlsService nlsService) {
        this.id = id;
        this.primaryKey = primaryKey;
        this.translationKey = translationKey;
        this.categoryFactory = categoryFactory;
        this.propertySpecs = propertySpecs;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public long getMessageId() {
        return this.id;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return this.primaryKey;
    }

    @Override
    public String getName() {
        return this.nlsService
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public TranslationKey getNameTranslationKey() {
        return translationKey;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return categoryFactory.get(this.propertySpecService, this.nlsService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.propertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}