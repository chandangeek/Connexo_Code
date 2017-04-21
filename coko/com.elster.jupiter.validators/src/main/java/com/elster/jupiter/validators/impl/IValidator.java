/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.Validator;

import java.util.List;
import java.util.stream.Collectors;

interface IValidator extends Validator {

    NlsKey getNlsKey();

    List<Pair<? extends NlsKey, String>> getExtraTranslations();

    default List<String> getRequiredProperties() {
        return getPropertySpecs()
                .stream()
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .collect(Collectors.toList());
    }
}