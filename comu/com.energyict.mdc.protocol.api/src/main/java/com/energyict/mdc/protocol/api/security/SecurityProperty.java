package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.properties.PropertySpec;
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
     * Gets the name of this property as defined by its {@link PropertySpec}.
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


    /**
     * Tests if this SecurityProperty is complete.
     * A SecurityProperty is complete in the following cases:
     * <ul>
     * <li>this property relates to a required {@link PropertySpec} and the value is not <code>null</code></li>
     * <li>this property relates to a non-required {@link PropertySpec}</li>
     * </ul>
     *
     * @return the completion status
     */
    public boolean isComplete ();

}