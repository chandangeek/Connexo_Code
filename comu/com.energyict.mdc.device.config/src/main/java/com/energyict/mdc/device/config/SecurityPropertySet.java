/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityPropertySpecProvider;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

/**
 * Models named set of security properties whose values
 * are managed against a Device.
 * The exact set of {@link PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel}
 * and/or {@link EncryptionDeviceAccessLevel} select in the SecurityPropertySet.
 * That in turn depends on the actual {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-14 (10:29)
 */
@ProviderType
public interface SecurityPropertySet extends HasName, HasId, SecurityPropertySpecProvider {

    void setName(String name);

    AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel();

    EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel();

    DeviceConfiguration getDeviceConfiguration();

    Set<DeviceSecurityUserAction> getUserActions();

    /**
     * Gets the Set of {@link PropertySpec}s that are the result
     * of the selected {@link AuthenticationDeviceAccessLevel authentication}
     * and {@link EncryptionDeviceAccessLevel encryption} levels.
     *
     * @return The Set of PropertySpecs
     */
    Set<PropertySpec> getPropertySpecs();

    /**
     * Tests if the User that is currently
     * logged in to the application has sufficient privileges to edit
     * device security properties of this SecurityPropertySet.
     *
     * @return A flag that indicates if the current User has sufficient privileges to edit this SecurityPropertySet
     */
    boolean currentUserIsAllowedToEditDeviceProperties();

    /**
     * Tests if the User that is currently
     * logged in to the application has sufficient privileges to view
     * device security properties of this SecurityPropertySet.
     *
     * @return A flag that indicates if the current User has sufficient privileges to view this SecurityPropertySet
     */
    boolean currentUserIsAllowedToViewDeviceProperties();

    void addUserAction(DeviceSecurityUserAction userAction);

    void removeUserAction(DeviceSecurityUserAction userAction);

    void setAuthenticationLevel(int authenticationLevelId);

    void setEncryptionLevelId(int encryptionLevelId);

    void update();

    long getVersion();
}