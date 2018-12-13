/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;

import java.util.Optional;

public class ImporterProperties {

    private final Thesaurus thesaurus;
    private final SecurityManagementService securityManagementService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final TrustStore trustStore;
    private final Optional<ImporterExtension> importExtension;
    private final HsmEnergyService hsmEnergyService;

    private ImporterProperties(Thesaurus thesaurus, SecurityManagementService securityManagementService, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, TrustStore trustStore, Optional<ImporterExtension> importExtension, HsmEnergyService hsmEnergyService) {
        this.thesaurus = thesaurus;
        this.securityManagementService = securityManagementService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.trustStore = trustStore;
        this.importExtension = importExtension;
        this.hsmEnergyService = hsmEnergyService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public TrustStore getTrustStore() {
        return trustStore;
    }

    public SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    public Optional<ImporterExtension> getImportExtension() {
        return importExtension;
    }

    public HsmEnergyService getHsmEnergyService() {
        return hsmEnergyService;
    }

    public static class ImporterPropertiesBuilder {
        private Thesaurus thesaurus;
        private SecurityManagementService securityManagementService;
        private DeviceConfigurationService deviceConfigurationService;
        private DeviceService deviceService;
        private TrustStore trustStore;
        private Optional<ImporterExtension> importExtension;
        private HsmEnergyService hsmEnergyService;

        public ImporterPropertiesBuilder withThesaurus(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
            return this;
        }

        public ImporterPropertiesBuilder withSecurityManagementService(SecurityManagementService securityManagementService) {
            this.securityManagementService = securityManagementService;
            return this;
        }

        public ImporterPropertiesBuilder withDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
            this.deviceConfigurationService = deviceConfigurationService;
            return this;
        }
        public ImporterPropertiesBuilder withDeviceService(DeviceService deviceService) {
            this.deviceService = deviceService;
            return this;
        }
        public ImporterPropertiesBuilder withImporterExtension(Optional<ImporterExtension> importExtension) {
            this.importExtension = importExtension;
            return this;
        }

        public ImporterPropertiesBuilder withTrustStore(TrustStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public ImporterPropertiesBuilder withHsmEnergyService(HsmEnergyService hsmEnergyService) {
            this.hsmEnergyService = hsmEnergyService;
            return this;
        }

        public ImporterProperties build() {
            return new ImporterProperties(thesaurus, securityManagementService, deviceConfigurationService, deviceService, trustStore, importExtension, hsmEnergyService);
        }
    }
}
