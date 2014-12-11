package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.common.AbstractMbusDevice;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 9:13
 */
public class A100C extends AbstractMbusDevice {

    private EMeterMessaging EMeterMessaging;

    @Inject
    public A100C(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService) {
        super(new GarnetConcentrator(propertySpecService, socketService, serialComponentService));

    }

    public A100C() {
        super(new GarnetConcentrator());
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

    @Override
    public String getVersion() {
        return "$Date: 2014-07-02 15:27:53 +0200 (Wed, 02 Jul 2014) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec(String s) {
        return null;
    }
}