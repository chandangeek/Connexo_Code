package com.energyict.mdc.device.config;

import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.util.Set;

/**
 * Models named set of security properties whose values
 * are managed against a Device.
 * The exact set of {@link com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel}
 * and/or {@link EncryptionDeviceAccessLevel} select in the SecurityPropertySet.
 * That in turn depends on the actual {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (10:29)
 */
public interface SecurityPropertySet extends NamedBusinessObject, RelationParticipant {

    public AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel();

    public EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel();

    public DeviceCommunicationConfiguration getDeviceCommunicationConfiguration ();

    public Set<DeviceSecurityUserAction> getUserActions ();

    /**
     * Gets the Set of {@link PropertySpec}s that are the result
     * of the selected {@link AuthenticationDeviceAccessLevel authentication}
     * and {@link EncryptionDeviceAccessLevel encryption} levels.
     *
     * @return The Set of PropertySpecs
     */
    public Set<PropertySpec> getPropertySpecs();

    /**
     * Tests if the User that is currently
     * logged in to the application has sufficient privileges to edit
     * device security properties of this SecurityPropertySet.
     * This is the case the {@link Role} of the
     * UserGroup that he belongs to
     * or his own Role contains one of the {@link DeviceSecurityUserAction}s
     * that have been assigned to this SecurityPropertySet.
     *
     * @return A flag that indicates if the current User has sufficient privileges to edit this SecurityPropertySet
     */
    public boolean currentUserIsAllowedToEditDeviceProperties ();

    /**
     * Tests if the User that is currently
     * logged in to the application has sufficient privileges to view
     * device security properties of this SecurityPropertySet.
     * This is the case the {@link Role} of the
     * UserGroup that he belongs to
     * or his own Role contains one of the {@link DeviceSecurityUserAction}s
     * that have been assigned to this SecurityPropertySet.
     *
     * @return A flag that indicates if the current User has sufficient privileges to view this SecurityPropertySet
     */
    public boolean currentUserIsAllowedToViewDeviceProperties ();

    /**
     * Tests if this SecurityPropertySet is still used
     * at the Device level.
     * This will be the case when there is at least one
     * Device that specifies {@link SecurityProperty values}
     * for this SecurityPropertySet.
     *
     * @return A flag that indicates if this SecurityPropertySet is still used
     */
    public boolean isUsedOnDevices ();

}