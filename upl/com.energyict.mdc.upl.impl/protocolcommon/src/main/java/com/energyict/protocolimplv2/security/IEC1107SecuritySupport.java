package com.energyict.protocolimplv2.security;

import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an IEC1107 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 11:10
 */
public class IEC1107SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private final String translationKeyConstant = "IEC1107SecuritySupport.authenticationlevel.";

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        NO_AUTHENTICATION(0),
        LEVEL_ONE(1),
        LEVEL_TWO(2),
        LEVEL_THREE(3);

        private final int accessLevel;

        private AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        protected int getAccessLevel() {
            return this.accessLevel;
        }

    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.IEC1107_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LevelOneAuthentication(),
                new LevelTwoAuthentication(),
                new LevelThreeAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = new TypedProperties();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            typedProperties.setProperty("SecurityLevel", String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelOneAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_ONE.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    /**
     * Authentication for level 2 security
     */
    protected class LevelTwoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_TWO.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelThreeAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_THREE.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }
}
