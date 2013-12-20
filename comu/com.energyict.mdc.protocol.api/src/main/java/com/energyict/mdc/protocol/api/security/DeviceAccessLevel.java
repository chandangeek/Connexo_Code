package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.dynamic.PropertySpec;

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
     * Returns a String that serves as the key in a
     * translation resource bundle to provide a human
     * readable description or name for this DeviceAccessLevel.
     * The suggested format for this key is
     * &lt;fully qualified class name of the device protocol&gt;.accesslevel.&lt;id&gt;
     *
     * @return The key in the translation resource bundle
     */
    public String getTranslationKey ();

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