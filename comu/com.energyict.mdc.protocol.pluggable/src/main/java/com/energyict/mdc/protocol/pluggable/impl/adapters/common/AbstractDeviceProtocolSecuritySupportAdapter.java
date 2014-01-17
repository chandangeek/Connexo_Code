package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;

import java.util.Collections;
import java.util.List;

/**
 * Abstract class for implementing the {@link DeviceSecuritySupport} interface
 * for legacy protocols.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 14:47
 */
public abstract class AbstractDeviceProtocolSecuritySupportAdapter implements DeviceSecuritySupport {

    private DeviceProtocolSecurityCapabilities legacySecuritySupport;
    private LegacySecurityPropertyConverter legacySecurityPropertyConverter;
    private final PropertySpecService propertySpecService;
    private final PropertiesAdapter propertiesAdapter;
    private final SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;

    protected AbstractDeviceProtocolSecuritySupportAdapter(PropertySpecService propertySpecService, PropertiesAdapter propertiesAdapter, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
        super();
        this.propertySpecService = propertySpecService;
        this.propertiesAdapter = propertiesAdapter;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
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
    public List<PropertySpec> getSecurityProperties() {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getSecurityProperties();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSecurityRelationTypeName() {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getSecurityRelationTypeName();
        } else {
            return "";
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

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        if (checkExistingSecuritySupport()) {
            return this.legacySecuritySupport.getSecurityPropertySpec(name);
        } else {
            return null;
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
            return Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(e, className);
        } catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(e, className);
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
            throw DeviceProtocolAdapterCodingExceptions.mappingElementDoesNotExist(this.getClass(), "securitySupportAdapter", deviceProtocolJavaClassname);
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
