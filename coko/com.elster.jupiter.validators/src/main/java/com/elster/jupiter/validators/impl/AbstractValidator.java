package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 10/07/2014
 * Time: 14:33
 */
abstract class AbstractValidator implements IValidator {

    private final Thesaurus thesaurus;

    AbstractValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.absent();
    }

    @Override
    public Map<Date, ValidationResult> finish() {
        return Collections.emptyMap();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public String getDisplayName(String property) {
        return getThesaurus().getString(getPropertyNlsKey(property).getKey(), getPropertyDefaultFormat(property));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNlsKey().getKey(), getDefaultFormat());
    }

    boolean isAProperty(String property) {
        return getRequiredKeys().contains(property) || getOptionalKeys().contains(property);
    }
}
