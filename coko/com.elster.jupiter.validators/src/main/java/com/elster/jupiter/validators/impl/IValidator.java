/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.Validator;

import java.util.List;
import java.util.stream.Collectors;

interface IValidator extends Validator {

    NlsKey getNlsKey();

    /**
     * Provides a list of {@link TranslationKey}s that should be installed.<br/>
     * This list normally includes keys for property names and descriptions and any extra keys required by {@link Validator}.
     *
     * @return a list of {@link TranslationKey}s
     */
    List<TranslationKey> getExtraTranslationKeys();

    default List<String> getRequiredProperties() {
        return getPropertySpecs()
                .stream()
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .collect(Collectors.toList());
    }
}