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
import java.util.stream.Collectors;

public class HasValidStatusAndFormatValidator implements ConstraintValidator<HasValidStatusAndFormat, SIMCardDomainExtension> {
    private Thesaurus thesaurus;

    @Inject
    public HasValidStatusAndFormatValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(HasValidStatusAndFormat constraintAnnotation) {

    }

    @Override
    public boolean isValid(SIMCardDomainExtension value, ConstraintValidatorContext context) {
        boolean isValid = true;
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
            String errmsg = thesaurus.getSimpleFormat(MessageSeeds.WRONG_CARD_FORMAT).format(cardFormats.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            context.buildConstraintViolationWithTemplate(errmsg)
                    .addPropertyNode(SIMCardDomainExtension.FieldNames.CARD_FORMAT.javaName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            isValid = false;
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
            String errmsg = thesaurus.getSimpleFormat(MessageSeeds.WRONG_STATUS).format(statuses.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            context.buildConstraintViolationWithTemplate(errmsg)
                    .addPropertyNode(SIMCardDomainExtension.FieldNames.STATUS.javaName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            isValid = false;
        }
        return isValid;
    }
}
