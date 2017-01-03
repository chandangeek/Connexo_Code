package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;

/**
 * Models a level of security for a Device and the {@link PropertySpec}s
 * that the Device will require to be specified
 * before accessing the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:14)
 */
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
     * {@link com.energyict.mdc.upl.DeviceProtocol}
     * that returned this DeviceAccessLevel.
     * <p/>
     * <b>Note</b> that the ID may not be equal to the {@link #NOT_USED_DEVICE_ACCESS_LEVEL_ID}
     *
     * @return The identifier
     */
    int getId();

    /**
     * Returns a String that serves as the key in a
     * translation resource bundle to provide a human
     * readable description or name for this DeviceAccessLevel.
     * The suggested format for this key is
     * &lt;fully qualified class name of the device protocol&gt;.accesslevel.&lt;id&gt;
     *
     * @return The key in the translation resource bundle
     */
    String getTranslationKey();

    /**
     * Returns the default translation.
     */
    String getDefaultTranslation();

    /**
     * Gets the List of {@link PropertySpec properties}
     * that the related {@link com.energyict.mdc.upl.DeviceProtocol}
     * will require to be defined on a Device
     * to use this DeviceAccessLevel.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getSecurityProperties();

}