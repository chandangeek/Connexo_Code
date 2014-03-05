package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueReadingType} constraint against a {@link ProductSpecImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-04 (16:44)
 */
public class UniqueReadingTypeValidator implements ConstraintValidator<UniqueReadingType, ProductSpecImpl> {

    private DataModel dataModel;

    @Inject
    public UniqueReadingTypeValidator(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(UniqueReadingType constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ProductSpecImpl productSpec, ConstraintValidatorContext context) {
        ReadingType readingType = productSpec.getReadingType();
        if (readingType != null && !this.findOthersByReadingType(readingType).isEmpty()) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.READING_TYPE_ALREADY_EXISTS_KEY + "}")
                .addPropertyNode("readingType").addConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

    private List<ProductSpec> findOthersByReadingType(ReadingType readingType) {
        return this.getDataMapper().find("readingType", readingType);
    }

    private DataMapper<ProductSpec> getDataMapper() {
        return this.dataModel.mapper(ProductSpec.class);
    }

}