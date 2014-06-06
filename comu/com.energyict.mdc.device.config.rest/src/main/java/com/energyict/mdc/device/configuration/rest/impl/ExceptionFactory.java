package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import javax.inject.Inject;

public class ExceptionFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ExceptionFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public LocalizedFieldValidationException illegalRegisterMappingReference() {
        return new LocalizedFieldValidationException(MessageSeeds.INVALID_REFERENCE_TO_REGISTER_MAPPING, "registerMapping");
    }
}
