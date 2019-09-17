/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;

public class HasValidStatusAndFormatValidator implements ConstraintValidator<HasValidStatusAndFormat, SIMCardDomainExtension> {
    Thesaurus thesaurus;

    @Inject
    public HasValidStatusAndFormatValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(HasValidStatusAndFormat constraintAnnotation) {

    }

    @Override
    public boolean isValid(SIMCardDomainExtension value, ConstraintValidatorContext context) {
        ArrayList<String> cardFormats = new ArrayList<>();
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_DESCRIPTION, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_EMBEDDED, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_FULL_SIZE, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_MICRO, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_MINI, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_NANO, thesaurus));
        cardFormats.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_SW, thesaurus));
        cardFormats.add(null);
        if (!cardFormats.contains(value.getCardFormat())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.WRONG_CARD_FORMAT + "}")
                    .addPropertyNode(SIMCardDomainExtension.FieldNames.CARD_FORMAT.javaName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        ArrayList<String> statuses = new ArrayList<>();
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_ACTIVE, thesaurus));
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_DEMOLISHED, thesaurus));
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_DESCRIPTION, thesaurus));
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_INACTIVE, thesaurus));
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_PRE_ACTIVE, thesaurus));
        statuses.add(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_TEST, thesaurus));
        statuses.add(null);
        if (!statuses.contains(value.getStatus())) {
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.WRONG_STATUS + "}")
                    .addPropertyNode(SIMCardDomainExtension.FieldNames.STATUS.javaName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
