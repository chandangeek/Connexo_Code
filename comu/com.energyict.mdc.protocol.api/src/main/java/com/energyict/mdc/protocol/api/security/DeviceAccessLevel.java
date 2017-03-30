/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;

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
public interface DeviceAccessLevel {

    /**
     * Represents a non-used DeviceAccessLevel.
     */
    public static final int NOT_USED_DEVICE_ACCESS_LEVEL_ID = -1;

    /**
     * Indicates that a (slave device) security set can inherit its properties from the master device's security set
     */
    public static final int CAN_INHERIT_PROPERTIES_FROM_MASTER_ID = -100;

    /**
     * Returns a number that uniquely identifies
     * this DeviceAccessLevel within the scope of the
     * {@link DeviceProtocol}
     * that returned this DeviceAccessLevel.
     * <p/>
     * <b>Note</b> that the ID may not be equal to the {@link #NOT_USED_DEVICE_ACCESS_LEVEL_ID}
     *
     * @return The identifier
     */
    public int getId ();

    /**
     * Returns the human
     * readable description or name for this DeviceAccessLevel.
     * The internal format for translation key is
     * &lt;fully qualified class name of the device protocol&gt;.accesslevel.&lt;id&gt;
     *
     * @return The translation resource bundle
     */
    public String getTranslation();

    /**
     * Gets the List of {@link PropertySpec properties}
     * that the related {@link DeviceProtocol}
     * will require to be defined on a physcial device
     * to use this DeviceAccessLevel.
     *
     * @return The List of PropertySpec
     */
    public List<PropertySpec> getSecurityProperties ();

}