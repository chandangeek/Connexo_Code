/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.collections.Zipper;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FilePermission;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlValidator implements ConstraintValidator<ValidUrl, EndPointConfigurationImpl> {

    @Override
    public void initialize(ValidUrl ValidUrl) {

    }

    private final DataModel dataModel;

    @Inject
    public UrlValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext context) {
        if (endPointConfiguration.isInbound()) {
            if (Checks.is(endPointConfiguration.getUrl()).emptyOrOnlyWhiteSpace()) {
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
                        .addPropertyNode(EndPointConfigurationImpl.Fields.URL.fieldName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }

            if (usesInvalidCharacters(endPointConfiguration.getUrl()) || hasSpaces(endPointConfiguration.getUrl()) || hasDots(endPointConfiguration.getUrl())) {
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.INVALID_PATH + "}")
                        .addPropertyNode(EndPointConfigurationImpl.Fields.URL.fieldName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        }

        Optional<EndPointConfiguration> found = dataModel.mapper(EndPointConfiguration.class)
                .getUnique(EndPointConfigurationImpl.Fields.URL.fieldName(), endPointConfiguration.getUrl());
        if (found.isPresent() && found.get().getId() != endPointConfiguration.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_MUST_BE_UNIQUE + "}")
                    .addPropertyNode(EndPointConfigurationImpl.Fields.URL.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean usesInvalidCharacters(String url) {
        Pattern pattern = Pattern.compile("^\\p{ASCII}*$");
        Matcher matcher = pattern.matcher(url);
        return !matcher.matches();
    }

    private boolean hasSpaces(String url) {
        return url.contains(" ");
    }

    private boolean hasDots(String url) {
        return url.contains(".");
    }
}
