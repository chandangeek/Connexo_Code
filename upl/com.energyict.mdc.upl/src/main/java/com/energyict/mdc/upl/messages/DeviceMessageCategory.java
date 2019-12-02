package com.energyict.mdc.upl.messages;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Models the category of a {@link DeviceMessage}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:03)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface DeviceMessageCategory {

    /**
     * Gets the translated name of this DeviceMessageCategory.
     *
     * @return The translated name
     */
    String getName();

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    String getNameResourceKey();

    /**
     * Gets the description of this DeviceMessageCategory.
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the unique identifier this DeviceMessageCategory.
     *
     * @return The identifier
     */
    int getId();

    /**
     * Gets the {@link DeviceMessageSpec}s that are part of this DeviceMessageCategory.
     *
     * @return The DeviceMessageSpecs that are part of this DeviceMessageCategory
     */
    List<DeviceMessageSpec> getMessageSpecifications();

    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {}

}