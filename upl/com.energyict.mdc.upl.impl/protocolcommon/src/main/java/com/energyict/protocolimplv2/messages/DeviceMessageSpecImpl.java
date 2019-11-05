package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;


/**
 * Provides an implementation for the {@link DeviceMessageSpec} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (13:06)
 */
public class DeviceMessageSpecImpl implements DeviceMessageSpec {

    private long id;
    private String name;
    private TranslationKey nameTranslationKey;
    private DeviceMessageCategorySupplier categoryFactory;
    private List<PropertySpec> propertySpecs;

    private PropertySpecService propertySpecService;
    private NlsService nlsService;
    private Converter converter;

    private DeviceMessageCategory category;

    public DeviceMessageSpecImpl() {
    }

    public DeviceMessageSpecImpl(long id, TranslationKey translationKey, DeviceMessageCategorySupplier categoryFactory, List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.id = id;
        this.nameTranslationKey = translationKey;
        this.categoryFactory = categoryFactory;
        this.propertySpecs = propertySpecs;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    @XmlAttribute
    public String getName() {
        if (this.nlsService != null)
            name = this.nlsService
                    .getThesaurus(Thesaurus.ID.toString())
                    .getFormat(this.getNameTranslationKey())
                    .format();
        return name;
    }

    @Override
    @XmlAttribute
    public long getId() {
        return this.id;
    }

    @Override
    @XmlElement(type = TranslationKeyImpl.class)
    public TranslationKey getNameTranslationKey() {
        return nameTranslationKey;
    }

    @Override
    @XmlElement(type=DeviceMessageCategoryImpl.class)
    public DeviceMessageCategory getCategory() {
        if (propertySpecService != null)
            category = categoryFactory.get(this.propertySpecService, this.nlsService, this.converter);
        return category;
    }

    @Override
    @JsonIgnore
    @XmlTransient
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