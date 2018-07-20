/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

public interface DeviceKeyImporterProvider extends SymmetricKeyFactory {

    /**
     * Returns a {@link DeviceSecretImporter}. The importer will know which SecurityValueWrapper to create, more specifically, which
     * properties it should contain, and how to create it.
     * @param securityAccessorType Information for the importer, might not be needed
     * @return The {@link DeviceSecretImporter}
     */
    DeviceSecretImporter getDeviceKeyImporter(SecurityAccessorType securityAccessorType);
}
