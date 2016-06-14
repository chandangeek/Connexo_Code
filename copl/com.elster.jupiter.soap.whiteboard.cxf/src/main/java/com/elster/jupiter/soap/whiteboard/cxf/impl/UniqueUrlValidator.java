package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Created by bvn on 6/13/16.
 */
public class UniqueUrlValidator implements ConstraintValidator<UniqueUrl, EndPointConfigurationImpl> {

    private final DataModel dataModel;

    @Inject
    public UniqueUrlValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueUrl uniqueName) {

    }

    @Override
    public boolean isValid(EndPointConfigurationImpl endPointConfiguration, ConstraintValidatorContext context) {
        Optional<EndPointConfiguration> found = dataModel.mapper(EndPointConfiguration.class)
                .getUnique(EndPointConfigurationImpl.Fields.URL.fieldName(), endPointConfiguration.getUrl());
        if (found.isPresent() && found.get().getId() != endPointConfiguration.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(EndPointConfigurationImpl.Fields.URL.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
