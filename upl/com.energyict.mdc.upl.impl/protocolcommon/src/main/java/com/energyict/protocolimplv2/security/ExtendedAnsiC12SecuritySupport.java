package com.energyict.protocolimplv2.security;

import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;

import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol with
 * <i>extended security functionality</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/01/13
 * Time: 11:30
 */
public class ExtendedAnsiC12SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private final String authenticationTranslationKeyConstant = "AnsiC12SecuritySupport.authenticationlevel.";
    private final String encryptionTranslationKeyConstant = "AnsiC12SecuritySupport.encryptionlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec(),
                DeviceSecurityProperty.CALLED_AP_TITLE.getPropertySpec(),
                DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = new TypedProperties();

        if (deviceProtocolSecurityPropertySet != null) {

        }
        return typedProperties;
    }
}
