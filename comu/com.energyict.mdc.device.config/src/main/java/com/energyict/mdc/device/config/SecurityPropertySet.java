/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecurityPropertySpecProvider;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Set;

/**
 * Models named set of security properties whose values
 * are managed against a Device.
 * The exact set of {@link PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel} and/or
 * {@link EncryptionDeviceAccessLevel} and/or {@link SecuritySuite},{@link RequestSecurityLevel}
 * and/or {@link ResponseSecurityLevel} select in the SecurityPropertySet.
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

    String getClient();

    SecuritySuite getSecuritySuite();

    RequestSecurityLevel getRequestSecurityLevel();

    ResponseSecurityLevel getResponseSecurityLevel();

    List<ConfigurationSecurityProperty> getConfigurationSecurityProperties();

    void addConfigurationSecurityProperty(String name, KeyAccessorType keyAccessor);

    void updateConfigurationSecurityProperty(String configurationSecurityPropertyName, KeyAccessorType keyAccessor);

    void removeConfigurationSecurityProperty(String configurationSecurityPropertyName);

    /**
     * Gets the Set of {@link PropertySpec}s that are the result of the selected security levels.
     *
     * @return The Set of PropertySpecs
     */
    Set<PropertySpec> getPropertySpecs();


    DeviceConfiguration getDeviceConfiguration();

    void setAuthenticationLevelId(int authenticationLevelId);

    void setEncryptionLevelId(int encryptionLevelId);

    void setClient(String client);

    void setSecuritySuiteId(int securitySuiteId);

    void setRequestSecurityLevelId(int requestSecurityLevelId);

    void setResponseSecurityLevelId(int responseSecurityLevelId);

    void update();

    long getVersion();
}