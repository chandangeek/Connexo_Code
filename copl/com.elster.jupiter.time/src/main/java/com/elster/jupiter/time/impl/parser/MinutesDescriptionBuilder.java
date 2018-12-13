/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

import com.elster.jupiter.nls.Thesaurus;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author grhodes
 * @since 10 Dec 2012 14:11:11
 */
class MinutesDescriptionBuilder extends AbstractDescriptionBuilder {

    public MinutesDescriptionBuilder(Thesaurus thesaurus, Locale locale) {
        super(thesaurus, locale);
    }

    @Override
    protected String getSingleItemDescription(String expression) {
        return Utils.formatMinutes(expression);
    }

    @Override
    protected String getIntervalDescriptionFormat(String expression) {
        return MessageFormat.format(thesaurus.getFormat(TranslationKeys.every_x).format() + " " + minPlural(expression), expression);
    }

    @Override
    protected String getBetweenDescriptionFormat(String expression) {
        return thesaurus.getFormat(TranslationKeys.minutes_through_past_the_hour).format();
    }

    @Override
    protected String getDescriptionFormat(String expression) {
        return "0".equals(expression) ? "" : thesaurus.getFormat(TranslationKeys.at_x).format() + " " + minPlural(expression) +
                " " + thesaurus.getFormat(TranslationKeys.past_the_hour).format();
    }

    private String minPlural(String expression) {
        return plural(expression, thesaurus.getFormat(TranslationKeys.minute).format(), thesaurus.getFormat(TranslationKeys.minutes).format());
    }

}
