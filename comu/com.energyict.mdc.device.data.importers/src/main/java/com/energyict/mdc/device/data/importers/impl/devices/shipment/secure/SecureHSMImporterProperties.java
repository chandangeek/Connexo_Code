/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import java.util.Arrays;

public enum SecureHSMImporterProperties {

    TRUSTSTORE(TranslationKeys.DEVICE_DATA_IMPORTER_TRUSTSTORE, TranslationKeys.DEVICE_DATA_IMPORTER_TRUSTSTORE_DESCRIPTION) {
        @Override
        public PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, SecurityManagementService pkiService) {
            return propertySpecService
                    .referenceSpec(TrustStore.class)
                    .named(this.getPropertyKey(), this.getNameTranslationKey())
                    .describedAs(this.getDescriptionTranslationKey())
                    .fromThesaurus(thesaurus)
                    .markExhaustive()
                    .addValues(pkiService.getAllTrustStores())
                    .markRequired()
                    .finish();
        }
    };

    private final TranslationKeys nameTranslationKey;
    private final TranslationKeys descriptionTranslationKey;

    SecureHSMImporterProperties(TranslationKeys nameTranslationKey, TranslationKeys descriptionTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

    public TranslationKeys getNameTranslationKey() {
        return nameTranslationKey;
    }

    public TranslationKeys getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    public String getPropertyKey() {
        return SecureHSMDeviceShipmentImporterFactory.NAME + "." + this.nameTranslationKey.getKey();
    }

    public abstract PropertySpec getPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus, SecurityManagementService pkiService);

    }
