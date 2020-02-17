/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol.security;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Models a level of security for a physical device
 * and the {@link PropertySpec}s
 * that the Device will require to be specified
 * before accessing the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:14)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface DeviceAccessLevel {

    /**
     * Represents a non-used DeviceAccessLevel.
     */
    int NOT_USED_DEVICE_ACCESS_LEVEL_ID = -1;

    /**
     * Indicates that a (slave device) security set can inherit its properties from the master device's security set
     */
    int CAN_INHERIT_PROPERTIES_FROM_MASTER_ID = -100;

    /**
     * Returns a number that uniquely identifies
     * this DeviceAccessLevel within the scope of the
     * {@link DeviceProtocol}
     * that returned this DeviceAccessLevel.
     * <p>
     * <b>Note</b> that the ID may not be equal to the {@link #NOT_USED_DEVICE_ACCESS_LEVEL_ID}
     *
     * @return The identifier
     */
    @XmlAttribute
    int getId();

    /**
     * Returns the human
     * readable description or name for this DeviceAccessLevel.
     * The internal format for translation key is
     * &lt;fully qualified class name of the device protocol&gt;.accesslevel.&lt;id&gt;
     *
     * @return The translation resource bundle
     */
    @XmlAttribute
    String getTranslation();

    /**
     * Gets the List of {@link PropertySpec properties}
     * that the related {@link DeviceProtocol}
     * will require to be defined on a physcial device
     * to use this DeviceAccessLevel.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getSecurityProperties();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);
}