package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.common.AbstractMbusDevice;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 9:13
 */
public class A100C extends AbstractMbusDevice {

    private EMeterMessaging EMeterMessaging;

    public A100C() {
        super(new GarnetConcentrator(collectedDataFactory, issueFactory));
    }

    public A100C(GarnetConcentrator meterProtocol, String serialNumber) {
        super(meterProtocol, serialNumber);
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (EMeterMessaging == null) {
            EMeterMessaging = new EMeterMessaging(this);
        }
        return EMeterMessaging;
    }

    /**
     * Return a dummy level that indicates that this device must
     * inherit the security properties of the master device
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.add(new InheritedAuthenticationDeviceAccessLevel());
        return authenticationAccessLevels;
    }

    /**
     * Return a dummy level that indicates that this device must
     * inherit the security properties of the master device
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = new ArrayList<>();
        encryptionAccessLevels.add(new InheritedEncryptionDeviceAccessLevel());
        return encryptionAccessLevels;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster A100C Garnet";
    }

    /**
     * The version date
     */
    @Override
    public String getVersion() {
        return "$Date: 2015-05-27 15:32:39 +0200 (Wed, 27 May 2015) $";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        // Ignore since there are not properties
    }

}