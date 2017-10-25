/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

/**
 * A PassphraseFactory allows creation and renewal of passphrases of a certain KeyEncryptionMethod.
 * Each {@link PassphraseFactory} only supports a single KeyEncryptionMethod
 */
public interface PassphraseFactory {
    /**
     * Announce which key encryption method this factory supports.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * Create a new, persisted instance of the {@link PassphraseWrapper}'s implementation
     * @param securityAccessorType Container of information required to create the actual class.
     * @return Persisted implementation of {@link PassphraseWrapper} for the KeyEncryptionMethod
     */
    PassphraseWrapper newPassphraseWrapper(SecurityAccessorType securityAccessorType);

    /**
     * Report the expected property specs for {@link PassphraseWrapper} implementation for the KeyEncryptionMethod
     * @return List of PropertySpecs an actual implementation would have
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Returns a {@link DeviceSecretImporter}. The importer will know which SecurityValueWrapper to create, more specifically, which
     * properties it should contain, and how to create it.
     * @param securityAccessorType Information for the importer, might not be needed
     * @return The {@link DeviceSecretImporter}
     */
    DeviceSecretImporter getDevicePassphraseImporter(SecurityAccessorType securityAccessorType);

}
