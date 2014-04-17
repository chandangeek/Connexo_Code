package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueReadingTypeValidator implements ConstraintValidator<UniqueReadingType, RegisterMapping> {

    private final MasterDataService masterDataService;

    @Inject
    public UniqueReadingTypeValidator(MasterDataService masterDataService) {
        super();
        this.masterDataService = masterDataService;
    }

    @Override
    public void initialize(UniqueReadingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(RegisterMapping value, ConstraintValidatorContext context) {
        Optional<RegisterMapping> xRegisterMapping = this.masterDataService.findRegisterMappingByReadingType(value.getReadingType());
        if (xRegisterMapping.isPresent() && xRegisterMapping.get().getId() != value.getId()) {
            context.disableDefaultConstraintViolation();
            context.
                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_MAPPING_DUPLICATE_READING_TYPE + "}").
                addPropertyNode(RegisterMappingImpl.Fields.READING_TYPE.fieldName()).
                addConstraintViolation();
            return false;
        }
        return true;
    }

}