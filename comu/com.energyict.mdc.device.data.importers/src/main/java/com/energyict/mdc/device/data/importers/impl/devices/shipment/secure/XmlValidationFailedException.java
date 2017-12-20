package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import javax.xml.bind.JAXBException;

/**
 * Created by bvn on 7/19/17.
 */
public class XmlValidationFailedException extends LocalizedException {
    protected XmlValidationFailedException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.VALIDATION_OF_FILE_FAILED);
    }

    protected XmlValidationFailedException(Thesaurus thesaurus, String message) {
        super(thesaurus, MessageSeeds.VALIDATION_OF_FILE_FAILED_WITH_DETAIL, message);
    }
}
