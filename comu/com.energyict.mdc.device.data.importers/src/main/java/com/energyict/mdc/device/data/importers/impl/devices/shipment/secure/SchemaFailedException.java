package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

public class SchemaFailedException extends LocalizedException {
    protected SchemaFailedException(Thesaurus thesaurus, Exception e) {
        super(thesaurus, MessageSeeds.SCHEMA_FAILED, e);
    }
}
