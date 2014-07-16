package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
    private final PropertySpecService propertySpecService;

    AbstractValidator(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return Optional.absent();
    }

    @Override
    public Map<Date, ValidationResult> finish() {
        return Collections.emptyMap();
    }

    final Thesaurus getThesaurus() {
        return thesaurus;
    }

    final PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getDisplayName(String property) {
        return getThesaurus().getString(getPropertyNlsKey(property).getKey(), getPropertyDefaultFormat(property));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNlsKey().getKey(), getDefaultFormat());
    }

    boolean isAProperty(final String property) {
        return Iterables.any(getPropertySpecs(), new Predicate<PropertySpec>() {
            @Override
            public boolean apply(PropertySpec input) {
                return property.equals(input.getName());
            }
        });
    }
}
