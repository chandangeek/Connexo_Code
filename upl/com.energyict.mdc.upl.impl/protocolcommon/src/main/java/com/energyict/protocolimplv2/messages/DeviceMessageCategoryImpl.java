package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceMessageCategory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (14:15)
 */
public class DeviceMessageCategoryImpl implements DeviceMessageCategory {
    private int id;
    private String name;
    private String description;
    private TranslationKey nameTranslationKey;
    private TranslationKey descriptionTranslationKey;
    private List<DeviceMessageSpecSupplier> factories;
    private PropertySpecService propertySpecService;
    private NlsService nlsService;
    private Converter converter;

    private List<DeviceMessageSpec> messageSpecifications;

    public DeviceMessageCategoryImpl() {
    }

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
        if (this.nlsService != null)
            name = this.nlsService
                    .getThesaurus(Thesaurus.ID.toString())
                    .getFormat(this.nameTranslationKey)
                    .format();

        return name;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public String getNameResourceKey() {
        return nameTranslationKey.getKey();
    }

    @XmlElement(type = TranslationKeyImpl.class)
    public TranslationKey getNameTranslationKey() {
        return nameTranslationKey;
    }

    @XmlElement(type = TranslationKeyImpl.class)
    public TranslationKey getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    @Override
    public String getDescription() {
        if (this.nlsService != null)
            description = this.nlsService
                    .getThesaurus(Thesaurus.ID.toString())
                    .getFormat(this.descriptionTranslationKey)
                    .format();
        return description;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public List<DeviceMessageSpec> getMessageSpecifications() {
        if (propertySpecService != null)
            messageSpecifications = this.factories
                .stream()
                .map(factory -> factory.get(this.propertySpecService, this.nlsService, this.converter))
                .collect(Collectors.toList());
        return messageSpecifications;
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