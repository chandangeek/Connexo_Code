/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.properties.PropertySpec;
import java.util.List;

/**
 * A SymmetricKeyFactory allows creation and renewal of symmetric keys of a certain KeyEncryptionMethod.
 * Each {@link SymmetricKeyFactory} only supports a single KeyEncryptionMethod
 */
public interface SymmetricKeyFactory{
    /**
     * Announce which key encryption method this factory supports.
     *
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * Create a new, persisted instance of the {@link SymmetricKeyWrapper}'s implementation
     *
     * @param securityAccessorType Container of information required to create the actual class.
     * @return Persisted implementation of {@link SymmetricKeyWrapper} for the KeyEncryptionMethod
     */
    SymmetricKeyWrapper newSymmetricKey(SecurityAccessorType securityAccessorType); // TODO parameter could be reduced to KeyType

    /**
     * Report the expected property specs for {@link SymmetricKeyWrapper} implementation for the KeyEncryptionMethod
     *
     * @return List of PropertySpecs an actual implementation would have
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Returns a {@link DeviceSecretImporter}. The importer will know which SecurityValueWrapper to create, more specifically, which
     * properties it should contain, and how to create it.
     * @param securityAccessorType Information for the importer, might not be needed
     * @return The {@link DeviceSecretImporter}
     */
    DeviceSecretImporter getDeviceKeyImporter(SecurityAccessorType securityAccessorType);

}
