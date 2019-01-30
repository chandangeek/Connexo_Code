/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.KeyAccessorTpl;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class SecurityPropertySetBuilder extends NamedBuilder<SecurityPropertySet, SecurityPropertySetBuilder> {

    private final SecurityManagementService securityManagementService;

    private DeviceConfiguration deviceConfiguration;
    private BigDecimal client;
    private int suite;
    private int authLevel;
    private int encLevel;
    private List<KeyAccessorTpl> keys;

    @Inject
    public SecurityPropertySetBuilder(SecurityManagementService securityManagementService) {
        super(SecurityPropertySetBuilder.class);
        this.securityManagementService = securityManagementService;
    }

    public SecurityPropertySetBuilder withDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    public SecurityPropertySetBuilder withSuite(int suite) {
        this.suite = suite;
        return this;
    }

    public SecurityPropertySetBuilder withAuthLevel(int authLevel) {
        this.authLevel = authLevel;
        return this;
    }

    public SecurityPropertySetBuilder withEncLevel(int encLevel) {
        this.encLevel = encLevel;
        return this;
    }

    public SecurityPropertySetBuilder withClient(BigDecimal client) {
        this.client = client;
        return this;
    }

    public SecurityPropertySetBuilder withKeys(List<KeyAccessorTpl> keys) {
        this.keys = keys;
        return this;
    }

    private void check() {
        if (this.deviceConfiguration == null) {
            throw new UnableToCreate("You must set the device configuration");
        }
    }

    @Override
    public Optional<SecurityPropertySet> find() {
        check();
        return deviceConfiguration.getSecurityPropertySets().stream().filter(sps -> sps.getName().equals(getName())).findFirst();
    }

    @Override
    public SecurityPropertySet create() {
        com.energyict.mdc.device.config.SecurityPropertySetBuilder securityPropertySetBuilder = deviceConfiguration.createSecurityPropertySet(getName())
                .client(client)
                .securitySuite(suite)
                .authenticationLevel(authLevel)
                .encryptionLevel(encLevel);

        // Add for each of the propertySpecs a configuration security property - the propertySpec name will be used as name for the KeyAccessorType
        securityPropertySetBuilder.getPropertySpecs().forEach(
                propertySpec -> securityPropertySetBuilder.addConfigurationSecurityProperty(
                        propertySpec.getName(),
                        createOrGetKeyAccessorType(keys.stream().filter(k -> k.getName().contains(propertySpec.getName())).findAny().get()))
        );
        SecurityPropertySet securityPropertySet = securityPropertySetBuilder.build();
        securityPropertySet.update();
        return securityPropertySet;
    }

    private SecurityAccessorType createOrGetKeyAccessorType(KeyAccessorTpl key) {
        SecurityAccessorType securityAccessorType = securityManagementService.findSecurityAccessorTypeByName(key.getName())
                .orElseGet(() -> securityManagementService.addSecurityAccessorType(key.getName(), createOrGetKeyType(key))
                        .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                        .duration(key.getTimeDuration())
                        .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS)
                        .add());
        deviceConfiguration.getDeviceType().addSecurityAccessorTypes(securityAccessorType);
        return securityAccessorType;
    }

    private KeyType createOrGetKeyType(KeyAccessorTpl keyAccessorType) {
        if (keyAccessorType.getName().contains("Password")) {
            return securityManagementService.getKeyType(keyAccessorType.getName())
                    .orElseGet(() -> securityManagementService.newPassphraseType(keyAccessorType.getName()).withSpecialCharacters().length(keyAccessorType.getKeyType().getKeySize() !=null ? keyAccessorType.getKeyType().getKeySize() : keyAccessorType.getKeyType().getSpecialCharacters()).add());
        } else {
            return securityManagementService.getKeyType(keyAccessorType.getName())
                    .orElseGet(() -> securityManagementService.newSymmetricKeyType(keyAccessorType.getName(), keyAccessorType.getKeyType().getKeyAlgorithmName(), keyAccessorType.getKeyType().getKeySize()).add());
        }
    }
}
