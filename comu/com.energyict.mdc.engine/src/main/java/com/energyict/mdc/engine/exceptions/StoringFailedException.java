package com.energyict.mdc.engine.exceptions;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;

/**
 * Copyrights EnergyICT
 * Date: 27/07/15
 * Time: 16:46
 */
public class StoringFailedException extends LocalizedException {

    public StoringFailedException(NlsService nlsService) {
        super(nlsService.getThesaurus(MessageSeeds.INBOUND_DATA_STORAGE_FAILURE.getModule(), Layer.DOMAIN), MessageSeeds.INBOUND_DATA_STORAGE_FAILURE);
    }
}
