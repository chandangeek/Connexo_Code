package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Adapts the given UPL {@link DeviceProtocolSecurityCapabilities} (from protocols 9.1) to the CXO format (that has CPS support etc).
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 4/01/2017 - 13:49
 */
public abstract class AbstractSecuritySupportAdapter {

    protected DeviceProtocolSecurityCapabilities securitySupport;
    protected volatile PropertySpecService propertySpecService;
    protected volatile Thesaurus thesaurus;

    public AbstractSecuritySupportAdapter() {
        super();
    }

    @Inject
    public AbstractSecuritySupportAdapter(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    /**
     * The implementation of the {@link DeviceProtocolSecurityCapabilities} comes from the 9.1 protocol code
     * and provides the security property specs and the security levels.
     */
    protected abstract DeviceProtocolSecurityCapabilities getSecuritySupport();

    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        //The levels for this security set are defined in the 9.1 protocol code base
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        //The levels for this security set are defined in the 9.1 protocol code base
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    public com.energyict.mdc.upl.properties.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (getSecuritySupport() instanceof LegacySecurityPropertyConverter) {
            //Conversion is delegated to the 9.1 protocol code
            return ((LegacySecurityPropertyConverter) getSecuritySupport()).convertToTypedProperties(deviceProtocolSecurityPropertySet);
        } else {
            return TypedProperties.empty();
        }
    }

    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        if (getSecuritySupport() instanceof LegacySecurityPropertyConverter) {
            //Conversion is delegated to the 9.1 protocol code
            return ((LegacySecurityPropertyConverter) getSecuritySupport()).convertFromTypedProperties(typedProperties);
        } else {
            return new DeviceProtocolSecurityPropertySet() {

                @Override
                public int getAuthenticationDeviceAccessLevel() {
                    return 0;
                }

                @Override
                public int getEncryptionDeviceAccessLevel() {
                    return 0;
                }

                @Override
                public com.energyict.mdc.upl.properties.TypedProperties getSecurityProperties() {
                    return TypedProperties.empty();
                }
            };
        }
    }
}