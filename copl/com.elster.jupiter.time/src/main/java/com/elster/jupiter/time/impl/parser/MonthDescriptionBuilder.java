/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:23:50
 */
class MonthDescriptionBuilder extends AbstractDescriptionBuilder {

    public MonthDescriptionBuilder(Thesaurus thesaurus, Locale locale) {
        super(thesaurus, locale);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return DateTimeFormatter.ofPattern("MMMM", locale).format(
                ZonedDateTime.now().withDayOfMonth(1).withMonth(Integer.parseInt(expression)));
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(", "+thesaurus.getFormat(TranslationKeys.every_x).format()+" " +
                plural(expression, thesaurus.getFormat(TranslationKeys.month).format(), thesaurus.getFormat(TranslationKeys.months).format()), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.between_description_format).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return ", "+thesaurus.getFormat(TranslationKeys.only_in).format();
    }

}
