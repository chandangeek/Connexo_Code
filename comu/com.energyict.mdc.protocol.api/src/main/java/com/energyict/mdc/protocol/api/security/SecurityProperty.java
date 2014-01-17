package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

/**
 * Holds the value of a single security property for a Device.
 * The complete list of security properties is determined
 * by the SecurityPropertySet.
 * <p>
 * The values of properties are versioned over time
 * so a SecurityProperty has a activity period during
 * which the property was active.
 * The versioning is only used to keep a history of the
 * previous values and therefore it will not be
 * possible to change values in the past.
 * Setting properties will therefore only affect the
 * actual or current situation and the new values
 * will be active from the current time until forever
 * or until new values are provided.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-02 (10:30)
 */
public interface SecurityProperty {

    /**
     * Gets the {@link AuthenticationDeviceAccessLevel} of the security property set
     * that contains this SecurityProperty.
     *
     * @return The AuthenticationDeviceAccessLevel
     */
    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel();

    /**
     * Gets the {@link EncryptionDeviceAccessLevel} of the security property set
     * that contains this SecurityProperty.
     *
     * @return The EncryptionDeviceAccessLevel
     */
    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel();

    /**
     * Gets the name of this property as defined by its
     * {@link com.energyict.mdc.dynamic.PropertySpec}.
     *
     * @return The name of this property
     */
    public String getName ();

    /**
     * Gets the value of this property.
     *
     * @return The value
     */
    public Object getValue ();

    /**
     * Gets the time Interval during which this SecurityProperty is active.
     *
     * @return The activity period
     */
    public Interval getActivePeriod ();

}