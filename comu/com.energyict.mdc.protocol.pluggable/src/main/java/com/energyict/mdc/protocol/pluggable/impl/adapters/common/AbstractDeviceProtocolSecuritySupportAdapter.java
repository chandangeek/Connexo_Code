/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDeviceProtocolSecuritySupportAdapter implements DeviceSecuritySupport {

    private DeviceProtocolSecurityCapabilities legacySecuritySupport;
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    private final PropertySpecService propertySpecService;
    private final ProtocolPluggableService protocolPluggableService;
    private final PropertiesAdapter propertiesAdapter;
    private final SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;

    protected AbstractDeviceProtocolSecuritySupportAdapter(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, PropertiesAdapter propertiesAdapter, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
        super();
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.propertiesAdapter = propertiesAdapter;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void setLegacySecuritySupport(DeviceProtocolSecurityCapabilities legacySecuritySupport) {
        this.legacySecuritySupport = legacySecuritySupport;
    }

    public void setLegacySecurityPropertyConverter(LegacySecurityPropertyConverter legacySecurityPropertyConverter) {
        this.legacySecurityPropertyConverter = legacySecurityPropertyConverter;
    }

    private boolean checkExistingSecuritySupport() {
        return this.legacySecuritySupport != null;
    }

    protected boolean checkExistingSecurityPropertyConverter() {
        return this.legacySecurityPropertyConverter != null;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getCustomPropertySet();
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getAuthenticationAccessLevels();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getEncryptionAccessLevels();
        } else {
            return Collections.emptyList();
        }
    }

    public DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties) {
        return getLegacySecurityPropertyConverter().convertFromTypedProperties(typedProperties);
    }

    /**
     * Creates a new instance of a {@link DeviceSecuritySupport}
     * component based on the given className.
     *
     * @param className the className of the securityComponent
     * @return the newly created instance
     */
    protected Object createNewSecurityInstance(String className) {
        try {
            return this.protocolPluggableService.createDeviceProtocolSecurityFor(className);
        } catch (ProtocolCreationException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(MessageSeeds.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS, e, className);
        }
    }

    protected LegacySecurityPropertyConverter getLegacySecurityPropertyConverter() {
        return this.legacySecurityPropertyConverter;
    }

    /**
     * Get the className of the SecurityAdapter based on the given MeterProtocol.
     *
     * @param deviceProtocolJavaClassname the javaClassName to match
     * @return the className of the DeviceSecuritySupport to use
     */
    protected String getDeviceSecuritySupportMappingFor(String deviceProtocolJavaClassname) {
        final String securitySupportJavaClassName = this.securitySupportAdapterMappingFactory.getSecuritySupportJavaClassNameForDeviceProtocol(deviceProtocolJavaClassname);
        if (securitySupportJavaClassName == null) {
            throw DeviceProtocolAdapterCodingExceptions.mappingElementDoesNotExist(MessageSeeds.NON_EXISTING_MAP_ELEMENT, this.getClass(), "securitySupportAdapter", deviceProtocolJavaClassname);
        }
        return securitySupportJavaClassName;
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (checkExistingSecurityPropertyConverter()) {
            this.propertiesAdapter.copyProperties(getLegacySecurityPropertyConverter().convertToTypedProperties(deviceProtocolSecurityPropertySet));
        }
    }

}