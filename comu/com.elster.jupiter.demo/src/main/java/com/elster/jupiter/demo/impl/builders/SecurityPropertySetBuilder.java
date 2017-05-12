/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.inject.Inject;
import java.util.Optional;

public class SecurityPropertySetBuilder extends NamedBuilder<SecurityPropertySet, SecurityPropertySetBuilder> {

    private final PkiService pkiService;

    private DeviceConfiguration deviceConfiguration;
    private String client;
    private int authLevel;
    private int encLevel;

    @Inject
    public SecurityPropertySetBuilder(PkiService pkiService) {
        super(SecurityPropertySetBuilder.class);
        this.pkiService = pkiService;
    }

    public SecurityPropertySetBuilder withDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
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

    public SecurityPropertySetBuilder withClient(String client) {
        this.client = client;
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
                .authenticationLevel(authLevel)
                .encryptionLevel(encLevel);
        // Add for each of the propertySpecs a configuration security property - the propertySpec name will be used as name for the KeyAccessorType
        securityPropertySetBuilder.getPropertySpecs().forEach(
                propertySpec -> securityPropertySetBuilder.addConfigurationSecurityProperty(
                        propertySpec.getName(),
                        createOrGetKeyAccessorType(propertySpec.getName()))
        );
        SecurityPropertySet securityPropertySet = securityPropertySetBuilder.build();
        securityPropertySet.update();
        return securityPropertySet;
    }

    private KeyAccessorType createOrGetKeyAccessorType(String keyAccessorTypeName) {
        DeviceType deviceType = this.deviceConfiguration.getDeviceType();
        return deviceType.getKeyAccessorTypes()
                .stream()
                .filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessorTypeName))
                .findFirst()
                .orElseGet(() -> deviceType.addKeyAccessorType(keyAccessorTypeName, createOrGetKeyType(keyAccessorTypeName))
                        .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                        .duration(TimeDuration.years(1))
                        .add());
    }

    private KeyType createOrGetKeyType(String keyAccessorTypeName) {
        if (keyAccessorTypeName.equals("Password")) {
            return pkiService.getKeyType("Passphrase").orElseGet(() -> pkiService.newPassphraseType("Passphrase").withSpecialCharacters().length(30).add());
        } else {
            return pkiService.getKeyType("AES 128").orElseGet(() -> pkiService.newSymmetricKeyType("AES 128", "AES", 128).add());
        }

    }
}
