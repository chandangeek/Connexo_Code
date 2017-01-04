package com.energyict.mdc.upl.security;

/**
 * Models named set of security properties whose values
 * are managed against a Device.
 * The exact set of {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel}
 * and/or {@link EncryptionDeviceAccessLevel} select in the SecurityPropertySet.
 * That in turn depends on the actual {@link com.energyict.mdc.upl.DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (11:08)
 */
public interface SecurityPropertySet {

    AuthenticationDeviceAccessLevel getAuthenticationDeviceAccessLevel();

    int getAuthenticationDeviceAccessLevelId();

    EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel();

    int getEncryptionDeviceAccessLevelId();

    int getSecuritySuiteId();

    int getRequestSecurityLevelId();

    int getResponseSecurityLevelId();

}